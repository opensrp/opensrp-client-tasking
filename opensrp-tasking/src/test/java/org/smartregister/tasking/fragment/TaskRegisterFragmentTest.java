package org.smartregister.tasking.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.vijay.jsonwizard.activities.FormConfigurationJsonFormActivity;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.reflect.Whitebox;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowProgressDialog;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Task;
import org.smartregister.tasking.BaseUnitTest;
import org.smartregister.tasking.R;
import org.smartregister.tasking.TaskingLibrary;
import org.smartregister.tasking.activity.FilterTasksActivity;
import org.smartregister.tasking.activity.TaskRegisterActivity;
import org.smartregister.tasking.activity.TestTaskRegisterActivity;
import org.smartregister.tasking.adapter.TaskRegisterAdapter;
import org.smartregister.tasking.model.TaskDetails;
import org.smartregister.tasking.model.TaskFilterParams;
import org.smartregister.tasking.presenter.TaskRegisterFragmentPresenter;
import org.smartregister.tasking.presenter.ValidateUserLocationPresenter;
import org.smartregister.tasking.shadow.DrawerMenuViewShadow;
import org.smartregister.tasking.util.Constants;
import org.smartregister.tasking.util.LocationUtils;
import org.smartregister.tasking.util.RevealJsonFormUtils;
import org.smartregister.tasking.util.TaskingLibraryConfiguration;
import org.smartregister.tasking.util.TestingUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.ona.kujaku.utils.Constants.RequestCode;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;
import static org.smartregister.tasking.util.Constants.Filter.FILTER_SORT_PARAMS;
import static org.smartregister.tasking.util.Constants.Intervention.CASE_CONFIRMATION;
import static org.smartregister.tasking.util.Constants.JSON_FORM_PARAM_JSON;
import static org.smartregister.tasking.util.Constants.RequestCode.REQUEST_CODE_FILTER_TASKS;

/**
 * Created by samuelgithengi on 1/27/20.
 */
@Config(shadows = {DrawerMenuViewShadow.class})
public class TaskRegisterFragmentTest extends BaseUnitTest {


    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private TaskRegisterFragment fragment;

    private TaskRegisterActivity activity;

    @Mock
    private TaskRegisterFragmentPresenter presenter;

    @Mock
    private TaskRegisterAdapter taskAdapter;

    @Mock
    private RevealJsonFormUtils jsonFormUtils;

    @Mock
    private LocationUtils locationUtils;

    @Mock
    private CaseClassificationFragment caseClassificationFragment;

    @Mock
    private ValidateUserLocationPresenter locationPresenter;

    @Captor
    private ArgumentCaptor<TaskDetails> taskDetailsArgumentCaptor;

    private TaskingLibraryConfiguration taskingLibraryConfiguration;


    @Before
    public void setUp() {
        org.smartregister.Context.bindtypes = new ArrayList<>();
        fragment = Mockito.spy(new TaskRegisterFragment());
        Whitebox.setInternalState(fragment, "presenter", presenter);
        activity = Mockito.spy(Robolectric.buildActivity(TestTaskRegisterActivity.class).create().start().get());
        activity.setContentView(R.layout.activity_base_register);
        activity.getSupportFragmentManager().beginTransaction().add(0, fragment).commit();

        taskingLibraryConfiguration = Mockito.spy(TaskingLibrary.getInstance().getTaskingLibraryConfiguration());
        ReflectionHelpers.setField(TaskingLibrary.getInstance(), "taskingLibraryConfiguration", taskingLibraryConfiguration);

        Mockito.doReturn(activity).when(fragment).getActivity();
    }


    @Test
    public void testOnCreate() {
        assertNotNull(fragment);
    }

    @Test
    public void testGetLayout() {
        assertEquals(R.layout.fragment_task_register, fragment.getLayout());

    }

    @Test
    public void testInitializeAdapter() {
        fragment.initializePresenter();
        assertNotNull(Whitebox.getInternalState(fragment, "presenter"));
        assertNotNull(Whitebox.getInternalState(fragment, "locationUtils"));
    }

    @Test
    public void testFilter() {
        fragment.filter("Doe", "", "", false);
        verify(presenter).searchTasks("Doe");
        assertEquals(View.VISIBLE, fragment.getSearchCancelView().getVisibility());
    }


