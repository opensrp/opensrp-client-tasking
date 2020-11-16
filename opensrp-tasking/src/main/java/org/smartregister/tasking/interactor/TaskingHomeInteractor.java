package org.smartregister.tasking.interactor;

import com.mapbox.geojson.Feature;

import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.repository.StructureRepository;
import org.smartregister.repository.TaskRepository;
import org.smartregister.tasking.TaskingLibrary;
import org.smartregister.tasking.contract.TaskingHomeActivityContract;
import org.smartregister.tasking.util.InteractorUtils;

/**
 * Created by samuelgithengi on 11/27/18.
 */
public class TaskingHomeInteractor extends BaseInteractor {

    private CommonRepository commonRepository;
    private InteractorUtils interactorUtils;
    private StructureRepository structureRepository;
    private TaskRepository taskRepository;
    private TaskingLibrary taskingLibrary;

    public TaskingHomeInteractor(TaskingHomeActivityContract.Presenter presenter) {
        super(presenter);
        commonRepository = getCommonRepository();
        taskingLibrary = taskingLibrary.getInstance();
        structureRepository = taskingLibrary.getStructureRepository();
        taskRepository = taskingLibrary.getTaskRepository();
        interactorUtils = new InteractorUtils(taskRepository, eventClientRepository, clientProcessor);
    }

    public void fetchInterventionDetails(String interventionType, String featureId, boolean isForForm) {
    }

    public void fetchLocations(String plan, String operationalArea) {
        fetchLocations(plan, operationalArea, null, null);
    }

    public void fetchLocations(String plan, String operationalArea, String point, Boolean locationComponentActive) {

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
