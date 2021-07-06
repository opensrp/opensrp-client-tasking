package org.smartregister.tasking.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
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
import androidx.annotation.Nullable;
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
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.pluginscalebar.ScaleBarOptions;
import com.mapbox.pluginscalebar.ScaleBarPlugin;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.SyncEntity;
import org.smartregister.domain.SyncProgress;
import org.smartregister.domain.Task;
import org.smartregister.dto.UserAssignmentDTO;
import org.smartregister.receiver.SyncProgressBroadcastReceiver;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.receiver.ValidateAssignmentReceiver;
import org.smartregister.tasking.R;
import org.smartregister.tasking.TaskingLibrary;
import org.smartregister.tasking.contract.BaseDrawerContract;
import org.smartregister.tasking.contract.TaskingMapActivityContract;
import org.smartregister.tasking.contract.UserLocationContract;
import org.smartregister.tasking.model.CardDetails;
import org.smartregister.tasking.model.TaskFilterParams;
import org.smartregister.tasking.presenter.TaskingMapPresenter;
import org.smartregister.tasking.repository.TaskingMappingHelper;
import org.smartregister.tasking.util.AlertDialogUtils;
import org.smartregister.tasking.util.TaskingConstants;
import org.smartregister.tasking.util.TaskingJsonFormUtils;
import org.smartregister.tasking.util.TaskingLibraryConfiguration;
import org.smartregister.tasking.util.TaskingMapHelper;
import org.smartregister.tasking.util.Utils;

import java.util.List;

import io.ona.kujaku.callbacks.OnLocationComponentInitializedCallback;
import io.ona.kujaku.layers.BoundaryLayer;
import io.ona.kujaku.listeners.OnFeatureLongClickListener;
import io.ona.kujaku.utils.Constants;
import timber.log.Timber;

import static android.content.DialogInterface.BUTTON_POSITIVE;
import static org.smartregister.tasking.util.TaskingConstants.ANIMATE_TO_LOCATION_DURATION;
import static org.smartregister.tasking.util.TaskingConstants.CONFIGURATION.LOCAL_SYNC_DONE;
import static org.smartregister.tasking.util.TaskingConstants.CONFIGURATION.UPDATE_LOCATION_BUFFER_RADIUS;
import static org.smartregister.tasking.util.TaskingConstants.DatabaseKeys.STRUCTURE_ID;
import static org.smartregister.tasking.util.TaskingConstants.DatabaseKeys.TASK_ID;
import static org.smartregister.tasking.util.TaskingConstants.Filter.FILTER_SORT_PARAMS;
import static org.smartregister.tasking.util.TaskingConstants.Intervention.IRS;
import static org.smartregister.tasking.util.TaskingConstants.Intervention.LARVAL_DIPPING;
import static org.smartregister.tasking.util.TaskingConstants.Intervention.MOSQUITO_COLLECTION;
import static org.smartregister.tasking.util.TaskingConstants.Intervention.PAOT;
import static org.smartregister.tasking.util.TaskingConstants.JSON_FORM_PARAM_JSON;
import static org.smartregister.tasking.util.TaskingConstants.RequestCode.REQUEST_CODE_FAMILY_PROFILE;
import static org.smartregister.tasking.util.TaskingConstants.RequestCode.REQUEST_CODE_FILTER_TASKS;
import static org.smartregister.tasking.util.TaskingConstants.RequestCode.REQUEST_CODE_GET_JSON;
import static org.smartregister.tasking.util.TaskingConstants.RequestCode.REQUEST_CODE_TASK_LISTS;
import static org.smartregister.tasking.util.TaskingConstants.VERTICAL_OFFSET;
import static org.smartregister.tasking.util.Utils.displayDistanceScale;
import static org.smartregister.tasking.util.Utils.getLocationBuffer;
import static org.smartregister.tasking.util.Utils.getPixelsPerDPI;

/**
 * Created by samuelgithengi on 11/20/18.
 */
