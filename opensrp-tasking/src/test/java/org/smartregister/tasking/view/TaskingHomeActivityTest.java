package org.smartregister.tasking.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.snackbar.Snackbar;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Projection;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.pluginscalebar.ScaleBarWidget;

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
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowProgressDialog;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.Context;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.SyncEntity;
import org.smartregister.domain.SyncProgress;
import org.smartregister.domain.Task;
import org.smartregister.dto.UserAssignmentDTO;
import org.smartregister.receiver.ValidateAssignmentReceiver;
import org.smartregister.tasking.BaseUnitTest;
import org.smartregister.tasking.R;
import org.smartregister.tasking.TaskingLibrary;
import org.smartregister.tasking.activity.TaskingHomeActivity;
import org.smartregister.tasking.contract.BaseDrawerContract;
import org.smartregister.tasking.model.TaskFilterParams;
import org.smartregister.tasking.presenter.TaskingHomePresenter;
import org.smartregister.tasking.presenter.ValidateUserLocationPresenter;
import org.smartregister.tasking.util.CardDetailsUtil;
import org.smartregister.tasking.util.TaskingJsonFormUtils;
import org.smartregister.tasking.util.TaskingLibraryConfiguration;
import org.smartregister.tasking.util.TaskingMapHelper;
import org.smartregister.tasking.util.TestingUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.ona.kujaku.interfaces.ILocationClient;
import io.ona.kujaku.layers.BoundaryLayer;

import static android.content.DialogInterface.BUTTON_POSITIVE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static io.ona.kujaku.utils.Constants.RequestCode.LOCATION_SETTINGS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.smartregister.receiver.SyncStatusBroadcastReceiver.SyncStatusListener;
import static org.smartregister.receiver.SyncStatusBroadcastReceiver.getInstance;
import static org.smartregister.receiver.SyncStatusBroadcastReceiver.init;
import static org.smartregister.tasking.util.TaskingConstants.ANIMATE_TO_LOCATION_DURATION;
import static org.smartregister.tasking.util.TaskingConstants.DatabaseKeys.STRUCTURE_ID;
import static org.smartregister.tasking.util.TaskingConstants.DatabaseKeys.TASK_ID;
import static org.smartregister.tasking.util.TaskingConstants.Filter.FILTER_SORT_PARAMS;
import static org.smartregister.tasking.util.TaskingConstants.JSON_FORM_PARAM_JSON;
import static org.smartregister.tasking.util.TaskingConstants.RequestCode.REQUEST_CODE_FAMILY_PROFILE;
import static org.smartregister.tasking.util.TaskingConstants.RequestCode.REQUEST_CODE_FILTER_TASKS;
import static org.smartregister.tasking.util.TaskingConstants.RequestCode.REQUEST_CODE_GET_JSON;
import static org.smartregister.tasking.util.TaskingConstants.RequestCode.REQUEST_CODE_TASK_LISTS;

/**
 * Created by samuelgithengi on 1/23/20.
 */
public class TaskingHomeActivityTest extends BaseUnitTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private TaskingHomeActivity taskingHomeActivity;

    private android.content.Context context = RuntimeEnvironment.application;

    private ImageButton myLocationButton = new ImageButton(context);

    private ImageButton layerSwitcherFab = new ImageButton(context);

    @Mock
    private TaskingHomePresenter taskingHomePresenter;

    @Mock
    private TaskingMapHelper taskingMapHelper;

    @Mock
    private BaseDrawerContract.View drawerView;

    @Mock
    private Feature feature;

    @Mock
    private TaskingMapView kujakuMapView;

    @Mock
    private GeoJsonSource geoJsonSource;

    @Mock
    private CardDetailsUtil cardDetailsUtil;

    @Mock
    private MapboxMap mMapboxMap;

    @Mock
    private ValidateUserLocationPresenter locationPresenter;

    @Mock
    private ILocationClient locationClient;

    @Mock
    private Projection projection;

    @Captor
    private ArgumentCaptor<BoundaryLayer> boundaryLayerArgumentCaptor;

    @Captor
    private ArgumentCaptor<CameraPosition> cameraPositionArgumentCaptor;

    @Captor
    private ArgumentCaptor<ScaleBarWidget> scaleBarWidgetArgumentCaptor;

    private TaskingLibraryConfiguration taskingLibraryConfiguration;

    @Mock
    private ValidateAssignmentReceiver validateAssignmentReceiver;

    @Before
    public void setUp() {
        Context.bindtypes = new ArrayList<>();
        taskingHomeActivity = Robolectric.buildActivity(TaskingHomeActivity.class).create().get();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = 30;
        myLocationButton.setLayoutParams(params);
        Whitebox.setInternalState(taskingHomeActivity, "myLocationButton", myLocationButton);
        Whitebox.setInternalState(taskingHomeActivity, "cardDetailsUtil", cardDetailsUtil);

        taskingLibraryConfiguration = spy(TaskingLibrary.getInstance().getTaskingLibraryConfiguration());
        Whitebox.setInternalState(taskingHomeActivity, "taskingLibraryConfiguration", taskingLibraryConfiguration);
        ReflectionHelpers.setStaticField(ValidateAssignmentReceiver.class, "instance", validateAssignmentReceiver);
    }


    @Test
    public void testOnCreate() {
        assertNotNull(Robolectric.buildActivity(TaskingHomeActivity.class).create().get());
    }


