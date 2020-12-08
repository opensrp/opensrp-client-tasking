package org.smartregister.tasking.presenter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.domain.PlanDefinition;
import org.smartregister.domain.form.FormLocation;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.tasking.R;
import org.smartregister.tasking.TaskingLibrary;
import org.smartregister.tasking.contract.BaseDrawerContract;
import org.smartregister.tasking.interactor.BaseDrawerInteractor;
import org.smartregister.tasking.util.PreferencesUtil;
import org.smartregister.tasking.util.TaskingConstants;
import org.smartregister.tasking.util.TaskingLibraryConfiguration;
import org.smartregister.util.AssetHandler;
import org.smartregister.util.Utils;
import org.smartregister.view.activity.DrishtiApplication;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

import static org.smartregister.AllConstants.OPERATIONAL_AREAS;
import static org.smartregister.tasking.util.TaskingConstants.TaskRegister.INTERVENTION_TYPE;

/**
 * Created by samuelgithengi on 3/21/19.
 */
public class BaseDrawerPresenter implements BaseDrawerContract.Presenter {

    private WeakReference<BaseDrawerContract.View> viewWeakReference;

    private BaseDrawerContract.DrawerActivity drawerActivity;

    private PreferencesUtil prefsUtil;

    private LocationHelper locationHelper;

    private boolean changedCurrentSelection;

    private BaseDrawerContract.Interactor interactor;

    private static TextView syncLabel;

    private static TextView syncBadge;

    private boolean viewInitialized = false;

    private DrishtiApplication drishtiApplication;

    private TaskingLibrary taskingLibrary;

    private TaskingLibraryConfiguration taskingLibraryConfiguration;

    public BaseDrawerPresenter(BaseDrawerContract.View view, BaseDrawerContract.DrawerActivity drawerActivity) {
        this.viewWeakReference = new WeakReference<>(view);
        this.drawerActivity = drawerActivity;
        this.prefsUtil = PreferencesUtil.getInstance();
        this.locationHelper = LocationHelper.getInstance();
        taskingLibrary = TaskingLibrary.getInstance();
        taskingLibraryConfiguration = taskingLibrary.getTaskingLibraryConfiguration();
        interactor = getInteractor();
        drishtiApplication = DrishtiApplication.getInstance();
    }

    public BaseDrawerContract.Interactor getInteractor() {
        if (interactor == null) {
            interactor = new BaseDrawerInteractor(this);
        }
        return interactor;
    }

    private void initializeDrawerLayout() {
        if (getView() != null) {
            getView().setOperator();
        }

        if (StringUtils.isBlank(prefsUtil.getCurrentOperationalArea())) {

            List<String> defaultLocation = locationHelper.generateDefaultLocationHierarchy(taskingLibraryConfiguration.getLocationLevels());

            if (defaultLocation != null) {
                prefsUtil.setCurrentDistrict(defaultLocation.get(2));
            }
        } else {
            populateLocationsFromPreferences();
        }

        if (getView() != null) {
            getView().setPlan(prefsUtil.getCurrentPlan());
        }
    }


    @Override
    public void onPlansFetched(Set<PlanDefinition> planDefinitions) {
        List<String> ids = new ArrayList<>();
        List<FormLocation> formLocations = new ArrayList<>();
        for (PlanDefinition planDefinition : planDefinitions) {
            if (!planDefinition.getStatus().equals(PlanDefinition.PlanStatus.ACTIVE)) {
                continue;
            }
            ids.add(planDefinition.getIdentifier());
            FormLocation formLocation = new FormLocation();
            formLocation.name = planDefinition.getTitle();
            formLocation.key = planDefinition.getIdentifier();
            formLocation.level = "";
            formLocations.add(formLocation);

            // get intervention type for plan
            for (PlanDefinition.UseContext useContext : planDefinition.getUseContext()) {
                if (useContext.getCode().equals(INTERVENTION_TYPE)) {
                    prefsUtil.setInterventionTypeForPlan(planDefinition.getTitle(), useContext.getValueCodableConcept());
                    break;
                }
            }

        }

        String entireTreeString = "";
        if (formLocations != null && !formLocations.isEmpty()) {
            entireTreeString = AssetHandler.javaToJsonString(formLocations,
                    new TypeToken<List<FormLocation>>() {
                    }.getType());
        }

        if (getView() != null) {
            getView().showPlanSelector(ids, entireTreeString);
        }
    }

