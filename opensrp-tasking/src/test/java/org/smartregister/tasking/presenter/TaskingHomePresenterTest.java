package org.smartregister.tasking.presenter;

import android.graphics.PointF;
import android.graphics.RectF;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.gson.JsonPrimitive;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Projection;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.reflect.Whitebox;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowAlertDialog;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Task;
import org.smartregister.tasking.BaseUnitTest;
import org.smartregister.tasking.R;
import org.smartregister.tasking.TaskingLibrary;
import org.smartregister.tasking.contract.BaseDrawerContract;
import org.smartregister.tasking.contract.TaskingHomeActivityContract;
import org.smartregister.tasking.interactor.TaskingHomeInteractor;
import org.smartregister.tasking.model.CardDetails;
import org.smartregister.tasking.model.TaskFilterParams;
import org.smartregister.tasking.util.Constants;
import org.smartregister.tasking.util.PreferencesUtil;
import org.smartregister.tasking.util.TaskingConstants;
import org.smartregister.tasking.util.TaskingJsonFormUtils;
import org.smartregister.tasking.util.TaskingLibraryConfiguration;
import org.smartregister.tasking.util.TestingUtils;
import org.smartregister.util.JsonFormUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static com.vijay.jsonwizard.constants.JsonFormConstants.TEXT;
import static com.vijay.jsonwizard.constants.JsonFormConstants.VALUE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.smartregister.domain.Task.TaskStatus.IN_PROGRESS;
import static org.smartregister.tasking.util.TaskingConstants.BusinessStatus.BEDNET_DISTRIBUTED;
import static org.smartregister.tasking.util.TaskingConstants.BusinessStatus.COMPLETE;
import static org.smartregister.tasking.util.TaskingConstants.BusinessStatus.NOT_VISITED;
import static org.smartregister.tasking.util.TaskingConstants.Intervention.BLOOD_SCREENING;
import static org.smartregister.tasking.util.TaskingConstants.Intervention.IRS;
import static org.smartregister.tasking.util.TaskingConstants.Intervention.PAOT;
import static org.smartregister.tasking.util.TaskingConstants.Intervention.REGISTER_FAMILY;
import static org.smartregister.tasking.util.TaskingConstants.JsonForm.LOCATION_COMPONENT_ACTIVE;
import static org.smartregister.tasking.util.TaskingConstants.JsonForm.SPRAY_FORM_ZAMBIA;
import static org.smartregister.tasking.util.TaskingConstants.JsonForm.STRUCTURE_NAME;
import static org.smartregister.tasking.util.TaskingConstants.Properties.FAMILY_MEMBER_NAMES;
import static org.smartregister.tasking.util.TaskingConstants.Properties.FEATURE_SELECT_TASK_BUSINESS_STATUS;
import static org.smartregister.tasking.util.TaskingConstants.Properties.TASK_BUSINESS_STATUS;
import static org.smartregister.tasking.util.TaskingConstants.Properties.TASK_CODE;
import static org.smartregister.tasking.util.TaskingConstants.Properties.TASK_IDENTIFIER;
import static org.smartregister.tasking.util.TaskingConstants.Properties.TASK_STATUS;
import static org.smartregister.tasking.util.TaskingConstants.REGISTER_STRUCTURE_EVENT;
import static org.smartregister.tasking.util.TaskingConstants.SPRAY_EVENT;

/**
 * Created by samuelgithengi on 1/27/20.
 */
