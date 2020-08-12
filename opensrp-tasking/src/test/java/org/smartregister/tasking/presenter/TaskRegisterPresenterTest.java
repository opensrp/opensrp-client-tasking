package org.smartregister.tasking.presenter;

import org.json.JSONArray;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.reflect.Whitebox;
import org.smartregister.domain.Task;
import org.smartregister.tasking.BaseUnitTest;
import org.smartregister.tasking.R;
import org.smartregister.tasking.contract.TaskRegisterContract;
import org.smartregister.tasking.interactor.TaskRegisterInteractor;
import org.smartregister.tasking.util.Constants;
import org.smartregister.tasking.util.Constants.BusinessStatus;
import org.smartregister.tasking.util.Constants.Intervention;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Created by samuelgithengi on 3/27/19.
 */
public class TaskRegisterPresenterTest extends BaseUnitTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private TaskRegisterContract.View view;

    @Mock
    private TaskRegisterInteractor interactor;

    private TaskRegisterPresenter presenter;

    @Before
    public void setUp() {
        presenter = new TaskRegisterPresenter(view);
        Whitebox.setInternalState(presenter, "taskRegisterInteractor", interactor);
    }

    @Test
    public void testRegisterViewConfigurations() {
        List<String> viewList = Collections.singletonList("task_register");
        presenter.registerViewConfigurations(viewList);
        verify(interactor).registerViewConfigurations(eq(viewList));
        verifyNoMoreInteractions(interactor);
    }

    @Test
    public void testUnregisterViewConfiguration() {
        List<String> viewList = Collections.singletonList("task_register");
        presenter.unregisterViewConfiguration(viewList);
        verify(interactor).unregisterViewConfiguration(eq(viewList));
        verifyNoMoreInteractions(interactor);
    }


    @Test
    public void testOnDestroy() {
        presenter.onDestroy(true);
        verify(interactor).cleanupResources();
        verifyNoMoreInteractions(interactor);
    }

    @Test
    public void testSaveJsonForm() {
        String jsonForm = "{form}";
        presenter.saveJsonForm(jsonForm);
        verify(interactor).saveJsonForm(eq(jsonForm));
        verify(view).showProgressDialog(eq(R.string.saving_dialog_title));
        verifyNoMoreInteractions(interactor);
    }

    @Test
    public void testUpdateInitialsDoesNothing() {
        presenter.updateInitials();
        verifyNoMoreInteractions(view);
        verifyNoMoreInteractions(interactor);
    }

    @Test
    public void testOnFormSaved() {
        presenter.onFormSaved(UUID.randomUUID().toString(), null, Task.TaskStatus.COMPLETED, BusinessStatus.NOT_SPRAYED, Intervention.IRS);
        verify(view).hideProgressDialog();
        verifyNoMoreInteractions(view);
    }

    @Test
    public void testOnStructureAdded() {
        presenter.onStructureAdded(null, new JSONArray(), 17);
        verify(view).hideProgressDialog();
        verifyNoMoreInteractions(view);
    }


    @Test
    public void testOnFormSaveFailure() {
        presenter.onFormSaveFailure(Constants.EventType.CASE_DETAILS_EVENT);
        verify(view).hideProgressDialog();
        verifyNoMoreInteractions(view);
    }

}