    private void populateLocationsFromPreferences() {
        if (getView() != null) {
            getView().setDistrict(prefsUtil.getCurrentDistrict());
            getView().setFacility(prefsUtil.getCurrentFacility(), prefsUtil.getCurrentFacilityLevel());
            getView().setOperationalArea(prefsUtil.getCurrentOperationalArea());
        }
    }

    @Override
    public void onShowOperationalAreaSelector() {
        if (getView() != null) {
            if (!Utils.getBooleanProperty(TaskingConstants.CONFIGURATION.SELECT_PLAN_THEN_AREA) || StringUtils.isNotBlank(prefsUtil.getCurrentPlanId())) {
                Pair<String, ArrayList<String>> locationHierarchy = extractLocationHierarchy();

                if (locationHierarchy != null) {
                    getView().showOperationalAreaSelector(locationHierarchy);
                } else {
                    getView().displayNotification(R.string.error_fetching_location_hierarchy_title, R.string.error_fetching_location_hierarchy);
                    drishtiApplication.getContext().userService().forceRemoteLogin(drishtiApplication.getContext().allSharedPreferences().fetchRegisteredANM());
                }

            } else {
                getView().displayNotification(R.string.campaign, R.string.plan_not_selected);
            }
        }
    }

    public Pair<String, ArrayList<String>> extractLocationHierarchy() {

        List<String> operationalAreaLevels = taskingLibraryConfiguration.getFacilityLevels();

        List<String> defaultLocation = locationHelper.generateDefaultLocationHierarchy(operationalAreaLevels);

        if (defaultLocation != null) {
            List<FormLocation> entireTree = locationHelper.generateLocationHierarchyTree(false, operationalAreaLevels);
            if (!prefsUtil.isKeycloakConfigured()) { // Only required when fetching hierarchy from openmrs
                List<String> authorizedOperationalAreas = Arrays.asList(StringUtils.split(prefsUtil.getPreferenceValue(OPERATIONAL_AREAS), ','));
                removeUnauthorizedOperationalAreas(authorizedOperationalAreas, entireTree);
            }
            String entireTreeString = AssetHandler.javaToJsonString(entireTree,
                    new TypeToken<List<FormLocation>>() {
                    }.getType());

            return new Pair<>(entireTreeString, new ArrayList<>(defaultLocation));
        } else {
            return null;
        }
    }


    @Override
    public void onOperationalAreaSelectorClicked(ArrayList<String> name) {
        Timber.d("Selected Location Hierarchy: " + TextUtils.join(",", name));
        if (name.size() <= 2)//no operational area was selected, dialog was dismissed
            return;
        List<String> operationalAreaLevels = taskingLibraryConfiguration.getFacilityLevels();
        List<FormLocation> entireTree = locationHelper.generateLocationHierarchyTree(false, operationalAreaLevels);
        try {
            prefsUtil.setCurrentProvince(getProvinceFromTreeDialogValue(name));
            prefsUtil.setCurrentDistrict(getDistrictFromTreeDialogValue(name));
            String operationalArea = name.get(name.size() - 1);
            prefsUtil.setCurrentOperationalArea(operationalArea);
            Pair<String, String> facility = getFacilityFromOperationalArea(entireTree);
            if (facility != null) {
                prefsUtil.setCurrentFacility(facility.second);
                prefsUtil.setCurrentFacilityLevel(facility.first);
            }
            validateSelectedPlan(operationalArea);
        } catch (NullPointerException e) {
            Timber.e(e);
        }
        changedCurrentSelection = true;
        populateLocationsFromPreferences();
        unlockDrawerLayout();
    }

