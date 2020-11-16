package org.smartregister.tasking.interactor;

import org.smartregister.repository.PlanDefinitionSearchRepository;
import org.smartregister.tasking.TaskingLibrary;
import org.smartregister.tasking.contract.BaseDrawerContract;
import org.smartregister.util.AppExecutors;

/**
 * Created by samuelgithengi on 3/21/19.
 */
public class BaseDrawerInteractor implements BaseDrawerContract.Interactor {

    private AppExecutors appExecutors;

    private BaseDrawerContract.Presenter presenter;

    private PlanDefinitionSearchRepository planDefinitionSearchRepository;

    private TaskingLibrary taskingLibrary;

    public BaseDrawerInteractor(BaseDrawerContract.Presenter presenter) {
        this.presenter = presenter;
        taskingLibrary = TaskingLibrary.getInstance();
        appExecutors = taskingLibrary.getAppExecutors();
        planDefinitionSearchRepository = taskingLibrary.getPlanDefinitionSearchRepository();
    }

    @Override
    public void fetchPlans(String jurisdictionName) {

    }

    @Override
    public void validateCurrentPlan(String selectedOperationalArea, String currentPlanId) {

    }

    @Override
    public void checkSynced() {

    }
}
