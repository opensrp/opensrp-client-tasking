package org.smartregister.tasking.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.snackbar.Snackbar;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.UiSettings;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.pluginscalebar.ScaleBarOptions;
import com.mapbox.pluginscalebar.ScaleBarPlugin;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.SyncEntity;
import org.smartregister.domain.SyncProgress;
import org.smartregister.dto.UserAssignmentDTO;
import org.smartregister.receiver.SyncProgressBroadcastReceiver;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.receiver.ValidateAssignmentReceiver;
import org.smartregister.tasking.R;
import org.smartregister.tasking.TaskingLibrary;
import org.smartregister.tasking.contract.BaseDrawerContract;
import org.smartregister.tasking.contract.TaskingHomeActivityContract;
import org.smartregister.tasking.contract.UserLocationContract;
import org.smartregister.tasking.model.CardDetails;
import org.smartregister.tasking.model.TaskFilterParams;
import org.smartregister.tasking.presenter.TaskingHomePresenter;
import org.smartregister.tasking.repository.TaskingMappingHelper;
import org.smartregister.tasking.util.AlertDialogUtils;
import org.smartregister.tasking.util.CardDetailsUtil;
import org.smartregister.tasking.util.TaskingConstants;
import org.smartregister.tasking.util.TaskingJsonFormUtils;
import org.smartregister.tasking.util.TaskingLibraryConfiguration;
import org.smartregister.tasking.util.TaskingMapHelper;
import org.smartregister.tasking.util.Utils;

import io.ona.kujaku.callbacks.OnLocationComponentInitializedCallback;
import io.ona.kujaku.layers.BoundaryLayer;
import timber.log.Timber;

import static android.content.DialogInterface.BUTTON_POSITIVE;
import static org.smartregister.tasking.util.TaskingConstants.ANIMATE_TO_LOCATION_DURATION;
import static org.smartregister.tasking.util.TaskingConstants.CONFIGURATION.LOCAL_SYNC_DONE;
import static org.smartregister.tasking.util.TaskingConstants.CONFIGURATION.UPDATE_LOCATION_BUFFER_RADIUS;
import static org.smartregister.tasking.util.TaskingConstants.Filter.FILTER_SORT_PARAMS;
import static org.smartregister.tasking.util.TaskingConstants.RequestCode.REQUEST_CODE_TASK_LISTS;
import static org.smartregister.tasking.util.TaskingConstants.VERTICAL_OFFSET;
import static org.smartregister.tasking.util.Utils.displayDistanceScale;
import static org.smartregister.tasking.util.Utils.getLocationBuffer;
import static org.smartregister.tasking.util.Utils.getPixelsPerDPI;

/**
 * Created by samuelgithengi on 11/20/18.
 */
