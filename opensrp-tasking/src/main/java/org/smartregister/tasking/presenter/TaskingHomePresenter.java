package org.smartregister.tasking.presenter;

import android.content.DialogInterface;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.gson.JsonElement;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Event;
import org.smartregister.domain.Task;
import org.smartregister.domain.Task.TaskStatus;
import org.smartregister.tasking.R;
import org.smartregister.tasking.TaskingLibrary;
import org.smartregister.tasking.contract.BaseDrawerContract;
import org.smartregister.tasking.contract.PasswordRequestCallback;
import org.smartregister.tasking.contract.TaskingHomeActivityContract;
import org.smartregister.tasking.contract.UserLocationContract;
import org.smartregister.tasking.interactor.TaskingHomeInteractor;
import org.smartregister.tasking.model.CardDetails;
import org.smartregister.tasking.model.TaskDetails;
import org.smartregister.tasking.model.TaskFilterParams;
import org.smartregister.tasking.repository.TaskingMappingHelper;
import org.smartregister.tasking.util.AlertDialogUtils;
import org.smartregister.tasking.util.PasswordDialogUtils;
import org.smartregister.tasking.util.PreferencesUtil;
import org.smartregister.tasking.util.TaskingConstants;
import org.smartregister.tasking.util.TaskingJsonFormUtils;
import org.smartregister.tasking.util.TaskingLibraryConfiguration;
import org.smartregister.tasking.util.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import timber.log.Timber;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_NEUTRAL;
import static org.smartregister.domain.LocationProperty.PropertyStatus.INACTIVE;
import static org.smartregister.tasking.util.Constants.JsonForm.STRUCTURE_NAME;
import static org.smartregister.tasking.util.TaskingConstants.BusinessStatus.NOT_ELIGIBLE;
import static org.smartregister.tasking.util.TaskingConstants.Properties.FEATURE_SELECT_TASK_BUSINESS_STATUS;
import static org.smartregister.tasking.util.TaskingConstants.Properties.LOCATION_STATUS;
import static org.smartregister.tasking.util.TaskingConstants.Properties.TASK_BUSINESS_STATUS;
import static org.smartregister.tasking.util.TaskingConstants.Properties.TASK_CODE;
import static org.smartregister.tasking.util.TaskingConstants.Properties.TASK_CODE_LIST;
import static org.smartregister.tasking.util.TaskingConstants.Properties.TASK_IDENTIFIER;
import static org.smartregister.tasking.util.TaskingConstants.Properties.TASK_STATUS;
import static org.smartregister.tasking.util.TaskingConstants.SPRAY_EVENT;
import static org.smartregister.tasking.util.Utils.validateFarStructures;


/**
 * Created by samuelgithengi on 11/27/18.
 */
