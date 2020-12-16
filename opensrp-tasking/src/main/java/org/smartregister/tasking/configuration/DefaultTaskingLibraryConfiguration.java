package org.smartregister.tasking.configuration;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

import net.sqlcipher.database.SQLiteDatabase;

import org.json.JSONObject;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Location;
import org.smartregister.domain.Task;
import org.smartregister.tasking.TaskingLibrary;
import org.smartregister.tasking.activity.TaskingMapActivity;
import org.smartregister.tasking.adapter.TaskRegisterAdapter;
import org.smartregister.tasking.contract.BaseContract;
import org.smartregister.tasking.contract.BaseDrawerContract;
import org.smartregister.tasking.contract.BaseFormFragmentContract;
import org.smartregister.tasking.contract.TaskingMapActivityContract;
import org.smartregister.tasking.layer.DigitalGlobeLayer;
import org.smartregister.tasking.model.BaseTaskDetails;
import org.smartregister.tasking.model.CardDetails;
import org.smartregister.tasking.model.TaskDetails;
import org.smartregister.tasking.model.TaskFilterParams;
import org.smartregister.tasking.repository.TaskingMappingHelper;
import org.smartregister.tasking.util.ActivityConfiguration;
import org.smartregister.tasking.util.DefaultLocationHierarchyUtils;
import org.smartregister.tasking.util.GeoJsonUtils;
import org.smartregister.tasking.util.TaskingConstants;
import org.smartregister.tasking.util.TaskingJsonFormUtils;
import org.smartregister.tasking.util.TaskingLibraryConfiguration;
import org.smartregister.tasking.util.TaskingMapHelper;
import org.smartregister.tasking.view.DrawerMenuView;
import org.smartregister.util.AppExecutors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.smartregister.tasking.util.Utils.getGlobalConfig;

public class DefaultTaskingLibraryConfiguration extends TaskingLibraryConfiguration {

    private boolean isSynced;

    @NonNull
    @Override
    public Pair<Drawable, String> getActionDrawable(Context context, TaskDetails task) {
        return null;
    }

    @Override
    public int getInterventionLabel() {
        return 0;
    }

    @NonNull
    @Override
    public Float getLocationBuffer() {
        return Float.valueOf(getGlobalConfig(TaskingConstants.CONFIGURATION.LOCATION_BUFFER_RADIUS_IN_METRES, "25"));
    }

    @Override
    public void startImmediateSync() {

    }

    @Override
    public boolean validateFarStructures() {
        return false;
    }

    @Override
    public int getResolveLocationTimeoutInSeconds() {
        return 0;
    }

    @Override
    public String getAdminPasswordNotNearStructures() {
        return null;
    }

    @Override
    public boolean isFocusInvestigation() {
        return false;
    }

    @Override
    public boolean isMDA() {
        return false;
    }

    @Override
    public String getCurrentLocationId() {
        return null;
    }

    @Override
    public String getCurrentOperationalAreaId() {
        return null;
    }

    @Override
    public Integer getDatabaseVersion() {
        return null;
    }