    protected String getDistrictFromTreeDialogValue(ArrayList<String> name) {
        return taskingLibraryConfiguration.getDistrictFromTreeDialogValue(name);
    }

    protected String getProvinceFromTreeDialogValue(ArrayList<String> name) {
        return taskingLibraryConfiguration.getProvinceFromTreeDialogValue(name);
    }

    private void removeUnauthorizedOperationalAreas(List<String> operationalAreas, List<FormLocation> entireTree) {

        for (FormLocation countryLocation : entireTree) {
            for (FormLocation provinceLocation : countryLocation.nodes) {
                if (provinceLocation.nodes == null)
                    return;
                for (FormLocation districtLocation : provinceLocation.nodes) {
                    if (districtLocation.nodes == null)
                        return;
                    for (FormLocation healthFacilityLocation : districtLocation.nodes) {
                        if (healthFacilityLocation.nodes == null)
                            return;
                        List<FormLocation> toRemove = new ArrayList<>();
                        for (FormLocation operationalAreaLocation : healthFacilityLocation.nodes) {
                            if (!operationalAreas.contains(operationalAreaLocation.name))
                                toRemove.add(operationalAreaLocation);
                        }
                        healthFacilityLocation.nodes.removeAll(toRemove);
                    }
                }
            }
        }
    }