//    @Test
//    public void testCloseSprayCardView() {
//        View sprayCardView = taskingHomeActivity.findViewById(R.id.spray_card_view);
//        sprayCardView.setVisibility(VISIBLE);
//        taskingHomeActivity.closeCardView(R.id.btn_collapse_spray_card_view);
//        assertEquals(GONE, sprayCardView.getVisibility());
//    }
//
//    @Test
//    public void testCloseMosquitoCardView() {
//        View cardView = taskingHomeActivity.findViewById(R.id.mosquito_collection_card_view);
//        cardView.setVisibility(VISIBLE);
//        taskingHomeActivity.closeCardView(R.id.btn_collapse_mosquito_collection_card_view);
//        assertEquals(GONE, cardView.getVisibility());
//    }
//
//
//    @Test
//    public void testCloseCardLarvalCardView() {
//        View cardView = taskingHomeActivity.findViewById(R.id.larval_breeding_card_view);
//        cardView.setVisibility(VISIBLE);
//        taskingHomeActivity.closeCardView(R.id.btn_collapse_larval_breeding_card_view);
//        assertEquals(GONE, cardView.getVisibility());
//    }
//
//    @Test
//    public void testClosePAOTCardView() {
//        View cardView = taskingHomeActivity.findViewById(R.id.potential_area_of_transmission_card_view);
//        cardView.setVisibility(VISIBLE);
//        taskingHomeActivity.closeCardView(R.id.btn_collapse_paot_card_view);
//        assertEquals(GONE, cardView.getVisibility());
//    }
//
//    @Test
//    public void testCloseIndicatorsCardView() {
//        View cardView = taskingHomeActivity.findViewById(R.id.indicators_card_view);
//        cardView.setVisibility(VISIBLE);
//        taskingHomeActivity.closeCardView(R.id.btn_collapse_indicators_card_view);
//        assertEquals(GONE, cardView.getVisibility());
//    }
//
//    @Test
//    public void testCloseVerificationCardView() {
//        View cardView = taskingHomeActivity.findViewById(R.id.irs_verification_card_view);
//        cardView.setVisibility(VISIBLE);
//        taskingHomeActivity.closeCardView(R.id.btn_collapse_irs_verification_card_view);
//        assertEquals(GONE, cardView.getVisibility());
//    }
//
//
//    @Test
//    public void testCloseAllCardViews() {
//        taskingHomeActivity.findViewById(R.id.spray_card_view).setVisibility(VISIBLE);
//        taskingHomeActivity.findViewById(R.id.mosquito_collection_card_view).setVisibility(VISIBLE);
//        taskingHomeActivity.findViewById(R.id.larval_breeding_card_view).setVisibility(VISIBLE);
//        taskingHomeActivity.findViewById(R.id.potential_area_of_transmission_card_view).setVisibility(VISIBLE);
//        taskingHomeActivity.findViewById(R.id.indicators_card_view).setVisibility(VISIBLE);
//        taskingHomeActivity.findViewById(R.id.irs_verification_card_view).setVisibility(VISIBLE);
//        taskingHomeActivity.closeAllCardViews();
//        assertEquals(GONE, taskingHomeActivity.findViewById(R.id.spray_card_view).getVisibility());
//        assertEquals(GONE, taskingHomeActivity.findViewById(R.id.mosquito_collection_card_view).getVisibility());
//        assertEquals(GONE, taskingHomeActivity.findViewById(R.id.larval_breeding_card_view).getVisibility());
//        assertEquals(GONE, taskingHomeActivity.findViewById(R.id.potential_area_of_transmission_card_view).getVisibility());
//        assertEquals(GONE, taskingHomeActivity.findViewById(R.id.indicators_card_view).getVisibility());
//        assertEquals(GONE, taskingHomeActivity.findViewById(R.id.irs_verification_card_view).getVisibility());
//    }

    @Test
    public void testPositionMyLocation() {
        taskingHomeActivity = spy(taskingHomeActivity);
        taskingHomeActivity.positionMyLocationAndLayerSwitcher();
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) myLocationButton.getLayoutParams();
        assertEquals(0, layoutParams.topMargin);
        assertEquals(30, layoutParams.bottomMargin);
        assertEquals(Gravity.BOTTOM | Gravity.END, layoutParams.gravity);
    }

    //
