package org.smartregister.tasking.view;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.util.Pair;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.vijay.jsonwizard.customviews.TreeViewDialog;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.smartregister.CoreLibrary;
import org.smartregister.p2p.activity.P2pModeSelectActivity;
import org.smartregister.tasking.R;
import org.smartregister.tasking.TaskingLibrary;
import org.smartregister.tasking.activity.OfflineMapsActivity;
import org.smartregister.tasking.contract.BaseDrawerContract;
import org.smartregister.tasking.interactor.BaseDrawerInteractor;
import org.smartregister.tasking.presenter.BaseDrawerPresenter;
import org.smartregister.tasking.util.AlertDialogUtils;
import org.smartregister.tasking.util.TaskingConstants;
import org.smartregister.tasking.util.TaskingLibraryConfiguration;
import org.smartregister.tasking.util.Utils;
import org.smartregister.util.NetworkUtils;
import org.smartregister.view.activity.DrishtiApplication;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by samuelgithengi on 3/21/19.
 */
public class DrawerMenuView implements View.OnClickListener, BaseDrawerContract.View, View.OnLongClickListener, DrawerLayout.DrawerListener {


    private TextView planTextView;
    private TextView operationalAreaTextView;
    private TextView districtTextView;
    private TextView facilityTextView;
    private TextView operatorTextView;
    private TextView p2pSyncTextView;

    private DrawerLayout mDrawerLayout;

    private BaseDrawerContract.Presenter presenter;

    private BaseDrawerContract.DrawerActivity activity;

    private BaseDrawerContract.Interactor interactor;

    TaskingLibraryConfiguration taskingLibraryConfiguration;

    public DrawerMenuView(BaseDrawerContract.DrawerActivity activity) {
        this.activity = activity;
        presenter = new BaseDrawerPresenter(this, activity);
        interactor = new BaseDrawerInteractor(presenter);
        taskingLibraryConfiguration = TaskingLibrary.getInstance().getTaskingLibraryConfiguration();
    }

    @Override
    public void initializeDrawerLayout() {

        mDrawerLayout = getContext().findViewById(R.id.drawer_layout);

        mDrawerLayout.addDrawerListener(this);

        NavigationView navigationView = getContext().findViewById(R.id.nav_view);

        setUpViews(navigationView);

        checkSynced();
    }

    @Override
    public void setUpViews(NavigationView navigationView) {
        View headerView = navigationView.getHeaderView(0);

        try {
            String manifestVersion = getManifestVersion();
            String appVersion = getContext().getString(R.string.tasking_app_version, org.smartregister.util.Utils.getVersion(getContext()));
            String appVersionText = appVersion + (manifestVersion == null ? "" : getContext().getString(R.string.manifest_version_parenthesis_placeholder, manifestVersion));
            ((TextView) headerView.findViewById(R.id.application_version))
                    .setText(appVersionText);
        } catch (PackageManager.NameNotFoundException e) {
            Timber.e(e);
        }

        TextView offlineMapTextView = headerView.findViewById(R.id.btn_navMenu_offline_maps);

        TextView summaryFormsTextView = headerView.findViewById(R.id.btn_navMenu_summaryForms);

        planTextView = headerView.findViewById(R.id.plan_selector);

        operationalAreaTextView = headerView.findViewById(R.id.operational_area_selector);

        districtTextView = headerView.findViewById(R.id.district_label);

        facilityTextView = headerView.findViewById(R.id.facility_label);

        p2pSyncTextView = headerView.findViewById(R.id.btn_navMenu_p2pSyncBtn);

        operatorTextView = getContext().findViewById(R.id.operator_label);

        getContext().findViewById(R.id.logout_button).setOnClickListener(this);

        operationalAreaTextView.setOnClickListener(this);

        planTextView.setOnClickListener(this);

        offlineMapTextView.setVisibility(View.VISIBLE);
        offlineMapTextView.setOnClickListener(this);

        headerView.findViewById(R.id.sync_button).setOnClickListener(this);
        headerView.findViewById(R.id.btn_navMenu_offline_maps).setOnClickListener(this);

        districtTextView.setOnLongClickListener(this);

        operatorTextView.setOnLongClickListener(this);
    }

    @Override
    public void setPlan(String campaign) {
        planTextView.setText(campaign);
    }

    @Override
    public void setOperationalArea(String operationalArea) {
        operationalAreaTextView.setText(operationalArea);
    }

    @Override
    public String getPlan() {
        return planTextView.getText().toString();
    }

    @Override
    public String getOperationalArea() {
        return operationalAreaTextView.getText().toString();
    }

    @Override
    public void setDistrict(String district) {
        Utils.setTextViewText(districtTextView, R.string.district, district);
    }

    @Override
    public void setFacility(String facility, String facilityLevel) {
        Utils.setTextViewText(facilityTextView,
                TaskingConstants.Tags.CANTON.equals(facilityLevel) ? R.string.canton : R.string.facility, facility);
    }

    @Override
    public void setOperator() {
        Utils.setTextViewText(operatorTextView, R.string.operator,
                DrishtiApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM());
    }

    @Override
    public void lockNavigationDrawerForSelection() {
        mDrawerLayout.openDrawer(GravityCompat.START);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
    }