public class TaskingMapActivity extends BaseMapActivity implements TaskingMapActivityContract.View,
        View.OnClickListener, SyncStatusBroadcastReceiver.SyncStatusListener, UserLocationContract.UserLocationView,
        OnLocationComponentInitializedCallback, SyncProgressBroadcastReceiver.SyncProgressListener,
        ValidateAssignmentReceiver.UserAssignmentListener, OnMapReadyCallback, Style.OnStyleLoaded, MapboxMap.OnMapClickListener, MapboxMap.OnMapLongClickListener, View.OnTouchListener {

    protected TaskingMapPresenter taskingMapPresenter;

    private android.view.View rootView;

    protected GeoJsonSource geoJsonSource;

    protected GeoJsonSource selectedGeoJsonSource;

    private ProgressDialog progressDialog;

    protected MapboxMap mMapboxMap;

    private CardView cardView;

    private CardView sprayCardView;

    private CardView mosquitoCollectionCardView;
    private CardView larvalBreedingCardView;
    private CardView potentialAreaOfTransmissionCardView;
    private CardView indicatorsCardView;
    private CardView irsVerificationCardView;

    private TextView tvReason;

    private RefreshGeowidgetReceiver refreshGeowidgetReceiver = new RefreshGeowidgetReceiver();

    private SyncProgressBroadcastReceiver syncProgressBroadcastReceiver = new SyncProgressBroadcastReceiver(this);

    protected boolean hasRequestedLocation;

    private Snackbar syncProgressSnackbar;

    protected BaseDrawerContract.View drawerView;

    private TaskingJsonFormUtils jsonFormUtils;

    private BoundaryLayer boundaryLayer;

    private TaskingMappingHelper mappingHelper;

    private TaskingMapHelper mapHelper;

    protected ImageButton myLocationButton;

    private ImageButton layerSwitcherFab;

    private ImageButton filterTasksFab;

    private FrameLayout filterCountLayout;

    private TextView filterCountTextView;

    private EditText searchView;

    private boolean formOpening;

    private TaskingLibraryConfiguration taskingLibraryConfiguration;

    private TaskingLibrary taskingLibrary;

    private LineLayer indexCaseLineLayer;

    private TaskingMapActivityContract.Presenter presenter;

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

        taskingMapPresenter = (TaskingMapPresenter) getPresenter();

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
    public TaskingMapActivityContract.Presenter getPresenter() {
        if (presenter == null) {
            presenter = new TaskingMapPresenter(this, drawerView.getPresenter());
        }
        return presenter;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_tasking_map;
    }

    protected void initializeCardViews() {
        cardView = findViewById(R.id.card_view);

        sprayCardView = findViewById(R.id.spray_card_view);

        sprayCardView.setOnTouchListener(this);

        mosquitoCollectionCardView = findViewById(R.id.mosquito_collection_card_view);

        larvalBreedingCardView = findViewById(R.id.larval_breeding_card_view);

        potentialAreaOfTransmissionCardView = findViewById(R.id.potential_area_of_transmission_card_view);

        irsVerificationCardView = findViewById(R.id.irs_verification_card_view);

        cardView.setOnTouchListener(this);

        findViewById(R.id.btn_add_structure).setOnClickListener(this);

        findViewById(R.id.btn_collapse_spray_card_view).setOnClickListener(this);

        tvReason = findViewById(R.id.reason);

        findViewById(R.id.change_spray_status).setOnClickListener(this);

        findViewById(R.id.btn_undo_spray).setOnClickListener(this);

        findViewById(R.id.btn_collapse_mosquito_collection_card_view).setOnClickListener(this);

        findViewById(R.id.btn_record_mosquito_collection).setOnClickListener(this);

        findViewById(R.id.btn_undo_mosquito_collection).setOnClickListener(this);

        findViewById(R.id.btn_collapse_larval_breeding_card_view).setOnClickListener(this);

        findViewById(R.id.btn_record_larval_dipping).setOnClickListener(this);

        findViewById(R.id.btn_undo_larval_dipping).setOnClickListener(this);

        findViewById(R.id.btn_collapse_paot_card_view).setOnClickListener(this);

        findViewById(R.id.btn_edit_paot_details).setOnClickListener(this);

        findViewById(R.id.btn_undo_paot_details).setOnClickListener(this);

        findViewById(R.id.btn_collapse_irs_verification_card_view).setOnClickListener(this);

        indicatorsCardView = findViewById(R.id.indicators_card_view);

        indicatorsCardView.setOnClickListener(this);

        findViewById(R.id.btn_collapse_indicators_card_view).setOnClickListener(this);

        findViewById(R.id.register_family).setOnClickListener(this);

    }

    @Override
    public void closeCardView(@IdRes int id) {
        if (id == R.id.btn_collapse_spray_card_view) {
            setViewVisibility(sprayCardView, false);
        } else if (id == R.id.btn_collapse_mosquito_collection_card_view) {
            setViewVisibility(mosquitoCollectionCardView, false);
        } else if (id == R.id.btn_collapse_larval_breeding_card_view) {
            setViewVisibility(larvalBreedingCardView, false);
        } else if (id == R.id.btn_collapse_paot_card_view) {
            setViewVisibility(potentialAreaOfTransmissionCardView, false);
        } else if (id == R.id.btn_collapse_indicators_card_view) {
            setViewVisibility(indicatorsCardView, false);
        } else if (id == R.id.btn_collapse_irs_verification_card_view) {
            setViewVisibility(irsVerificationCardView, false);
        } else {
            setViewVisibility(findViewById(id), false);
        }
    }

    @Override
    public void closeAllCardViews() {
        setViewVisibility(cardView, false);
        setViewVisibility(sprayCardView, false);
        setViewVisibility(mosquitoCollectionCardView, false);
        setViewVisibility(larvalBreedingCardView, false);
        setViewVisibility(potentialAreaOfTransmissionCardView, false);
        setViewVisibility(indicatorsCardView, false);
        setViewVisibility(irsVerificationCardView, false);
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

        kujakuMapView.setDisableMyLocationOnMapMove(taskingLibraryConfiguration.disableMyLocationOnMapMove());

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

    public void initializeScaleBarPlugin(MapboxMap mapboxMap) {
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
                taskingMapPresenter.searchTasks(s.toString().trim());
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
            taskingMapPresenter.onOpenTaskRegisterClicked();
        } else if (v.getId() == R.id.drawerMenu) {
            drawerView.openDrawerLayout();
        } else if (v.getId() == R.id.progressIndicatorsGroupView) {
            openIndicatorsCardView();
        } else if (v.getId() == R.id.filter_tasks_fab || v.getId() == R.id.filter_tasks_count_layout) {
            taskingMapPresenter.onFilterTasksClicked();
        } else if (v.getId() == R.id.btn_add_structure) {
            taskingMapPresenter.onAddStructureClicked(mapHelper.isMyLocationComponentActive(this, myLocationButton));
        } else if (v.getId() == R.id.change_spray_status) {
            taskingMapPresenter.onChangeInterventionStatus(IRS);
        } else if (v.getId() == R.id.btn_undo_spray) {
            displayResetInterventionTaskDialog(IRS);
        } else if (v.getId() == R.id.btn_record_mosquito_collection) {
            taskingMapPresenter.onChangeInterventionStatus(MOSQUITO_COLLECTION);
        } else if (v.getId() == R.id.btn_undo_mosquito_collection) {
            displayResetInterventionTaskDialog(MOSQUITO_COLLECTION);
        } else if (v.getId() == R.id.btn_record_larval_dipping) {
            taskingMapPresenter.onChangeInterventionStatus(LARVAL_DIPPING);
        } else if (v.getId() == R.id.btn_undo_larval_dipping) {
            displayResetInterventionTaskDialog(LARVAL_DIPPING);
        } else if (v.getId() == R.id.btn_edit_paot_details) {
            taskingMapPresenter.onChangeInterventionStatus(PAOT);
        } else if (v.getId() == R.id.btn_collapse_mosquito_collection_card_view
                || v.getId() == R.id.btn_collapse_larval_breeding_card_view
                || v.getId() == R.id.btn_collapse_paot_card_view
                || v.getId() == R.id.btn_collapse_indicators_card_view
                || v.getId() == R.id.btn_collapse_irs_verification_card_view) {
            closeCardView(v.getId());
        } else if (v.getId() == R.id.btn_collapse_spray_card_view) {
            setViewVisibility(tvReason, false);
            closeCardView(v.getId());
        } else if (v.getId() == R.id.register_family) {
            registerFamily();
            closeCardView(R.id.btn_collapse_spray_card_view);
        }
    }

    @Override
    public void openFilterTaskActivity(TaskFilterParams filterParams) {
        taskingLibraryConfiguration.openFilterTaskActivity(filterParams, this);
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
        taskingLibraryConfiguration.openStructureProfile(family, this, taskingMapPresenter);
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
        if (geoJsonSource != null) {
            geoJsonSource.setGeoJson(featureCollection);
            if (operationalArea != null) {
                CameraPosition cameraPosition = null;
                if (operationalArea.geometry() != null) {
                    cameraPosition = mMapboxMap.getCameraForGeometry(operationalArea.geometry());
                }
                if (taskingMapPresenter.getInterventionLabel() == R.string.focus_investigation) {
                    Feature indexCase = mapHelper.getIndexCase(featureCollection);
                    if (indexCase != null) {
                        Location center = mappingHelper.getCenter(indexCase.geometry().toJson());
                        double currentZoom = mMapboxMap.getCameraPosition().zoom;
                        cameraPosition = new CameraPosition.Builder()
                                .target(new LatLng(center.getLatitude(), center.getLongitude())).zoom(currentZoom).build();
                    }
                }


                if (cameraPosition != null && (boundaryLayer == null || isChangeMapPosition)) {
                    mMapboxMap.setCameraPosition(cameraPosition);
                }

                Boolean drawOperationalAreaBoundaryAndLabel = getDrawOperationalAreaBoundaryAndLabel();
                if (drawOperationalAreaBoundaryAndLabel) {
                    if (boundaryLayer == null) {
                        boundaryLayer = createBoundaryLayer(operationalArea);
                        kujakuMapView.addLayer(boundaryLayer);

                        kujakuMapView.setOnFeatureLongClickListener(new OnFeatureLongClickListener() {
                            @Override
                            public void onFeatureLongClick(List<Feature> features) {
                                taskingMapPresenter.onFociBoundaryLongClicked();
                            }
                        }, boundaryLayer.getLayerIds());

                    } else {
                        boundaryLayer.updateFeatures(FeatureCollection.fromFeature(operationalArea));
                    }
                }

                if (taskingMapPresenter.getInterventionLabel() == R.string.focus_investigation && mapHelper.getIndexCaseLineLayer() == null) {
                    mapHelper.addIndexCaseLayers(mMapboxMap, getContext(), featureCollection);
                } else {
                    mapHelper.updateIndexCaseLayers(mMapboxMap, featureCollection, this);
                }
            }
        }
    }

    private Boolean getDrawOperationalAreaBoundaryAndLabel() {
        return taskingLibraryConfiguration.getDrawOperationalAreaBoundaryAndLabel();
    }

    protected BoundaryLayer createBoundaryLayer(Feature operationalArea) {
        return new BoundaryLayer.Builder(FeatureCollection.fromFeature(operationalArea))
                .setLabelProperty(TaskingConstants.Map.NAME_PROPERTY)
                .setLabelTextSize(getResources().getDimension(R.dimen.operational_area_boundary_text_size))
                .setLabelColorInt(Color.WHITE)
                .setBoundaryColor(Color.WHITE)
                .setBoundaryWidth(getResources().getDimension(R.dimen.operational_area_boundary_width)).build();
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
        taskingLibraryConfiguration.openCardView(cardDetails, this);
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
        taskingMapPresenter = null;
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
        taskingMapPresenter.onResume();

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
        taskingMapPresenter.onDrawerClosed();
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
                            taskingMapPresenter.onMarkStructureInactiveConfirmed();
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

        mapboxMap.addOnMapClickListener(this);

        mapboxMap.addOnMapLongClickListener(this);

        Style.Builder builder = new Style.Builder()
                .fromUri(getString(R.string.reveal_satellite_style));

        mapboxMap.setStyle(builder, this);

        taskingMapPresenter.onMapReady();

        positionMyLocationAndLayerSwitcher();
    }

    @Override
    public void onStyleLoaded(@NonNull Style style) {
        if (isCompassEnabled()) {
            enableCompass(mMapboxMap);
        }
        geoJsonSource = style.getSourceAs(getString(R.string.reveal_datasource_name));

        selectedGeoJsonSource = style.getSourceAs(getString(R.string.selected_datasource_name));

        mapHelper.addCustomLayers(style, TaskingMapActivity.this);

        mapHelper.addBaseLayers(kujakuMapView, style, TaskingMapActivity.this);

        initializeScaleBarPlugin(mMapboxMap);
    }

    protected boolean isCompassEnabled() {
        return taskingLibraryConfiguration.isCompassEnabled();
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        taskingMapPresenter.onMapClicked(mMapboxMap, point, false);
        return false;
    }

    @Override
    public boolean onMapLongClick(@NonNull LatLng point) {
        taskingMapPresenter.onMapClicked(mMapboxMap, point, true);
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        onActivityResultExtended(requestCode, resultCode, data);
    }

    public void onActivityResultExtended(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_GET_JSON && resultCode == RESULT_OK && data.hasExtra(JSON_FORM_PARAM_JSON)) {
            String json = data.getStringExtra(JSON_FORM_PARAM_JSON);
            Timber.d(json);
            taskingMapPresenter.saveJsonForm(json);
        } else if (requestCode == Constants.RequestCode.LOCATION_SETTINGS && hasRequestedLocation) {
            if (resultCode == RESULT_OK) {
                taskingMapPresenter.getLocationPresenter().waitForUserLocation();
            } else if (resultCode == RESULT_CANCELED) {
                taskingMapPresenter.getLocationPresenter().onGetUserLocationFailed();
            }
            hasRequestedLocation = false;
        } else if (requestCode == REQUEST_CODE_FAMILY_PROFILE && resultCode == RESULT_OK && data.hasExtra(STRUCTURE_ID)) {
            String structureId = data.getStringExtra(STRUCTURE_ID);
            Task task = (Task) data.getSerializableExtra(TASK_ID);
            taskingMapPresenter.resetFeatureTasks(structureId, task);
        } else if (requestCode == REQUEST_CODE_FILTER_TASKS && resultCode == RESULT_OK && data.hasExtra(FILTER_SORT_PARAMS)) {
            TaskFilterParams filterParams = (TaskFilterParams) data.getSerializableExtra(FILTER_SORT_PARAMS);
            taskingMapPresenter.filterTasks(filterParams);
        } else if (requestCode == REQUEST_CODE_TASK_LISTS && resultCode == RESULT_OK && data.hasExtra(FILTER_SORT_PARAMS)) {
            TaskFilterParams filterParams = (TaskFilterParams) data.getSerializableExtra(FILTER_SORT_PARAMS);
            taskingMapPresenter.setTaskFilterParams(filterParams);
        }
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
            taskingMapPresenter.refreshStructures(localSyncDone);
        }
    }

    public void displayResetInterventionTaskDialog(String interventionType) {
        AlertDialogUtils.displayNotificationWithCallback(this, R.string.undo_task_title,
                R.string.undo_task_msg, R.string.confirm, R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == BUTTON_POSITIVE)
                            taskingMapPresenter.onUndoInterventionStatus(interventionType);
                        dialog.dismiss();
                    }
                });
    }
}