//    @Test
//    public void testPositionMyLocationZambia() {
//        taskingHomeActivity = spy(taskingHomeActivity);
//        when(taskingHomeActivity.getBuildCountry()).thenReturn(Country.ZAMBIA);
//        Whitebox.setInternalState(taskingHomeActivity, "myLocationButton", myLocationButton);
//        taskingHomeActivity.positionMyLocationAndLayerSwitcher();
//        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) myLocationButton.getLayoutParams();
//        assertEquals(0, layoutParams.topMargin);
//        int progressHeight = context.getResources().getDimensionPixelSize(R.dimen.progress_height);
//        assertEquals(progressHeight + 40, layoutParams.bottomMargin);
//        assertEquals(Gravity.BOTTOM | Gravity.END, layoutParams.gravity);
//    }
//
//
    @Test
    public void testOpenFilterTaskActivity() {
        taskingHomeActivity.findViewById(R.id.filter_tasks_fab).performClick();
        verify(taskingLibraryConfiguration).openFilterTaskActivity(isNull(), any(TaskingHomeActivity.class));
    }


    @Test
    public void testOpenTaskRegister() {
        taskingHomeActivity.findViewById(R.id.task_register).performClick();
        verify(taskingLibraryConfiguration).openTaskRegister(isNull(), any(TaskingHomeActivity.class));
    }


    @Test
    public void testOnAddStructure() {
        Whitebox.setInternalState(taskingHomeActivity, "taskingHomePresenter", taskingHomePresenter);
        Whitebox.setInternalState(taskingHomeActivity, "mapHelper", taskingMapHelper);
        taskingHomeActivity.findViewById(R.id.btn_add_structure).performClick();
        verify(taskingHomePresenter).onAddStructureClicked(false);
        verify(taskingMapHelper).isMyLocationComponentActive(any(), any());
    }

//    @Test
//    public void testOnChangeSprayStatus() {
//        Whitebox.setInternalState(taskingHomeActivity, "taskingHomePresenter", taskingHomePresenter);
//        taskingHomeActivity.findViewById(R.id.change_spray_status).performClick();
//        verify(taskingHomePresenter).onChangeInterventionStatus(Intervention.IRS);
//    }
//
//
//    @Test
//    public void testRecordMosquitoCollection() {
//        Whitebox.setInternalState(taskingHomeActivity, "taskingHomePresenter", taskingHomePresenter);
//        taskingHomeActivity.findViewById(R.id.btn_record_mosquito_collection).performClick();
//        verify(taskingHomePresenter).onChangeInterventionStatus(Intervention.MOSQUITO_COLLECTION);
//    }
//
//
//    @Test
//    public void testRecordLarvalDipping() {
//        Whitebox.setInternalState(taskingHomeActivity, "taskingHomePresenter", taskingHomePresenter);
//        taskingHomeActivity.findViewById(R.id.btn_record_larval_dipping).performClick();
//        verify(taskingHomePresenter).onChangeInterventionStatus(Intervention.LARVAL_DIPPING);
//    }
//
//    @Test
//    public void testEditPAOTDetails() {
//        Whitebox.setInternalState(taskingHomeActivity, "taskingHomePresenter", taskingHomePresenter);
//        taskingHomeActivity.findViewById(R.id.btn_edit_paot_details).performClick();
//        verify(taskingHomePresenter).onChangeInterventionStatus(Intervention.PAOT);
//    }
//
//    @Test
//    public void testCloseCardView() {
//
//        taskingHomeActivity.findViewById(R.id.btn_collapse_spray_card_view).performClick();
//        assertEquals(GONE, taskingHomeActivity.findViewById(R.id.spray_card_view).getVisibility());
//    }
//
//    @Test
//    public void testOtherCardView() {
//        taskingHomeActivity.findViewById(R.id.btn_collapse_mosquito_collection_card_view).performClick();
//        assertEquals(GONE, taskingHomeActivity.findViewById(R.id.mosquito_collection_card_view).getVisibility());
//    }

    @Test
    public void testOpenDrawerMenu() {
        Whitebox.setInternalState(taskingHomeActivity, "drawerView", drawerView);
        taskingHomeActivity.findViewById(R.id.drawerMenu).performClick();
        verify(drawerView).openDrawerLayout();
    }

    @Test
    public void testOpenIndicators() {
        assertEquals(GONE, taskingHomeActivity.findViewById(R.id.indicators_card_view).getVisibility());
        taskingHomeActivity.findViewById(R.id.progressIndicatorsGroupView).performClick();
        assertEquals(VISIBLE, taskingHomeActivity.findViewById(R.id.indicators_card_view).getVisibility());
    }