    @Test
    public void testFilterWithEmptyFilter() {
        fragment.filter("", "", "", false);
        verify(presenter).searchTasks("");
        assertEquals(View.INVISIBLE, fragment.getSearchCancelView().getVisibility());
    }

    @Test
    public void testStartMapActivityShouldCallConfigurationStartMapActivity() {
        TaskFilterParams params = new TaskFilterParams("Doe");
        fragment.startMapActivity(params);


        Mockito.verify(taskingLibraryConfiguration).startMapActivity(activity, fragment.getSearchView().getText().toString(), params);
    }

    @Test
    public void testGetLastLocation() {
        assertNull(fragment.getLastLocation());
        Location location = new Location("Test");
        Intent intent = new Intent();
        intent.putExtra(Constants.TaskRegister.LAST_USER_LOCATION, location);
        activity.setIntent(intent);
        assertEquals(location, fragment.getLastLocation());
    }

    @Test
    public void testSetUniqueID() {
        fragment.setUniqueID("123");
        assertEquals("123", fragment.getSearchView().getText().toString());
    }

    @Test
    public void testOnViewClicked() {
        View view = fragment.getView();
        TaskDetails details = TestingUtils.getTaskDetails();
        details.setTaskCode(Constants.Intervention.REGISTER_FAMILY);
        view.setTag(R.id.task_details, details);
        fragment.onViewClicked(view);
        verify(presenter).onTaskSelected(details, false);
    }

    @Test
    public void testOnViewClickedForTaskReset() {
        View view = fragment.getView();
        TaskDetails details = TestingUtils.getTaskDetails();
        details.setTaskStatus(Task.TaskStatus.COMPLETED.name());
        details.setTaskCode(CASE_CONFIRMATION);
        view.setTag(R.id.task_details, details);

        fragment.onViewClicked(view);
        AlertDialog alertDialog = (AlertDialog) ShadowAlertDialog.getLatestDialog();
        assertTrue(alertDialog.isShowing());

        TextView tv = alertDialog.findViewById(android.R.id.message);
        assertEquals(getString(R.string.choose_action), tv.getText());
    }

    @Test
    public void testSetTotalTasks() {
        Whitebox.setInternalState(fragment, "taskAdapter", taskAdapter);
        when(taskAdapter.getItemCount()).thenReturn(16);
        fragment.setTotalTasks(5);
        TextView header = fragment.getView().findViewById(R.id.header_text_display);
        assertEquals("5 structures within 25 m (16 total)", header.getText().toString());
    }

    @Test
    public void testSetTaskDetailsShouldCallTaskLibraryConfigurationSetTaskDetails() {
        Whitebox.setInternalState(fragment, "taskAdapter", taskAdapter);
        List<TaskDetails> taskDetailsList = Collections.singletonList(TestingUtils.getTaskDetails());
        fragment.setTaskDetails(taskDetailsList);
        verify(taskingLibraryConfiguration).setTaskDetails(activity, taskAdapter, taskDetailsList);
    }

    @Test
    public void testDisplayNotification() {
        fragment.displayNotification(R.string.saving_dialog_title, R.string.loading_form_message);
        AlertDialog dialog = (AlertDialog) ShadowAlertDialog.getLatestDialog();
        assertTrue(dialog.isShowing());
        assertEquals(activity.getString(R.string.loading_form_message), ((TextView) dialog.findViewById(android.R.id.message)).getText());
    }

    @Test
    public void testDisplayError() {
        fragment.displayNotification(R.string.opening_form_title, R.string.an_error_occured);
        AlertDialog dialog = (AlertDialog) ShadowAlertDialog.getLatestDialog();
        assertTrue(dialog.isShowing());
        assertEquals(activity.getString(R.string.an_error_occured), ((TextView) dialog.findViewById(android.R.id.message)).getText());
    }

    @Test
    public void testStartForm() {
        JSONObject form = new JSONObject();
        fragment.startForm(form);
        Intent intent = shadowOf(activity).getNextStartedActivity();
        assertEquals(FormConfigurationJsonFormActivity.class, shadowOf(intent).getIntentClass());
        assertEquals(form.toString(), intent.getStringExtra(JSON_FORM_PARAM_JSON));
    }

    @Test
    public void testOnDestroy() {
        fragment.onDestroy();
        verify(presenter).onDestroy();
    }

    @Test
    public void testOnDrawerClosed() {
        fragment.onDrawerClosed();
        verify(presenter).onDrawerClosed();
    }

