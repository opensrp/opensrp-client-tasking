package org.smartregister.tasking.interactor;

import com.mapbox.geojson.Feature;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.domain.Location;
import org.smartregister.domain.Task;
import org.smartregister.repository.StructureRepository;
import org.smartregister.repository.TaskRepository;
import org.smartregister.tasking.R;
import org.smartregister.tasking.TaskingLibrary;
import org.smartregister.tasking.contract.TaskingHomeActivityContract;
import org.smartregister.tasking.model.StructureDetails;
import org.smartregister.tasking.model.TaskDetails;
import org.smartregister.tasking.repository.TaskingRepository;
import org.smartregister.tasking.util.GeoJsonUtils;
import org.smartregister.tasking.util.IndicatorUtils;
import org.smartregister.tasking.util.InteractorUtils;
import org.smartregister.tasking.util.TaskingConstants;
import org.smartregister.tasking.util.Utils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

import static org.smartregister.tasking.util.Utils.getInterventionLabel;

/**
 * Created by samuelgithengi on 11/27/18.
 */
public class TaskingHomeInteractor extends BaseInteractor {

    private CommonRepository commonRepository;
    private InteractorUtils interactorUtils;
    private StructureRepository structureRepository;
    private TaskRepository taskRepository;
    private TaskingLibrary taskingLibrary;
    private TaskingHomeActivityContract.Presenter presenter;
    private TaskingRepository taskingRepository;
    private GeoJsonUtils geoJsonUtils;

    public TaskingHomeInteractor(TaskingHomeActivityContract.Presenter presenter) {
        super(presenter);
        commonRepository = getCommonRepository();
        taskingLibrary = taskingLibrary.getInstance();
        geoJsonUtils = taskingLibrary.getTaskingLibraryConfiguration().getGeoJsonUtils();
        structureRepository = taskingLibrary.getStructureRepository();
        taskRepository = taskingLibrary.getTaskRepository();
        taskingRepository = taskingLibrary.getTaskingRepository();
        interactorUtils = new InteractorUtils(taskRepository, eventClientRepository, clientProcessor);
    }

    public void fetchInterventionDetails(String interventionType, String featureId, boolean isForForm) {
    }

    public void fetchLocations(String plan, String operationalArea) {
        fetchLocations(plan, operationalArea, null, null);
    }

    public void fetchLocations(String plan, String operationalArea, String point, Boolean locationComponentActive) {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                JSONObject featureCollection = null;

                Location operationalAreaLocation = Utils.getOperationalAreaLocation(operationalArea);
                List<TaskDetails> taskDetailsList = null;

                try {
                    featureCollection = createFeatureCollection();
                    if (operationalAreaLocation != null) {
                        Map<String, Set<Task>> tasks = taskRepository.getTasksByPlanAndGroup(plan, operationalAreaLocation.getId());
                        List<Location> structures = structureRepository.getLocationsByParentId(operationalAreaLocation.getId());
                        Map<String, StructureDetails> structureNames = taskingRepository.getStructureName(operationalAreaLocation.getId());
                        taskDetailsList = IndicatorUtils.processTaskDetails(tasks);
                        String indexCase = null;
                        if (getInterventionLabel() == R.string.focus_investigation)
                            indexCase = taskingRepository.getIndexCaseStructure(plan);
                        String features = geoJsonUtils.getGeoJsonFromStructuresAndTasks(structures, tasks, indexCase, structureNames);
                        featureCollection.put(TaskingConstants.GeoJSON.FEATURES, new JSONArray(features));

                    }
                } catch (Exception e) {
                    Timber.e(e);
                }
                JSONObject finalFeatureCollection = featureCollection;
                List<TaskDetails> finalTaskDetailsList = taskDetailsList;
                appExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (operationalAreaLocation != null) {
                            operationalAreaId = operationalAreaLocation.getId();
                            Feature operationalAreaFeature = Feature.fromJson(gson.toJson(operationalAreaLocation));
                            if (locationComponentActive != null) {
                                getPresenter().onStructuresFetched(finalFeatureCollection, operationalAreaFeature, finalTaskDetailsList, point, locationComponentActive);
                            } else {
                                getPresenter().onStructuresFetched(finalFeatureCollection, operationalAreaFeature, finalTaskDetailsList);
                            }
                        } else {
                            getPresenter().onStructuresFetched(finalFeatureCollection, null, null);
                        }
                    }
                });

            }

        };


        appExecutors.diskIO().execute(runnable);
    }

    private JSONObject createFeatureCollection() throws JSONException {
        JSONObject featureCollection = new JSONObject();
        featureCollection.put(TaskingConstants.GeoJSON.TYPE, TaskingConstants.GeoJSON.FEATURE_COLLECTION);
        return featureCollection;
    }

    public TaskingHomeActivityContract.Presenter getPresenter() {
        return presenter;
    }

    public void markStructureAsInactive(Feature selectedFeature) {
    }

    public void markStructureAsIneligible(Feature selectedFeature, String reasonUnEligible) {
    }

    public void resetInterventionTaskInfo(String interventionType, String featureId) {
    }

    public CommonRepository getCommonRepository() {
        return commonRepository;
    }

    public TaskRepository getTaskRepository() {
        return taskRepository;
    }

    public InteractorUtils getInteractorUtils() {
        return interactorUtils;
    }

    public StructureRepository getStructureRepository() {
        return structureRepository;
    }

    public TaskingLibrary getTaskingLibrary() {
        return taskingLibrary;
    }
}
