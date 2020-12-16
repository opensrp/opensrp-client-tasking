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
import org.smartregister.tasking.activity.TaskingMapActivity;
import org.smartregister.tasking.contract.BaseDrawerContract;
import org.smartregister.tasking.model.TaskFilterParams;
import org.smartregister.tasking.presenter.TaskingMapPresenter;
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
public class TaskingMapActivityTest extends BaseUnitTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private TaskingMapActivity taskingMapActivity;

    private android.content.Context context = RuntimeEnvironment.application;

    private ImageButton myLocationButton = new ImageButton(context);

    private ImageButton layerSwitcherFab = new ImageButton(context);

    @Mock
    private TaskingMapPresenter taskingMapPresenter;

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
        taskingMapActivity = Robolectric.buildActivity(TaskingMapActivity.class).create().get();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = 30;
        myLocationButton.setLayoutParams(params);
        Whitebox.setInternalState(taskingMapActivity, "myLocationButton", myLocationButton);
        Whitebox.setInternalState(taskingMapActivity, "cardDetailsUtil", cardDetailsUtil);

        taskingLibraryConfiguration = spy(TaskingLibrary.getInstance().getTaskingLibraryConfiguration());
        Whitebox.setInternalState(taskingMapActivity, "taskingLibraryConfiguration", taskingLibraryConfiguration);
        ReflectionHelpers.setStaticField(ValidateAssignmentReceiver.class, "instance", validateAssignmentReceiver);
    }


    @Test
    public void testOnCreate() {
        assertNotNull(Robolectric.buildActivity(TaskingMapActivity.class).create().get());
    }


//    @Test
//    public void testCloseSprayCardView() {
//        View sprayCardView = taskingMapActivity.findViewById(R.id.spray_card_view);
//        sprayCardView.setVisibility(VISIBLE);
//        taskingMapActivity.closeCardView(R.id.btn_collapse_spray_card_view);
//        assertEquals(GONE, sprayCardView.getVisibility());
//    }
//
//    @Test
//    public void testCloseMosquitoCardView() {
//        View cardView = taskingMapActivity.findViewById(R.id.mosquito_collection_card_view);
//        cardView.setVisibility(VISIBLE);
//        taskingMapActivity.closeCardView(R.id.btn_collapse_mosquito_collection_card_view);
//        assertEquals(GONE, cardView.getVisibility());
//    }
//
//
//    @Test
//    public void testCloseCardLarvalCardView() {
//        View cardView = taskingMapActivity.findViewById(R.id.larval_breeding_card_view);
//        cardView.setVisibility(VISIBLE);
//        taskingMapActivity.closeCardView(R.id.btn_collapse_larval_breeding_card_view);
//        assertEquals(GONE, cardView.getVisibility());
//    }
//
//    @Test
//    public void testClosePAOTCardView() {
//        View cardView = taskingMapActivity.findViewById(R.id.potential_area_of_transmission_card_view);
//        cardView.setVisibility(VISIBLE);
//        taskingMapActivity.closeCardView(R.id.btn_collapse_paot_card_view);
//        assertEquals(GONE, cardView.getVisibility());
//    }
//
//    @Test
//    public void testCloseIndicatorsCardView() {
//        View cardView = taskingMapActivity.findViewById(R.id.indicators_card_view);
//        cardView.setVisibility(VISIBLE);
//        taskingMapActivity.closeCardView(R.id.btn_collapse_indicators_card_view);
//        assertEquals(GONE, cardView.getVisibility());
//    }
//
//    @Test
//    public void testCloseVerificationCardView() {
//        View cardView = taskingMapActivity.findViewById(R.id.irs_verification_card_view);
//        cardView.setVisibility(VISIBLE);
//        taskingMapActivity.closeCardView(R.id.btn_collapse_irs_verification_card_view);
//        assertEquals(GONE, cardView.getVisibility());
//    }
//
//
//    @Test
//    public void testCloseAllCardViews() {
//        taskingMapActivity.findViewById(R.id.spray_card_view).setVisibility(VISIBLE);
//        taskingMapActivity.findViewById(R.id.mosquito_collection_card_view).setVisibility(VISIBLE);
//        taskingMapActivity.findViewById(R.id.larval_breeding_card_view).setVisibility(VISIBLE);
//        taskingMapActivity.findViewById(R.id.potential_area_of_transmission_card_view).setVisibility(VISIBLE);
//        taskingMapActivity.findViewById(R.id.indicators_card_view).setVisibility(VISIBLE);
//        taskingMapActivity.findViewById(R.id.irs_verification_card_view).setVisibility(VISIBLE);
//        taskingMapActivity.closeAllCardViews();
//        assertEquals(GONE, taskingMapActivity.findViewById(R.id.spray_card_view).getVisibility());
//        assertEquals(GONE, taskingMapActivity.findViewById(R.id.mosquito_collection_card_view).getVisibility());
//        assertEquals(GONE, taskingMapActivity.findViewById(R.id.larval_breeding_card_view).getVisibility());
//        assertEquals(GONE, taskingMapActivity.findViewById(R.id.potential_area_of_transmission_card_view).getVisibility());
//        assertEquals(GONE, taskingMapActivity.findViewById(R.id.indicators_card_view).getVisibility());
//        assertEquals(GONE, taskingMapActivity.findViewById(R.id.irs_verification_card_view).getVisibility());
//    }

    @Test
    public void testPositionMyLocation() {
        taskingMapActivity = spy(taskingMapActivity);
        taskingMapActivity.positionMyLocationAndLayerSwitcher();
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) myLocationButton.getLayoutParams();
        assertEquals(0, layoutParams.topMargin);
        assertEquals(30, layoutParams.bottomMargin);
        assertEquals(Gravity.BOTTOM | Gravity.END, layoutParams.gravity);
    }

    //