//    @Test
//    public void testRegisterFamily() {
//        Whitebox.setInternalState(taskingHomeActivity, "taskingHomePresenter", taskingHomePresenter);
//        when(taskingHomePresenter.getSelectedFeature()).thenReturn(feature);
//        taskingHomeActivity.findViewById(R.id.register_family).performClick();
//        Intent startedIntent = shadowOf(taskingHomeActivity).getNextStartedActivity();
//        assertEquals(FamilyRegisterActivity.class, shadowOf(startedIntent).getIntentClass());
//        assertEquals(GONE, taskingHomeActivity.findViewById(R.id.spray_card_view).getVisibility());
//        assertTrue(startedIntent.hasExtra(TASK_IDENTIFIER));
//    }
//
//
//    @Test
//    public void testOpenStructureProfile() {
//        Whitebox.setInternalState(taskingHomeActivity, "taskingHomePresenter", taskingHomePresenter);
//        when(taskingHomePresenter.getSelectedFeature()).thenReturn(feature);
//        CommonPersonObjectClient client = TestingUtils.getCommonPersonObjectClient();
//        taskingHomeActivity.openStructureProfile(client);
//        Intent startedIntent = shadowOf(taskingHomeActivity).getNextStartedActivity();
//        assertEquals(FamilyProfileActivity.class, shadowOf(startedIntent).getIntentClass());
//        assertEquals(client.getColumnmaps().get(FIRST_NAME), startedIntent.getStringExtra(INTENT_KEY.FAMILY_NAME));
//        assertEquals(client.getCaseId(), startedIntent.getStringExtra(INTENT_KEY.FAMILY_BASE_ENTITY_ID));
//        assertTrue(startedIntent.hasExtra(TASK_IDENTIFIER));
//
//    }


    @Test
    public void testSetGeoJsonSourceWithNullOperationalArea() {
        Whitebox.setInternalState(taskingHomeActivity, "geoJsonSource", geoJsonSource);
        FeatureCollection featureCollection = FeatureCollection.fromFeature(feature);
        taskingHomeActivity.setGeoJsonSource(featureCollection, null, true);
        verify(geoJsonSource).setGeoJson(featureCollection);
        verifyZeroInteractions(mMapboxMap);
    }


    @Test
    public void testSetGeoJsonSource() {
        Whitebox.setInternalState(taskingHomeActivity, "taskingHomePresenter", taskingHomePresenter);
        Whitebox.setInternalState(taskingHomeActivity, "mMapboxMap", mMapboxMap);
        Whitebox.setInternalState(taskingHomeActivity, "geoJsonSource", geoJsonSource);
        Whitebox.setInternalState(taskingHomeActivity, "kujakuMapView", kujakuMapView);
        Whitebox.setInternalState(taskingHomeActivity, "mapHelper", taskingMapHelper);
        Feature operationalArea = mock(Feature.class);
        doReturn(mock(Geometry.class)).when(operationalArea).geometry();

        CameraPosition cameraPosition = new CameraPosition.Builder().build();
        when(mMapboxMap.getCameraForGeometry(operationalArea.geometry())).thenReturn(cameraPosition);
        when(taskingHomePresenter.getInterventionLabel()).thenReturn(R.string.focus_investigation);
        FeatureCollection featureCollection = FeatureCollection.fromFeature(feature);
        taskingHomeActivity.setGeoJsonSource(featureCollection, operationalArea, true);
        verify(geoJsonSource).setGeoJson(featureCollection);
        verify(mMapboxMap).setCameraPosition(cameraPosition);
        verify(kujakuMapView).addLayer(boundaryLayerArgumentCaptor.capture());
        verify(taskingMapHelper).addIndexCaseLayers(mMapboxMap, taskingHomeActivity, featureCollection);
        assertEquals(FeatureCollection.fromFeature(operationalArea), boundaryLayerArgumentCaptor.getValue().getFeatureCollection());
        assertEquals(2, boundaryLayerArgumentCaptor.getValue().getLayerIds().length);
    }


    @Test
    public void testSetGeoJsonSourceSetsCameraOnIndexCase() {
        Whitebox.setInternalState(taskingHomeActivity, "taskingHomePresenter", taskingHomePresenter);
        Whitebox.setInternalState(taskingHomeActivity, "mMapboxMap", mMapboxMap);
        Whitebox.setInternalState(taskingHomeActivity, "geoJsonSource", geoJsonSource);
        Whitebox.setInternalState(taskingHomeActivity, "kujakuMapView", kujakuMapView);
        Whitebox.setInternalState(taskingHomeActivity, "mapHelper", taskingMapHelper);
        Feature indexCase = Feature.fromJson("{\"type\":\"Feature\",\"id\":\"000c99d7\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[28.821878,-10.203831]}}");
        FeatureCollection featureCollection = FeatureCollection.fromFeature(feature);
        CameraPosition cameraPosition = new CameraPosition.Builder().zoom(18.5).build();
        when(mMapboxMap.getCameraPosition()).thenReturn(cameraPosition);
        when(taskingMapHelper.getIndexCase(featureCollection)).thenReturn(indexCase);
        when(taskingHomePresenter.getInterventionLabel()).thenReturn(R.string.focus_investigation);
        taskingHomeActivity.setGeoJsonSource(featureCollection, mock(Feature.class), true);
        verify(geoJsonSource).setGeoJson(featureCollection);
        verify(mMapboxMap).setCameraPosition(cameraPositionArgumentCaptor.capture());
        verify(kujakuMapView).addLayer(boundaryLayerArgumentCaptor.capture());
        verify(taskingMapHelper).addIndexCaseLayers(mMapboxMap, taskingHomeActivity, featureCollection);
        assertEquals(28.821878, cameraPositionArgumentCaptor.getValue().target.getLongitude(), 0);
        assertEquals(-10.203831, cameraPositionArgumentCaptor.getValue().target.getLatitude(), 0);
    }

    @Test
    public void testSetGeoJsonSourceUpdatesBoundaryLayerAndIndexCaseLayers() throws JSONException {
        Whitebox.setInternalState(taskingHomeActivity, "taskingHomePresenter", taskingHomePresenter);
        Whitebox.setInternalState(taskingHomeActivity, "mMapboxMap", mMapboxMap);
        Whitebox.setInternalState(taskingHomeActivity, "geoJsonSource", geoJsonSource);
        Whitebox.setInternalState(taskingHomeActivity, "kujakuMapView", kujakuMapView);
        Whitebox.setInternalState(taskingHomeActivity, "mapHelper", taskingMapHelper);
        BoundaryLayer boundaryLayer = mock(BoundaryLayer.class);
        Whitebox.setInternalState(taskingHomeActivity, "boundaryLayer", boundaryLayer);
        Feature operationalArea = mock(Feature.class);
        when(taskingHomePresenter.getInterventionLabel()).thenReturn(R.string.focus_investigation);
        FeatureCollection featureCollection = FeatureCollection.fromFeature(feature);
        when(taskingMapHelper.getIndexCaseLineLayer()).thenReturn(mock(LineLayer.class));
        taskingHomeActivity.setGeoJsonSource(featureCollection, operationalArea, true);
        verify(geoJsonSource).setGeoJson(featureCollection);
        verify(boundaryLayer).updateFeatures(FeatureCollection.fromFeature(operationalArea));
        verify(taskingMapHelper).updateIndexCaseLayers(mMapboxMap, featureCollection, taskingHomeActivity);

    }


    @Test
    public void testDisplayNotificationWithArgs() {
        taskingHomeActivity.displayNotification(R.string.archive_family, R.string.archive_family_failed, "Test");
        AlertDialog alertDialog = (AlertDialog) ShadowAlertDialog.getLatestDialog();
        assertTrue(alertDialog.isShowing());
        TextView tv = alertDialog.findViewById(android.R.id.message);
        assertEquals("Archiving family Test failed", tv.getText());

    }

    @Test
    public void testDisplayNotification() {
        taskingHomeActivity.displayNotification(R.string.archive_family, R.string.confirm_archive_family);
        AlertDialog alertDialog = (AlertDialog) ShadowAlertDialog.getLatestDialog();
        assertTrue(alertDialog.isShowing());
        TextView tv = alertDialog.findViewById(android.R.id.message);
        assertEquals("Confirm Household Archival", tv.getText());

    }