    @Test
    public void testGetJsonFormUtils() {
        assertNull(fragment.getJsonFormUtils());
        fragment.setJsonFormUtils(jsonFormUtils);
        assertEquals(jsonFormUtils, fragment.getJsonFormUtils());
    }


    @Test
    public void testGetUserCurrentLocation() {
        Whitebox.setInternalState(fragment, "locationUtils", locationUtils);
        Location location = new Location("test");
        when(locationUtils.getLastLocation()).thenReturn(location);
        assertEquals(location, fragment.getUserCurrentLocation());
    }


    @Test
    public void testRequestUserLocation() {
        Whitebox.setInternalState(fragment, "locationUtils", locationUtils);
        fragment.requestUserLocation();
        verify(locationUtils).checkLocationSettingsAndStartLocationServices(activity, presenter);
        assertTrue(Whitebox.getInternalState(fragment, "hasRequestedLocation"));
    }


    @Test
    public void testDisplayToast() {
        fragment.displayToast("Hello");
        Toast toast = ShadowToast.getLatestToast();
        assertEquals(Toast.LENGTH_LONG, toast.getDuration());
        assertEquals("Hello", ShadowToast.getTextOfLatestToast());
    }


    @Test
    public void testGetLocationUtils() {
        fragment.initializePresenter();
        assertNotNull(fragment.getLocationUtils());
    }

    @Test
    public void testShowProgressDialog() {
        fragment.showProgressDialog(R.string.opening_form_title, R.string.opening_form_message);
        ProgressDialog progressDialog = (ProgressDialog) ShadowProgressDialog.getLatestDialog();
        assertTrue(progressDialog.isShowing());
        assertEquals(activity.getString(R.string.opening_form_title), ShadowApplication.getInstance().getLatestDialog().getTitle());
    }


    @Test
    public void testHideProgressDialog() {
        fragment.showProgressDialog(R.string.opening_form_title, R.string.opening_form_message);
        ProgressDialog progressDialog = (ProgressDialog) ShadowProgressDialog.getLatestDialog();
        assertTrue(progressDialog.isShowing());
        fragment.hideProgressDialog();
        assertFalse(progressDialog.isShowing());

    }


    @Test
    public void testSetInventionType() {
        fragment.setInventionType(R.string.intervention_unit);
        assertEquals(activity.getString(R.string.intervention_unit), ((TextView) fragment.getView().findViewById(R.id.intervention_type)).getText());
    }


    @Test
    public void testRegisterFamily() {
        TaskDetails details = TestingUtils.getTaskDetails();
        fragment.registerFamily(details);

        Mockito.verify(activity).startFamilyRegistration(details);
    }

    @Test
    public void testOpenFamilyProfile() {
        TaskDetails details = TestingUtils.getTaskDetails();
        CommonPersonObjectClient person = TestingUtils.getCommonPersonObjectClient();
        fragment.openFamilyProfile(person, details);

        Mockito.verify(taskingLibraryConfiguration).openFamilyProfile(activity, person, details);
    }

    @Test
    public void testDisplayIndexCaseDetails() {
        Whitebox.setInternalState(activity, "caseClassificationFragment", caseClassificationFragment);
        JSONObject indexCase = new JSONObject();
        fragment.displayIndexCaseDetails(indexCase);
        verify(caseClassificationFragment).displayIndexCase(indexCase);

    }

    @Test
    public void testSetNumberOfFilters() {
        fragment.setNumberOfFilters(2);
        TextView filterTextView = fragment.getView().findViewById(R.id.filter_text_view);
        assertEquals(activity.getResources().getString(R.string.filters, 2), filterTextView.getText());
        assertEquals(activity.getResources().getDimensionPixelSize(R.dimen.filter_toggle_end_margin), filterTextView.getPaddingStart());
    }


    @Test
    public void testClearFilters() {
        fragment.clearFilter();
        TextView filterTextView = fragment.getView().findViewById(R.id.filter_text_view);
        assertEquals(activity.getResources().getString(R.string.filter), filterTextView.getText());
        assertEquals(activity.getResources().getDimensionPixelSize(R.dimen.filter_toggle_padding), filterTextView.getPaddingStart());
    }

    @Test
    public void testOpenFilterActivityShouldCallTaskLibraryConfigurationOpenFilterActivity() {
        fragment.openFilterActivity(null);
        Intent intent = shadowOf(activity).getNextStartedActivity();
        assertEquals(FilterTasksActivity.class, shadowOf(intent).getIntentClass());
    }