public class TaskingHomePresenterTest extends BaseUnitTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private TaskingHomePresenter presenter;

    @Mock
    private TaskingHomeActivityContract.View view;

    @Mock
    private BaseDrawerContract.Presenter drawerPresenter;

    @Mock
    private TaskingHomeInteractor taskingHomeInteractor;

    @Mock
    private BaseDrawerContract.View drawerView;

    @Mock
    private MapboxMap mapboxMap;

    @Mock
    private Projection mapProjection;

    @Mock
    private Feature feature;

    @Mock
    private TaskingJsonFormUtils jsonFormUtils;

    @Captor
    private ArgumentCaptor<FeatureCollection> featureCollectionArgumentCaptor;

    @Captor
    private ArgumentCaptor<Feature> featureArgumentCaptor;

    @Captor
    private ArgumentCaptor<Boolean> booleanArgumentCaptor;

    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;

    @Captor
    private ArgumentCaptor<CardDetails> cardDetailsArgumentCaptor;

    @Captor
    private ArgumentCaptor<CommonPersonObjectClient> commonPersonObjectClientArgumentCaptor;

    @Captor
    private ArgumentCaptor<JSONObject> jsonArgumentCaptor;

    private PreferencesUtil prefsUtil = PreferencesUtil.getInstance();

    private String planId = UUID.randomUUID().toString();

    private String operationalArea = UUID.randomUUID().toString();

    private TaskFilterParams filterParams = TestingUtils.getFilterParams();

    private TaskingLibraryConfiguration taskingLibraryConfiguration;

    @Before
    public void setUp() {
        org.smartregister.Context.bindtypes = new ArrayList<>();
        presenter = new TaskingHomePresenter(view, drawerPresenter);
        Whitebox.setInternalState(presenter, "taskingHomeInteractor", taskingHomeInteractor);
        Whitebox.setInternalState(presenter, "jsonFormUtils", jsonFormUtils);
        prefsUtil.setCurrentPlanId(planId);
        prefsUtil.setCurrentOperationalArea(operationalArea);
        when(view.getContext()).thenReturn(RuntimeEnvironment.application);
        taskingLibraryConfiguration = spy(TaskingLibrary.getInstance().getTaskingLibraryConfiguration());
        Whitebox.setInternalState(presenter, "taskingLibraryConfiguration", taskingLibraryConfiguration);

//        Whitebox.setInternalState(BuildConfig.class, BuildConfig.BUILD_COUNTRY, Country.THAILAND);
    }

    @Test
    public void testOnDrawerClosed() {
        when(drawerPresenter.isChangedCurrentSelection()).thenReturn(true);
        presenter.onDrawerClosed();
        verify(view).showProgressDialog(R.string.fetching_structures_title, R.string.fetching_structures_message);
        verify(taskingHomeInteractor).fetchLocations(planId, operationalArea);
    }

    @Test
    public void testOnStructuresFetchedWithNoStructures() throws JSONException {
        JSONObject features = new JSONObject();
        features.put(Constants.GeoJSON.TYPE, Constants.GeoJSON.FEATURE_COLLECTION);
        presenter.onStructuresFetched(features, feature, new ArrayList<>());
        verify(view).displayNotification(R.string.fetching_structures_title,
                R.string.fetch_location_and_structures_failed, operationalArea);

        verify(view).setGeoJsonSource(FeatureCollection.fromJson(features.toString()), feature, false);
        verify(view).clearSelectedFeature();
        verify(view).closeCardView(R.id.btn_collapse_spray_card_view);
        verify(drawerPresenter).setChangedCurrentSelection(false);
    }


    @Test
    public void testOnStructuresFetchedWithEmptyFeatures() throws JSONException {
        JSONObject features = new com.cocoahero.android.geojson.FeatureCollection().toJSON();
        presenter.onStructuresFetched(features, feature, new ArrayList<>());
        verify(view).displayNotification(R.string.fetching_structures_title, R.string.no_structures_found);

        verify(view).setGeoJsonSource(FeatureCollection.fromJson(features.toString()), feature, false);
        verify(view, never()).clearSelectedFeature();
        verify(view, never()).closeCardView(R.id.btn_collapse_spray_card_view);
    }


    @Test
    public void testOnStructuresFetched() throws JSONException {
        FeatureCollection featureCollection = FeatureCollection.fromFeature(TestingUtils.getStructure());
        presenter.onStructuresFetched(new JSONObject(featureCollection.toJson()), feature, Collections.singletonList(TestingUtils.getTaskDetails()));
        verify(drawerPresenter).setChangedCurrentSelection(false);
        verify(view).setGeoJsonSource(featureCollection, feature, false);
        assertEquals(feature, Whitebox.getInternalState(presenter, "operationalArea"));
        assertFalse(Whitebox.getInternalState(presenter, "isTasksFiltered"));

    }

    @Test
    public void testOnStructuresFetchedWithFilterAndNoSearchFiltersStructures() throws JSONException {
        FeatureCollection featureCollection = FeatureCollection.fromFeature(TestingUtils.getStructure());
        TaskFilterParams params = TestingUtils.getFilterParams();
        Whitebox.setInternalState(presenter, "filterParams", params);
        presenter.onStructuresFetched(new JSONObject(featureCollection.toJson()), feature, Collections.singletonList(TestingUtils.getTaskDetails()));
        verify(drawerPresenter).setChangedCurrentSelection(false);
        verify(view).setGeoJsonSource(FeatureCollection.fromFeatures(new ArrayList<>()), null, false);
        assertEquals(feature, Whitebox.getInternalState(presenter, "operationalArea"));
        assertTrue(Whitebox.getInternalState(presenter, "isTasksFiltered"));
        verify(view).setNumberOfFilters(params.getCheckedFilters().size());
        verify(view).setSearchPhrase("");
    }


    @Test
    public void testOnStructuresFetchedWithFilterAndSearchSearchesStructures() throws JSONException {
        FeatureCollection featureCollection = FeatureCollection.fromFeature(TestingUtils.getStructure());
        TaskFilterParams params = TestingUtils.getFilterParams();
        Whitebox.setInternalState(presenter, "filterParams", params);
        Whitebox.setInternalState(presenter, "searchPhrase", "Doe");
        presenter.onStructuresFetched(new JSONObject(featureCollection.toJson()), feature, Collections.singletonList(TestingUtils.getTaskDetails()));
        verify(drawerPresenter).setChangedCurrentSelection(false);
        verify(view).setGeoJsonSource(FeatureCollection.fromFeatures(new ArrayList<>()), null, false);
        assertEquals(feature, Whitebox.getInternalState(presenter, "operationalArea"));
    }

    @Test
    public void testOnMapReady() {
        presenter.onMapReady();
        verify(taskingHomeInteractor).fetchLocations(planId, operationalArea);

    }

    @Test
    public void testOnMapReadyWithNoPlanOrOprationalAreaSelected() {
        when(drawerPresenter.getView()).thenReturn(drawerView);
        prefsUtil.setCurrentOperationalArea("");
        presenter.onMapReady();
        verify(view).displayNotification(R.string.select_campaign_operational_area_title, R.string.select_campaign_operational_area);
        verify(drawerView).lockNavigationDrawerForSelection();

    }

    @Test
    public void testOnFilterTasksClicked() {
        Whitebox.setInternalState(presenter, "filterParams", filterParams);
        presenter.onFilterTasksClicked();
        verify(view).openFilterTaskActivity(filterParams);
    }

    @Test
    public void testOnOpenTaskRegisterClicked() {
        Whitebox.setInternalState(presenter, "filterParams", filterParams);
        presenter.onOpenTaskRegisterClicked();
        verify(view).openTaskRegister(filterParams);
    }

    @Test
    public void setTaskFilterParams() {
        TaskFilterParams params = new TaskFilterParams("Doe");
        presenter.setTaskFilterParams(params);
        verify(view).setSearchPhrase(params.getSearchPhrase());
    }

    @Test
    public void testOnMapClickedWithInvalidZoomLevel() {
        CameraPosition cameraPosition = new CameraPosition.Builder().zoom(10).build();
        when(mapboxMap.getCameraPosition()).thenReturn(cameraPosition);
        presenter.onMapClicked(mapboxMap, new LatLng(), false);
        verify(view).displayToast(R.string.zoom_in_to_select);
    }

    @Test
    public void testOnMapClickedMatches1Feature() {
        LatLng clickedPoint = new LatLng(12.06766, -18.02341);
        CameraPosition cameraPosition = new CameraPosition.Builder().zoom(19).build();
        when(mapboxMap.getCameraPosition()).thenReturn(cameraPosition);
        when(mapboxMap.getProjection()).thenReturn(mapProjection);
        when(mapProjection.toScreenLocation(clickedPoint)).thenReturn(new PointF());
        when(mapboxMap.queryRenderedFeatures(any(PointF.class), any())).thenReturn(new ArrayList<>());
        when(mapboxMap.queryRenderedFeatures(any(RectF.class), any())).thenReturn(Collections.singletonList(feature));
        when(view.getContext()).thenReturn(RuntimeEnvironment.application);
        presenter.onMapClicked(mapboxMap, clickedPoint, false);
        verify(view).closeAllCardViews();
        verify(view).displaySelectedFeature(feature, clickedPoint);
    }

    @Test
    public void testOnMapClickedMatchesMultipleFeature() {
        LatLng clickedPoint = new LatLng(12.06766, -18.02341);
        CameraPosition cameraPosition = new CameraPosition.Builder().zoom(19).build();
        when(mapboxMap.getCameraPosition()).thenReturn(cameraPosition);
        when(mapboxMap.getProjection()).thenReturn(mapProjection);
        when(mapProjection.toScreenLocation(clickedPoint)).thenReturn(new PointF());
        Feature structure = TestingUtils.getStructure();
        when(mapboxMap.queryRenderedFeatures(any(PointF.class), any())).thenReturn(Arrays.asList(structure, feature));
        when(view.getContext()).thenReturn(RuntimeEnvironment.application);
        presenter.onMapClicked(mapboxMap, clickedPoint, false);
        verify(view).closeAllCardViews();
        verify(view).displaySelectedFeature(structure, clickedPoint);
    }

    @Test
    public void testFilterTasksWithNullParams() {
        presenter.filterTasks(new TaskFilterParams(""));
        verify(view).setNumberOfFilters(0);
        assertFalse(Whitebox.getInternalState(presenter, "isTasksFiltered"));
    }

    @Test
    public void testFilterTasksBusinessStatus() {
        Feature structure = TestingUtils.getStructure();
        TaskFilterParams params = new TaskFilterParams("");
        params.getCheckedFilters().put(TaskingConstants.Filter.STATUS, Collections.singleton(NOT_VISITED));
        Whitebox.setInternalState(presenter, "featureCollection", FeatureCollection.fromFeature(structure));
        //match is filter for business status works no feature is returned
        presenter.filterTasks(params);
        verify(view).setGeoJsonSource(FeatureCollection.fromFeatures(new ArrayList<>()), null, false);
        assertTrue(Whitebox.getInternalState(presenter, "isTasksFiltered"));


        //match is filter for business status works feature is returned
        structure.addStringProperty(Constants.Properties.TASK_BUSINESS_STATUS, NOT_VISITED);
        Whitebox.setInternalState(presenter, "featureCollection", FeatureCollection.fromFeatures(new Feature[]{structure, feature}));
        presenter.filterTasks(params);
        verify(view).setGeoJsonSource(FeatureCollection.fromFeature(structure), null, false);
        assertTrue(Whitebox.getInternalState(presenter, "isTasksFiltered"));


    }


    @Test
    public void testFilterWithAllFilters() {
        Feature structure = TestingUtils.getStructure();
        TaskFilterParams params = new TaskFilterParams("");
        params.getCheckedFilters().put(TaskingConstants.Filter.STATUS, Collections.singleton(NOT_VISITED));
        params.getCheckedFilters().put(TaskingConstants.Filter.CODE, Collections.singleton(IRS));
        Whitebox.setInternalState(presenter, "featureCollection", FeatureCollection.fromFeature(structure));
        //match is filter for business status works no feature is returned
        presenter.filterTasks(params);
        verify(view).setGeoJsonSource(FeatureCollection.fromFeatures(new ArrayList<>()), null, false);
        assertTrue(Whitebox.getInternalState(presenter, "isTasksFiltered"));


        //match is filter for business status works feature is returned
        structure.addStringProperty(Constants.Properties.TASK_BUSINESS_STATUS, NOT_VISITED);
        structure.addProperty(Constants.Properties.TASK_CODE_LIST, new JsonPrimitive(IRS));
        structure.addStringProperty(Constants.Properties.TASK_CODE, IRS);

        Whitebox.setInternalState(presenter, "featureCollection", FeatureCollection.fromFeatures(new Feature[]{structure, feature}));
        presenter.filterTasks(params);
        verify(view).setGeoJsonSource(FeatureCollection.fromFeature(structure), null, false);
        assertTrue(Whitebox.getInternalState(presenter, "isTasksFiltered"));

        //filter by intervention unit structure
        params.getCheckedFilters().put(TaskingConstants.Filter.INTERVENTION_UNIT, Collections.singleton(TaskingConstants.InterventionType.STRUCTURE));
        presenter.filterTasks(params);
        verify(view, times(2)).setGeoJsonSource(FeatureCollection.fromFeature(structure), null, false);

        //no features are returned with wrong intervention unit
        params.getCheckedFilters().put(TaskingConstants.Filter.INTERVENTION_UNIT, Collections.singleton(TaskingConstants.InterventionType.PERSON));
        presenter.filterTasks(params);
        verify(view, times(2)).setGeoJsonSource(FeatureCollection.fromFeatures(new ArrayList<>()), null, false);


    }


    @Test
    public void testSearchTasksWithEmptyPhrase() {
        Whitebox.setInternalState(presenter, "featureCollection", FeatureCollection.fromFeatures(new Feature[]{feature}));
        presenter.searchTasks("");
        verify(view).setGeoJsonSource(FeatureCollection.fromFeatures(new Feature[]{feature}), null, false);
    }

    @Test
    public void testSearchTasks() throws JSONException {
        com.cocoahero.android.geojson.Feature feature1 = new com.cocoahero.android.geojson.Feature();
        feature1.setIdentifier("id1");
        feature1.setProperties(new JSONObject().accumulate(STRUCTURE_NAME, "John Doe House"));

        com.cocoahero.android.geojson.Feature feature2 = new com.cocoahero.android.geojson.Feature();
        feature2.setIdentifier("id2");
        feature2.setProperties(new JSONObject().accumulate(FAMILY_MEMBER_NAMES, "John Doe,Jane Doe,Helli Pad"));


        Whitebox.setInternalState(presenter, "featureCollection",
                FeatureCollection.fromFeatures(new Feature[]{Feature.fromJson(feature1.toJSON().toString()),
                        Feature.fromJson(feature2.toJSON().toString())}));

        presenter.searchTasks("Doe");
        verify(view).setGeoJsonSource(featureCollectionArgumentCaptor.capture(), eq(null), eq(false));
        assertEquals(2, featureCollectionArgumentCaptor.getValue().features().size());

        presenter.searchTasks("House");
        verify(view, times(2)).setGeoJsonSource(featureCollectionArgumentCaptor.capture(), eq(null), eq(false));
        assertEquals(1, featureCollectionArgumentCaptor.getValue().features().size());
        assertEquals("id1", featureCollectionArgumentCaptor.getValue().features().get(0).id());


        presenter.searchTasks("Helli");
        verify(view, times(3)).setGeoJsonSource(featureCollectionArgumentCaptor.capture(), eq(null), eq(false));
        assertEquals(1, featureCollectionArgumentCaptor.getValue().features().size());
        assertEquals("id2", featureCollectionArgumentCaptor.getValue().features().get(0).id());
    }

    @Test
    public void testOnInterventionTaskInfoReset() {
        taskingLibraryConfiguration.setRefreshMapOnEventSaved(true);
        presenter.onInterventionTaskInfoReset(true);

        verify(view).hideProgressDialog();
        verify(view).clearSelectedFeature();
        assertFalse(taskingLibraryConfiguration.isRefreshMapOnEventSaved());
    }

    @Test
    public void testOnstructureMarkedInactive() throws Exception {

        com.cocoahero.android.geojson.Feature feature1 = new com.cocoahero.android.geojson.Feature();
        feature1.setIdentifier("id1");
        feature1.setProperties(new JSONObject()
                .accumulate(TASK_BUSINESS_STATUS, BEDNET_DISTRIBUTED)
                .accumulate(TASK_IDENTIFIER, "task1"));


        Feature mapboxFeature = Feature.fromJson(feature1.toJSON().toString());
        assertEquals(BEDNET_DISTRIBUTED, mapboxFeature.getStringProperty(TASK_BUSINESS_STATUS));
        assertEquals("task1", mapboxFeature.getStringProperty(TASK_IDENTIFIER));

        Whitebox.setInternalState(presenter, "featureCollection",
                FeatureCollection.fromFeatures(new Feature[]{mapboxFeature}));

        Whitebox.setInternalState(presenter, "selectedFeature", mapboxFeature);

        presenter.onStructureMarkedInactive();
        verify(view).setGeoJsonSource(featureCollectionArgumentCaptor.capture(), any(), booleanArgumentCaptor.capture());
        assertFalse(booleanArgumentCaptor.getValue());
        assertNull(featureCollectionArgumentCaptor.getValue().features().get(0).getStringProperty(TASK_BUSINESS_STATUS));
        assertNull(featureCollectionArgumentCaptor.getValue().features().get(0).getStringProperty(TASK_IDENTIFIER));
    }

    @Test
    public void testOnMarkStructureInEligibleConfirmed() throws Exception {

        com.cocoahero.android.geojson.Feature feature1 = new com.cocoahero.android.geojson.Feature();
        feature1.setIdentifier("id1");
        Feature mapboxFeature = Feature.fromJson(feature1.toJSON().toString());
        Whitebox.setInternalState(presenter, "selectedFeature", mapboxFeature);
        Whitebox.setInternalState(presenter, "reasonUnEligible", "No residents");
        presenter.onMarkStructureIneligibleConfirmed();

        verify(taskingHomeInteractor).markStructureAsIneligible(featureArgumentCaptor.capture(), stringArgumentCaptor.capture());
        assertEquals("No residents", stringArgumentCaptor.getValue());
        assertEquals(mapboxFeature.id(), featureArgumentCaptor.getValue().id());
    }

    @Test
    public void testOnStructureMarkedIneligible() throws Exception {
        com.cocoahero.android.geojson.Feature feature1 = new com.cocoahero.android.geojson.Feature();
        feature1.setIdentifier("id1");
        feature1.setProperties(new JSONObject());


        Feature mapboxFeature = Feature.fromJson(feature1.toJSON().toString());
        assertNull(mapboxFeature.getStringProperty(TASK_BUSINESS_STATUS));
        assertNull(mapboxFeature.getStringProperty(TASK_IDENTIFIER));

        Whitebox.setInternalState(presenter, "featureCollection",
                FeatureCollection.fromFeatures(new Feature[]{mapboxFeature}));

        Whitebox.setInternalState(presenter, "selectedFeature", mapboxFeature);

        presenter.onStructureMarkedIneligible();
        verify(view).setGeoJsonSource(featureCollectionArgumentCaptor.capture(), any(), booleanArgumentCaptor.capture());
        assertFalse(booleanArgumentCaptor.getValue());
//        assertEquals(NOT_ELIGIBLE, featureCollectionArgumentCaptor.getValue().features().get(0).getStringProperty(TASK_BUSINESS_STATUS));
//        assertEquals(NOT_ELIGIBLE, featureCollectionArgumentCaptor.getValue().features().get(0).getStringProperty(FEATURE_SELECT_TASK_BUSINESS_STATUS));
    }

    @Test
    public void testOnFamilyFound() {
        CommonPersonObjectClient family = new CommonPersonObjectClient("caseId", null, "test family");

        presenter.onFamilyFound(family);

        verify(view).openStructureProfile(commonPersonObjectClientArgumentCaptor.capture());
        assertEquals("caseId", commonPersonObjectClientArgumentCaptor.getValue().getCaseId());
        assertEquals("test family", commonPersonObjectClientArgumentCaptor.getValue().getName());
    }

    @Test
    public void testOnFamilyFoundWithNullParam() {

        presenter.onFamilyFound(null);
        verify(view).displayNotification(R.string.fetch_family_failed, R.string.failed_to_find_family);
    }

    @Test
    public void testOnResume() {
        taskingLibraryConfiguration.setRefreshMapOnEventSaved(true);
        presenter = spy(presenter);
        presenter.onResume();

        verify(presenter).refreshStructures(true);
        verify(view).clearSelectedFeature();
        assertFalse(taskingLibraryConfiguration.isRefreshMapOnEventSaved());
    }

    @Test
    public void testDisplayMarkStructureIneligibleDialog() {

        Whitebox.setInternalState(presenter, "markStructureIneligibleConfirmed", false);
        Whitebox.setInternalState(presenter, "reasonUnEligible", "eligible");

        assertFalse(Whitebox.getInternalState(presenter, "markStructureIneligibleConfirmed"));
        assertEquals("eligible", Whitebox.getInternalState(presenter, "reasonUnEligible"));
        presenter = spy(presenter);
        presenter.displayMarkStructureIneligibleDialog();

        AlertDialog alertDialog = (AlertDialog) ShadowAlertDialog.getLatestDialog();
        assertTrue(alertDialog.isShowing());

        TextView tv = alertDialog.findViewById(android.R.id.message);
        assertEquals(getString(R.string.is_structure_eligible_for_fam_reg), tv.getText());

        alertDialog.getButton(BUTTON_NEGATIVE).performClick();
        assertFalse(alertDialog.isShowing());

        verify(presenter).onMarkStructureIneligibleConfirmed();

        assertFalse(Whitebox.getInternalState(presenter, "markStructureIneligibleConfirmed"));
        assertEquals(view.getContext().getString(R.string.not_eligible_unoccupied),
                Whitebox.getInternalState(presenter, "reasonUnEligible"));

    }

    @Test
    public void testOnUndoInterventionStatus() throws Exception {

        com.cocoahero.android.geojson.Feature feature1 = new com.cocoahero.android.geojson.Feature();
        feature1.setIdentifier("id1");


        Feature mapboxFeature = Feature.fromJson(feature1.toJSON().toString());


        Whitebox.setInternalState(presenter, "selectedFeature", mapboxFeature);

        presenter.onUndoInterventionStatus(BLOOD_SCREENING);

        verify(view).showProgressDialog(R.string.resetting_task_title, R.string.resetting_task_msg);
        verify(taskingHomeInteractor).resetInterventionTaskInfo(BLOOD_SCREENING, mapboxFeature.id());

    }

    @Test
    public void testSaveJsonForm() {
        String jsonString = "{\"name\":\"trever\",\"encounter_type\":\"custom\"}";

        presenter.saveJsonForm(jsonString);
        verify(view).showProgressDialog(R.string.saving_title, R.string.saving_message);
        verify(taskingHomeInteractor).saveJsonForm(jsonString);
    }


    @Test
    public void testSaveJsonFormForRegisterStructureShouldFetchLocations() {
        String jsonString = "{\"name\":\"trever\",\"encounter_type\":\"Register_Structure\",\"step1\":{\"fields\":[{\"key\":\"valid_operational_area\",\"type\":\"hidden\",\"value\":\"3244354-345435434\"},{\"key\":\"my_location_active\",\"type\":\"hidden\",\"value\":\"true\"},{\"key\":\"structure\",\"type\":\"geowidget\",\"v_zoom_max\":{\"value\":\"16.5\",\"err\":\"Please zoom in to add a point\"},\"value\":{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[28.740448054710495,-9.311798364364043,0]},\"properties\":null}}]}}";
        presenter.saveJsonForm(jsonString);
        String name = JsonFormUtils.getFieldValue(jsonString, TaskingConstants.JsonForm.VALID_OPERATIONAL_AREA);
        verify(view).showProgressDialog(R.string.opening_form_title, R.string.add_structure_form_redirecting, name);
        verify(taskingHomeInteractor).fetchLocations(planId, name, JsonFormUtils.getFieldValue(jsonString, TaskingConstants.JsonForm.STRUCTURE), Boolean.valueOf(JsonFormUtils.getFieldValue(jsonString, LOCATION_COMPONENT_ACTIVE)));
    }

    @Test
    public void testOnFormSaved() throws Exception {
        String structureId = "id1";
        String taskId = "taskId";

        com.cocoahero.android.geojson.Feature feature1 = new com.cocoahero.android.geojson.Feature();
        feature1.setIdentifier(structureId);
        feature1.setProperties(new JSONObject());


        Feature mapboxFeature = Feature.fromJson(feature1.toJSON().toString());
        assertNull(mapboxFeature.getStringProperty(TASK_BUSINESS_STATUS));
        assertNull(mapboxFeature.getStringProperty(TASK_STATUS));

        Whitebox.setInternalState(presenter, "featureCollection",
                FeatureCollection.fromFeatures(new Feature[]{mapboxFeature}));

        Whitebox.setInternalState(presenter, "selectedFeature", mapboxFeature);

        presenter.onFormSaved(structureId, taskId, IN_PROGRESS, COMPLETE, BLOOD_SCREENING);

        verify(view).hideProgressDialog();

        verify(view).setGeoJsonSource(featureCollectionArgumentCaptor.capture(), any(), booleanArgumentCaptor.capture());
        assertFalse(booleanArgumentCaptor.getValue());
        assertEquals(COMPLETE, featureCollectionArgumentCaptor.getValue().features().get(0).getStringProperty(TASK_BUSINESS_STATUS));
        assertEquals(IN_PROGRESS.name(), featureCollectionArgumentCaptor.getValue().features().get(0).getStringProperty(TASK_STATUS));

        verify(taskingHomeInteractor).fetchInterventionDetails(BLOOD_SCREENING, structureId, false);
    }

    @Test
    public void testResetFeatureTasks() throws Exception {
        String structureId = "id1";
        com.cocoahero.android.geojson.Feature feature1 = new com.cocoahero.android.geojson.Feature();
        feature1.setIdentifier(structureId);
        feature1.setProperties(new JSONObject());


        Feature mapboxFeature = Feature.fromJson(feature1.toJSON().toString());
        assertNull(mapboxFeature.getStringProperty(TASK_BUSINESS_STATUS));
        assertNull(mapboxFeature.getStringProperty(TASK_STATUS));
        assertNull(mapboxFeature.getStringProperty(TASK_IDENTIFIER));
        assertNull(mapboxFeature.getStringProperty(TASK_CODE));
        assertNull(mapboxFeature.getStringProperty(FEATURE_SELECT_TASK_BUSINESS_STATUS));

        Whitebox.setInternalState(presenter, "featureCollection",
                FeatureCollection.fromFeatures(new Feature[]{mapboxFeature}));

        Task task = TestingUtils.getTask("entityid");

        presenter.resetFeatureTasks(structureId, task);

        verify(view).setGeoJsonSource(featureCollectionArgumentCaptor.capture(), any(), booleanArgumentCaptor.capture());
        assertFalse(booleanArgumentCaptor.getValue());
        assertEquals(task.getBusinessStatus(), featureCollectionArgumentCaptor.getValue().features().get(0).getStringProperty(TASK_BUSINESS_STATUS));
        assertEquals(task.getBusinessStatus(), featureCollectionArgumentCaptor.getValue().features().get(0).getStringProperty(FEATURE_SELECT_TASK_BUSINESS_STATUS));
        assertEquals(task.getStatus().name(), featureCollectionArgumentCaptor.getValue().features().get(0).getStringProperty(TASK_STATUS));
        assertEquals(task.getIdentifier(), featureCollectionArgumentCaptor.getValue().features().get(0).getStringProperty(TASK_IDENTIFIER));
        assertEquals(task.getCode(), featureCollectionArgumentCaptor.getValue().features().get(0).getStringProperty(TASK_CODE));

    }

    @Test
    public void testOnFormSaveFailureForSprayEvent() {

        presenter.onFormSaveFailure(SPRAY_EVENT);

        verify(view).hideProgressDialog();
        verify(view).displayNotification(R.string.form_save_failure_title, R.string.spray_form_save_failure);
    }

    @Test
    public void testOnFormSaveFailureForStructureEvent() {

        presenter.onFormSaveFailure(REGISTER_STRUCTURE_EVENT);

        verify(view).hideProgressDialog();
        verify(view).displayNotification(R.string.form_save_failure_title, R.string.add_structure_form_save_failure);
    }

