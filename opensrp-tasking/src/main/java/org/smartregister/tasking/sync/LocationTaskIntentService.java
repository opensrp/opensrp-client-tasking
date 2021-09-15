package org.smartregister.tasking.sync;

import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.jetbrains.annotations.NotNull;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.Location;
import org.smartregister.domain.Task;
import org.smartregister.domain.db.EventClient;
import org.smartregister.job.SyncServiceJob;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.TaskRepository;
import org.smartregister.sync.helper.LocationServiceHelper;
import org.smartregister.sync.helper.PlanIntentServiceHelper;
import org.smartregister.sync.helper.TaskServiceHelper;
import org.smartregister.tasking.TaskingLibrary;
import org.smartregister.tasking.job.TaskingSyncSettingsServiceJob;
import org.smartregister.tasking.util.PreferencesUtil;
import org.smartregister.tasking.util.Utils;
import org.smartregister.util.NetworkUtils;
import org.smartregister.util.SyncUtils;
import org.smartregister.view.activity.DrishtiApplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

import static org.smartregister.tasking.util.Constants.Action.STRUCTURE_TASK_SYNCED;
import static org.smartregister.tasking.util.Constants.TABLE_NAME.FAMILY_MEMBER;

public class LocationTaskIntentService extends IntentService {

    private static final String TAG = "LocationTaskIntentService";

    private SyncUtils syncUtils;

    public LocationTaskIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (!NetworkUtils.isNetworkAvailable()) {
            sendSyncStatusBroadcastMessage(FetchStatus.noConnection);
            return;
        }
        if (!syncUtils.verifyAuthorization()) {
            try {
                syncUtils.logoutUser();
            } catch (AuthenticatorException e) {
                Timber.e(e);
            } catch (OperationCanceledException e) {
                Timber.e(e);
            } catch (IOException e) {
                Timber.e(e);
            }
            return;

        }
        sendSyncStatusBroadcastMessage(FetchStatus.fetchStarted);

        doSync();