//    @Test
//    public void testPositionMyLocationZambia() {
//        taskingMapActivity = spy(taskingMapActivity);
//        when(taskingMapActivity.getBuildCountry()).thenReturn(Country.ZAMBIA);
//        Whitebox.setInternalState(taskingMapActivity, "myLocationButton", myLocationButton);
//        taskingMapActivity.positionMyLocationAndLayerSwitcher();
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
        taskingMapActivity.findViewById(R.id.filter_tasks_fab).performClick();
        verify(taskingLibraryConfiguration).openFilterTaskActivity(isNull(), any(TaskingMapActivity.class));
    }


    @Test
    public void testOpenTaskRegister() {
        taskingMapActivity.findViewById(R.id.task_register).performClick();
        verify(taskingLibraryConfiguration).openTaskRegister(isNull(), any(TaskingMapActivity.class));
    }


    @Test
    public void testOnAddStructure() {
        Whitebox.setInternalState(taskingMapActivity, "taskingMapPresenter", taskingMapPresenter);
        Whitebox.setInternalState(taskingMapActivity, "mapHelper", taskingMapHelper);
        taskingMapActivity.findViewById(R.id.btn_add_structure).performClick();
        verify(taskingMapPresenter).onAddStructureClicked(false);
        verify(taskingMapHelper).isMyLocationComponentActive(any(), any());
    }

//    @Test
//    public void testOnChangeSprayStatus() {
//        Whitebox.setInternalState(taskingMapActivity, "taskingMapPresenter", taskingMapPresenter);
//        taskingMapActivity.findViewById(R.id.change_spray_status).performClick();
//        verify(taskingMapPresenter).onChangeInterventionStatus(Intervention.IRS);
//    }
//
//
//    @Test
//    public void testRecordMosquitoCollection() {
//        Whitebox.setInternalState(taskingMapActivity, "taskingMapPresenter", taskingMapPresenter);
//        taskingMapActivity.findViewById(R.id.btn_record_mosquito_collection).performClick();
//        verify(taskingMapPresenter).onChangeInterventionStatus(Intervention.MOSQUITO_COLLECTION);
//    }
//
//
//    @Test
//    public void testRecordLarvalDipping() {
//        Whitebox.setInternalState(taskingMapActivity, "taskingMapPresenter", taskingMapPresenter);
//        taskingMapActivity.findViewById(R.id.btn_record_larval_dipping).performClick();
//        verify(taskingMapPresenter).onChangeInterventionStatus(Intervention.LARVAL_DIPPING);
//    }
//
//    @Test
//    public void testEditPAOTDetails() {
//        Whitebox.setInternalState(taskingMapActivity, "taskingMapPresenter", taskingMapPresenter);
//        taskingMapActivity.findViewById(R.id.btn_edit_paot_details).performClick();
//        verify(taskingMapPresenter).onChangeInterventionStatus(Intervention.PAOT);
//    }
//
//    @Test
//    public void testCloseCardView() {
//
//        taskingMapActivity.findViewById(R.id.btn_collapse_spray_card_view).performClick();
//        assertEquals(GONE, taskingMapActivity.findViewById(R.id.spray_card_view).getVisibility());
//    }
//
//    @Test
//    public void testOtherCardView() {
//        taskingMapActivity.findViewById(R.id.btn_collapse_mosquito_collection_card_view).performClick();
//        assertEquals(GONE, taskingMapActivity.findViewById(R.id.mosquito_collection_card_view).getVisibility());
//    }

    @Test
    public void testOpenDrawerMenu() {
        Whitebox.setInternalState(taskingMapActivity, "drawerView", drawerView);
        taskingMapActivity.findViewById(R.id.drawerMenu).performClick();
        verify(drawerView).openDrawerLayout();
    }

    @Test
    public void testOpenIndicators() {
        assertEquals(GONE, taskingMapActivity.findViewById(R.id.indicators_card_view).getVisibility());
        taskingMapActivity.findViewById(R.id.progressIndicatorsGroupView).performClick();
        assertEquals(VISIBLE, taskingMapActivity.findViewById(R.id.indicators_card_view).getVisibility());
    }