    @Test
    public void testSetSearchPhrase() {
        fragment.setSearchPhrase("H");
        assertEquals("H", fragment.getSearchView().getText().toString());
    }


    @Test
    public void testOnActivityResultGetUserLocation() {
        Whitebox.setInternalState(fragment, "hasRequestedLocation", true);
        when(presenter.getLocationPresenter()).thenReturn(locationPresenter);
        Whitebox.setInternalState(fragment, "locationUtils", locationUtils);
        fragment.onActivityResult(RequestCode.LOCATION_SETTINGS, RESULT_OK, null);
        verify(locationPresenter).waitForUserLocation();
        verify(locationUtils).requestLocationUpdates(presenter);
    }


    @Test
    public void testOnActivityResultGetUserLocationFailed() {
        Whitebox.setInternalState(fragment, "hasRequestedLocation", true);
        when(presenter.getLocationPresenter()).thenReturn(locationPresenter);
        fragment.onActivityResult(RequestCode.LOCATION_SETTINGS, RESULT_CANCELED, null);
        verify(locationPresenter).onGetUserLocationFailed();
        verify(locationUtils, never()).requestLocationUpdates(presenter);
    }

    @Test
    public void testOnActivityResultFilterTasks() {
        Intent intent = new Intent();
        TaskFilterParams params = new TaskFilterParams("");
        intent.putExtra(FILTER_SORT_PARAMS, params);
        fragment.onActivityResult(REQUEST_CODE_FILTER_TASKS, RESULT_OK, intent);
        verify(presenter).filterTasks(params);
    }

    @Test
    public void testDisplayResetTaskInfoDialog() {
        TaskDetails taskDetails = TestingUtils.getTaskDetails();
        fragment.displayResetTaskInfoDialog(taskDetails);

        AlertDialog alertDialog = (AlertDialog) ShadowAlertDialog.getLatestDialog();
        assertTrue(alertDialog.isShowing());

        TextView tv = alertDialog.findViewById(android.R.id.message);
        assertEquals(getString(R.string.undo_task_msg), tv.getText());

        Whitebox.setInternalState(fragment, "presenter", presenter);
        alertDialog.getButton(BUTTON_POSITIVE).performClick();
        verify(presenter).resetTaskInfo(taskDetailsArgumentCaptor.capture());
        assertEquals(taskDetails.getTaskId(), taskDetailsArgumentCaptor.getValue().getTaskId());
        assertFalse(alertDialog.isShowing());
    }

    @Test
    public void testDisplayTaskActionDialogForEdit() {
        TaskDetails taskDetails = TestingUtils.getTaskDetails();
        fragment.displayTaskActionDialog(taskDetails, mock(View.class));

        AlertDialog alertDialog = (AlertDialog) ShadowAlertDialog.getLatestDialog();
        assertTrue(alertDialog.isShowing());

        TextView tv = alertDialog.findViewById(android.R.id.message);
        assertEquals(getString(R.string.choose_action), tv.getText());

        Whitebox.setInternalState(fragment, "presenter", presenter);
        alertDialog.getButton(BUTTON_POSITIVE).performClick();
        verify(presenter).onTaskSelected(taskDetailsArgumentCaptor.capture(), anyBoolean());
        assertEquals(taskDetails.getTaskId(), taskDetailsArgumentCaptor.getValue().getTaskId());
        assertFalse(alertDialog.isShowing());
    }

    @Test
    public void testDisplayTaskActionDialogForUndo() {
        TaskDetails taskDetails = TestingUtils.getTaskDetails();
        fragment = spy(fragment);
        fragment.displayTaskActionDialog(taskDetails, mock(View.class));

        AlertDialog alertDialog = (AlertDialog) ShadowAlertDialog.getLatestDialog();
        assertTrue(alertDialog.isShowing());

        TextView tv = alertDialog.findViewById(android.R.id.message);
        assertEquals(getString(R.string.choose_action), tv.getText());

        Whitebox.setInternalState(fragment, "presenter", presenter);
        alertDialog.getButton(BUTTON_NEGATIVE).performClick();
        verify(fragment).displayResetTaskInfoDialog(taskDetailsArgumentCaptor.capture());
        assertEquals(taskDetails.getTaskId(), taskDetailsArgumentCaptor.getValue().getTaskId());
        assertFalse(alertDialog.isShowing());
    }

}
