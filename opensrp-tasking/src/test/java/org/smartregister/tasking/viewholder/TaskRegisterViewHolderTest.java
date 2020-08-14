package org.smartregister.tasking.viewholder;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.util.Pair;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.reflect.Whitebox;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.tasking.BaseUnitTest;
import org.smartregister.tasking.R;
import org.smartregister.tasking.TaskingLibrary;
import org.smartregister.tasking.model.CardDetails;
import org.smartregister.tasking.model.TaskDetails;
import org.smartregister.tasking.util.Constants;
import org.smartregister.tasking.util.PreferencesUtil;
import org.smartregister.tasking.util.TaskingLibraryConfiguration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.smartregister.tasking.util.Constants.Intervention.FI;
import static org.smartregister.tasking.util.Constants.Intervention.MDA;

/**
 * Created by samuelgithengi on 3/26/19.
 */
public class TaskRegisterViewHolderTest extends BaseUnitTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private PreferencesUtil prefsUtil;

    @Mock
    private TaskDetails taskDetails;

    @Mock
    private CardDetails cardDetails;

    @Mock
    private View.OnClickListener onClickListener;

    private Context context = RuntimeEnvironment.application;

    private TaskRegisterViewHolder viewHolder;
    private TaskingLibraryConfiguration taskingLibraryConfiguration;

    @Before
    public void setUp() {
        View view = LayoutInflater.from(context).inflate(R.layout.task_register_row, null);
        viewHolder = new TaskRegisterViewHolder(view);

        taskingLibraryConfiguration = Mockito.spy(TaskingLibrary.getInstance().getTaskingLibraryConfiguration());
        ReflectionHelpers.setField(TaskingLibrary.getInstance(), "taskingLibraryConfiguration", taskingLibraryConfiguration);
    }

    @Test
    public void testSetIcon() {
        viewHolder.setIcon(R.drawable.woman_placeholder);
        ImageView iconView = viewHolder.itemView.findViewById(R.id.task_icon);
        assertEquals(View.VISIBLE, iconView.getVisibility());
        assertEquals(R.drawable.woman_placeholder, Shadows.shadowOf(iconView.getDrawable()).getCreatedFromResId());

        viewHolder.setIcon(R.drawable.ic_icon_danger);
        assertEquals(R.drawable.ic_icon_danger, Shadows.shadowOf(iconView.getDrawable()).getCreatedFromResId());
    }

    @Test
    public void testSetTaskName() {
        viewHolder.setTaskName("Structure 19");
        assertEquals("Structure 19", ((TextView) viewHolder.itemView.findViewById(R.id.task_name)).getText());
    }


    @Test
    public void testSetDistanceFromStructure() {
        viewHolder.setDistanceFromStructure(34, false);
        assertEquals("34 m away", ((TextView) viewHolder.itemView.findViewById(R.id.distance_from_structure)).getText());
    }

    @Test
    public void testSetDistanceFromCenterOfOperationalArea() {
        Whitebox.setInternalState(viewHolder, "prefsUtil", prefsUtil);
        when(prefsUtil.getCurrentOperationalArea()).thenReturn("MTI_13");
        viewHolder.setDistanceFromStructure(34, true);
        assertEquals("34 m from MTI_13 center", ((TextView) viewHolder.itemView.findViewById(R.id.distance_from_structure)).getText());
    }


    @Test
    public void testHideDistanceFromStructure() {
        viewHolder.hideDistanceFromStructure();
        assertEquals(View.GONE, viewHolder.itemView.findViewById(R.id.distance_from_structure).getVisibility());
    }

    @Test
    public void testHideIcon() {
        viewHolder.hideIcon();
        assertEquals(View.GONE, viewHolder.itemView.findViewById(R.id.task_icon).getVisibility());
    }


    @Test
    public void testSetTaskDetailsForNotSprayed() {
        viewHolder.setTaskDetails(Constants.BusinessStatus.NOT_SPRAYED, "No one Home");
        TextView taskDetails = viewHolder.itemView.findViewById(R.id.task_details);
        assertEquals(View.VISIBLE, taskDetails.getVisibility());
        assertEquals("Reason: No one Home", taskDetails.getText());
    }

    @Test
    public void testSetTaskDetails() {
        viewHolder.setTaskDetails(Constants.BusinessStatus.SPRAYED, "No one Home");
        TextView taskDetails = viewHolder.itemView.findViewById(R.id.task_details);
        assertEquals(View.GONE, taskDetails.getVisibility());
        assertEquals("", taskDetails.getText());
    }

    @Test
    public void testSetTaskActionWithoutCardDetails() {
        viewHolder.setTaskAction("Record\n Index", taskDetails, null, onClickListener);
        TextView taskDetails = viewHolder.itemView.findViewById(R.id.task_action);
        assertEquals(View.VISIBLE, taskDetails.getVisibility());
        assertEquals("Record\n Index", taskDetails.getText());
        taskDetails.performClick();
        verify(onClickListener).onClick(taskDetails);
    }

    @Test
    public void testSetTaskActionWithCardDetails() {
        when(cardDetails.getStatusColor()).thenReturn(R.color.toaster_note_red);
        when(taskDetails.getTaskCount()).thenReturn(null);
        viewHolder.setTaskAction("Record\n Status", taskDetails, cardDetails, onClickListener);
        TextView taskDetails = viewHolder.itemView.findViewById(R.id.task_action);
        assertEquals(View.VISIBLE, taskDetails.getVisibility());
        assertEquals("Record\n Status", taskDetails.getText());
        assertEquals(context.getColor(R.color.toaster_note_red), taskDetails.getCurrentTextColor());
        taskDetails.performClick();
        verify(onClickListener).onClick(taskDetails);

    }

    @Test
    public void testSetTaskActionForFITaskGroupingComplete() {
        setFIAsCurrentPlan();
        PreferencesUtil.getInstance().setCurrentPlan("Focus 1");
        PreferencesUtil.getInstance().setInterventionTypeForPlan("Focus 1", FI);
        when(taskDetails.getTaskCount()).thenReturn(2);
        when(taskDetails.isFamilyRegistered()).thenReturn(true);
        when(taskDetails.isBednetDistributed()).thenReturn(true);
        when(taskDetails.isBloodScreeningDone()).thenReturn(true);

        doReturn(new Pair<Drawable, String>(context.getResources().getDrawable(R.drawable.tasks_complete_bg)
                , RuntimeEnvironment.application.getString(R.string.tasks_complete))).when(taskingLibraryConfiguration).getActionDrawable(context, taskDetails);

        viewHolder.setTaskAction("Record\n Status", taskDetails, cardDetails, onClickListener);

        TextView taskDetails = viewHolder.itemView.findViewById(R.id.task_action);
        assertEquals(View.VISIBLE, taskDetails.getVisibility());
        assertEquals(context.getString(R.string.tasks_complete), taskDetails.getText());
        assertEquals(context.getColor(R.color.text_black), taskDetails.getCurrentTextColor());
        assertEquals(context.getResources().getDrawable(R.drawable.tasks_complete_bg).getConstantState(), taskDetails.getBackground().getConstantState());
        taskDetails.performClick();
        verify(onClickListener).onClick(taskDetails);

    }

    @Test
    public void testSetTaskActionForFITaskGroupingFamRegistered() {
        setFIAsCurrentPlan();
        when(taskDetails.getTaskCount()).thenReturn(2);
        when(taskDetails.isFamilyRegistered()).thenReturn(true);
        when(taskDetails.isBednetDistributed()).thenReturn(false);
        when(taskDetails.isBloodScreeningDone()).thenReturn(false);

        doReturn(new Pair<Drawable, String>(context.getResources().getDrawable(R.drawable.tasks_complete_bg)
                , RuntimeEnvironment.application.getString(R.string.view_tasks))).when(taskingLibraryConfiguration).getActionDrawable(context, taskDetails);

        viewHolder.setTaskAction("Record\n Status", taskDetails, cardDetails, onClickListener);

        TextView taskDetails = viewHolder.itemView.findViewById(R.id.task_action);
        assertEquals(View.VISIBLE, taskDetails.getVisibility());
        assertEquals(context.getString(R.string.view_tasks), taskDetails.getText());
        assertEquals(context.getColor(R.color.text_black), taskDetails.getCurrentTextColor());
        assertEquals(context.getResources().getDrawable(R.drawable.tasks_complete_bg).getConstantState(), taskDetails.getBackground().getConstantState());
        taskDetails.performClick();
        verify(onClickListener).onClick(taskDetails);

    }

    @Test
    public void testSetTaskActionForFITaskGroupingBednetDistributed() {
        setFIAsCurrentPlan();
        when(taskDetails.getTaskCount()).thenReturn(2);
        when(taskDetails.isFamilyRegistered()).thenReturn(true);
        when(taskDetails.isBednetDistributed()).thenReturn(true);
        when(taskDetails.isBloodScreeningDone()).thenReturn(false);

        doReturn(new Pair<Drawable, String>(context.getResources().getDrawable(R.drawable.tasks_complete_bg)
                , RuntimeEnvironment.application.getString(R.string.view_tasks))).when(taskingLibraryConfiguration).getActionDrawable(context, taskDetails);

        viewHolder.setTaskAction("Record\n Status", taskDetails, cardDetails, onClickListener);

        TextView taskDetails = viewHolder.itemView.findViewById(R.id.task_action);
        assertEquals(View.VISIBLE, taskDetails.getVisibility());
        assertEquals(context.getString(R.string.view_tasks), taskDetails.getText());
        assertEquals(context.getColor(R.color.text_black), taskDetails.getCurrentTextColor());
        assertEquals(context.getResources().getDrawable(R.drawable.tasks_complete_bg).getConstantState(), taskDetails.getBackground().getConstantState());
        taskDetails.performClick();
        verify(onClickListener).onClick(taskDetails);

    }

    @Test
    public void testSetTaskActionForFITaskGroupingBloodScreening() {
        setFIAsCurrentPlan();
        when(taskDetails.getTaskCount()).thenReturn(2);
        when(taskDetails.isFamilyRegistered()).thenReturn(true);
        when(taskDetails.isBednetDistributed()).thenReturn(false);
        when(taskDetails.isBloodScreeningDone()).thenReturn(true);

        doReturn(new Pair<Drawable, String>(context.getResources().getDrawable(R.drawable.tasks_complete_bg)
                , RuntimeEnvironment.application.getString(R.string.view_tasks))).when(taskingLibraryConfiguration).getActionDrawable(context, taskDetails);

        viewHolder.setTaskAction("Record\n Status", taskDetails, cardDetails, onClickListener);

        TextView taskDetails = viewHolder.itemView.findViewById(R.id.task_action);
        assertEquals(View.VISIBLE, taskDetails.getVisibility());
        assertEquals(context.getString(R.string.view_tasks), taskDetails.getText());
        assertEquals(context.getColor(R.color.text_black), taskDetails.getCurrentTextColor());
        assertEquals(context.getResources().getDrawable(R.drawable.tasks_complete_bg).getConstantState(), taskDetails.getBackground().getConstantState());
        taskDetails.performClick();
        verify(onClickListener).onClick(taskDetails);

    }

    @Test
    public void testSetTaskActionForFITaskGroupingNoTaskDone() {
        setFIAsCurrentPlan();
        when(taskDetails.getTaskCount()).thenReturn(2);
        when(taskDetails.isFamilyRegTaskExists()).thenReturn(true);
        when(taskDetails.isFamilyRegistered()).thenReturn(false);
        when(taskDetails.isBednetDistributed()).thenReturn(false);
        when(taskDetails.isBloodScreeningDone()).thenReturn(false);

        doReturn(new Pair<Drawable, String>(context.getResources().getDrawable(R.drawable.tasks_complete_bg), RuntimeEnvironment.application.getString(R.string.view_tasks))).when(taskingLibraryConfiguration).getActionDrawable(context, taskDetails);

        viewHolder.setTaskAction("Record\n Status", taskDetails, cardDetails, onClickListener);

        TextView taskDetails = viewHolder.itemView.findViewById(R.id.task_action);
        assertEquals(View.VISIBLE, taskDetails.getVisibility());
        assertEquals(context.getString(R.string.view_tasks), taskDetails.getText());
        assertEquals(context.getColor(R.color.text_black), taskDetails.getCurrentTextColor());
        assertEquals(context.getResources().getDrawable(R.drawable.tasks_complete_bg).getConstantState(), taskDetails.getBackground().getConstantState());
        taskDetails.performClick();
        verify(onClickListener).onClick(taskDetails);

    }

    @Test
    public void testSetTaskActionForMDATaskGroupingMDAAdhered() {
        setMDAAsCurrentPlan();
        when(taskDetails.getTaskCount()).thenReturn(2);
        when(taskDetails.isFamilyRegistered()).thenReturn(true);
        when(taskDetails.isMdaAdhered()).thenReturn(true);

        doReturn(new Pair<Drawable, String>(context.getResources().getDrawable(R.drawable.tasks_complete_bg)
                , RuntimeEnvironment.application.getString(R.string.tasks_complete))).when(taskingLibraryConfiguration).getActionDrawable(context, taskDetails);

        viewHolder.setTaskAction("Record\n Status", taskDetails, cardDetails, onClickListener);

        TextView taskDetails = viewHolder.itemView.findViewById(R.id.task_action);
        assertEquals(View.VISIBLE, taskDetails.getVisibility());
        assertEquals(context.getString(R.string.tasks_complete), taskDetails.getText());
        assertEquals(context.getColor(R.color.text_black), taskDetails.getCurrentTextColor());
        assertEquals(context.getResources().getDrawable(R.drawable.tasks_complete_bg).getConstantState(), taskDetails.getBackground().getConstantState());
        taskDetails.performClick();
        verify(onClickListener).onClick(taskDetails);

    }

    @Test
    public void testSetTaskActionForMDATaskGroupingMdaDispenced() {
        setMDAAsCurrentPlan();
        when(taskDetails.getTaskCount()).thenReturn(2);
        when(taskDetails.isFamilyRegistered()).thenReturn(true);
        when(taskDetails.isMdaAdhered()).thenReturn(false);
        when(taskDetails.isFullyReceived()).thenReturn(true);

        doReturn(new Pair<Drawable, String>(context.getResources().getDrawable(R.drawable.tasks_complete_bg)
                , RuntimeEnvironment.application.getString(R.string.view_tasks))).when(taskingLibraryConfiguration).getActionDrawable(context, taskDetails);

        viewHolder.setTaskAction("Record\n Status", taskDetails, cardDetails, onClickListener);
        TextView taskDetails = viewHolder.itemView.findViewById(R.id.task_action);
        assertEquals(View.VISIBLE, taskDetails.getVisibility());
        assertEquals(context.getString(R.string.view_tasks), taskDetails.getText());
        assertEquals(context.getColor(R.color.text_black), taskDetails.getCurrentTextColor());
        assertEquals(context.getResources().getDrawable(R.drawable.tasks_complete_bg).getConstantState(), taskDetails.getBackground().getConstantState());
        taskDetails.performClick();
        verify(onClickListener).onClick(taskDetails);

    }

    @Test
    public void testSetTaskActionForMDATaskGroupingPartiallyReceived() {
        setMDAAsCurrentPlan();
        when(taskDetails.getTaskCount()).thenReturn(2);
        when(taskDetails.isFamilyRegistered()).thenReturn(true);
        when(taskDetails.isMdaAdhered()).thenReturn(false);
        when(taskDetails.isFullyReceived()).thenReturn(false);
        when(taskDetails.isPartiallyReceived()).thenReturn(true);

        doReturn(new Pair<Drawable, String>(context.getResources().getDrawable(R.drawable.tasks_complete_bg)
                , RuntimeEnvironment.application.getString(R.string.view_tasks))).when(taskingLibraryConfiguration).getActionDrawable(context, taskDetails);

        viewHolder.setTaskAction("Record\n Status", taskDetails, cardDetails, onClickListener);

        TextView taskDetails = viewHolder.itemView.findViewById(R.id.task_action);
        assertEquals(View.VISIBLE, taskDetails.getVisibility());
        assertEquals(context.getString(R.string.view_tasks), taskDetails.getText());
        assertEquals(context.getColor(R.color.text_black), taskDetails.getCurrentTextColor());
        assertEquals(context.getResources().getDrawable(R.drawable.tasks_complete_bg).getConstantState(), taskDetails.getBackground().getConstantState());
        taskDetails.performClick();
        verify(onClickListener).onClick(taskDetails);

    }

    @Test
    public void testSetTaskActionForMDATaskGroupingNoneReceived() {
        setMDAAsCurrentPlan();
        when(taskDetails.getTaskCount()).thenReturn(2);
        when(taskDetails.isFamilyRegistered()).thenReturn(true);
        when(taskDetails.isMdaAdhered()).thenReturn(false);
        when(taskDetails.isFullyReceived()).thenReturn(false);
        when(taskDetails.isPartiallyReceived()).thenReturn(false);
        when(taskDetails.isNoneReceived()).thenReturn(true);

        doReturn(new Pair<Drawable, String>(context.getResources().getDrawable(R.drawable.tasks_complete_bg)
                , RuntimeEnvironment.application.getString(R.string.view_tasks))).when(taskingLibraryConfiguration).getActionDrawable(context, taskDetails);

        viewHolder.setTaskAction("Record\n Status", taskDetails, cardDetails, onClickListener);

        TextView taskDetails = viewHolder.itemView.findViewById(R.id.task_action);
        assertEquals(View.VISIBLE, taskDetails.getVisibility());
        assertEquals(context.getString(R.string.view_tasks), taskDetails.getText());
        assertEquals(context.getColor(R.color.text_black), taskDetails.getCurrentTextColor());
        assertEquals(context.getResources().getDrawable(R.drawable.tasks_complete_bg).getConstantState(), taskDetails.getBackground().getConstantState());
        taskDetails.performClick();
        verify(onClickListener).onClick(taskDetails);

    }

    @Test
    public void testSetTaskActionForMDATaskGroupingNotEligible() {
        setMDAAsCurrentPlan();
        when(taskDetails.getTaskCount()).thenReturn(2);
        when(taskDetails.isFamilyRegistered()).thenReturn(true);
        when(taskDetails.isMdaAdhered()).thenReturn(false);
        when(taskDetails.isFullyReceived()).thenReturn(false);
        when(taskDetails.isPartiallyReceived()).thenReturn(false);
        when(taskDetails.isNoneReceived()).thenReturn(false);
        when(taskDetails.isNotEligible()).thenReturn(true);

        doReturn(new Pair<Drawable, String>(context.getResources().getDrawable(R.drawable.tasks_complete_bg)
                , RuntimeEnvironment.application.getString(R.string.view_tasks))).when(taskingLibraryConfiguration).getActionDrawable(context, taskDetails);

        viewHolder.setTaskAction("Record\n Status", taskDetails, cardDetails, onClickListener);

        TextView taskDetails = viewHolder.itemView.findViewById(R.id.task_action);
        assertEquals(View.VISIBLE, taskDetails.getVisibility());
        assertEquals(context.getString(R.string.view_tasks), taskDetails.getText());
        assertEquals(context.getColor(R.color.text_black), taskDetails.getCurrentTextColor());
        assertEquals(context.getResources().getDrawable(R.drawable.tasks_complete_bg).getConstantState(), taskDetails.getBackground().getConstantState());
        taskDetails.performClick();
        verify(onClickListener).onClick(taskDetails);

    }

    @Test
    public void testSetTaskActionForMDATaskGroupingFamRegistered() {
        setMDAAsCurrentPlan();
        when(taskDetails.getTaskCount()).thenReturn(2);
        when(taskDetails.isFamilyRegistered()).thenReturn(true);
        when(taskDetails.isMdaAdhered()).thenReturn(false);
        when(taskDetails.isFullyReceived()).thenReturn(false);
        when(taskDetails.isPartiallyReceived()).thenReturn(false);
        when(taskDetails.isNoneReceived()).thenReturn(false);
        when(taskDetails.isNotEligible()).thenReturn(false);

        doReturn(new Pair<Drawable, String>(context.getResources().getDrawable(R.drawable.tasks_complete_bg)
                , RuntimeEnvironment.application.getString(R.string.view_tasks))).when(taskingLibraryConfiguration).getActionDrawable(context, taskDetails);

        viewHolder.setTaskAction("Record\n Status", taskDetails, cardDetails, onClickListener);

        TextView taskDetails = viewHolder.itemView.findViewById(R.id.task_action);
        assertEquals(View.VISIBLE, taskDetails.getVisibility());
        assertEquals(context.getString(R.string.view_tasks), taskDetails.getText());
        assertEquals(context.getColor(R.color.text_black), taskDetails.getCurrentTextColor());
        assertEquals(context.getResources().getDrawable(R.drawable.tasks_complete_bg).getConstantState(), taskDetails.getBackground().getConstantState());
        taskDetails.performClick();
        verify(onClickListener).onClick(taskDetails);

    }

    @Test
    public void testSetTaskActionForMDATaskGroupingNoneComplete() {
        setMDAAsCurrentPlan();
        when(taskDetails.getTaskCount()).thenReturn(2);
        when(taskDetails.isFamilyRegTaskExists()).thenReturn(true);
        when(taskDetails.isFamilyRegistered()).thenReturn(false);
        when(taskDetails.isMdaAdhered()).thenReturn(false);
        when(taskDetails.isFullyReceived()).thenReturn(false);
        when(taskDetails.isPartiallyReceived()).thenReturn(false);
        when(taskDetails.isNoneReceived()).thenReturn(false);
        when(taskDetails.isNotEligible()).thenReturn(false);

        doReturn(new Pair<Drawable, String>(context.getResources().getDrawable(R.drawable.tasks_complete_bg)
                , RuntimeEnvironment.application.getString(R.string.view_tasks))).when(taskingLibraryConfiguration).getActionDrawable(context, taskDetails);

        viewHolder.setTaskAction("Record\n Status", taskDetails, cardDetails, onClickListener);

        TextView taskDetails = viewHolder.itemView.findViewById(R.id.task_action);
        assertEquals(View.VISIBLE, taskDetails.getVisibility());
        assertEquals(context.getString(R.string.view_tasks), taskDetails.getText());
        assertEquals(context.getColor(R.color.text_black), taskDetails.getCurrentTextColor());
        assertEquals(context.getResources().getDrawable(R.drawable.tasks_complete_bg).getConstantState(), taskDetails.getBackground().getConstantState());
        taskDetails.performClick();
        verify(onClickListener).onClick(taskDetails);

    }

    @Test
    public void testShowFITasksCompleteActionView() {
        setFIAsCurrentPlan();
        when(taskDetails.getTaskCount()).thenReturn(2);
        when(taskDetails.getCompleteTaskCount()).thenReturn(2);

        doReturn(new Pair<Drawable, String>(context.getResources().getDrawable(R.drawable.tasks_complete_bg)
                , RuntimeEnvironment.application.getString(R.string.tasks_complete))).when(taskingLibraryConfiguration).getActionDrawable(context, taskDetails);

        viewHolder.setTaskAction("Record\n Status", taskDetails, cardDetails, onClickListener);

        TextView taskDetails = viewHolder.itemView.findViewById(R.id.task_action);
        assertEquals(View.VISIBLE, taskDetails.getVisibility());
        assertEquals("Record\n Status", taskDetails.getText());
        verify(taskingLibraryConfiguration).showTasksCompleteActionView(taskDetails);
        taskDetails.performClick();
        verify(onClickListener).onClick(taskDetails);

    }

    @Test
    public void testShowMDATasksCompleteActionView() {
        setMDAAsCurrentPlan();
        when(taskDetails.getTaskCount()).thenReturn(2);
        when(taskDetails.getCompleteTaskCount()).thenReturn(2);

        doReturn(new Pair<Drawable, String>(context.getResources().getDrawable(R.drawable.tasks_complete_bg)
                , RuntimeEnvironment.application.getString(R.string.tasks_complete))).when(taskingLibraryConfiguration).getActionDrawable(context, taskDetails);

        viewHolder.setTaskAction("Record\n Status", taskDetails, cardDetails, onClickListener);

        TextView taskDetails = viewHolder.itemView.findViewById(R.id.task_action);
        assertEquals(View.VISIBLE, taskDetails.getVisibility());
        assertEquals("Record\n Status", taskDetails.getText());
        verify(taskingLibraryConfiguration).showTasksCompleteActionView(taskDetails);
        taskDetails.performClick();
        verify(onClickListener).onClick(taskDetails);

    }

    @Test
    public void testSetHouseNumber() {
        viewHolder.setHouseNumber("House 2");
        TextView taskDetails = viewHolder.itemView.findViewById(R.id.house_number);
        assertEquals("House 2", taskDetails.getText());
    }


    @Test
    public void testShowHouseNumber() {
        viewHolder.showHouseNumber();
        TextView taskDetails = viewHolder.itemView.findViewById(R.id.house_number);
        assertEquals(View.VISIBLE, taskDetails.getVisibility());
    }

    private void setFIAsCurrentPlan() {
        PreferencesUtil.getInstance().setCurrentPlan("Focus 1");
        PreferencesUtil.getInstance().setInterventionTypeForPlan("Focus 1", FI);
    }

    private void setMDAAsCurrentPlan() {
        PreferencesUtil.getInstance().setCurrentPlan("MDA 1");
        PreferencesUtil.getInstance().setInterventionTypeForPlan("MDA 1", MDA);
    }

}