    //
    public Pair<String, String> getFacilityFromOperationalArea(@NonNull List<FormLocation> entireTree) {
        for (FormLocation districtLocation : entireTree) {
            if (!districtLocation.name.equals(prefsUtil.getCurrentDistrict()))
                continue;
            for (FormLocation facilityLocation : districtLocation.nodes) {
                if (facilityLocation.nodes == null)
                    continue;
                for (FormLocation operationalAreaLocation : facilityLocation.nodes) {
                    if (operationalAreaLocation.name.equals(prefsUtil.getCurrentOperationalArea())) {
                        return new Pair<>(facilityLocation.level, facilityLocation.name);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void onShowPlanSelector() {
        if ((StringUtils.isBlank(prefsUtil.getCurrentOperationalArea()) && !Utils.getBooleanProperty(TaskingConstants.CONFIGURATION.SELECT_PLAN_THEN_AREA))
                && getView() != null) {
            getView().displayNotification(R.string.operational_area, R.string.operational_area_not_selected);
        } else {
            interactor.fetchPlans(prefsUtil.getCurrentOperationalArea());
        }
    }


    @Override
    public void onPlanSelectorClicked(ArrayList<String> value, ArrayList<String> name) {
        if (Utils.isEmptyCollection(name) || (name.size() > 1))
            return;
        Timber.d("Selected Plan : " + TextUtils.join(",", name));
        Timber.d("Selected Plan Ids: " + TextUtils.join(",", value));

        prefsUtil.setCurrentPlan(name.get(0));
        prefsUtil.setCurrentPlanId(value.get(0));
        if (getView() != null) {
            getView().setPlan(name.get(0));
        }
        changedCurrentSelection = true;
        unlockDrawerLayout();
    }

    @Override
    public void onDrawerClosed() {
        drawerActivity.onDrawerClosed();
    }

    @Override
    public void unlockDrawerLayout() {
        if (getView() != null) {
            getView().unlockNavigationDrawer();
        }
    }

    @Override
    public boolean isChangedCurrentSelection() {
        return changedCurrentSelection;
    }

    @Override
    public void setChangedCurrentSelection(boolean changedCurrentSelection) {
        this.changedCurrentSelection = changedCurrentSelection;
    }

    @Override
    public BaseDrawerContract.View getView() {
        if (viewWeakReference != null) {
            return viewWeakReference.get();
        }
        return null;
    }

    @Override
    public void onViewResumed() {
        if (!viewInitialized) {
            initializeDrawerLayout();
        }
        if (getView() == null)
            return;

        if ((StringUtils.isBlank(prefsUtil.getCurrentPlan()) || StringUtils.isBlank(prefsUtil.getCurrentOperationalArea())) &&
                (StringUtils.isNotBlank(getView().getPlan()) || StringUtils.isNotBlank(getView().getOperationalArea()))) {
            getView().setOperationalArea(prefsUtil.getCurrentOperationalArea());
            getView().setPlan(prefsUtil.getCurrentPlan());
            getView().lockNavigationDrawerForSelection(R.string.select_campaign_operational_area_title, R.string.revoked_plan_operational_area);
        } else if (!prefsUtil.getCurrentPlan().equals(getView().getPlan())
                || !prefsUtil.getCurrentOperationalArea().equals(getView().getOperationalArea())) {
            changedCurrentSelection = true;
            onDrawerClosed();
        } else {
            initializeDrawerLayout();
            viewInitialized = true;
        }
        if (taskingLibraryConfiguration.getSynced() && taskingLibraryConfiguration.isRefreshMapOnEventSaved()) {
            getView().checkSynced();
        } else {
            updateSyncStatusDisplay(taskingLibraryConfiguration.getSynced());
        }
    }


    @Override
    public boolean isPlanAndOperationalAreaSelected() {
        String planId = prefsUtil.getCurrentPlanId();
        String operationalArea = prefsUtil.getCurrentOperationalArea();
        return StringUtils.isNotBlank(planId) && StringUtils.isNotBlank(operationalArea);
    }

    @Override
    public void onShowOfflineMaps() {
        if (getView() != null) {
            getView().openOfflineMapsView();
        }
    }

    private void validateSelectedPlan(String operationalArea) {
        if (!prefsUtil.getCurrentPlanId().isEmpty()) {
            interactor.validateCurrentPlan(operationalArea, prefsUtil.getCurrentPlanId());
        }
    }

    @Override
    public void onPlanValidated(boolean isValid) {
        if (!isValid) {
            prefsUtil.setCurrentPlanId("");
            prefsUtil.setCurrentPlan("");
            if (getView() != null) {
                getView().setPlan("");
                getView().lockNavigationDrawerForSelection();
            }
        }
    }

    /**
     * Updates the Hamburger menu of the navigation drawer to display the sync status of the application
     * Updates also the Text view next to the sync button with the sync status of the application
     *
     * @param synced Sync status of the application
     */
    @Override
    public void updateSyncStatusDisplay(boolean synced) {
        if (getView() != null) {
            Activity activity = getView().getContext();
            NavigationView navigationView = activity.findViewById(R.id.nav_view);
            View headerView = navigationView.getHeaderView(0);
            syncLabel = headerView.findViewById(R.id.sync_label);
            syncBadge = activity.findViewById(R.id.sync_badge);
            if (syncBadge != null && syncLabel != null) {
                if (synced) {
                    syncBadge.setBackground(ContextCompat.getDrawable(activity, R.drawable.badge_green_oval));
                    syncLabel.setText(getView().getContext().getString(R.string.device_data_synced));
                    syncLabel.setTextColor(ContextCompat.getColor(activity, R.color.alert_complete_green));
                    syncLabel.setBackground(ContextCompat.getDrawable(activity, R.drawable.rounded_border_alert_green));
                } else {
                    syncBadge.setBackground(ContextCompat.getDrawable(activity, R.drawable.badge_oval));
                    syncLabel.setText(getView().getContext().getString(R.string.device_data_not_synced));
                    syncLabel.setTextColor(ContextCompat.getColor(activity, R.color.alert_urgent_red));
                    syncLabel.setBackground(ContextCompat.getDrawable(activity, R.drawable.rounded_border_alert_red));
                }
            }
        }
    }

    @Override
    public void startOtherFormsActivity() {

    }

    @Override
    public void onShowFilledForms() {
        taskingLibraryConfiguration.onShowFilledForms();
    }

    @Override
    public void checkSynced() {
        interactor.checkSynced();
    }
}