        (TaskingLibrary.getInstance().getAppExecutors()).mainThread().execute(new Runnable() {
            @Override
            public void run() {
                TaskingSyncSettingsServiceJob.scheduleJobImmediately(TaskingSyncSettingsServiceJob.TAG);
            }
        });
    }

    private void sendSyncStatusBroadcastMessage(FetchStatus fetchStatus) {
        Intent intent = new Intent();
        intent.setAction(SyncStatusBroadcastReceiver.ACTION_SYNC_STATUS);
        intent.putExtra(SyncStatusBroadcastReceiver.EXTRA_FETCH_STATUS, fetchStatus);
        sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        syncUtils = new SyncUtils(getBaseContext());
        return super.onStartCommand(intent, flags, startId);
    }


    private void doSync() {
        sendSyncStatusBroadcastMessage(FetchStatus.fetchStarted);

        LocationServiceHelper locationServiceHelper = new LocationServiceHelper(
                DrishtiApplication.getInstance().getContext().getLocationRepository(),
                DrishtiApplication.getInstance().getContext().getLocationTagRepository(),
                TaskingLibrary.getInstance().getStructureRepository());


        List<Location> syncedStructures = syncStructures(locationServiceHelper);

        syncPlans();

        List<Task> syncedTasks = syncTasks();

        Set<String> baseEntityIds = getEventBaseEntityIds(syncedStructures, syncedTasks);

        clientProcessEvents(baseEntityIds);

        if (!org.smartregister.util.Utils.isEmptyCollection(syncedStructures)
                || !org.smartregister.util.Utils.isEmptyCollection(syncedTasks)) {
            doSync();
        }

        (TaskingLibrary.getInstance().getAppExecutors()).mainThread().execute(new Runnable() {
            @Override
            public void run() {
                SyncServiceJob.scheduleJobImmediately(SyncServiceJob.TAG);
            }
        });

    }

    @NotNull
    protected Set<String> getEventBaseEntityIds(List<Location> syncedStructures, List<Task> syncedTasks) {
        TaskRepository taskRepository = DrishtiApplication.getInstance().getContext().getTaskRepository();
        taskRepository.updateTaskStructureIdFromStructure(syncedStructures);
        taskRepository.updateTaskStructureIdsFromExistingStructures();
        taskRepository.updateTaskStructureIdsFromExistingClients(FAMILY_MEMBER);

        if (hasChangesInCurrentOperationalArea(syncedStructures, syncedTasks)) {
            Intent intent = new Intent(STRUCTURE_TASK_SYNCED);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }

        return extractStructureIds(syncedStructures, syncedTasks);
    }

    protected List<Location> syncStructures(LocationServiceHelper locationServiceHelper) {
        return locationServiceHelper.fetchLocationsStructures();
    }

    protected List<Task> syncTasks() {
        sendSyncStatusBroadcastMessage(FetchStatus.fetchStarted);
        TaskServiceHelper taskServiceHelper = TaskServiceHelper.getInstance();
        return taskServiceHelper.syncTasks();
    }

    protected void syncPlans() {
        sendSyncStatusBroadcastMessage(FetchStatus.fetchStarted);
        PlanIntentServiceHelper planServiceHelper = PlanIntentServiceHelper.getInstance();
        planServiceHelper.syncPlans();
    }

    /**
     * Checks if there a synced structure or task on the currently opened operational area
     *
     * @param syncedStructures the list of synced structures
     * @param syncedTasks      the list of synced tasks
     * @return true if there is a synced structure or task on the currently opened operational area; otherwise returns false
     */
    protected boolean hasChangesInCurrentOperationalArea(List<Location> syncedStructures, List<Task> syncedTasks) {
        Location operationalAreaLocation = Utils.getOperationalAreaLocation(PreferencesUtil.getInstance().getCurrentOperationalArea());
        String operationalAreaLocationId;
        if (operationalAreaLocation == null) {
            return false;
        } else {
            operationalAreaLocationId = operationalAreaLocation.getId();
        }
        if (syncedStructures != null) {
            for (Location structure : syncedStructures) {
                if (operationalAreaLocationId.equals(structure.getProperties().getParentId())) {
                    return true;
                }
            }
        }
        if (syncedTasks != null) {
            for (Task task : syncedTasks) {
                if (operationalAreaLocationId.equals(task.getGroupIdentifier())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Extracts a set of Structures ids from synced structures and tasks
     *
     * @param syncedStructures the list of synced structures
     * @param syncedTasks      the list of synced tasks
     * @return a set of baseEntityIds
     */
    protected Set<String> extractStructureIds(List<Location> syncedStructures, List<Task> syncedTasks) {
        Set<String> structureIds = new HashSet<>();
        if (!org.smartregister.util.Utils.isEmptyCollection(syncedStructures)) {
            for (Location structure : syncedStructures) {
                structureIds.add(structure.getId());
            }
        }
        if (!org.smartregister.util.Utils.isEmptyCollection(syncedTasks)) {
            for (Task task : syncedTasks) {
                structureIds.add(task.getForEntity());
            }
        }
        return structureIds;
    }

    /**
     * Clients Processes events of a set of structure baseEntityIds that have the task status TYPE_Task_Unprocessed
     *
     * @param syncedStructuresIds the set of structure baseEntityIds to client process
     */
    private void clientProcessEvents(Set<String> syncedStructuresIds) {
        if (org.smartregister.util.Utils.isEmptyCollection(syncedStructuresIds))
            return;
        EventClientRepository ecRepository = DrishtiApplication.getInstance().getContext().getEventClientRepository();
        List<EventClient> eventClients = ecRepository.getEventsByBaseEntityIdsAndSyncStatus(BaseRepository.TYPE_Task_Unprocessed, new ArrayList<>(syncedStructuresIds));
        if (!eventClients.isEmpty()) {

            try {
                DrishtiApplication.getInstance().getClientProcessor().getInstance(getApplicationContext()).processClient(eventClients);
            } catch (Exception ex) {
                Timber.e(ex);
            }
        }
    }

}