public class TaskingHomePresenter implements TaskingHomeActivityContract.Presenter, PasswordRequestCallback,
        UserLocationContract.UserLocationCallback {

    private WeakReference<TaskingHomeActivityContract.View> viewWeakReference;

    private TaskingHomeInteractor taskingHomeInteractor;

    private PreferencesUtil prefsUtil;

    private FeatureCollection featureCollection;

    private List<Feature> filterFeatureCollection;

    private List<Feature> searchFeatureCollection;

    private Feature operationalArea;

    private Feature selectedFeature;

    private String selectedFeatureInterventionType;

    private LatLng clickedPoint;

    private AlertDialog passwordDialog;

    private ValidateUserLocationPresenter locationPresenter;

    private CardDetails cardDetails;

    private boolean changeInterventionStatus;

    private BaseDrawerContract.Presenter drawerPresenter;

    private TaskingJsonFormUtils jsonFormUtils;

    private boolean changeMapPosition;

    private TaskingLibrary taskingLibrary;

    private TaskingMappingHelper mappingHelper;

    private boolean markStructureIneligibleConfirmed;

    private String reasonUnEligible;

    private boolean isTasksFiltered;

    private String searchPhrase;

    private TaskFilterParams filterParams;

    private TaskingLibraryConfiguration libraryConfiguration;

    public TaskingHomePresenter(TaskingHomeActivityContract.View view, BaseDrawerContract.Presenter drawerPresenter) {
        this.viewWeakReference = new WeakReference<>(view);
        this.drawerPresenter = drawerPresenter;
        taskingHomeInteractor = getTaskingHomeInteractor();
        passwordDialog = PasswordDialogUtils.initPasswordDialog(view.getContext(), this);
        locationPresenter = new ValidateUserLocationPresenter(view, this);
        prefsUtil = PreferencesUtil.getInstance();
        setChangeMapPosition(true);
        taskingLibrary = TaskingLibrary.getInstance();
        libraryConfiguration = taskingLibrary.getTaskingLibraryConfiguration();
        jsonFormUtils = taskingLibrary.getTaskingLibraryConfiguration().getJsonFormUtils();
        mappingHelper = taskingLibrary.getTaskingLibraryConfiguration().getMappingHelper();
    }

    public TaskingHomeInteractor getTaskingHomeInteractor() {
        if (taskingHomeInteractor == null) {
            taskingHomeInteractor = new TaskingHomeInteractor(this);
        }
        return taskingHomeInteractor;
    }

    @Override
    public void onDrawerClosed() {
        if (drawerPresenter.isChangedCurrentSelection()) {
            if (getView() != null) {
                getView().showProgressDialog(R.string.fetching_structures_title, R.string.fetching_structures_message);
            }
            taskingHomeInteractor.fetchLocations(prefsUtil.getCurrentPlanId(), prefsUtil.getCurrentOperationalArea());
        }
    }

    public void refreshStructures(boolean localSyncDone) {
        setChangeMapPosition(!localSyncDone);
        if (getView() != null) {
            getView().showProgressDialog(R.string.fetching_structures_title, R.string.fetching_structures_message);
        }
        taskingHomeInteractor.fetchLocations(prefsUtil.getCurrentPlanId(), prefsUtil.getCurrentOperationalArea());
    }

    @Override
    public void onStructuresFetched(JSONObject structuresGeoJson, Feature operationalArea, List<TaskDetails> taskDetailsList, String point, Boolean locationComponentActive) {

    }

    @Override
    public void onStructuresFetched(JSONObject structuresGeoJson, Feature operationalArea, List<TaskDetails> taskDetailsList) {
    }


    @Override
    public void onMapReady() {
        String planId = PreferencesUtil.getInstance().getCurrentPlanId();
        String operationalArea = PreferencesUtil.getInstance().getCurrentOperationalArea();
        if (StringUtils.isNotBlank(planId) &&
                StringUtils.isNotBlank(operationalArea)) {
            taskingHomeInteractor.fetchLocations(planId, operationalArea);
        } else {
            if (getView() != null) {
                getView().displayNotification(R.string.select_campaign_operational_area_title, R.string.select_campaign_operational_area);
            }
            //drawerPresenter.getView().lockNavigationDrawerForSelection();
        }
    }

    @Override
    public void onMapClicked(MapboxMap mapboxMap, LatLng point, boolean isLongClick) {
    }

    @Override
    public void onFeatureSelected(Feature feature, boolean isLongClick) {
        this.selectedFeature = feature;
        this.changeInterventionStatus = false;
        cardDetails = null;

        if (getView() != null) {
            getView().closeAllCardViews();
            getView().displaySelectedFeature(feature, clickedPoint);
        }
        if (isLongClick)
            onFeatureSelectedByLongClick(feature);
        else
            onFeatureSelectedByClick(feature);
    }

    @Override
    public void onFeatureSelectedByClick(Feature feature) {
    }

    @Override
    public void onFeatureSelectedByLongClick(Feature feature) {
    }

    @Override
    public void validateUserLocation() {
        Location location = null;
        if (getView() != null) {
            location = getView().getUserCurrentLocation();
        }
        if (location == null) {
            locationPresenter.requestUserLocation();
        } else {
            locationPresenter.onGetUserLocation(location);
        }
    }

    @Override
    public void onFilterTasksClicked() {
        if (getView() != null) {
            getView().openFilterTaskActivity(filterParams);
        }
    }

    @Override
    public void onOpenTaskRegisterClicked() {
        if (getView() != null) {
            getView().openTaskRegister(filterParams);
        }
    }

    @Override
    public void setTaskFilterParams(TaskFilterParams filterParams) {
        if (filterParams != null) {
            filterTasks(filterParams);
            if (getView() != null) {
                getView().setSearchPhrase(filterParams.getSearchPhrase());
            }
        }
    }

    @Override
    public void onEventFound(Event event) {
        startForm(selectedFeature, cardDetails, selectedFeatureInterventionType, event);
    }

    @Override
    public void findLastEvent(String featureId, String eventType) {
    }

    @Override
    public void onFociBoundaryLongClicked() {
    }

    @Override
    public void onInterventionFormDetailsFetched(CardDetails cardDetails) {
        this.cardDetails = cardDetails;
        this.changeInterventionStatus = true;
        if (getView() != null) {
            getView().hideProgressDialog();
        }
        if (validateFarStructures()) {
            validateUserLocation();
        } else {
            onLocationValidated();
        }
    }

    @Override
    public TaskingHomeActivityContract.View getView() {
        if (viewWeakReference != null) {
            return viewWeakReference.get();
        }
        return null;
    }

    @Override
    public void onInterventionTaskInfoReset(boolean success) {
        if (getView() != null) {
            getView().hideProgressDialog();
            getView().closeAllCardViews();
        }
        if (libraryConfiguration.isRefreshMapOnEventSaved()) {
            refreshStructures(true);
            if (getView() != null) {
                getView().clearSelectedFeature();
            }
            libraryConfiguration.setRefreshMapOnEventSaved(false);
        }
    }


    @Override
    public void onCardDetailsFetched(CardDetails cardDetails) {
        if (getView() != null) {
            getView().openCardView(cardDetails);
        }
    }

    @Override
    public void startForm(Feature feature, CardDetails cardDetails, String interventionType) {
        startForm(feature, cardDetails, interventionType, null);
    }

    @Override
    public void startForm(Feature feature, CardDetails cardDetails, String interventionType, Event event) {
//        String formName = jsonFormUtils.getFormName(null, interventionType);
//        String sprayStatus = cardDetails == null ? null : cardDetails.getStatus();
//        String familyHead = null;
//        if (cardDetails instanceof SprayCardDetails) {
//            familyHead = ((SprayCardDetails) cardDetails).getFamilyHead();
//        }
//        startForm(formName, feature, sprayStatus, familyHead, event);
    }

    @Override
    public void startForm(String formName, Feature feature, String sprayStatus, String familyHead, Event event) {
        if (getView() != null) {
            JSONObject formJson = jsonFormUtils.getFormJSON(getView().getContext()
                    , formName, feature, sprayStatus, familyHead);
            startForm(formJson);
        }
    }

    @Override
    public void startForm(JSONObject formJson) {
        if (getView() != null && formJson != null) {
            getView().startJsonForm(formJson);
        }
    }

    @Override
    public void onUndoInterventionStatus(String interventionType) {
        if (getView() != null) {
            getView().showProgressDialog(R.string.resetting_task_title, R.string.resetting_task_msg);
        }
        taskingHomeInteractor.resetInterventionTaskInfo(interventionType, selectedFeature.id());
    }

    @Override
    public void saveJsonForm(String json) {
//        try {
//            JSONObject jsonForm = new JSONObject(json);
//            String encounterType = jsonForm.getString(ENCOUNTER_TYPE);
//            JSONArray fields = JsonFormUtils.getMultiStepFormFields(jsonForm);
//            String validOperationalArea = JsonFormUtils.getFieldValue(fields, VALID_OPERATIONAL_AREA);
//            if (Constants.REGISTER_STRUCTURE_EVENT.equals(encounterType) && StringUtils.isNotBlank(validOperationalArea)) {
//                registerStructureEvent();
//                getView().showProgressDialog(R.string.opening_form_title, R.string.add_structure_form_redirecting, validOperationalArea);
//                Boolean locationComponentActive = Boolean.valueOf(JsonFormUtils.getFieldValue(fields, LOCATION_COMPONENT_ACTIVE));
//                String point = JsonFormUtils.getFieldValue(fields, TaskingConstants.JsonForm.STRUCTURE);
//                taskingHomeInteractor.fetchLocations(prefsUtil.getCurrentPlanId(), validOperationalArea, point, locationComponentActive);
//            } else {
//                if (getView() != null) {
//                    getView().showProgressDialog(R.string.saving_title, R.string.saving_message);
//                }
//                taskingHomeInteractor.saveJsonForm(json);
//            }
//        } catch (JSONException e) {
//            Timber.e(e);
//            if (getView() != null) {
//                getView().displayToast(R.string.error_occurred_saving_form);
//            }
//        }
    }

    @Override
    public void onFormSaved(@NonNull String structureId, String taskID, @NonNull TaskStatus taskStatus, @NonNull String businessStatus, String interventionType) {
        if (getView() != null) {
            getView().hideProgressDialog();
        }
        setChangeMapPosition(false);
        for (Feature feature : getFeatureCollection().features()) {
            if (structureId.equals(feature.id())) {
                feature.addStringProperty(TASK_BUSINESS_STATUS, businessStatus);
                feature.addStringProperty(TASK_STATUS, taskStatus.name());
                break;
            }
        }
        if (getView() != null) {
            getView().setGeoJsonSource(getFeatureCollection(), null, isChangeMapPosition());
        }
        taskingHomeInteractor.fetchInterventionDetails(interventionType, structureId, false);
    }

    @Override
    public void resetFeatureTasks(String structureId, Task task) {
        setChangeMapPosition(false);
        for (Feature feature : getFeatureCollection().features()) {
            if (structureId.equals(feature.id())) {
                feature.addStringProperty(TASK_IDENTIFIER, task.getIdentifier());
                feature.addStringProperty(TASK_CODE, task.getCode());
                feature.addStringProperty(TASK_BUSINESS_STATUS, task.getBusinessStatus());
                feature.addStringProperty(TASK_STATUS, task.getStatus().name());
                feature.addStringProperty(FEATURE_SELECT_TASK_BUSINESS_STATUS, task.getBusinessStatus());
                feature.removeProperty(STRUCTURE_NAME);
                break;
            }
        }
        if (getView() != null) {
            getView().setGeoJsonSource(getFeatureCollection(), null, isChangeMapPosition());
        }
    }

    @Override
    public void onStructureAdded(Feature feature, JSONArray featureCoordinates, double zoomLevel) {
        if (getView() != null) {
            getView().closeAllCardViews();
            getView().hideProgressDialog();
        }
        getFeatureCollection().features().add(feature);
        setChangeMapPosition(false);
        getView().setGeoJsonSource(getFeatureCollection(), null, isChangeMapPosition());
        try {
            clickedPoint = new LatLng(featureCoordinates.getDouble(1), featureCoordinates.getDouble(0));
            if (getView() != null) {
                getView().displaySelectedFeature(feature, clickedPoint, zoomLevel);
            }

        } catch (JSONException e) {
            Timber.e(e, "error extracting coordinates of added structure");
        }
    }

    @Override
    public void onFormSaveFailure(String eventType) {
        if (getView() != null) {
            getView().hideProgressDialog();
            getView().displayNotification(R.string.form_save_failure_title,
                    SPRAY_EVENT.equals(eventType) ? R.string.spray_form_save_failure : R.string.add_structure_form_save_failure);
        }
    }

    @Override
    public void onAddStructureClicked(boolean myLocationComponentActive) {
        onAddStructureClicked(myLocationComponentActive, null);
    }

    @Override
    public void onAddStructureClicked(boolean myLocationComponentActive, String point) {
    }

    @Override
    public void onLocationValidated() {
    }

    @Override
    public void onPasswordVerified() {
        onLocationValidated();
    }

    @Override
    public LatLng getTargetCoordinates() {
        Location center = mappingHelper.getCenter(selectedFeature.geometry().toJson());
        return new LatLng(center.getLatitude(), center.getLongitude());
    }

    @Override
    public void requestUserPassword() {
        if (passwordDialog != null) {
            passwordDialog.show();
        }
    }

    @Override
    public ValidateUserLocationPresenter getLocationPresenter() {
        return locationPresenter;
    }

    @Override
    public Feature getSelectedFeature() {
        return selectedFeature;
    }

    @Override
    public int getInterventionLabel() {
        return 0;
    }

    @Override
    public void onMarkStructureInactiveConfirmed() {
        taskingHomeInteractor.markStructureAsInactive(selectedFeature);
    }

    @Override
    public void onStructureMarkedInactive() {
        for (Feature feature : getFeatureCollection().features()) {
            if (selectedFeature.id().equals(feature.id())) {
                feature.removeProperty(TASK_BUSINESS_STATUS);
                feature.removeProperty(TASK_IDENTIFIER);
                feature.addStringProperty(LOCATION_STATUS, INACTIVE.name());
                break;
            }
        }

        if (getView() != null) {
            getView().setGeoJsonSource(getFeatureCollection(), operationalArea, false);
        }
    }

    @Override
    public void onMarkStructureIneligibleConfirmed() {
        taskingHomeInteractor.markStructureAsIneligible(selectedFeature, reasonUnEligible);
    }

    @Override
    public void onStructureMarkedIneligible() {
        for (Feature feature : getFeatureCollection().features()) {
            if (selectedFeature.id().equals(feature.id())) {
                feature.addStringProperty(TASK_BUSINESS_STATUS, NOT_ELIGIBLE);
                feature.addStringProperty(FEATURE_SELECT_TASK_BUSINESS_STATUS, NOT_ELIGIBLE);
                break;
            }
        }

        if (getView() != null) {
            getView().setGeoJsonSource(getFeatureCollection(), operationalArea, false);
        }
    }

    @Override
    public void onFamilyFound(CommonPersonObjectClient finalFamily) {
        if (getView() != null) {
            if (finalFamily == null)
                getView().displayNotification(R.string.fetch_family_failed, R.string.failed_to_find_family);
            else
                getView().openStructureProfile(finalFamily);
        }
    }


    public void onResume() {
        if (libraryConfiguration.isRefreshMapOnEventSaved()) {
            refreshStructures(true);
            if (getView() != null) {
                getView().clearSelectedFeature();
            }
            libraryConfiguration.setRefreshMapOnEventSaved(false);
        }
        updateLocationComponentState();
    }

    private void updateLocationComponentState() {
        if (getView() != null) {
            if (libraryConfiguration.isMyLocationComponentEnabled() && !getView().isMyLocationComponentActive()) {
                getView().focusOnUserLocation(true);
            } else if (!libraryConfiguration.isMyLocationComponentEnabled() && getView().isMyLocationComponentActive()
                    || !getView().isMyLocationComponentActive()) {
                getView().focusOnUserLocation(false);
                if (!isTasksFiltered && StringUtils.isBlank(searchPhrase)) {
                    getView().setGeoJsonSource(getFeatureCollection(), operationalArea, false);
                }
            }
        }
    }

    public void displayMarkStructureIneligibleDialog() {
        if (getView() != null) {
            AlertDialogUtils.displayNotificationWithCallback(getView().getContext(), R.string.mark_location_ineligible,
                    R.string.is_structure_eligible_for_fam_reg, R.string.eligible, R.string.not_eligible_unoccupied, R.string.not_eligible_other, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == BUTTON_NEGATIVE || which == BUTTON_NEUTRAL) {
                                markStructureIneligibleConfirmed = true;
                                reasonUnEligible = which == BUTTON_NEGATIVE ? getView().getContext().getString(R.string.not_eligible_unoccupied) : getView().getContext().getString(R.string.not_eligible_other);
                            }
                            if (validateFarStructures()) {
                                validateUserLocation();
                            } else {
                                onLocationValidated();
                            }
                            dialog.dismiss();
                        }
                    });
        }
    }


    public boolean isChangeMapPosition() {
        return changeMapPosition;
    }

    public void setChangeMapPosition(boolean changeMapPosition) {
        this.changeMapPosition = changeMapPosition;
    }

    public void filterTasks(TaskFilterParams filterParams) {
        this.filterParams = filterParams;
        if (filterParams.getCheckedFilters() == null || filterParams.getCheckedFilters().isEmpty()) {
            isTasksFiltered = false;
            if (getView() != null) {
                getView().setNumberOfFilters(0);
            }
            return;
        }
        filterFeatureCollection = new ArrayList<>();
        Set<String> filterStatus = filterParams.getCheckedFilters().get(TaskingConstants.Filter.STATUS);
        Set<String> filterTaskCode = filterParams.getCheckedFilters().get(TaskingConstants.Filter.CODE);
        Set<String> filterInterventionUnitTasks = Utils.getInterventionUnitCodes(filterParams.getCheckedFilters().get(TaskingConstants.Filter.INTERVENTION_UNIT));
        Pattern pattern = Pattern.compile("~");
        for (Feature feature : featureCollection.features()) {
            boolean matches = true;
            if (filterStatus != null) {
                matches = feature.hasProperty(TASK_BUSINESS_STATUS) && filterStatus.contains(feature.getStringProperty(TASK_BUSINESS_STATUS));
            }
            if (matches && filterTaskCode != null) {
                matches = matchesTaskCodeFilterList(feature, filterTaskCode, pattern);
            }
            if (matches && filterInterventionUnitTasks != null) {
                matches = matchesTaskCodeFilterList(feature, filterInterventionUnitTasks, pattern);
            }
            if (matches) {
                filterFeatureCollection.add(feature);
            }
        }
        if (getView() != null) {
            getView().setGeoJsonSource(FeatureCollection.fromFeatures(filterFeatureCollection), operationalArea, false);
            getView().setNumberOfFilters(filterParams.getCheckedFilters().size());
            getView().setSearchPhrase("");
        }
        isTasksFiltered = true;
    }

    private boolean matchesTaskCodeFilterList(Feature feature, Set<String> filterList, Pattern pattern) {
        boolean matches = false;
        JsonElement taskCodes = feature.getProperty(TASK_CODE_LIST);
        if (taskCodes != null) {
            String[] array = pattern.split(taskCodes.getAsString());
            matches = CollectionUtils.containsAny(Arrays.asList(array), filterList);
        }
        return matches;

    }

    @Override
    public void searchTasks(String searchPhrase) {
//        if (searchPhrase.isEmpty()) {
//            searchFeatureCollection = null;
//            if (getView() != null) {
//                getView().setGeoJsonSource(filterFeatureCollection == null ? getFeatureCollection() : FeatureCollection.fromFeatures(filterFeatureCollection), operationalArea, false);
//            }
//        }
//        else {
//            List<Feature> features = new ArrayList<>();
//            for (Feature feature : !Utils.isEmptyCollection(searchFeatureCollection) && searchPhrase.length() > this.searchPhrase.length() ? searchFeatureCollection : Utils.isEmptyCollection(filterFeatureCollection) ? getFeatureCollection().features() : filterFeatureCollection) {
//                String structureName = feature.getStringProperty(STRUCTURE_NAME);
//                String familyMemberNames = feature.getStringProperty(FAMILY_MEMBER_NAMES);
//                if (Utils.matchesSearchPhrase(structureName, searchPhrase) ||
//                        Utils.matchesSearchPhrase(familyMemberNames, searchPhrase))
//                    features.add(feature);
//            }
//            searchFeatureCollection = features;
//            listTaskView.setGeoJsonSource(FeatureCollection.fromFeatures(searchFeatureCollection), operationalArea, false);
//        }
        this.searchPhrase = searchPhrase;
    }

    private FeatureCollection getFeatureCollection() {
        return isTasksFiltered && filterFeatureCollection != null ? FeatureCollection.fromFeatures(filterFeatureCollection) : featureCollection;
    }
}