//    @Test
//    public void testRegisterFamily() {
//        Whitebox.setInternalState(taskingMapActivity, "taskingMapPresenter", taskingMapPresenter);
//        when(taskingMapPresenter.getSelectedFeature()).thenReturn(feature);
//        taskingMapActivity.findViewById(R.id.register_family).performClick();
//        Intent startedIntent = shadowOf(taskingMapActivity).getNextStartedActivity();
//        assertEquals(FamilyRegisterActivity.class, shadowOf(startedIntent).getIntentClass());
//        assertEquals(GONE, taskingMapActivity.findViewById(R.id.spray_card_view).getVisibility());
//        assertTrue(startedIntent.hasExtra(TASK_IDENTIFIER));
//    }
//
//
//    @Test
//    public void testOpenStructureProfile() {
//        Whitebox.setInternalState(taskingMapActivity, "taskingMapPresenter", taskingMapPresenter);
//        when(taskingMapPresenter.getSelectedFeature()).thenReturn(feature);
//        CommonPersonObjectClient client = TestingUtils.getCommonPersonObjectClient();
//        taskingMapActivity.openStructureProfile(client);
//        Intent startedIntent = shadowOf(taskingMapActivity).getNextStartedActivity();
//        assertEquals(FamilyProfileActivity.class, shadowOf(startedIntent).getIntentClass());
//        assertEquals(client.getColumnmaps().get(FIRST_NAME), startedIntent.getStringExtra(INTENT_KEY.FAMILY_NAME));
//        assertEquals(client.getCaseId(), startedIntent.getStringExtra(INTENT_KEY.FAMILY_BASE_ENTITY_ID));
//        assertTrue(startedIntent.hasExtra(TASK_IDENTIFIER));
//
//    }


    @Test
    public void testSetGeoJsonSourceWithNullOperationalArea() {
        Whitebox.setInternalState(taskingMapActivity, "geoJsonSource", geoJsonSource);
        FeatureCollection featureCollection = FeatureCollection.fromFeature(feature);
        taskingMapActivity.setGeoJsonSource(featureCollection, null, true);
        verify(geoJsonSource).setGeoJson(featureCollection);
        verifyZeroInteractions(mMapboxMap);
    }


    @Test
    public void testSetGeoJsonSource() {
        Whitebox.setInternalState(taskingMapActivity, "taskingMapPresenter", taskingMapPresenter);
        Whitebox.setInternalState(taskingMapActivity, "mMapboxMap", mMapboxMap);
        Whitebox.setInternalState(taskingMapActivity, "geoJsonSource", geoJsonSource);
        Whitebox.setInternalState(taskingMapActivity, "kujakuMapView", kujakuMapView);
        Whitebox.setInternalState(taskingMapActivity, "mapHelper", taskingMapHelper);
        Feature operationalArea = mock(Feature.class);
        doReturn(mock(Geometry.class)).when(operationalArea).geometry();

        CameraPosition cameraPosition = new CameraPosition.Builder().build();
        when(mMapboxMap.getCameraForGeometry(operationalArea.geometry())).thenReturn(cameraPosition);
        when(taskingMapPresenter.getInterventionLabel()).thenReturn(R.string.focus_investigation);
        FeatureCollection featureCollection = FeatureCollection.fromFeature(feature);
        taskingMapActivity.setGeoJsonSource(featureCollection, operationalArea, true);
        verify(geoJsonSource).setGeoJson(featureCollection);
        verify(mMapboxMap).setCameraPosition(cameraPosition);
        verify(kujakuMapView).addLayer(boundaryLayerArgumentCaptor.capture());
        verify(taskingMapHelper).addIndexCaseLayers(mMapboxMap, taskingMapActivity, featureCollection);
        assertEquals(FeatureCollection.fromFeature(operationalArea), boundaryLayerArgumentCaptor.getValue().getFeatureCollection());
        assertEquals(2, boundaryLayerArgumentCaptor.getValue().getLayerIds().length);
    }


    @Test
    public void testSetGeoJsonSourceSetsCameraOnIndexCase() {
        Whitebox.setInternalState(taskingMapActivity, "taskingMapPresenter", taskingMapPresenter);
        Whitebox.setInternalState(taskingMapActivity, "mMapboxMap", mMapboxMap);
        Whitebox.setInternalState(taskingMapActivity, "geoJsonSource", geoJsonSource);
        Whitebox.setInternalState(taskingMapActivity, "kujakuMapView", kujakuMapView);
        Whitebox.setInternalState(taskingMapActivity, "mapHelper", taskingMapHelper);
        Feature indexCase = Feature.fromJson("{\"type\":\"Feature\",\"id\":\"000c99d7\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[28.821878,-10.203831]}}");
        FeatureCollection featureCollection = FeatureCollection.fromFeature(feature);
        CameraPosition cameraPosition = new CameraPosition.Builder().zoom(18.5).build();
        when(mMapboxMap.getCameraPosition()).thenReturn(cameraPosition);
        when(taskingMapHelper.getIndexCase(featureCollection)).thenReturn(indexCase);
        when(taskingMapPresenter.getInterventionLabel()).thenReturn(R.string.focus_investigation);
        taskingMapActivity.setGeoJsonSource(featureCollection, mock(Feature.class), true);
        verify(geoJsonSource).setGeoJson(featureCollection);
        verify(mMapboxMap).setCameraPosition(cameraPositionArgumentCaptor.capture());
        verify(kujakuMapView).addLayer(boundaryLayerArgumentCaptor.capture());
        verify(taskingMapHelper).addIndexCaseLayers(mMapboxMap, taskingMapActivity, featureCollection);
        assertEquals(28.821878, cameraPositionArgumentCaptor.getValue().target.getLongitude(), 0);
        assertEquals(-10.203831, cameraPositionArgumentCaptor.getValue().target.getLatitude(), 0);
    }

    @Test
    public void testSetGeoJsonSourceUpdatesBoundaryLayerAndIndexCaseLayers() throws JSONException {
        Whitebox.setInternalState(taskingMapActivity, "taskingMapPresenter", taskingMapPresenter);
        Whitebox.setInternalState(taskingMapActivity, "mMapboxMap", mMapboxMap);
        Whitebox.setInternalState(taskingMapActivity, "geoJsonSource", geoJsonSource);
        Whitebox.setInternalState(taskingMapActivity, "kujakuMapView", kujakuMapView);
        Whitebox.setInternalState(taskingMapActivity, "mapHelper", taskingMapHelper);
        BoundaryLayer boundaryLayer = mock(BoundaryLayer.class);
        Whitebox.setInternalState(taskingMapActivity, "boundaryLayer", boundaryLayer);
        Feature operationalArea = mock(Feature.class);
        when(taskingMapPresenter.getInterventionLabel()).thenReturn(R.string.focus_investigation);
        FeatureCollection featureCollection = FeatureCollection.fromFeature(feature);
        when(taskingMapHelper.getIndexCaseLineLayer()).thenReturn(mock(LineLayer.class));
        taskingMapActivity.setGeoJsonSource(featureCollection, operationalArea, true);
        verify(geoJsonSource).setGeoJson(featureCollection);
        verify(boundaryLayer).updateFeatures(FeatureCollection.fromFeature(operationalArea));
        verify(taskingMapHelper).updateIndexCaseLayers(mMapboxMap, featureCollection, taskingMapActivity);

    }


    @Test
    public void testDisplayNotificationWithArgs() {
        taskingMapActivity.displayNotification(R.string.archive_family, R.string.archive_family_failed, "Test");
        AlertDialog alertDialog = (AlertDialog) ShadowAlertDialog.getLatestDialog();
        assertTrue(alertDialog.isShowing());
        TextView tv = alertDialog.findViewById(android.R.id.message);
        assertEquals("Archiving family Test failed", tv.getText());

    }

    @Test
    public void testDisplayNotification() {
        taskingMapActivity.displayNotification(R.string.archive_family, R.string.confirm_archive_family);
        AlertDialog alertDialog = (AlertDialog) ShadowAlertDialog.getLatestDialog();
        assertTrue(alertDialog.isShowing());
        TextView tv = alertDialog.findViewById(android.R.id.message);
        assertEquals("Confirm Household Archival", tv.getText());

    }

