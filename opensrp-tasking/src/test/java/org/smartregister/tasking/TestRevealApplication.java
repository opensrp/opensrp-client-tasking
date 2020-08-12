package org.smartregister.tasking;


import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.vijay.jsonwizard.NativeFormLibrary;

import net.sqlcipher.database.SQLiteDatabase;

import org.json.JSONObject;
import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.configurableviews.ConfigurableViewsLibrary;
import org.smartregister.domain.Location;
import org.smartregister.domain.Task;
import org.smartregister.repository.Repository;
import org.smartregister.tasking.adapter.TaskRegisterAdapter;
import org.smartregister.tasking.contract.BaseContract;
import org.smartregister.tasking.contract.BaseFormFragmentContract;
import org.smartregister.tasking.model.BaseTaskDetails;
import org.smartregister.tasking.model.CardDetails;
import org.smartregister.tasking.model.TaskDetails;
import org.smartregister.tasking.model.TaskFilterParams;
import org.smartregister.tasking.util.TaskingLibraryConfiguration;
import org.smartregister.tasking.viewholder.TaskRegisterViewHolder;
import org.smartregister.util.AppExecutors;
import org.smartregister.view.activity.DrishtiApplication;

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

        @NonNull
        @Override
        public Pair<Drawable, String> getActionDrawable(android.content.Context context, TaskDetails task) {
            return null;
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
            return null;
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
            return false;
        }

        @Override
        public void setSynced(boolean synced) {

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
        public void onTaskRegisterBindViewHolder(@NonNull android.content.Context context, @NonNull TaskRegisterViewHolder viewHolder, @NonNull View.OnClickListener registerActionHandler, @NonNull TaskDetails taskDetails, int position) {

        }

        @NonNull
        @Override
        public AppExecutors getAppExecutors() {
            return ((TestRevealApplication) TestRevealApplication.getInstance()).getAppExecutors();
        }
    }
}