    @Override
    public void lockNavigationDrawerForSelection(@StringRes int title,@StringRes int message) {
        AlertDialogUtils.displayNotification(getContext(), title, message);
        lockNavigationDrawerForSelection();
    }


    @Override
    public void unlockNavigationDrawer() {
        if (mDrawerLayout.getDrawerLockMode(GravityCompat.START) == DrawerLayout.LOCK_MODE_LOCKED_OPEN) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }

    @Override
    public void showOperationalAreaSelector(Pair<String, ArrayList<String>> locationHierarchy) {
        try {
            TreeViewDialog treeViewDialog = new TreeViewDialog(getContext(),
                    R.style.AppTheme_WideDialog,
                    new JSONArray(locationHierarchy.first), locationHierarchy.second, locationHierarchy.second);
            treeViewDialog.setCancelable(true);
            treeViewDialog.setCanceledOnTouchOutside(true);
            treeViewDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    presenter.onOperationalAreaSelectorClicked(treeViewDialog.getName());
                }
            });
            treeViewDialog.show();
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @Override
    public void showPlanSelector(List<String> campaigns, String entireTreeString) {
        if (StringUtils.isBlank(entireTreeString)) {
            displayNotification(R.string.plans_download_on_progress_title, R.string.plans_download_on_progress);
            return;
        }
        try {
            TreeViewDialog treeViewDialog = new TreeViewDialog(getContext(),
                    R.style.AppTheme_WideDialog,
                    new JSONArray(entireTreeString), new ArrayList<>(campaigns), new ArrayList<>(campaigns));
            treeViewDialog.show();
            treeViewDialog.setCanceledOnTouchOutside(true);
            treeViewDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    presenter.onPlanSelectorClicked(treeViewDialog.getValue(), treeViewDialog.getName());
                }
            });
            treeViewDialog.show();
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @Override
    public void displayNotification(int title, int message, Object... formatArgs) {
        AlertDialogUtils.displayNotification(getContext(), title, message, formatArgs);
    }

    @Override
    public Activity getContext() {
        return activity.getActivity();
    }


    @Override
    public void openDrawerLayout() {
        mDrawerLayout.openDrawer(GravityCompat.START);
    }


    @Override
    public void closeDrawerLayout() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.operational_area_selector)
            presenter.onShowOperationalAreaSelector();
        else if (v.getId() == R.id.plan_selector)
            presenter.onShowPlanSelector();
        else if (v.getId() == R.id.logout_button)
            DrishtiApplication.getInstance().logoutCurrentUser();
        else if (v.getId() == R.id.btn_navMenu_offline_maps)
            presenter.onShowOfflineMaps();
        else if (v.getId() == R.id.btn_navMenu_p2pSyncBtn)
            startP2PActivity();
        else if (v.getId() == R.id.btn_navMenu_summaryForms)
            presenter.startOtherFormsActivity();
        else if (v.getId() == R.id.btn_navMenu_offline_maps)
            presenter.onShowOfflineMaps();
        else if (v.getId() == R.id.btn_navMenu_filled_forms)
            presenter.onShowFilledForms();
        else if (v.getId() == R.id.sync_button) {
            toggleProgressBarView(true);
            //Utils.startImmediateSync();
            closeDrawerLayout();
        }
    }

    @Override
    public BaseDrawerContract.Presenter getPresenter() {
        return presenter;
    }

    @Override
    public void onResume() {
        presenter.onViewResumed();
    }

    private void startP2PActivity() {
        getContext().startActivity(new Intent(getContext(), P2pModeSelectActivity.class));
    }

    @Override
    public void openOfflineMapsView() {
        Intent intent = new Intent(getContext(), taskingLibraryConfiguration.getActivityConfiguration().getOfflineMapsActivity());
        getContext().startActivity(intent);
    }

    @Override
    public void checkSynced() {
        interactor.checkSynced();
    }

    @Override
    public void toggleProgressBarView(boolean syncing) {
        ProgressBar progressBar = this.activity.getActivity().findViewById(R.id.sync_progress_bar);
        TextView progressLabel = this.activity.getActivity().findViewById(R.id.sync_progress_bar_label);
        TextView syncButton = this.activity.getActivity().findViewById(R.id.sync_button);
        TextView syncBadge = this.activity.getActivity().findViewById(R.id.sync_label);
        if (progressBar == null || syncBadge == null)
            return;
        if (syncing && NetworkUtils.isNetworkAvailable()) { //only hide the sync button when there is internet connection
            progressBar.setVisibility(View.VISIBLE);
            progressLabel.setVisibility(View.VISIBLE);
            syncButton.setVisibility(View.INVISIBLE);
            syncBadge.setVisibility(View.INVISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            progressLabel.setVisibility(View.INVISIBLE);
            syncButton.setVisibility(View.VISIBLE);
            syncBadge.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onLongClick(View view) {
        return false;
    }

    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(@NonNull View drawerView) {

    }

    @Override
    public void onDrawerClosed(@NonNull View drawerView) {
        presenter.onDrawerClosed();
    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }

    @Nullable
    @Override
    public String getManifestVersion() {
        return CoreLibrary.getInstance().context().allSharedPreferences().fetchManifestVersion();
    }

    @Override
    public BaseDrawerContract.DrawerActivity getActivity() {
        return activity;
    }
}