public class TaskingHomeActivity extends BaseMapActivity implements TaskingHomeActivityContract.View,
        View.OnClickListener, SyncStatusBroadcastReceiver.SyncStatusListener, UserLocationContract.UserLocationView,
        OnLocationComponentInitializedCallback, SyncProgressBroadcastReceiver.SyncProgressListener,
        ValidateAssignmentReceiver.UserAssignmentListener, OnMapReadyCallback, Style.OnStyleLoaded, MapboxMap.OnMapClickListener, MapboxMap.OnMapLongClickListener, View.OnTouchListener {

    protected TaskingHomePresenter taskingHomePresenter;

    private android.view.View rootView;

    private GeoJsonSource geoJsonSource;

    private GeoJsonSource selectedGeoJsonSource;

    private ProgressDialog progressDialog;

    protected MapboxMap mMapboxMap;

    private CardView cardView;

    private TextView tvReason;

    private CardView indicatorsCardView;

    private RefreshGeowidgetReceiver refreshGeowidgetReceiver = new RefreshGeowidgetReceiver();

    private SyncProgressBroadcastReceiver syncProgressBroadcastReceiver = new SyncProgressBroadcastReceiver(this);

    private boolean hasRequestedLocation;

    private Snackbar syncProgressSnackbar;

    private BaseDrawerContract.View drawerView;

    private TaskingJsonFormUtils jsonFormUtils;

    private BoundaryLayer boundaryLayer;

    private TaskingMappingHelper mappingHelper;

    private TaskingMapHelper mapHelper;

    private ImageButton myLocationButton;

    private ImageButton layerSwitcherFab;

    private ImageButton filterTasksFab;

    private FrameLayout filterCountLayout;

    private TextView filterCountTextView;

    private EditText searchView;

    private CardDetailsUtil cardDetailsUtil = new CardDetailsUtil();

    private boolean formOpening;

    private TaskingLibraryConfiguration taskingLibraryConfiguration;

    private TaskingLibrary taskingLibrary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutId());

        taskingLibrary = TaskingLibrary.getInstance();

        taskingLibraryConfiguration = taskingLibrary.getTaskingLibraryConfiguration();

        jsonFormUtils = taskingLibrary.getTaskingLibraryConfiguration().getJsonFormUtils();

        drawerView = taskingLibraryConfiguration.getDrawerMenuView(this);

        mappingHelper = taskingLibraryConfiguration.getMappingHelper();

        mapHelper = taskingLibraryConfiguration.getMapHelper();

        taskingHomePresenter = (TaskingHomePresenter) getPresenter();

        rootView = findViewById(R.id.content_frame);

        initializeProgressIndicatorViews();

        initializeMapView(savedInstanceState);

        drawerView.initializeDrawerLayout();

        initializeProgressDialog();

        findViewById(R.id.btn_add_structure).setOnClickListener(this);

        findViewById(R.id.drawerMenu).setOnClickListener(this);

        findViewById(R.id.task_register).setOnClickListener(this);

        initializeCardViews();

        initializeToolbar();

        syncProgressSnackbar = Snackbar.make(rootView, getString(org.smartregister.R.string.syncing), Snackbar.LENGTH_INDEFINITE);
    }

    @Override
    public TaskingHomeActivityContract.Presenter getPresenter() {
        return new TaskingHomePresenter(this, drawerView.getPresenter());
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_tasking_home;
    }

    protected void initializeCardViews() {
        cardView = findViewById(R.id.card_view);

        cardView.setOnTouchListener(this);

        findViewById(R.id.btn_add_structure).setOnClickListener(this);

        indicatorsCardView = findViewById(R.id.indicators_card_view);

        indicatorsCardView.setOnClickListener(this);

        findViewById(R.id.btn_collapse_indicators_card_view).setOnClickListener(this);
    }

    @Override
    public void closeCardView(@IdRes int id) {
        setViewVisibility(findViewById(id), false);
    }

    @Override
    public void closeAllCardViews() {
        setViewVisibility(cardView, false);
    }

    private void setViewVisibility(android.view.View view, boolean isVisible) {
        if (view == null)
            return;
        view.setVisibility(isVisible ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    protected void initializeMapView(Bundle savedInstanceState) {
        kujakuMapView = findViewById(R.id.kujakuMapView);

        myLocationButton = findViewById(R.id.ib_mapview_focusOnMyLocationIcon);

        layerSwitcherFab = findViewById(R.id.fab_mapview_layerSwitcher);

        kujakuMapView.getMapboxLocationComponentWrapper()
                .setOnLocationComponentInitializedCallback(this);

        kujakuMapView.onCreate(savedInstanceState);

        kujakuMapView.showCurrentLocationBtn(taskingLibraryConfiguration.showCurrentLocationButton());

        kujakuMapView.setDisableMyLocationOnMapMove(true);

        Float locationBufferRadius = getLocationBuffer();

        kujakuMapView.setLocationBufferRadius(locationBufferRadius / getPixelsPerDPI(getResources()));

        kujakuMapView.getMapAsync(this);
    }

    protected void enableCompass(MapboxMap mapboxMap) {
        UiSettings uiSettings = mapboxMap.getUiSettings();

        uiSettings.setCompassGravity(Gravity.START | Gravity.TOP);
        uiSettings.setCompassMargins(getResources().getDimensionPixelSize(R.dimen.compass_left_margin),
                getResources().getDimensionPixelSize(R.dimen.compass_top_margin), 0, 0);
        uiSettings.setCompassFadeFacingNorth(false);
        uiSettings.setCompassEnabled(true);
    }

    protected void initializeScaleBarPlugin(MapboxMap mapboxMap) {
        if (displayDistanceScale()) {
            ScaleBarPlugin scaleBarPlugin = new ScaleBarPlugin(kujakuMapView, mapboxMap);
            // Create a ScaleBarOptions object to use custom styling
            ScaleBarOptions scaleBarOptions = new ScaleBarOptions(getContext());
            scaleBarOptions.setTextColor(R.color.distance_scale_text);
            scaleBarOptions.setTextSize(R.dimen.distance_scale_text_size);

            scaleBarPlugin.create(scaleBarOptions);
        }
    }

    private void positionMyLocationAndLayerSwitcher(FrameLayout.LayoutParams myLocationButtonParams, int bottomMargin) {
        if (myLocationButton != null) {
            myLocationButtonParams.gravity = Gravity.BOTTOM | Gravity.END;
            myLocationButtonParams.bottomMargin = bottomMargin;
            myLocationButtonParams.topMargin = 0;
            myLocationButton.setLayoutParams(myLocationButtonParams);
        }
    }

    @Override
    public void positionMyLocationAndLayerSwitcher() {
        FrameLayout.LayoutParams myLocationButtonParams = (FrameLayout.LayoutParams) myLocationButton.getLayoutParams();
        positionMyLocationAndLayerSwitcher(myLocationButtonParams, myLocationButtonParams.topMargin);
    }

    private void initializeProgressIndicatorViews() {
        LinearLayout progressIndicatorsGroupView = findViewById(R.id.progressIndicatorsGroupView);
        progressIndicatorsGroupView.setBackgroundColor(this.getResources().getColor(R.color.transluscent_white));
        progressIndicatorsGroupView.setOnClickListener(this);
    }

    protected void initializeToolbar() {
        searchView = findViewById(R.id.edt_search);
        searchView.setSingleLine();
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { //do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {//do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                taskingHomePresenter.searchTasks(s.toString().trim());
            }
        });

        filterTasksFab = findViewById(R.id.filter_tasks_fab);
        filterCountLayout = findViewById(R.id.filter_tasks_count_layout);
        filterCountTextView = findViewById(R.id.filter_tasks_count);

        filterTasksFab.setOnClickListener(this);
        filterCountLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(android.view.View v) {
        if (v.getId() == R.id.task_register) {
            taskingHomePresenter.onOpenTaskRegisterClicked();
        } else if (v.getId() == R.id.drawerMenu) {
            drawerView.openDrawerLayout();
        } else if (v.getId() == R.id.progressIndicatorsGroupView) {
            openIndicatorsCardView();
        } else if (v.getId() == R.id.filter_tasks_fab || v.getId() == R.id.filter_tasks_count_layout) {
            taskingHomePresenter.onFilterTasksClicked();
        }
    }

    @Override
    public void openFilterTaskActivity(TaskFilterParams filterParams) {
    }

    private void openIndicatorsCardView() {
        setViewVisibility(indicatorsCardView, true);
    }

    @Override
    public void openTaskRegister(TaskFilterParams filterParams) {
        taskingLibraryConfiguration.openTaskRegister(filterParams, this);
    }

    @Override
    public void openStructureProfile(CommonPersonObjectClient family) {
    }

    @Override
    public void registerFamily() {
        clearSelectedFeature();
        taskingLibraryConfiguration.registerFamily(getPresenter().getSelectedFeature());
    }

    @Override
    public void onLocationComponentInitialized() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            LocationComponent locationComponent = kujakuMapView.getMapboxLocationComponentWrapper()
                    .getLocationComponent();
            locationComponent.applyStyle(getApplicationContext(), R.style.LocationComponentStyling);
        }
    }

    @Override
    public void setGeoJsonSource(@NonNull FeatureCollection featureCollection, Feature operationalArea, boolean isChangeMapPosition) {
        taskingLibraryConfiguration.setGeoJsonSource(featureCollection, operationalArea, isChangeMapPosition);
    }

    @Override
    public void displayNotification(int title, int message, Object... formatArgs) {
        AlertDialogUtils.displayNotification(this, title, message, formatArgs);
    }

    @Override
    public void displayNotification(String message) {
        AlertDialogUtils.displayNotification(this, message);
    }

    @Override
    public void openCardView(CardDetails cardDetails) {

    }

    @Override
    public void startJsonForm(JSONObject form) {
        if (!formOpening) {
            jsonFormUtils.startJsonForm(form, this);
            formOpening = true;
        }
    }

    @Override
    public void displaySelectedFeature(Feature feature, LatLng point) {
        displaySelectedFeature(feature, point, mMapboxMap.getCameraPosition().zoom);
    }

    @Override
    public void displaySelectedFeature(Feature feature, LatLng clickedPoint, double zoomlevel) {
        adjustFocusPoint(clickedPoint);
        kujakuMapView.centerMap(clickedPoint, ANIMATE_TO_LOCATION_DURATION, zoomlevel);
        if (selectedGeoJsonSource != null) {
            selectedGeoJsonSource.setGeoJson(FeatureCollection.fromFeature(feature));
        }
    }

    protected void adjustFocusPoint(LatLng point) {
        int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        if (screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL || screenSize == Configuration.SCREENLAYOUT_SIZE_SMALL) {
            point.setLatitude(point.getLatitude() + VERTICAL_OFFSET);
        }
    }

    @Override
    public void clearSelectedFeature() {
        if (selectedGeoJsonSource != null) {
            try {
                selectedGeoJsonSource.setGeoJson(new com.cocoahero.android.geojson.FeatureCollection().toJSON().toString());
            } catch (JSONException e) {
                Timber.e(e, "Error clearing selected feature");
            }
        }
    }

    @Override
    public void displayToast(@StringRes int resourceId) {
        Toast.makeText(this, resourceId, Toast.LENGTH_SHORT).show();
    }

    private void initializeProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(R.string.fetching_structures_title);
        progressDialog.setMessage(getString(R.string.fetching_structures_message));
    }

    @Override
    public void showProgressDialog(@StringRes int title, @StringRes int message) {
        showProgressDialog(title, message, new Object[0]);
    }

    @Override
    public void showProgressDialog(@StringRes int title, @StringRes int message, Object... formatArgs) {
        if (progressDialog != null) {
            progressDialog.setTitle(title);
            if (formatArgs.length == 0) {
                progressDialog.setMessage(getString(message));
            } else {
                progressDialog.setMessage(getString(message, formatArgs));
            }
            progressDialog.show();
        }
    }

    @Override
    public void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public Location getUserCurrentLocation() {
        return kujakuMapView.getLocationClient() == null ? null : kujakuMapView.getLocationClient().getLastLocation();
    }

    @Override
    public void requestUserLocation() {
        kujakuMapView.setWarmGps(true, getString(R.string.location_service_disabled), getString(R.string.location_services_disabled_spray));
        hasRequestedLocation = true;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void onDestroy() {
        taskingHomePresenter = null;
        super.onDestroy();
    }

    @Override
    public void onSyncStart() {
        if (SyncStatusBroadcastReceiver.getInstance().isSyncing()) {
            syncProgressSnackbar.show();
        }
        toggleProgressBarView(true);
    }

    @Override
    public void onSyncInProgress(FetchStatus fetchStatus) {
        if (FetchStatus.fetched.equals(fetchStatus)) {
            syncProgressSnackbar.show();
            return;
        }
        syncProgressSnackbar.dismiss();
        if (fetchStatus.equals(FetchStatus.fetchedFailed)) {
            Snackbar.make(rootView, org.smartregister.R.string.sync_failed, Snackbar.LENGTH_LONG).show();
        } else if (fetchStatus.equals(FetchStatus.nothingFetched)) {
            Snackbar.make(rootView, org.smartregister.R.string.sync_complete, Snackbar.LENGTH_LONG).show();
        } else if (fetchStatus.equals(FetchStatus.noConnection)) {
            Snackbar.make(rootView, org.smartregister.R.string.sync_failed_no_internet, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSyncComplete(FetchStatus fetchStatus) {
        onSyncInProgress(fetchStatus);
        //Check sync status and Update UI to show sync status
        drawerView.checkSynced();
        // revert to sync status view
        toggleProgressBarView(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        formOpening = false;
        SyncStatusBroadcastReceiver.getInstance().addSyncStatusListener(this);
        ValidateAssignmentReceiver.getInstance().addListener(this);
        IntentFilter filter = new IntentFilter(TaskingConstants.Action.STRUCTURE_TASK_SYNCED);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(refreshGeowidgetReceiver, filter);
        IntentFilter syncProgressFilter = new IntentFilter(AllConstants.SyncProgressConstants.ACTION_SYNC_PROGRESS);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(syncProgressBroadcastReceiver, syncProgressFilter);
        drawerView.onResume();
        taskingHomePresenter.onResume();

        if (SyncStatusBroadcastReceiver.getInstance().isSyncing()) {
            syncProgressSnackbar.show();
            toggleProgressBarView(true);
        }
    }

    @Override
    public void onPause() {
        SyncStatusBroadcastReceiver.getInstance().removeSyncStatusListener(this);
        ValidateAssignmentReceiver.getInstance().removeLister(this);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(refreshGeowidgetReceiver);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(syncProgressBroadcastReceiver);
        taskingLibraryConfiguration
                .setMyLocationComponentEnabled(taskingLibraryConfiguration.isMyLocationComponentEnabled());
        super.onPause();
    }

    @Override
    public void onDrawerClosed() {
        taskingHomePresenter.onDrawerClosed();
    }

    @Override
    public AppCompatActivity getActivity() {
        return this;
    }

    @Override
    public TaskingJsonFormUtils getJsonFormUtils() {
        return jsonFormUtils;
    }

    @Override
    public void focusOnUserLocation(boolean focusOnUserLocation) {
        kujakuMapView.focusOnUserLocation(focusOnUserLocation, RenderMode.COMPASS);
    }

    @Override
    public boolean isMyLocationComponentActive() {
        return taskingLibraryConfiguration.isMyLocationComponentEnabled();
    }

    @Override
    public void displayMarkStructureInactiveDialog() {
        AlertDialogUtils.displayNotificationWithCallback(this, R.string.mark_location_inactive,
                R.string.confirm_mark_location_inactive, R.string.confirm, R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == BUTTON_POSITIVE)
                            taskingHomePresenter.onMarkStructureInactiveConfirmed();
                        dialog.dismiss();
                    }
                });
    }

    @Override
    public void setNumberOfFilters(int numberOfFilters) {
        if (numberOfFilters > 0) {
            filterTasksFab.setVisibility(android.view.View.GONE);
            filterCountLayout.setVisibility(android.view.View.VISIBLE);
            filterCountTextView.setText(String.valueOf(numberOfFilters));
        } else {
            filterTasksFab.setVisibility(android.view.View.VISIBLE);
            filterCountLayout.setVisibility(android.view.View.GONE);
        }
    }

    @Override
    public void setSearchPhrase(String searchPhrase) {
        searchView.setText(searchPhrase);
    }

    @Override
    public void toggleProgressBarView(boolean syncing) {
        drawerView.toggleProgressBarView(syncing);
    }

    @Override
    public void setOperationalArea(String operationalArea) {
        drawerView.setOperationalArea(operationalArea);
    }

    @Override
    public void onSyncProgress(SyncProgress syncProgress) {
        int progress = syncProgress.getPercentageSynced();
        String entity = getSyncEntityString(syncProgress.getSyncEntity());
        ProgressBar syncProgressBar = findViewById(R.id.sync_progress_bar);
        TextView syncProgressBarLabel = findViewById(R.id.sync_progress_bar_label);
        String labelText = String.format(getResources().getString(R.string.progressBarLabel), entity, progress);
        syncProgressBar.setProgress(progress);
        syncProgressBarLabel.setText(labelText);
    }

    @Override
    public void onUserAssignmentRevoked(UserAssignmentDTO userAssignmentDTO) {
        drawerView.onResume();
    }

    protected String getSyncEntityString(SyncEntity syncEntity) {
        return Utils.getSyncEntityString(syncEntity);
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        mMapboxMap = mapboxMap;

        Style.Builder builder = new Style.Builder()
                .fromUri(getString(org.smartregister.tasking.R.string.reveal_satellite_style));

        mapboxMap.setStyle(builder, this);

        mapboxMap.setMinZoomPreference(10);

        mapboxMap.setMaxZoomPreference(21);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .zoom(16)
                .build();
        mapboxMap.setCameraPosition(cameraPosition);

        mapboxMap.addOnMapClickListener(this);

        mapboxMap.addOnMapLongClickListener(this);

        taskingHomePresenter.onMapReady();

        positionMyLocationAndLayerSwitcher();
    }

    @Override
    public void onStyleLoaded(@NonNull Style style) {
        if (isCompassEnabled()) {
            enableCompass(mMapboxMap);
        }
        geoJsonSource = style.getSourceAs(getString(R.string.reveal_datasource_name));

        selectedGeoJsonSource = style.getSourceAs(getString(R.string.selected_datasource_name));

        mapHelper.addCustomLayers(style, getActivity());

        mapHelper.addBaseLayers(kujakuMapView, style, getActivity());

        initializeScaleBarPlugin(mMapboxMap);
    }

    protected boolean isCompassEnabled() {
        return taskingLibraryConfiguration.isCompassEnabled();
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        return false;
    }

    @Override
    public boolean onMapLongClick(@NonNull LatLng point) {
        taskingHomePresenter.onMapClicked(mMapboxMap, point, true);
        return false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return true;
    }

    private class RefreshGeowidgetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            boolean localSyncDone;
            if (extras != null && extras.getBoolean(UPDATE_LOCATION_BUFFER_RADIUS)) {
                float bufferRadius = getLocationBuffer() / getPixelsPerDPI(getResources());
                kujakuMapView.setLocationBufferRadius(bufferRadius);
            }
            localSyncDone = extras != null && extras.getBoolean(LOCAL_SYNC_DONE);
            taskingHomePresenter.refreshStructures(localSyncDone);
        }
    }
}




