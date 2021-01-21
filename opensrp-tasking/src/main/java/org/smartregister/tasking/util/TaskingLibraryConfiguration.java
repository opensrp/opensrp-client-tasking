package org.smartregister.tasking.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import net.sqlcipher.database.SQLiteDatabase;

import org.json.JSONObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.domain.Location;
import org.smartregister.domain.Task;
import org.smartregister.tasking.adapter.TaskRegisterAdapter;
import org.smartregister.tasking.configuration.TaskRegisterV1Configuration;
import org.smartregister.tasking.contract.BaseContract;
import org.smartregister.tasking.contract.BaseDrawerContract;
import org.smartregister.tasking.contract.BaseFormFragmentContract;
import org.smartregister.tasking.model.BaseTaskDetails;
import org.smartregister.tasking.model.CardDetails;
import org.smartregister.tasking.model.TaskDetails;
import org.smartregister.tasking.model.TaskFilterParams;
import org.smartregister.tasking.viewholder.TaskRegisterViewHolder;
import org.smartregister.util.AppExecutors;

import java.util.List;
import java.util.Map;

/**
 * Created by Ephraim Kigamba - nek.eam@gmail.com on 06-08-2020.
 */
public abstract class TaskingLibraryConfiguration {

    private TaskRegisterConfiguration taskRegisterConfiguration;

    @NonNull
    public abstract Pair<Drawable, String> getActionDrawable(Context context, TaskDetails task);

    public abstract int getInterventionLabel();

    @NonNull
    public abstract Float getLocationBuffer();

    public abstract void startImmediateSync();

    public abstract boolean validateFarStructures();

    public abstract int getResolveLocationTimeoutInSeconds();

    public abstract String getAdminPasswordNotNearStructures();


    public abstract boolean isFocusInvestigation();

    public abstract boolean isMDA();

    public abstract String getCurrentLocationId();

    public abstract String getCurrentOperationalAreaId();

    public abstract Integer getDatabaseVersion();

    public abstract void tagEventTaskDetails(List<Event> events, SQLiteDatabase sqLiteDatabase);

    /**
     * Uses the server setting "DISPLAY_DISTANCE_SCALE" to determine whether to display the distance scale
     * If this variable is not available on the server the value is retrieved from BuildConfig.DISPLAY_DISTANCE_SCALE
     *
     * @return displayDistanceScale
     */
    public abstract Boolean displayDistanceScale();

    public abstract String getFormName(@NonNull String encounterType, @Nullable String taskCode);

    public abstract boolean resetTaskInfo(@NonNull SQLiteDatabase db, @NonNull BaseTaskDetails taskDetails);

    public abstract boolean archiveClient(String baseEntityId, boolean isFamily);

    public abstract String getTranslatedIRSVerificationStatus(String status);

    public abstract String getTranslatedBusinessStatus(String businessStatus);

    public abstract void formatCardDetails(CardDetails cardDetails);

    public abstract void processServerConfigs();

    public abstract Map<String, Integer> populateLabels();

    public abstract void showBasicForm(BaseFormFragmentContract.View view, Context context, String formName);

    public abstract void onLocationValidated(@NonNull Context context, @NonNull BaseFormFragmentContract.View view, @NonNull BaseFormFragmentContract.Interactor interactor, @NonNull BaseTaskDetails baseTaskDetails, @NonNull Location structure);

    public abstract String generateTaskRegisterSelectQuery(String mainCondition);

    public abstract String nonRegisteredStructureTasksSelect(String mainCondition);

    public abstract String groupedRegisteredStructureTasksSelect(String mainCondition);

    public abstract String[] taskRegisterMainColumns(String tableName);

    public abstract String familyRegisterTableName();

    public abstract void saveCaseConfirmation(BaseContract.BaseInteractor baseInteractor, BaseContract.BasePresenter presenterCallBack, JSONObject jsonForm, String eventType);

    public abstract String calculateBusinessStatus(@NonNull org.smartregister.domain.Event event);

    public abstract String getCurrentPlanId();

    public abstract boolean getSynced();

    public abstract void setSynced(boolean synced);

    public abstract boolean isMyLocationComponentEnabled();

    public abstract void setMyLocationComponentEnabled(boolean myLocationComponentEnabled);

    public abstract Task generateTaskFromStructureType(@NonNull Context context, @NonNull String structureId, @NonNull String structureType);

    public abstract void saveLocationInterventionForm(BaseContract.BaseInteractor baseInteractor, BaseContract.BasePresenter presenterCallBack, JSONObject jsonForm);

    public abstract void saveJsonForm(BaseContract.BaseInteractor baseInteractor, String json);

    public abstract  void openFilterActivity(Activity activity, TaskFilterParams filterParams);

    public abstract void openFamilyProfile(Activity activity, CommonPersonObjectClient family, BaseTaskDetails taskDetails);

    public abstract void setTaskDetails(Activity activity, TaskRegisterAdapter taskAdapter, List<TaskDetails> tasks);

    public abstract void showNotFoundPopup(Activity activity, String opensrpId);

    public abstract void startMapActivity(Activity activity, String searchViewText, TaskFilterParams taskFilterParams);

    public abstract void onTaskRegisterBindViewHolder(@NonNull Context context, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull View.OnClickListener registerActionHandler, @NonNull TaskDetails taskDetails, int position);

    public abstract void onTaskRegisterItemClicked(@NonNull Activity activity, @NonNull TaskDetails taskDetails);

    @NonNull
    public abstract AppExecutors getAppExecutors();

    @Nullable
    public abstract BaseDrawerContract.View getDrawerMenuView(BaseDrawerContract.DrawerActivity activity);

    public abstract void showTasksCompleteActionView(TextView actionView);

    public abstract Map<String, Object> getServerConfigs();

    public TaskRegisterConfiguration getTasksRegisterConfiguration(){
        if (taskRegisterConfiguration == null) {
            taskRegisterConfiguration = new TaskRegisterV1Configuration();
        }

        return taskRegisterConfiguration;
    }

    public interface TaskRegisterConfiguration {

        boolean isV2Design();

        boolean showGroupedTasks();
    }
}
