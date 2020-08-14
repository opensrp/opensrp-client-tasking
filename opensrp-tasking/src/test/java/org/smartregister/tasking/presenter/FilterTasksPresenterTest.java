package org.smartregister.tasking.presenter;

import android.content.Intent;
import android.widget.ToggleButton;

import com.google.android.flexbox.FlexboxLayout;

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
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.tasking.BaseUnitTest;
import org.smartregister.tasking.R;
import org.smartregister.tasking.TaskingLibrary;
import org.smartregister.tasking.contract.FilterTasksContract;
import org.smartregister.tasking.model.TaskFilterParams;
import org.smartregister.tasking.util.Constants.BusinessStatus;
import org.smartregister.tasking.util.Constants.Filter;
import org.smartregister.tasking.util.Constants.Intervention;
import org.smartregister.tasking.util.Constants.InterventionType;
import org.smartregister.tasking.util.PreferencesUtil;
import org.smartregister.tasking.util.TaskingLibraryConfiguration;
import org.smartregister.tasking.util.TestingUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by samuelgithengi on 1/28/20.
 */
public class FilterTasksPresenterTest extends BaseUnitTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private FilterTasksContract.View view;

    @Mock
    private FlexboxLayout flexboxLayout;

    @Mock
    private ToggleButton toggleButton;

    @Captor
    private ArgumentCaptor<Intent> intentArgumentCaptor;

    private FilterTasksPresenter filterTasksPresenter;

    private String planId = UUID.randomUUID().toString();

    private TaskingLibraryConfiguration taskingLibraryConfiguration;

    @Before
    public void setUp() {
        PreferencesUtil.getInstance().setCurrentPlan(planId);
        taskingLibraryConfiguration = spy(TaskingLibrary.getInstance().getTaskingLibraryConfiguration());
        ReflectionHelpers.setField(TaskingLibrary.getInstance(), "taskingLibraryConfiguration", taskingLibraryConfiguration);
        filterTasksPresenter = new FilterTasksPresenter(view);
    }

    @Test
    public void testPopulateLabels() {
        Map<String, Integer> labelsMap = Whitebox.getInternalState(filterTasksPresenter, "labelsMap");

        Mockito.verify(taskingLibraryConfiguration).populateLabels();
        assertEquals(taskingLibraryConfiguration.populateLabels(), labelsMap);
    }

    @Test
    public void testGetStringResource() {
        assertNull(filterTasksPresenter.getStringResource("anu12"));
    }

    @Test
    public void testOnToggleChanged() {
        filterTasksPresenter.onToggleChanged(true, Filter.STATUS, BusinessStatus.BLOOD_SCREENING_COMPLETE);
        verify(view).onFiltedSelected(1);
        Map<String, Set<String>> filters = Whitebox.getInternalState(filterTasksPresenter, "checkedFilters");
        assertEquals(1, filters.size());
        assertEquals(1, filters.get(Filter.STATUS).size());
        assertEquals(BusinessStatus.BLOOD_SCREENING_COMPLETE, filters.get(Filter.STATUS).iterator().next());

        filterTasksPresenter.onToggleChanged(false, Filter.STATUS, BusinessStatus.BLOOD_SCREENING_COMPLETE);
        verify(view).onFiltedSelected(0);
        filters = Whitebox.getInternalState(filterTasksPresenter, "checkedFilters");
        assertTrue(filters.isEmpty());

    }

    @Test
    public void testOnToggleChangedMultipleFilters() {
        filterTasksPresenter.onToggleChanged(true, Filter.STATUS, BusinessStatus.BLOOD_SCREENING_COMPLETE);
        filterTasksPresenter.onToggleChanged(true, Filter.STATUS, BusinessStatus.COMPLETE);
        filterTasksPresenter.onToggleChanged(true, Filter.INTERVENTION_UNIT, InterventionType.PERSON);
        verify(view).onFiltedSelected(2);
        Map<String, Set<String>> filters = Whitebox.getInternalState(filterTasksPresenter, "checkedFilters");
        assertEquals(2, filters.size());
        assertEquals(2, filters.get(Filter.STATUS).size());
        assertEquals(new HashSet<>(Arrays.asList(BusinessStatus.BLOOD_SCREENING_COMPLETE, BusinessStatus.COMPLETE)), filters.get(Filter.STATUS));


        assertEquals(1, filters.get(Filter.INTERVENTION_UNIT).size());
        assertEquals(InterventionType.PERSON, filters.get(Filter.INTERVENTION_UNIT).iterator().next());

        filterTasksPresenter.onToggleChanged(false, Filter.STATUS, BusinessStatus.BLOOD_SCREENING_COMPLETE);
        filters = Whitebox.getInternalState(filterTasksPresenter, "checkedFilters");
        assertEquals(2, filters.size());
        assertEquals(1, filters.get(Filter.STATUS).size());
        assertEquals(BusinessStatus.COMPLETE, filters.get(Filter.STATUS).iterator().next());

    }

    @Test
    public void testGetIntentionTypesMDA() {
        doReturn(true).when(taskingLibraryConfiguration).isMDA();
        doReturn(false).when(taskingLibraryConfiguration).isFocusInvestigation();

        assertEquals(Intervention.MDA_INTERVENTIONS, filterTasksPresenter.getIntentionTypes());
    }


    @Test
    public void testGetIntentionTypesFI() {
        doReturn(false).when(taskingLibraryConfiguration).isMDA();
        doReturn(true).when(taskingLibraryConfiguration).isFocusInvestigation();

        assertEquals(Intervention.FI_INTERVENTIONS, filterTasksPresenter.getIntentionTypes());
    }

    @Test
    public void testGetIntentionTypesIRS() {
        doReturn(false).when(taskingLibraryConfiguration).isMDA();
        doReturn(false).when(taskingLibraryConfiguration).isFocusInvestigation();

        assertEquals(Intervention.IRS_INTERVENTIONS, filterTasksPresenter.getIntentionTypes());
    }


    @Test
    public void testGetBusinessStatusOptionsMDA() {
        doReturn(false).when(taskingLibraryConfiguration).isFocusInvestigation();
        doReturn(true).when(taskingLibraryConfiguration).isMDA();

        assertEquals(BusinessStatus.MDA_BUSINESS_STATUS, filterTasksPresenter.getBusinessStatusOptions());
    }


    @Test
    public void testGetBusinessStatusOptionsFI() {
        doReturn(true).when(taskingLibraryConfiguration).isFocusInvestigation();

        assertEquals(BusinessStatus.FI_BUSINESS_STATUS, filterTasksPresenter.getBusinessStatusOptions());
    }

    @Test
    public void testGetBusinessStatusOptionsIRS() {
        doReturn(false).when(taskingLibraryConfiguration).isFocusInvestigation();
        doReturn(false).when(taskingLibraryConfiguration).isMDA();

        assertEquals(BusinessStatus.IRS_BUSINESS_STATUS, filterTasksPresenter.getBusinessStatusOptions());
    }


    @Test
    public void testOnApplyFilters() {
        filterTasksPresenter.onApplyFilters("Status");
        verify(view).applyFilters(intentArgumentCaptor.capture());
        TaskFilterParams taskFilter = (TaskFilterParams) intentArgumentCaptor.getValue().getSerializableExtra(Filter.FILTER_SORT_PARAMS);
        assertNotNull(taskFilter);
        assertTrue(taskFilter.getCheckedFilters().isEmpty());
        assertEquals("Status", taskFilter.getSortBy());
    }


    @Test
    public void testRestoreCheckedFilters() {
        TaskFilterParams filterParams = TestingUtils.getFilterParams();
        when(view.getBusinessStatusLayout()).thenReturn(flexboxLayout);
        when(view.getTaskCodeLayout()).thenReturn(flexboxLayout);
        when(view.getInterventionTypeLayout()).thenReturn(flexboxLayout);
        when(flexboxLayout.findViewWithTag(any())).thenReturn(toggleButton);
        when(flexboxLayout.getResources()).thenReturn(RuntimeEnvironment.application.getResources());
        filterTasksPresenter.restoreCheckedFilters(filterParams);
        verify(toggleButton, times(3)).setChecked(true);
        verify(view).setSortBySelection(1);

        filterParams.getCheckedFilters().remove(Filter.INTERVENTION_UNIT);
        filterParams.setSortBy("Type");
        filterTasksPresenter.restoreCheckedFilters(filterParams);
        verify(toggleButton, times(3 + 2)).setChecked(true);
        verify(view).setSortBySelection(2);
    }
}