//    @Test
//    public void testOnInterventionFormDetailsFetched() {
//        assertNull(Whitebox.getInternalState(presenter, "cardDetails"));
//        assertFalse(Whitebox.getInternalState(presenter, "changeInterventionStatus"));
//
//        FamilyCardDetails expectedCardDetails = new FamilyCardDetails(COMPLETE, "12-2-2020", "nifi-user");
//
//        presenter.onInterventionFormDetailsFetched(expectedCardDetails);
//
//        verify(view).hideProgressDialog();
//        assertTrue(Whitebox.getInternalState(presenter, "changeInterventionStatus"));
//
//        FamilyCardDetails actualCardDetails = Whitebox.getInternalState(presenter, "cardDetails");
//        assertEquals(expectedCardDetails.getStatus(), actualCardDetails.getStatus());
//        assertEquals(expectedCardDetails.getDateCreated(), actualCardDetails.getDateCreated());
//        assertEquals(expectedCardDetails.getOwner(), actualCardDetails.getOwner());
//
//    }
//
//    @Test
//    public void testOnFamilyCardDetailsFetched() {
//
//        FamilyCardDetails expectedCardDetails = new FamilyCardDetails(COMPLETE, "1582279044", "nifi-user");
//
//        presenter.onCardDetailsFetched(expectedCardDetails);
//
//        verify(view).openCardView(cardDetailsArgumentCaptor.capture());
//
//        FamilyCardDetails actualCardDetails = (FamilyCardDetails) cardDetailsArgumentCaptor.getValue();
//
//        assertEquals(COMPLETE, actualCardDetails.getStatus());
//        assertEquals("nifi-user", actualCardDetails.getOwner());
//        assertEquals("19 Jan 1970", actualCardDetails.getDateCreated());
//
//
//    }
//
//    @Test
//    public void testOnSprayCardDetailsFetched() {
//
//        SprayCardDetails expectedCardDetails = new SprayCardDetails(NOT_SPRAYED, "Residential", "2014-07-04T12:08:56.235-0700", "gideon", "Mark", "Available");
//
//        presenter.onCardDetailsFetched(expectedCardDetails);
//
//        verify(view).openCardView(cardDetailsArgumentCaptor.capture());
//
//        SprayCardDetails actualCardDetails = (SprayCardDetails) cardDetailsArgumentCaptor.getValue();
//
//        assertEquals(NOT_SPRAYED, actualCardDetails.getStatus());
//        assertEquals("Residential", actualCardDetails.getPropertyType());
//        assertEquals("04 Jul 2014", actualCardDetails.getSprayDate());
//        assertEquals("gideon", actualCardDetails.getSprayOperator());
//        assertEquals("Mark", actualCardDetails.getFamilyHead());
//        assertEquals("Available", actualCardDetails.getReason());
//
//    }
//
//    @Test
//    public void testOnMosquitoHarvestCardDetailsFetched() {
//
//        MosquitoHarvestCardDetails expectedCardDetails = new MosquitoHarvestCardDetails(NOT_VISITED, "2019-07-04", "2019-08-05", MOSQUITO_COLLECTION);
//
//
//        presenter.onCardDetailsFetched(expectedCardDetails);
//
//        verify(view).openCardView(cardDetailsArgumentCaptor.capture());
//
//        MosquitoHarvestCardDetails actualCardDetails = (MosquitoHarvestCardDetails) cardDetailsArgumentCaptor.getValue();
//
//        assertEquals(NOT_VISITED, actualCardDetails.getStatus());
//        assertEquals("2019-07-04", actualCardDetails.getStartDate());
//        assertEquals("2019-08-05", actualCardDetails.getEndDate());
//        assertEquals(MOSQUITO_COLLECTION, actualCardDetails.getInterventionType());
//    }
//
//    @Test
//    public void testOnIRSVerificationCardDetailsFetched() {
//        IRSVerificationCardDetails expectedCardDetails = new IRSVerificationCardDetails(NOT_VISITED,
//                "yes", "no", "sprayed", "No chalk",
//                "No sticker", "No card");
//
//
//        presenter.onCardDetailsFetched(expectedCardDetails);
//
//        verify(view).openCardView(cardDetailsArgumentCaptor.capture());
//
//        IRSVerificationCardDetails actualCardDetails = (IRSVerificationCardDetails) cardDetailsArgumentCaptor.getValue();
//
//        assertEquals(NOT_VISITED, actualCardDetails.getStatus());
//        assertEquals("yes", actualCardDetails.getTrueStructure());
//        assertEquals("no", actualCardDetails.getEligStruc());
//        assertEquals("sprayed", actualCardDetails.getReportedSprayStatus());
//        assertEquals("No chalk", actualCardDetails.getChalkSprayStatus());
//        assertEquals("No sticker", actualCardDetails.getStickerSprayStatus());
//        assertEquals("No card", actualCardDetails.getCardSprayStatus());
//
//    }

    @Test
    public void testOnLocationValidatedForMarkStructureIneligible() {

        Whitebox.setInternalState(presenter, "markStructureIneligibleConfirmed", true);
        assertTrue(Whitebox.getInternalState(presenter, "markStructureIneligibleConfirmed"));
        presenter = spy(presenter);
        presenter.onLocationValidated();

        verify(presenter).onMarkStructureIneligibleConfirmed();
        assertFalse(Whitebox.getInternalState(presenter, "markStructureIneligibleConfirmed"));
    }

    @Test
    public void testOnLocationValidatedForRegisterFamily() {

        Whitebox.setInternalState(presenter, "selectedFeatureInterventionType", REGISTER_FAMILY);

        presenter.onLocationValidated();

        verify(view).registerFamily();
    }

    @Test
    public void testOnLocationValidatedForCardDetailsWithChangeInterventionStatusFalse() {

        Whitebox.setInternalState(presenter, "selectedFeatureInterventionType", PAOT);
        Whitebox.setInternalState(presenter, "changeInterventionStatus", false);
        presenter = spy(presenter);
        presenter.onLocationValidated();

        verify(presenter).startForm(featureArgumentCaptor.capture(), cardDetailsArgumentCaptor.capture(), stringArgumentCaptor.capture());
        assertNull(cardDetailsArgumentCaptor.getValue());
        assertEquals(PAOT, stringArgumentCaptor.getValue());
    }