//    @Test
//    public void testOpenSprayCardView() {
//        assertEquals(GONE, taskingMapActivity.findViewById(R.id.spray_card_view).getVisibility());
//        Whitebox.setInternalState(taskingMapActivity, "cardDetailsUtil", cardDetailsUtil);
//        SprayCardDetails cardDetails = mock(SprayCardDetails.class);
//        taskingMapActivity.openCardView(cardDetails);
//        verify(cardDetailsUtil).populateSprayCardTextViews(cardDetails, taskingMapActivity);
//        assertEquals(VISIBLE, taskingMapActivity.findViewById(R.id.spray_card_view).getVisibility());
//    }
//
//    @Test
//    public void testOpenMosquitoCardView() {
//        MosquitoHarvestCardDetails mosquitoHarvestCardDetails = mock(MosquitoHarvestCardDetails.class);
//        taskingMapActivity.openCardView(mosquitoHarvestCardDetails);
//        verify(cardDetailsUtil).populateAndOpenMosquitoHarvestCard(mosquitoHarvestCardDetails, taskingMapActivity);
//    }
//
//    @Test
//    public void testOpenIRSVerificationCardView() {
//        IRSVerificationCardDetails irsVerificationCardDetails = mock(IRSVerificationCardDetails.class);
//        taskingMapActivity.openCardView(irsVerificationCardDetails);
//        verify(cardDetailsUtil).populateAndOpenIRSVerificationCard(irsVerificationCardDetails, taskingMapActivity);
//    }