//    @Test
//    public void testOpenSprayCardView() {
//        assertEquals(GONE, taskingHomeActivity.findViewById(R.id.spray_card_view).getVisibility());
//        Whitebox.setInternalState(taskingHomeActivity, "cardDetailsUtil", cardDetailsUtil);
//        SprayCardDetails cardDetails = mock(SprayCardDetails.class);
//        taskingHomeActivity.openCardView(cardDetails);
//        verify(cardDetailsUtil).populateSprayCardTextViews(cardDetails, taskingHomeActivity);
//        assertEquals(VISIBLE, taskingHomeActivity.findViewById(R.id.spray_card_view).getVisibility());
//    }
//
//    @Test
//    public void testOpenMosquitoCardView() {
//        MosquitoHarvestCardDetails mosquitoHarvestCardDetails = mock(MosquitoHarvestCardDetails.class);
//        taskingHomeActivity.openCardView(mosquitoHarvestCardDetails);
//        verify(cardDetailsUtil).populateAndOpenMosquitoHarvestCard(mosquitoHarvestCardDetails, taskingHomeActivity);
//    }
//
//    @Test
//    public void testOpenIRSVerificationCardView() {
//        IRSVerificationCardDetails irsVerificationCardDetails = mock(IRSVerificationCardDetails.class);
//        taskingHomeActivity.openCardView(irsVerificationCardDetails);
//        verify(cardDetailsUtil).populateAndOpenIRSVerificationCard(irsVerificationCardDetails, taskingHomeActivity);
//    }