//    @Test
//    public void testOnlocationValidatedForCardDetailWithChangeInterventionTrue() {
//        FamilyCardDetails expectedCardDetails = new FamilyCardDetails(COMPLETE, "19 Jan 1970", "nifi-user");
//        Whitebox.setInternalState(presenter, "cardDetails", expectedCardDetails);
//        Whitebox.setInternalState(presenter, "selectedFeatureInterventionType", PAOT);
//        Whitebox.setInternalState(presenter, "changeInterventionStatus", true);
//        presenter = spy(presenter);
//        presenter.onLocationValidated();
//
//        verify(presenter).startForm(featureArgumentCaptor.capture(), cardDetailsArgumentCaptor.capture(), stringArgumentCaptor.capture());
//        assertEquals(PAOT, stringArgumentCaptor.getValue());
//
//        FamilyCardDetails actualCardDetails = (FamilyCardDetails) cardDetailsArgumentCaptor.getValue();
//
//        assertEquals(COMPLETE, actualCardDetails.getStatus());
//        assertEquals("nifi-user", actualCardDetails.getOwner());
//        assertEquals("19 Jan 1970", actualCardDetails.getDateCreated());
//    }

//    @Test
//    public void testOnChangeInterventionStatusForIRS() throws JSONException {
//        Feature mapboxFeature = initTestFeature("id1");
//
//        Whitebox.setInternalState(presenter, "selectedFeature", mapboxFeature);
//        presenter.onChangeInterventionStatus(IRS);
//        verify(view).showProgressDialog(R.string.fetching_structure_title, R.string.fetching_structure_message);
//        verify(taskingHomeInteractor).fetchInterventionDetails(IRS, "id1", true);
//    }
//
//    @Test
//    public void testOnChangeInterventionStatusForMosquitoCollection() throws JSONException {
//
//        Feature mapboxFeature = initTestFeature("id1");
//        Whitebox.setInternalState(presenter, "selectedFeature", mapboxFeature);
//        presenter.onChangeInterventionStatus(MOSQUITO_COLLECTION);
//        verify(view).showProgressDialog(R.string.fetching_mosquito_collection_points_title, R.string.fetching_mosquito_collection_points_message);
//        verify(taskingHomeInteractor).fetchInterventionDetails(MOSQUITO_COLLECTION, "id1", true);
//    }
//
//    @Test
//    public void testOnChangeInterventionStatusForLarvalDipping() throws JSONException {
//
//        Feature mapboxFeature = initTestFeature("id1");
//        Whitebox.setInternalState(presenter, "selectedFeature", mapboxFeature);
//        presenter.onChangeInterventionStatus(LARVAL_DIPPING);
//        verify(view).showProgressDialog(R.string.fetching_larval_dipping_points_title, R.string.fetching_larval_dipping_points_message);
//        verify(taskingHomeInteractor).fetchInterventionDetails(LARVAL_DIPPING, "id1", true);
//    }
//
//    @Test
//    public void testOnChangeInterventionStatusForPAOT() throws JSONException {
//
//        Feature mapboxFeature = initTestFeature("id1");
//        Whitebox.setInternalState(presenter, "selectedFeature", mapboxFeature);
//        presenter.onChangeInterventionStatus(PAOT);
//        verify(view).showProgressDialog(R.string.fetching_paot_title, R.string.fetching_paot_message);
//        verify(taskingHomeInteractor).fetchInterventionDetails(PAOT, "id1", true);
//    }
//
//    @Test
//    public void testOnFeatureSelectedByLongClickWhenStructureIsInactive() throws Exception {
//
//        Feature feature = initTestFeature("id1");
//        feature.addStringProperty(LOCATION_STATUS, INACTIVE.name());
//
//        Whitebox.invokeMethod(presenter, "onFeatureSelectedByLongClick", feature);
//        verify(view).displayToast(R.string.structure_is_inactive);
//    }