//    @Test
//    public void testOpenFamilyCardView() {
//        assertEquals(GONE, taskingMapActivity.findViewById(R.id.spray_card_view).getVisibility());
//        FamilyCardDetails familyCardDetails = mock(FamilyCardDetails.class);
//        taskingMapActivity.openCardView(familyCardDetails);
//        verify(cardDetailsUtil).populateFamilyCard(familyCardDetails, taskingMapActivity);
//        assertEquals(VISIBLE, taskingMapActivity.findViewById(R.id.spray_card_view).getVisibility());
//    }

    @Test
    public void testStartJsonForm() {
        TaskingJsonFormUtils jsonFormUtils = mock(TaskingJsonFormUtils.class);
        Whitebox.setInternalState(taskingMapActivity, "jsonFormUtils", jsonFormUtils);
        JSONObject form = new JSONObject();
        taskingMapActivity.startJsonForm(form);
        verify(jsonFormUtils).startJsonForm(form, taskingMapActivity);
    }

    @Test
    public void testDisplaySelectedFeature() {
        Whitebox.setInternalState(taskingMapActivity, "kujakuMapView", kujakuMapView);
        Whitebox.setInternalState(taskingMapActivity, "selectedGeoJsonSource", geoJsonSource);
        Whitebox.setInternalState(taskingMapActivity, "mMapboxMap", mMapboxMap);
        LatLng latLng = new LatLng();
        when(mMapboxMap.getCameraPosition()).thenReturn(new CameraPosition.Builder().zoom(18).build());
        taskingMapActivity.displaySelectedFeature(feature, latLng);
        verify(kujakuMapView).centerMap(latLng, ANIMATE_TO_LOCATION_DURATION, 18);
        verify(geoJsonSource).setGeoJson(FeatureCollection.fromFeature(feature));
    }

    @Test
    public void testClearSelectedFeature() {
        Whitebox.setInternalState(taskingMapActivity, "selectedGeoJsonSource", geoJsonSource);
        taskingMapActivity.clearSelectedFeature();
        verify(geoJsonSource).setGeoJson("{\"type\":\"FeatureCollection\",\"features\":[]}");
    }


    @Test
    public void testOnActivityResultSavesForm() {
        Whitebox.setInternalState(taskingMapActivity, "taskingMapPresenter", taskingMapPresenter);
        Intent intent = new Intent();
        intent.putExtra(JSON_FORM_PARAM_JSON, "{form_data}");
        taskingMapActivity.onActivityResultExtended(REQUEST_CODE_GET_JSON, Activity.RESULT_OK, intent);
        verify(taskingMapPresenter).saveJsonForm("{form_data}");
    }

    @Test
    public void testOnActivityResultWaitForUserLocation() {
        Whitebox.setInternalState(taskingMapActivity, "taskingMapPresenter", taskingMapPresenter);
        Whitebox.setInternalState(taskingMapActivity, "hasRequestedLocation", true);
        when(taskingMapPresenter.getLocationPresenter()).thenReturn(locationPresenter);
        taskingMapActivity.onActivityResultExtended(LOCATION_SETTINGS, Activity.RESULT_OK, null);
        verify(locationPresenter).waitForUserLocation();
        assertFalse(Whitebox.getInternalState(taskingMapActivity, "hasRequestedLocation"));
    }

    @Test
    public void testOnActivityResultWaitForUserLocationCancelled() {
        Whitebox.setInternalState(taskingMapActivity, "taskingMapPresenter", taskingMapPresenter);
        Whitebox.setInternalState(taskingMapActivity, "hasRequestedLocation", true);
        when(taskingMapPresenter.getLocationPresenter()).thenReturn(locationPresenter);
        taskingMapActivity.onActivityResultExtended(LOCATION_SETTINGS, Activity.RESULT_CANCELED, null);
        verify(locationPresenter).onGetUserLocationFailed();
        assertFalse(Whitebox.getInternalState(taskingMapActivity, "hasRequestedLocation"));
    }


    @Test
    public void testOnActivityResultResetFeatures() {
        Whitebox.setInternalState(taskingMapActivity, "taskingMapPresenter", taskingMapPresenter);
        Intent intent = new Intent();
        String id = UUID.randomUUID().toString();
        Task task = TestingUtils.getTask(id);
        intent.putExtra(STRUCTURE_ID, id);
        intent.putExtra(TASK_ID, task);
        taskingMapActivity.onActivityResultExtended(REQUEST_CODE_FAMILY_PROFILE, Activity.RESULT_OK, intent);
        verify(taskingMapPresenter).resetFeatureTasks(id, task);
    }

    @Test
    public void testOnActivityResultFilterFeatures() {
        Whitebox.setInternalState(taskingMapActivity, "taskingMapPresenter", taskingMapPresenter);
        TaskFilterParams params = new TaskFilterParams("Doe");
        Intent intent = new Intent();
        intent.putExtra(FILTER_SORT_PARAMS, params);
        taskingMapActivity.onActivityResultExtended(REQUEST_CODE_FILTER_TASKS, Activity.RESULT_OK, intent);
        verify(taskingMapPresenter).filterTasks(params);
    }


    @Test
    public void testOnActivityResultInializeFilterParams() {
        Whitebox.setInternalState(taskingMapActivity, "taskingMapPresenter", taskingMapPresenter);
        TaskFilterParams params = new TaskFilterParams("Doe");
        Intent intent = new Intent();
        intent.putExtra(FILTER_SORT_PARAMS, params);
        taskingMapActivity.onActivityResultExtended(REQUEST_CODE_TASK_LISTS, Activity.RESULT_OK, intent);
        verify(taskingMapPresenter).setTaskFilterParams(params);
    }

    @Test
    public void testShowProgressDialog() {
        taskingMapActivity.showProgressDialog(R.string.saving_title, R.string.saving_message);
        ProgressDialog progressDialog = (ProgressDialog) ShadowProgressDialog.getLatestDialog();
        assertNotNull(progressDialog);
        assertTrue(progressDialog.isShowing());
        assertEquals(context.getString(R.string.saving_title), ShadowApplication.getInstance().getLatestDialog().getTitle());
    }

    @Test
    public void testHideProgressDialog() {
        taskingMapActivity.showProgressDialog(R.string.saving_title, R.string.saving_message);
        ProgressDialog progressDialog = (ProgressDialog) ShadowProgressDialog.getLatestDialog();
        taskingMapActivity.hideProgressDialog();
        assertNotNull(progressDialog);
        assertFalse(progressDialog.isShowing());
    }


    @Test
    public void testGetUserCurrentLocation() {
        Whitebox.setInternalState(taskingMapActivity, "kujakuMapView", kujakuMapView);
        Location location = taskingMapActivity.getUserCurrentLocation();
        assertNull(location);

        Location expected = new Location("test");
        when(kujakuMapView.getLocationClient()).thenReturn(locationClient);
        when(locationClient.getLastLocation()).thenReturn(expected);
        assertEquals(expected, taskingMapActivity.getUserCurrentLocation());

    }

    @Test
    public void testRequestUserLocation() {
        Whitebox.setInternalState(taskingMapActivity, "kujakuMapView", kujakuMapView);
        taskingMapActivity.requestUserLocation();
        verify(kujakuMapView).setWarmGps(true, getString(R.string.location_service_disabled), getString(R.string.location_services_disabled_spray));
        assertTrue(Whitebox.getInternalState(taskingMapActivity, "hasRequestedLocation"));
    }


    @Test
    public void testOnDestroy() {
        taskingMapActivity.onDestroy();
        assertNull(Whitebox.getInternalState(taskingMapActivity, "taskingMapPresenter"));
    }

    @Test
    public void testDisplayToast() {
        taskingMapActivity.displayToast(R.string.sync_complete);
        assertEquals(getString(R.string.sync_complete), ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void testOnSyncStart() {
        init(context);
        Whitebox.setInternalState(getInstance(), "isSyncing", true);
        taskingMapActivity = spy(taskingMapActivity);
        doNothing().when(taskingMapActivity).toggleProgressBarView(true);
        taskingMapActivity.onSyncStart();
        Snackbar snackbar = Whitebox.getInternalState(taskingMapActivity, "syncProgressSnackbar");
        assertTrue(snackbar.isShown());
    }


    @Test
    public void testOnSyncInProgressFetchedDataSnackBarIsStillShown() {
        init(context);
        taskingMapActivity.onSyncInProgress(FetchStatus.fetched);
        Snackbar snackbar = Whitebox.getInternalState(taskingMapActivity, "syncProgressSnackbar");
        assertTrue(snackbar.isShown());
    }


    @Test
    public void testOnSyncInProgressFetchFailedSnackBarIsDismissed() {
        init(context);
        taskingMapActivity.onSyncInProgress(FetchStatus.fetchedFailed);
        Snackbar snackbar = Whitebox.getInternalState(taskingMapActivity, "syncProgressSnackbar");
        assertFalse(snackbar.isShown());


    }

    @Test
    public void testOnSyncInProgressNothingFetchedSnackBarIsDismissed() {
        init(context);
        taskingMapActivity.onSyncInProgress(FetchStatus.nothingFetched);
        Snackbar snackbar = Whitebox.getInternalState(taskingMapActivity, "syncProgressSnackbar");
        assertFalse(snackbar.isShown());
    }

    @Test
    public void testOnSyncInProgressNoConnectionSnackBarIsDismissed() {
        init(context);
        taskingMapActivity.onSyncInProgress(FetchStatus.noConnection);
        Snackbar snackbar = Whitebox.getInternalState(taskingMapActivity, "syncProgressSnackbar");
        assertFalse(snackbar.isShown());
    }

    @Test
    public void testOnSyncCompleteSnackBarIsDismissed() {
        init(context);
        taskingMapActivity = spy(taskingMapActivity);
        doNothing().when(taskingMapActivity).toggleProgressBarView(false);
        taskingMapActivity.onSyncComplete(FetchStatus.nothingFetched);
        Snackbar snackbar = Whitebox.getInternalState(taskingMapActivity, "syncProgressSnackbar");
        assertFalse(snackbar.isShown());
    }

    @Test
    public void testOnResume() {
        init(context);
        Whitebox.setInternalState(taskingMapActivity, "drawerView", drawerView);
        Whitebox.setInternalState(taskingMapActivity, "taskingMapPresenter", taskingMapPresenter);
        taskingMapActivity.onResume();
        verify(drawerView).onResume();
        verify(taskingMapPresenter).onResume();
    }

    @Test
    public void testOnPause() {
        init(context);
        Whitebox.setInternalState(taskingMapActivity, "mapHelper", taskingMapHelper);
        getInstance().addSyncStatusListener(taskingMapActivity);
        taskingMapActivity.onPause();
        List<SyncStatusListener> syncStatusListeners = Whitebox.getInternalState(getInstance(), "syncStatusListeners");
        assertTrue(syncStatusListeners.isEmpty());
        assertFalse(taskingLibraryConfiguration.isMyLocationComponentEnabled());
    }

    @Test
    public void testOnDrawerClosed() {
        Whitebox.setInternalState(taskingMapActivity, "taskingMapPresenter", taskingMapPresenter);
        taskingMapActivity.onDrawerClosed();
        verify(taskingMapPresenter).onDrawerClosed();
    }

    @Test
    public void testFocusOnUserLocation() {
        Whitebox.setInternalState(taskingMapActivity, "kujakuMapView", kujakuMapView);
        taskingMapActivity.focusOnUserLocation(true);
        verify(kujakuMapView).focusOnUserLocation(true, RenderMode.COMPASS);
    }

    @Test
    public void testDisplayMarkStructureInactiveDialog() {
        Whitebox.setInternalState(taskingMapActivity, "taskingMapPresenter", taskingMapPresenter);
        taskingMapActivity.displayMarkStructureInactiveDialog();
        AlertDialog alertDialog = (AlertDialog) ShadowAlertDialog.getLatestDialog();
        assertTrue(alertDialog.isShowing());
        TextView tv = alertDialog.findViewById(android.R.id.message);
        assertEquals(getString(R.string.confirm_mark_location_inactive), tv.getText());

        alertDialog.getButton(BUTTON_POSITIVE).performClick();
        verify(taskingMapPresenter).onMarkStructureInactiveConfirmed();
        assertFalse(alertDialog.isShowing());

    }

    @Test
    public void testSetNumberOfFiltersToZero() {
        taskingMapActivity.setNumberOfFilters(0);
        assertEquals(VISIBLE, taskingMapActivity.findViewById(R.id.filter_tasks_fab).getVisibility());
        assertEquals(GONE, taskingMapActivity.findViewById(R.id.filter_tasks_count_layout).getVisibility());
    }

    @Test
    public void testSetNumberOfFiltersToNoneZero() {
        taskingMapActivity.setNumberOfFilters(3);
        assertEquals(GONE, taskingMapActivity.findViewById(R.id.filter_tasks_fab).getVisibility());
        assertEquals(VISIBLE, taskingMapActivity.findViewById(R.id.filter_tasks_count_layout).getVisibility());
        assertEquals("3", ((TextView) taskingMapActivity.findViewById(R.id.filter_tasks_count)).getText());
    }

    @Test
    public void testSetSearchPhrase() {
        Whitebox.setInternalState(taskingMapActivity, "taskingMapPresenter", taskingMapPresenter);
        String searchBy = "Jane Doe";
        taskingMapActivity.setSearchPhrase(searchBy);
        assertEquals(searchBy, ((TextView) taskingMapActivity.findViewById(R.id.edt_search)).getText().toString());
        verify(taskingMapPresenter).searchTasks(searchBy);
    }


//    @Test
//    public void testDisplayResetTaskInfoDialog() {
//        Whitebox.setInternalState(taskingMapActivity, "taskingMapPresenter", taskingMapPresenter);
//        taskingMapActivity.displayResetInterventionTaskDialog(BEDNET_DISTRIBUTION);
//
//        AlertDialog alertDialog = (AlertDialog) ShadowAlertDialog.getLatestDialog();
//        assertTrue(alertDialog.isShowing());
//
//        TextView tv = alertDialog.findViewById(android.R.id.message);
//        assertEquals(getString(R.string.undo_task_msg), tv.getText());
//
//        alertDialog.getButton(BUTTON_POSITIVE).performClick();
//        verify(taskingMapPresenter).onUndoInterventionStatus(eq(BEDNET_DISTRIBUTION));
//        assertFalse(alertDialog.isShowing());
//    }

    @Test
    public void testInitScaleBarPlugin() {
        Whitebox.setInternalState(taskingMapActivity, "kujakuMapView", kujakuMapView);
        when(projection.getMetersPerPixelAtLatitude(12.06766)).thenReturn(1.0);
        LatLng target = new LatLng(12.06766, -18.02341);
        when(mMapboxMap.getCameraPosition()).thenReturn(new CameraPosition.Builder().zoom(18).target(target).build());
        when(mMapboxMap.getProjection()).thenReturn(projection);
        taskingMapActivity.initializeScaleBarPlugin(mMapboxMap);
        verify(kujakuMapView).addView(scaleBarWidgetArgumentCaptor.capture());
        ScaleBarWidget actualScaleBarWidget = scaleBarWidgetArgumentCaptor.getValue();
        assertNotNull(actualScaleBarWidget);
        assertEquals(Color.WHITE, actualScaleBarWidget.getTextColor());
        assertEquals(14d, actualScaleBarWidget.getTextSize(), 0);

    }

//    @Test
//    public void testDisplayNotificationWithSingleParam() {
//        taskingMapActivity.displayNotification(context.getString(R.string.confirm_archive_family));
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
        TaskingMapActivity spyTaskingMapActivity = spy(taskingMapActivity);
        doReturn(50).when(mockSyncProgress).getPercentageSynced();
        doReturn(mockSyncEntity).when(mockSyncProgress).getSyncEntity();
        doReturn("Tasks").when(mockSyncEntity).toString();
        doReturn(progress).when(spyTaskingMapActivity).findViewById(eq(R.id.sync_progress_bar));
        doReturn(progressLabel).when(spyTaskingMapActivity).findViewById(eq(R.id.sync_progress_bar_label));

        spyTaskingMapActivity.onSyncProgress(mockSyncProgress);

        assertEquals(progressLabel.getText(), String.format(context.getString(R.string.progressBarLabel), "Tasks", 50));
    }

    @Test
    public void testOnUserAssignmentRevokedShouldResumeDrawer() {
        Whitebox.setInternalState(taskingMapActivity, "drawerView", drawerView);
        doNothing().when(drawerView).onResume();
        taskingMapActivity.onUserAssignmentRevoked(mock(UserAssignmentDTO.class));
        verify(drawerView).onResume();
    }
}
