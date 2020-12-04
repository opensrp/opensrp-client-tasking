package org.smartregister.tasking.interactor;

import org.smartregister.repository.PlanDefinitionSearchRepository;
import org.smartregister.tasking.TaskingLibrary;
import org.smartregister.tasking.contract.BaseDrawerContract;
import org.smartregister.tasking.repository.TaskingRepository;
import org.smartregister.tasking.util.TaskingLibraryConfiguration;
import org.smartregister.util.AppExecutors;

/**
 * Created by samuelgithengi on 3/21/19.
 */
public class BaseDrawerInteractor implements BaseDrawerContract.Interactor {

    private AppExecutors appExecutors;

    private BaseDrawerContract.Presenter presenter;

    private PlanDefinitionSearchRepository planDefinitionSearchRepository;

    private TaskingLibrary taskingLibrary;

    private TaskingRepository taskingRepository;

    private TaskingLibraryConfiguration taskingLibraryConfiguration;

    public BaseDrawerInteractor(BaseDrawerContract.Presenter presenter) {
        this.presenter = presenter;
        taskingLibrary = TaskingLibrary.getInstance();
        appExecutors = taskingLibrary.getAppExecutors();
        planDefinitionSearchRepository = taskingLibrary.getPlanDefinitionSearchRepository();
        taskingRepository = taskingLibrary.getTaskingRepository();
        taskingLibraryConfiguration = taskingLibrary.getTaskingLibraryConfiguration();
    }

    @Override
    public void fetchPlans(String jurisdictionName) {

    }

    @Override
    public void validateCurrentPlan(String selectedOperationalArea, String currentPlanId) {

    }

    @Override
    public void checkSynced() {
        Runnable runnable = () -> {
            int syncCount = taskingRepository.checkSynced();
            taskingLibraryConfiguration.setSynced(syncCount == 0);
            appExecutors.mainThread().execute(() -> {
                boolean synced = taskingLibraryConfiguration.getSynced();
                (presenter).updateSyncStatusDisplay(synced);
            });
        };
        appExecutors.diskIO().execute(runnable);
    }
}