//    @Test
//    public void testOpenFamilyCardView() {
//        assertEquals(GONE, taskingHomeActivity.findViewById(R.id.spray_card_view).getVisibility());
//        FamilyCardDetails familyCardDetails = mock(FamilyCardDetails.class);
//        taskingHomeActivity.openCardView(familyCardDetails);
//        verify(cardDetailsUtil).populateFamilyCard(familyCardDetails, taskingHomeActivity);
//        assertEquals(VISIBLE, taskingHomeActivity.findViewById(R.id.spray_card_view).getVisibility());
//    }

    @Test
    public void testStartJsonForm() {
        TaskingJsonFormUtils jsonFormUtils = mock(TaskingJsonFormUtils.class);
        Whitebox.setInternalState(taskingHomeActivity, "jsonFormUtils", jsonFormUtils);
        JSONObject form = new JSONObject();
        taskingHomeActivity.startJsonForm(form);
        verify(jsonFormUtils).startJsonForm(form, taskingHomeActivity);
    }

    @Test
    public void testDisplaySelectedFeature() {
        Whitebox.setInternalState(taskingHomeActivity, "kujakuMapView", kujakuMapView);
        Whitebox.setInternalState(taskingHomeActivity, "selectedGeoJsonSource", geoJsonSource);
        Whitebox.setInternalState(taskingHomeActivity, "mMapboxMap", mMapboxMap);
        LatLng latLng = new LatLng();
        when(mMapboxMap.getCameraPosition()).thenReturn(new CameraPosition.Builder().zoom(18).build());
        taskingHomeActivity.displaySelectedFeature(feature, latLng);
        verify(kujakuMapView).centerMap(latLng, ANIMATE_TO_LOCATION_DURATION, 18);
        verify(geoJsonSource).setGeoJson(FeatureCollection.fromFeature(feature));
    }

    @Test
    public void testClearSelectedFeature() {
        Whitebox.setInternalState(taskingHomeActivity, "selectedGeoJsonSource", geoJsonSource);
        taskingHomeActivity.clearSelectedFeature();
        verify(geoJsonSource).setGeoJson("{\"type\":\"FeatureCollection\",\"features\":[]}");
    }


    @Test
    public void testOnActivityResultSavesForm() {
        Whitebox.setInternalState(taskingHomeActivity, "taskingHomePresenter", taskingHomePresenter);
        Intent intent = new Intent();
        intent.putExtra(JSON_FORM_PARAM_JSON, "{form_data}");
        taskingHomeActivity.onActivityResultExtended(REQUEST_CODE_GET_JSON, Activity.RESULT_OK, intent);
        verify(taskingHomePresenter).saveJsonForm("{form_data}");
    }

    @Test
    public void testOnActivityResultWaitForUserLocation() {
        Whitebox.setInternalState(taskingHomeActivity, "taskingHomePresenter", taskingHomePresenter);
        Whitebox.setInternalState(taskingHomeActivity, "hasRequestedLocation", true);
        when(taskingHomePresenter.getLocationPresenter()).thenReturn(locationPresenter);
        taskingHomeActivity.onActivityResultExtended(LOCATION_SETTINGS, Activity.RESULT_OK, null);
        verify(locationPresenter).waitForUserLocation();
        assertFalse(Whitebox.getInternalState(taskingHomeActivity, "hasRequestedLocation"));
    }

    @Test
    public void testOnActivityResultWaitForUserLocationCancelled() {
        Whitebox.setInternalState(taskingHomeActivity, "taskingHomePresenter", taskingHomePresenter);
        Whitebox.setInternalState(taskingHomeActivity, "hasRequestedLocation", true);
        when(taskingHomePresenter.getLocationPresenter()).thenReturn(locationPresenter);
        taskingHomeActivity.onActivityResultExtended(LOCATION_SETTINGS, Activity.RESULT_CANCELED, null);
        verify(locationPresenter).onGetUserLocationFailed();
        assertFalse(Whitebox.getInternalState(taskingHomeActivity, "hasRequestedLocation"));
    }


    @Test
    public void testOnActivityResultResetFeatures() {
        Whitebox.setInternalState(taskingHomeActivity, "taskingHomePresenter", taskingHomePresenter);
        Intent intent = new Intent();
        String id = UUID.randomUUID().toString();
        Task task = TestingUtils.getTask(id);
        intent.putExtra(STRUCTURE_ID, id);
        intent.putExtra(TASK_ID, task);
        taskingHomeActivity.onActivityResultExtended(REQUEST_CODE_FAMILY_PROFILE, Activity.RESULT_OK, intent);
        verify(taskingHomePresenter).resetFeatureTasks(id, task);
    }

    @Test
    public void testOnActivityResultFilterFeatures() {
        Whitebox.setInternalState(taskingHomeActivity, "taskingHomePresenter", taskingHomePresenter);
        TaskFilterParams params = new TaskFilterParams("Doe");
        Intent intent = new Intent();
        intent.putExtra(FILTER_SORT_PARAMS, params);
        taskingHomeActivity.onActivityResultExtended(REQUEST_CODE_FILTER_TASKS, Activity.RESULT_OK, intent);
        verify(taskingHomePresenter).filterTasks(params);
    }


    @Test
    public void testOnActivityResultInializeFilterParams() {
        Whitebox.setInternalState(taskingHomeActivity, "taskingHomePresenter", taskingHomePresenter);
        TaskFilterParams params = new TaskFilterParams("Doe");
        Intent intent = new Intent();
        intent.putExtra(FILTER_SORT_PARAMS, params);
        taskingHomeActivity.onActivityResultExtended(REQUEST_CODE_TASK_LISTS, Activity.RESULT_OK, intent);
        verify(taskingHomePresenter).setTaskFilterParams(params);
    }

    @Test
    public void testShowProgressDialog() {
        taskingHomeActivity.showProgressDialog(R.string.saving_title, R.string.saving_message);
        ProgressDialog progressDialog = (ProgressDialog) ShadowProgressDialog.getLatestDialog();
        assertNotNull(progressDialog);
        assertTrue(progressDialog.isShowing());
        assertEquals(context.getString(R.string.saving_title), ShadowApplication.getInstance().getLatestDialog().getTitle());
    }

    @Test
    public void testHideProgressDialog() {
        taskingHomeActivity.showProgressDialog(R.string.saving_title, R.string.saving_message);
        ProgressDialog progressDialog = (ProgressDialog) ShadowProgressDialog.getLatestDialog();
        taskingHomeActivity.hideProgressDialog();
        assertNotNull(progressDialog);
        assertFalse(progressDialog.isShowing());
    }


    @Test
    public void testGetUserCurrentLocation() {
        Whitebox.setInternalState(taskingHomeActivity, "kujakuMapView", kujakuMapView);
        Location location = taskingHomeActivity.getUserCurrentLocation();
        assertNull(location);

        Location expected = new Location("test");
        when(kujakuMapView.getLocationClient()).thenReturn(locationClient);
        when(locationClient.getLastLocation()).thenReturn(expected);
        assertEquals(expected, taskingHomeActivity.getUserCurrentLocation());

    }

    @Test
    public void testRequestUserLocation() {
        Whitebox.setInternalState(taskingHomeActivity, "kujakuMapView", kujakuMapView);
        taskingHomeActivity.requestUserLocation();
        verify(kujakuMapView).setWarmGps(true, getString(R.string.location_service_disabled), getString(R.string.location_services_disabled_spray));
        assertTrue(Whitebox.getInternalState(taskingHomeActivity, "hasRequestedLocation"));
    }


    @Test
    public void testOnDestroy() {
        taskingHomeActivity.onDestroy();
        assertNull(Whitebox.getInternalState(taskingHomeActivity, "taskingHomePresenter"));
    }

    @Test
    public void testDisplayToast() {
        taskingHomeActivity.displayToast(R.string.sync_complete);
        assertEquals(getString(R.string.sync_complete), ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void testOnSyncStart() {
        init(context);
        Whitebox.setInternalState(getInstance(), "isSyncing", true);
        taskingHomeActivity = spy(taskingHomeActivity);
        doNothing().when(taskingHomeActivity).toggleProgressBarView(true);
        taskingHomeActivity.onSyncStart();
        Snackbar snackbar = Whitebox.getInternalState(taskingHomeActivity, "syncProgressSnackbar");
        assertTrue(snackbar.isShown());
    }


    @Test
    public void testOnSyncInProgressFetchedDataSnackBarIsStillShown() {
        init(context);
        taskingHomeActivity.onSyncInProgress(FetchStatus.fetched);
        Snackbar snackbar = Whitebox.getInternalState(taskingHomeActivity, "syncProgressSnackbar");
        assertTrue(snackbar.isShown());
    }


    @Test
    public void testOnSyncInProgressFetchFailedSnackBarIsDismissed() {
        init(context);
        taskingHomeActivity.onSyncInProgress(FetchStatus.fetchedFailed);
        Snackbar snackbar = Whitebox.getInternalState(taskingHomeActivity, "syncProgressSnackbar");
        assertFalse(snackbar.isShown());


    }

    @Test
    public void testOnSyncInProgressNothingFetchedSnackBarIsDismissed() {
        init(context);
        taskingHomeActivity.onSyncInProgress(FetchStatus.nothingFetched);
        Snackbar snackbar = Whitebox.getInternalState(taskingHomeActivity, "syncProgressSnackbar");
        assertFalse(snackbar.isShown());
    }

    @Test
    public void testOnSyncInProgressNoConnectionSnackBarIsDismissed() {
        init(context);
        taskingHomeActivity.onSyncInProgress(FetchStatus.noConnection);
        Snackbar snackbar = Whitebox.getInternalState(taskingHomeActivity, "syncProgressSnackbar");
        assertFalse(snackbar.isShown());
    }

    @Test
    public void testOnSyncCompleteSnackBarIsDismissed() {
        init(context);
        taskingHomeActivity = spy(taskingHomeActivity);
        doNothing().when(taskingHomeActivity).toggleProgressBarView(false);
        taskingHomeActivity.onSyncComplete(FetchStatus.nothingFetched);
        Snackbar snackbar = Whitebox.getInternalState(taskingHomeActivity, "syncProgressSnackbar");
        assertFalse(snackbar.isShown());
    }

    @Test
    public void testOnResume() {
        init(context);
        Whitebox.setInternalState(taskingHomeActivity, "drawerView", drawerView);
        Whitebox.setInternalState(taskingHomeActivity, "taskingHomePresenter", taskingHomePresenter);
        taskingHomeActivity.onResume();
        verify(drawerView).onResume();
        verify(taskingHomePresenter).onResume();
    }

    @Test
    public void testOnPause() {
        init(context);
        Whitebox.setInternalState(taskingHomeActivity, "mapHelper", taskingMapHelper);
        getInstance().addSyncStatusListener(taskingHomeActivity);
        taskingHomeActivity.onPause();
        List<SyncStatusListener> syncStatusListeners = Whitebox.getInternalState(getInstance(), "syncStatusListeners");
        assertTrue(syncStatusListeners.isEmpty());
        assertFalse(taskingLibraryConfiguration.isMyLocationComponentEnabled());
    }

    @Test
    public void testOnDrawerClosed() {
        Whitebox.setInternalState(taskingHomeActivity, "taskingHomePresenter", taskingHomePresenter);
        taskingHomeActivity.onDrawerClosed();
        verify(taskingHomePresenter).onDrawerClosed();
    }

    @Test
    public void testFocusOnUserLocation() {
        Whitebox.setInternalState(taskingHomeActivity, "kujakuMapView", kujakuMapView);
        taskingHomeActivity.focusOnUserLocation(true);
        verify(kujakuMapView).focusOnUserLocation(true, RenderMode.COMPASS);
    }

    @Test
    public void testDisplayMarkStructureInactiveDialog() {
        Whitebox.setInternalState(taskingHomeActivity, "taskingHomePresenter", taskingHomePresenter);
        taskingHomeActivity.displayMarkStructureInactiveDialog();
        AlertDialog alertDialog = (AlertDialog) ShadowAlertDialog.getLatestDialog();
        assertTrue(alertDialog.isShowing());
        TextView tv = alertDialog.findViewById(android.R.id.message);
        assertEquals(getString(R.string.confirm_mark_location_inactive), tv.getText());

        alertDialog.getButton(BUTTON_POSITIVE).performClick();
        verify(taskingHomePresenter).onMarkStructureInactiveConfirmed();
        assertFalse(alertDialog.isShowing());

    }

    @Test
    public void testSetNumberOfFiltersToZero() {
        taskingHomeActivity.setNumberOfFilters(0);
        assertEquals(VISIBLE, taskingHomeActivity.findViewById(R.id.filter_tasks_fab).getVisibility());
        assertEquals(GONE, taskingHomeActivity.findViewById(R.id.filter_tasks_count_layout).getVisibility());
    }

    @Test
    public void testSetNumberOfFiltersToNoneZero() {
        taskingHomeActivity.setNumberOfFilters(3);
        assertEquals(GONE, taskingHomeActivity.findViewById(R.id.filter_tasks_fab).getVisibility());
        assertEquals(VISIBLE, taskingHomeActivity.findViewById(R.id.filter_tasks_count_layout).getVisibility());
        assertEquals("3", ((TextView) taskingHomeActivity.findViewById(R.id.filter_tasks_count)).getText());
    }

    @Test
    public void testSetSearchPhrase() {
        Whitebox.setInternalState(taskingHomeActivity, "taskingHomePresenter", taskingHomePresenter);
        String searchBy = "Jane Doe";
        taskingHomeActivity.setSearchPhrase(searchBy);
        assertEquals(searchBy, ((TextView) taskingHomeActivity.findViewById(R.id.edt_search)).getText().toString());
        verify(taskingHomePresenter).searchTasks(searchBy);
    }


//    @Test
//    public void testDisplayResetTaskInfoDialog() {
//        Whitebox.setInternalState(taskingHomeActivity, "taskingHomePresenter", taskingHomePresenter);
//        taskingHomeActivity.displayResetInterventionTaskDialog(BEDNET_DISTRIBUTION);
//
//        AlertDialog alertDialog = (AlertDialog) ShadowAlertDialog.getLatestDialog();
//        assertTrue(alertDialog.isShowing());
//
//        TextView tv = alertDialog.findViewById(android.R.id.message);
//        assertEquals(getString(R.string.undo_task_msg), tv.getText());
//
//        alertDialog.getButton(BUTTON_POSITIVE).performClick();
//        verify(taskingHomePresenter).onUndoInterventionStatus(eq(BEDNET_DISTRIBUTION));
//        assertFalse(alertDialog.isShowing());
//    }

    @Test
    public void testInitScaleBarPlugin() {
        Whitebox.setInternalState(taskingHomeActivity, "kujakuMapView", kujakuMapView);
        when(projection.getMetersPerPixelAtLatitude(12.06766)).thenReturn(1.0);
        LatLng target = new LatLng(12.06766, -18.02341);
        when(mMapboxMap.getCameraPosition()).thenReturn(new CameraPosition.Builder().zoom(18).target(target).build());
        when(mMapboxMap.getProjection()).thenReturn(projection);
        taskingHomeActivity.initializeScaleBarPlugin(mMapboxMap);
        verify(kujakuMapView).addView(scaleBarWidgetArgumentCaptor.capture());
        ScaleBarWidget actualScaleBarWidget = scaleBarWidgetArgumentCaptor.getValue();
        assertNotNull(actualScaleBarWidget);
        assertEquals(Color.WHITE, actualScaleBarWidget.getTextColor());
        assertEquals(14d, actualScaleBarWidget.getTextSize(), 0);

    }

//    @Test
//    public void testDisplayNotificationWithSingleParam() {
//        taskingHomeActivity.displayNotification(context.getString(R.string.confirm_archive_family));
//        AlertDialog alertDialog = (AlertDialog) ShadowAlertDialog.getLatestDialog();
//        assertTrue(alertDialog.isShowing());
//        TextView tv = alertDialog.findViewById(android.R.id.message);
//        assertEquals("Confirm Household Archival", tv.getText());
//    }

    @Test
    public void testOnSyncProgress() {
        ProgressBar progress = new ProgressBar(context);
        TextView progressLabel = new TextView(context);
        SyncProgress mockSyncProgress = mock(SyncProgress.class);
        SyncEntity mockSyncEntity = mock(SyncEntity.class);
        TaskingHomeActivity spyTaskingHomeActivity = spy(taskingHomeActivity);
        doReturn(50).when(mockSyncProgress).getPercentageSynced();
        doReturn(mockSyncEntity).when(mockSyncProgress).getSyncEntity();
        doReturn("Tasks").when(mockSyncEntity).toString();
        doReturn(progress).when(spyTaskingHomeActivity).findViewById(eq(R.id.sync_progress_bar));
        doReturn(progressLabel).when(spyTaskingHomeActivity).findViewById(eq(R.id.sync_progress_bar_label));

        spyTaskingHomeActivity.onSyncProgress(mockSyncProgress);

        assertEquals(progressLabel.getText(), String.format(context.getString(R.string.progressBarLabel), "Tasks", 50));
    }

    @Test
    public void testOnUserAssignmentRevokedShouldResumeDrawer() {
        Whitebox.setInternalState(taskingHomeActivity, "drawerView", drawerView);
        doNothing().when(drawerView).onResume();
        taskingHomeActivity.onUserAssignmentRevoked(mock(UserAssignmentDTO.class));
        verify(drawerView).onResume();
    }
}
