package org.smartregister.tasking;


import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.vijay.jsonwizard.NativeFormLibrary;

import net.sqlcipher.database.SQLiteDatabase;

import org.json.JSONObject;
import org.mockito.Mockito;
import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.configurableviews.ConfigurableViewsLibrary;
import org.smartregister.domain.Location;
import org.smartregister.domain.Task;
import org.smartregister.repository.Repository;
import org.smartregister.tasking.activity.TaskingMapActivity;
import org.smartregister.tasking.adapter.TaskRegisterAdapter;
import org.smartregister.tasking.contract.BaseContract;
import org.smartregister.tasking.contract.BaseDrawerContract;
import org.smartregister.tasking.contract.BaseFormFragmentContract;
import org.smartregister.tasking.contract.TaskingMapActivityContract;
import org.smartregister.tasking.layer.DigitalGlobeLayer;
import org.smartregister.tasking.model.BaseLayerSwitchModel;
import org.smartregister.tasking.model.BaseTaskDetails;
import org.smartregister.tasking.model.CardDetails;
import org.smartregister.tasking.model.FamilyCardDetails;
import org.smartregister.tasking.model.IRSVerificationCardDetails;
import org.smartregister.tasking.model.MosquitoHarvestCardDetails;
import org.smartregister.tasking.model.SprayCardDetails;
import org.smartregister.tasking.model.TaskDetails;
import org.smartregister.tasking.model.TaskFilterParams;
import org.smartregister.tasking.presenter.TaskingMapPresenter;
import org.smartregister.tasking.repository.TaskingMappingHelper;
import org.smartregister.tasking.util.ActivityConfiguration;
import org.smartregister.tasking.util.DefaultLocationHierarchyUtils;
import org.smartregister.tasking.util.GeoJsonUtils;
import org.smartregister.tasking.util.TaskingJsonFormUtils;
import org.smartregister.tasking.util.TaskingMapHelper;
import org.smartregister.util.AppExecutors;
import org.smartregister.view.activity.DrishtiApplication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import io.ona.kujaku.data.realm.RealmDatabase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class TestRevealApplication extends DrishtiApplication {


    @Override
    public void onCreate() {
        mInstance = this;
        context = Context.getInstance();
        context.updateApplicationContext(getApplicationContext());
        CoreLibrary.init(context);
        ConfigurableViewsLibrary.init(context);

        TaskingLibrary.init(new TaskingLibraryConfiguration());

        setTheme(R.style.Theme_AppCompat); //or just R.style.Theme_AppCompat

        NativeFormLibrary.getInstance().setClientFormDao(CoreLibrary.getInstance().context().getClientFormRepository());
    }

    @Override
    public void logoutCurrentUser() {

    }

    public AppExecutors getAppExecutors() {
        return new AppExecutors(Executors.newSingleThreadExecutor(), Executors.newSingleThreadExecutor(), Executors.newSingleThreadExecutor());
    }

    @Override
    public Repository getRepository() {
        repository = mock(Repository.class);
        SQLiteDatabase sqLiteDatabase = mock(SQLiteDatabase.class);
        when(repository.getWritableDatabase()).thenReturn(sqLiteDatabase);
        when(repository.getReadableDatabase()).thenReturn(sqLiteDatabase);

        return repository;
    }

    public RealmDatabase getRealmDatabase(android.content.Context context) {

        return mock(RealmDatabase.class);
    }

    public static class TaskingLibraryConfiguration extends org.smartregister.tasking.util.TaskingLibraryConfiguration {

        private boolean refreshMapOnEventSaved;

        private boolean synced;

        @NonNull
        @Override
        public Pair<Drawable, String> getActionDrawable(android.content.Context context, TaskDetails task) {
            return new Pair<>(Mockito.mock(Drawable.class), task.getTaskDetails());
        }

        @Override
        public int getInterventionLabel() {
            return 0;
        }

        @NonNull
        @Override
        public Float getLocationBuffer() {
            return 25F;
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
            return true;
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
            return new HashMap<>();
        }

        @Override
        public void showBasicForm(BaseFormFragmentContract.View view, android.content.Context context, String formName) {

        }

        @Override
        public void onLocationValidated(@NonNull android.content.Context context, @NonNull BaseFormFragmentContract.View view, @NonNull BaseFormFragmentContract.Interactor interactor, @NonNull BaseTaskDetails baseTaskDetails, @NonNull Location structure) {

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
            return "VISITED";
        }

        @Override
        public String getCurrentPlanId() {
            return "current-plan-id";
        }

        @Override
        public boolean getSynced() {
            return synced;
        }

        @Override
        public void setSynced(boolean synced) {
            this.synced = synced;
        }

        @Override
        public boolean isMyLocationComponentEnabled() {
            return false;
        }

        @Override
        public void setMyLocationComponentEnabled(boolean myLocationComponentEnabled) {

        }

        @Override
        public Task generateTaskFromStructureType(@NonNull android.content.Context context, @NonNull String structureId, @NonNull String structureType) {
            return new Task();
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
        public void onTaskRegisterBindViewHolder(@NonNull android.content.Context context, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull View.OnClickListener registerActionHandler, @NonNull TaskDetails taskDetails, int position) {

        }

        @NonNull
        @Override
        public AppExecutors getAppExecutors() {
            return ((TestRevealApplication) TestRevealApplication.getInstance()).getAppExecutors();
        }

        @Override
        public BaseDrawerContract.View getDrawerMenuView(BaseDrawerContract.DrawerActivity activity) {
            return Mockito.mock(BaseDrawerContract.View.class);
        }

        @Override
        public void showTasksCompleteActionView(TextView actionView) {

        }

        @Override
        public Map<String, Object> getServerConfigs() {
            return new HashMap<>();
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
            return refreshMapOnEventSaved;
        }

        @Override
        public void setRefreshMapOnEventSaved(boolean isRefreshMapOnEventSaved) {
            refreshMapOnEventSaved = isRefreshMapOnEventSaved;
        }

        @Override
        public void setFeatureCollection(FeatureCollection featureCollection) {

        }

        @Override
        public DigitalGlobeLayer getDigitalGlobeLayer() {
            return null;
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
            return false;
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
            return true;
        }

        @Override
        public GeoJsonUtils getGeoJsonUtils() {
            return null;
        }

        @Override
        public String getProvinceFromTreeDialogValue(List<String> name) {
            return name.get(1);
        }

        @Override
        public String getDistrictFromTreeDialogValue(List<String> name) {
            return name.get(0);
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
            return 11;
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
            return null;
        }

        @Override
        public Pair<Double, Double> getMinMaxZoomMapDownloadPair() {
            return null;
        }

        @Override
        public List<BaseLayerSwitchModel> getBaseLayers() {
            return null;
        }

        @Override
        public boolean showBaseLayerSwitcherPlugin() {
            return false;
        }

        @Override
        public void openStructureProfile(CommonPersonObjectClient family, TaskingMapActivity taskingMapActivity, TaskingMapPresenter taskingMapPresenter) {

        }

        @Override
        public void openCardView(CardDetails cardDetails, TaskingMapActivity taskingMapActivity) {

        }

        @Override
        public void populateFamilyCard(FamilyCardDetails familyCardDetails, Activity activity) {

        }

        @Override
        public void populateAndOpenIRSVerificationCard(IRSVerificationCardDetails cardDetails, Activity activity) {

        }

        @Override
        public void populateAndOpenMosquitoHarvestCard(MosquitoHarvestCardDetails mosquitoHarvestCardDetails, Activity activity) {

        }

        @Override
        public void populateSprayCardTextViews(SprayCardDetails sprayCardDetails, Activity activity) {

        }

        @Override
        public String getStructureNamesSelect(String mainCondition) {
            return "";
        }
    }
}
