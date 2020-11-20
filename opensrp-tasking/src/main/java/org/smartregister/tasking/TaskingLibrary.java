package org.smartregister.tasking;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.smartregister.CoreLibrary;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.PlanDefinitionSearchRepository;
import org.smartregister.repository.Repository;
import org.smartregister.repository.StructureRepository;
import org.smartregister.repository.TaskNotesRepository;
import org.smartregister.repository.TaskRepository;
import org.smartregister.tasking.repository.TaskingRepository;
import org.smartregister.tasking.util.TaskingLibraryConfiguration;
import org.smartregister.util.AppExecutors;
import org.smartregister.view.activity.DrishtiApplication;

import io.ona.kujaku.data.realm.RealmDatabase;

/**
 * Created by samuelgithengi on 6/10/20.
 */
public class TaskingLibrary {

    private static TaskingLibrary instance;
    private AppExecutors appExecutors;
    private TaskRepository taskRepository;
    private TaskNotesRepository taskNotesRepository;
    private StructureRepository structureRepository;
    private EventClientRepository eventClientRepository;
    private PlanDefinitionSearchRepository planDefinitionSearchRepository;
    private AllSharedPreferences allSharedPreferences;

    private String digitalGlobeConnectId;
    private String mapboxAccessToken;

    private RealmDatabase realmDatabase;
    private TaskingLibraryConfiguration taskingLibraryConfiguration;
    private TaskingRepository taskingRepository;

    public static void init(@NonNull TaskingLibraryConfiguration taskingLibraryConfiguration) {
        instance = new TaskingLibrary(taskingLibraryConfiguration);
    }

    public static TaskingLibrary getInstance() {
        return instance;
    }

    public TaskingLibrary(@NonNull TaskingLibraryConfiguration taskingLibraryConfiguration) {
        this.taskingLibraryConfiguration = taskingLibraryConfiguration;
    }

    @NonNull
    public AppExecutors getAppExecutors() {
        return taskingLibraryConfiguration.getAppExecutors();
    }

    @NonNull
    public TaskNotesRepository getTaskNotesRepository() {
        if (taskNotesRepository == null) {
            taskNotesRepository = new TaskNotesRepository();
        }

        return taskNotesRepository;
    }

    @NonNull
    public TaskRepository getTaskRepository() {
        if (taskRepository == null) {
            taskRepository = new TaskRepository(taskNotesRepository);
        }

        return taskRepository;
    }

    @NonNull
    public EventClientRepository getEventClientRepository() {
        if (eventClientRepository == null) {
            eventClientRepository = new EventClientRepository();
        }

        return eventClientRepository;
    }

    @NonNull
    public StructureRepository getStructureRepository() {
        if (structureRepository == null) {
            structureRepository = new StructureRepository();
        }

        return structureRepository;
    }

    @NonNull
    public PlanDefinitionSearchRepository getPlanDefinitionSearchRepository() {
        if (planDefinitionSearchRepository == null) {
            planDefinitionSearchRepository = new PlanDefinitionSearchRepository();
        }
        return planDefinitionSearchRepository;
    }

    @NonNull
    public TaskingRepository getTaskingRepository() {
        if (taskingRepository == null) {
            taskingRepository = new TaskingRepository();
        }
        return taskingRepository;
    }

    @NonNull
    public AllSharedPreferences getAllSharedPreferences() {
        return CoreLibrary.getInstance().context().allSharedPreferences();
    }

    @NonNull
    public Repository getRepository() {
        return DrishtiApplication.getInstance().getRepository();
    }

    @Nullable
    public String getDigitalGlobeConnectId() {
        return digitalGlobeConnectId;
    }

    public void setDigitalGlobeConnectId(@Nullable String digitalGlobeConnectId) {
        this.digitalGlobeConnectId = digitalGlobeConnectId;
    }

    @Nullable
    public String getMapboxAccessToken() {
        return mapboxAccessToken;
    }

    public void setMapboxAccessToken(String mapboxAccessToken) {
        this.mapboxAccessToken = mapboxAccessToken;
    }

    public RealmDatabase getRealmDatabase(@NonNull Context context) {
        if (realmDatabase == null) {
            realmDatabase = RealmDatabase.init(context);
        }

        return realmDatabase;
    }

    public TaskingLibraryConfiguration getTaskingLibraryConfiguration() {
        return taskingLibraryConfiguration;
    }
}