//    @Test
//    public void testOnFeatureSelectedByLongClickWhenStructureHasNoTask() throws Exception {
//
//        Feature feature = initTestFeature("id1");
//        feature.addStringProperty(LOCATION_STATUS, ACTIVE.name());
//        feature.removeProperty(TASK_IDENTIFIER);
//
//        Whitebox.invokeMethod(presenter, "onFeatureSelectedByLongClick", feature);
//        verify(view).displayMarkStructureInactiveDialog();
//    }
//
//    @Test
//    public void testOnFeatureSelectedByLongClickWhenTaskBusinessStatusIsNotVisited() throws Exception {
//
//        Feature feature = initTestFeature("id1");
//        feature.addStringProperty(LOCATION_STATUS, ACTIVE.name());
//        feature.addStringProperty(TASK_BUSINESS_STATUS, NOT_VISITED);
//        feature.addStringProperty(TASK_IDENTIFIER, "task-1");
//
//        Whitebox.invokeMethod(presenter, "onFeatureSelectedByLongClick", feature);
//        verify(view).displayMarkStructureInactiveDialog();
//    }
//
//    @Test
//    public void testOnFeatureSelectedByLongClickWhenTaskHasBeenCompleted() throws Exception {
//
//        Feature feature = initTestFeature("id1");
//        feature.addStringProperty(LOCATION_STATUS, ACTIVE.name());
//        feature.addStringProperty(TASK_BUSINESS_STATUS, SPRAYED);
//        feature.addStringProperty(TASK_IDENTIFIER, "task-1");
//
//        Whitebox.invokeMethod(presenter, "onFeatureSelectedByLongClick", feature);
//        verify(view).displayToast(R.string.cannot_make_structure_inactive);
//    }


    @Test
    public void testStartFormShouldPopulateFormData() throws Exception {
        when(jsonFormUtils.getFormName(null, IRS)).thenReturn(SPRAY_FORM_ZAMBIA);
        when(jsonFormUtils.getFormJSON(view.getContext(), SPRAY_FORM_ZAMBIA, feature, null, null)).thenReturn(new JSONObject(TestingUtils.DUMMY_JSON_FORM_STRING));
        presenter.startForm(feature, null, IRS);
//        verify(jsonFormUtils).populateForm(any(), any());
//        verify(jsonFormUtils, atLeastOnce()).populateServerOptions(any(), any(), any(), any(), any());
//        verify(jsonFormUtils, atLeastOnce()).populateField(any(), anyString(), anyString(), anyString());
        verify(view).startJsonForm(any());
    }

    @Test
    public void testOnAddStructureClickedShouldPopulateFormAndOpenIt() throws JSONException {
        Point point = Point.fromLngLat(28.740448054710495, -9.311798364364043);
        when(jsonFormUtils.getFormName(REGISTER_STRUCTURE_EVENT)).thenReturn(TaskingConstants.JsonForm.ADD_STRUCTURE_FORM);
        when(jsonFormUtils.getFormString(view.getContext(), TaskingConstants.JsonForm.ADD_STRUCTURE_FORM, null)).thenReturn(TestingUtils.AddStructureFormJson);
        Whitebox.setInternalState(presenter, "operationalArea", feature);
        presenter.onAddStructureClicked(true, point.toJson());
        verify(view).startJsonForm(jsonArgumentCaptor.capture());
        JSONObject formJson = jsonArgumentCaptor.getValue();
        assertEquals("true", jsonArgumentCaptor.getValue().getString(LOCATION_COMPONENT_ACTIVE));
        verify(jsonFormUtils).populateField(formJson, TaskingConstants.JsonForm.SELECTED_OPERATIONAL_AREA_NAME, prefsUtil.getCurrentOperationalArea(), TEXT);
        verify(jsonFormUtils).populateField(formJson, TaskingConstants.JsonForm.STRUCTURE, point.toJson(), VALUE);
    }


    private Feature initTestFeature(String identifier) throws JSONException {
        String structureId = identifier;
        com.cocoahero.android.geojson.Feature feature1 = new com.cocoahero.android.geojson.Feature();
        feature1.setIdentifier(structureId);
        feature1.setProperties(new JSONObject());

        return Feature.fromJson(feature1.toJSON().toString());
    }

}