    @Override
    public void tagEventTaskDetails(List<Event> events, SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public Boolean displayDistanceScale() {
        return Boolean.valueOf(getGlobalConfig(TaskingConstants.CONFIGURATION.DISPLAY_DISTANCE_SCALE, "true"));
    }

    @Override
    public String getFormName(@NonNull String encounterType, @Nullable String taskCode) {
        return null;
    }

    @Override
    public boolean resetTaskInfo(@NonNull SQLiteDatabase db, @NonNull BaseTaskDetails taskDetails) {
        return false;
    }

    @Override
    public boolean archiveClient(String baseEntityId, boolean isFamily) {
        return false;
    }

    @Override
    public String getTranslatedIRSVerificationStatus(String status) {
        return null;
    }

    @Override
    public String getTranslatedBusinessStatus(String businessStatus) {
        return null;
    }

    @Override
    public void formatCardDetails(CardDetails cardDetails) {

    }

    @Override
    public void processServerConfigs() {

    }

    @Override
    public Map<String, Integer> populateLabels() {
        return null;
    }

    @Override
    public void showBasicForm(BaseFormFragmentContract.View view, Context context, String formName) {

    }

    @Override
    public void onLocationValidated(@NonNull Context context, @NonNull BaseFormFragmentContract.View view, @NonNull BaseFormFragmentContract.Interactor interactor, @NonNull BaseTaskDetails baseTaskDetails, @NonNull Location structure) {

    }

    @Override
    public String mainSelect(String mainCondition) {
        return null;
    }

    @Override
    public String nonRegisteredStructureTasksSelect(String mainCondition) {
        return null;
    }

    @Override
    public String groupedRegisteredStructureTasksSelect(String mainCondition) {
        return null;
    }

    @Override
    public String[] taskRegisterMainColumns(String tableName) {
        return new String[0];
    }

    @Override
    public String familyRegisterTableName() {
        return null;
    }

    @Override
    public void saveCaseConfirmation(BaseContract.BaseInteractor baseInteractor, BaseContract.BasePresenter presenterCallBack, JSONObject jsonForm, String eventType) {

    }

    @Override
    public String calculateBusinessStatus(@NonNull org.smartregister.domain.Event event) {
        return null;
    }

    @Override
    public String getCurrentPlanId() {
        return null;
    }

    @Override
    public boolean getSynced() {
        return isSynced;
    }

    @Override
    public void setSynced(boolean synced) {
        isSynced = synced;
    }

    @Override
    public boolean isMyLocationComponentEnabled() {
        return false;
    }

    @Override
    public void setMyLocationComponentEnabled(boolean myLocationComponentEnabled) {

    }

    @Override
    public Task generateTaskFromStructureType(@NonNull Context context, @NonNull String structureId, @NonNull String structureType) {
        return null;
    }

    @Override
    public void saveLocationInterventionForm(BaseContract.BaseInteractor baseInteractor, BaseContract.BasePresenter presenterCallBack, JSONObject jsonForm) {

    }

    @Override
    public void saveJsonForm(BaseContract.BaseInteractor baseInteractor, String json) {

    }

    @Override
    public void openFilterActivity(Activity activity, TaskFilterParams filterParams) {

    }

    @Override
    public void openFamilyProfile(Activity activity, CommonPersonObjectClient family, BaseTaskDetails taskDetails) {

    }

    @Override
    public void setTaskDetails(Activity activity, TaskRegisterAdapter taskAdapter, List<TaskDetails> tasks) {

    }

    @Override
    public void showNotFoundPopup(Activity activity, String opensrpId) {

    }

    @Override
    public void startMapActivity(Activity activity, String searchViewText, TaskFilterParams taskFilterParams) {

    }

    @Override
    public void onTaskRegisterBindViewHolder(@NonNull Context context, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull View.OnClickListener registerActionHandler, @NonNull TaskDetails taskDetails, int position) {

    }

    @NonNull
    @Override
    public AppExecutors getAppExecutors() {
        return TaskingLibrary.getInstance().getAppExecutors();
    }

    @Override
    public BaseDrawerContract.View getDrawerMenuView(BaseDrawerContract.DrawerActivity activity) {
        return new DrawerMenuView(activity);
    }

    @Override
    public void showTasksCompleteActionView(TextView actionView) {

    }

    @Override
    public Map<String, Object> getServerConfigs() {
        return null;
    }

    @Override
    public TaskingJsonFormUtils getJsonFormUtils() {
        return new TaskingJsonFormUtils();
    }

    @Override
    public TaskingMappingHelper getMappingHelper() {
        return new TaskingMappingHelper();
    }

    @Override
    public TaskingMapHelper getMapHelper() {
        return new TaskingMapHelper();
    }

    @Override
    public boolean isRefreshMapOnEventSaved() {
        return false;
    }

    @Override
    public void setRefreshMapOnEventSaved(boolean isRefreshMapOnEventSaved) {

    }

    @Override
    public void setFeatureCollection(FeatureCollection featureCollection) {

    }

    @Override
    public DigitalGlobeLayer getDigitalGlobeLayer() {
        return new DigitalGlobeLayer();
    }

    @Override
    public List<String> getFacilityLevels() {
        return DefaultLocationHierarchyUtils.getFacilityLevels();
    }

    @Override
    public List<String> getLocationLevels() {
        return DefaultLocationHierarchyUtils.getLocationLevels();
    }

    @Override
    public ActivityConfiguration getActivityConfiguration() {
        return null;
    }

    @Override
    public void registerFamily(Feature selectedFeature) {

    }

    @Override
    public void openTaskRegister(TaskFilterParams filterParams, TaskingMapActivity taskingMapActivity) {

    }

    @Override
    public boolean isCompassEnabled() {
        return true;
    }

    @Override
    public boolean showCurrentLocationButton() {
        return false;
    }

    @Override
    public boolean disableMyLocationOnMapMove() {
        return false;
    }

    @Override
    public Boolean getDrawOperationalAreaBoundaryAndLabel() {
        return null;
    }

    @Override
    public GeoJsonUtils getGeoJsonUtils() {
        return new GeoJsonUtils();
    }

    @Override
    public String getProvinceFromTreeDialogValue(List<String> name) {
        return "";
    }

    @Override
    public String getDistrictFromTreeDialogValue(List<String> name) {
        return "";
    }

    @Override
    public void onShowFilledForms() {

    }

    @Override
    public void onFeatureSelectedByLongClick(Feature feature, TaskingMapActivityContract.Presenter taskingMapPresenter) {

    }

    @Override
    public void onFeatureSelectedByClick(Feature feature, TaskingMapActivityContract.Presenter taskingMapPresenter) {

    }

    @Override
    public double getOnClickMaxZoomLevel() {
        return TaskingConstants.Map.MAX_SELECT_ZOOM_LEVEL;
    }

    @Override
    public void fetchPlans(String jurisdictionName, BaseDrawerContract.Presenter presenter) {

    }

    @Override
    public void validateCurrentPlan(String selectedOperationalArea, String currentPlanId, BaseDrawerContract.Presenter presenter) {

    }

    @Override
    public void setFacility(List<String> defaultLocation, BaseDrawerContract.View view) {

    }

    @Override
    public void openFilterTaskActivity(TaskFilterParams filterParams, TaskingMapActivity activity) {

    }

    @Override
    public List<Location> getLocationsIdsForDownload(List<String> locationIds) {
        return new ArrayList<>();
    }

    @Override
    public Pair<Double, Double> getMinMaxZoomMapDownloadPair() {
        return null;
    }
}
