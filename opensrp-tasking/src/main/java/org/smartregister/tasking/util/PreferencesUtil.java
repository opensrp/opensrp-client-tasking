package org.smartregister.tasking.util;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.account.AccountHelper;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.view.activity.DrishtiApplication;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.smartregister.tasking.util.Constants.Preferences.CURRENT_DISTRICT;
import static org.smartregister.tasking.util.Constants.Preferences.CURRENT_FACILITY;
import static org.smartregister.tasking.util.Constants.Preferences.CURRENT_OPERATIONAL_AREA;
import static org.smartregister.tasking.util.Constants.Preferences.CURRENT_OPERATIONAL_AREA_ID;
import static org.smartregister.tasking.util.Constants.Preferences.CURRENT_PLAN;
import static org.smartregister.tasking.util.Constants.Preferences.CURRENT_PLAN_ID;
import static org.smartregister.tasking.util.Constants.Preferences.CURRENT_PROVINCE;
import static org.smartregister.tasking.util.Constants.Preferences.FACILITY_LEVEL;

/**
 * Created by samuelgithengi on 11/29/18.
 */
public class PreferencesUtil {

    private AllSharedPreferences allSharedPreferences;

    private static PreferencesUtil instance;

    public final static String OPERATIONAL_AREA_SEPARATOR = ",";

    private PreferencesUtil(AllSharedPreferences allSharedPreferences) {
        this.allSharedPreferences = allSharedPreferences;
    }


    public static PreferencesUtil getInstance() {
        if (instance == null) {
            instance = new PreferencesUtil(DrishtiApplication.getInstance().getContext().allSharedPreferences());
        }
        return instance;
    }

    public void setCurrentFacility(String facility) {
        allSharedPreferences.savePreference(CURRENT_FACILITY, facility);
    }

    public String getCurrentFacility() {
        return allSharedPreferences.getPreference(CURRENT_FACILITY);
    }

    public void setCurrentOperationalArea(String operationalArea) {
        allSharedPreferences.savePreference(CURRENT_OPERATIONAL_AREA, operationalArea);
        String currentLocationId = Utils.getCurrentLocationId();
        if (StringUtils.isNotBlank(operationalArea) && StringUtils.isNotBlank(currentLocationId)) {
            allSharedPreferences.savePreference(CURRENT_OPERATIONAL_AREA_ID, currentLocationId);
        }
    }

    public void setCurrentOperationalAreas(Set<String> operationalAreas) {
        if (operationalAreas == null || operationalAreas.isEmpty()) {
            allSharedPreferences.savePreference(CURRENT_OPERATIONAL_AREA, null);
        } else {
            String operationalArea = StringUtils.join(operationalAreas, OPERATIONAL_AREA_SEPARATOR);
            allSharedPreferences.savePreference(CURRENT_OPERATIONAL_AREA, operationalArea);
            String currentLocationId = Utils.getCurrentLocationId();
            if (StringUtils.isNotBlank(operationalArea) && StringUtils.isNotBlank(currentLocationId)) {
                allSharedPreferences.savePreference(CURRENT_OPERATIONAL_AREA_ID, currentLocationId);
            }
        }
    }

    public Set<String> getCurrentOperationalAreas() {
        Set<String> operationalAreas = new HashSet<>();
        String operationalArea = allSharedPreferences.getPreference(CURRENT_OPERATIONAL_AREA);
        if (StringUtils.isNotBlank(operationalArea)) {
            String[] operationalAreaArr = operationalArea.split(OPERATIONAL_AREA_SEPARATOR);
            operationalAreas = new HashSet<>(Arrays.asList(operationalAreaArr));
        }
        return operationalAreas;
    }

    public Set<String> getCurrentOperationalAreaIds() {
        Set<String> operationalAreaIds = new HashSet<>();
        String operationalAreaId = allSharedPreferences.getPreference(CURRENT_OPERATIONAL_AREA_ID);
        if (StringUtils.isNotBlank(operationalAreaId)) {
            String[] operationalAreaArr = operationalAreaId.split(OPERATIONAL_AREA_SEPARATOR);
            operationalAreaIds = new HashSet<>(Arrays.asList(operationalAreaArr));
        }
        return operationalAreaIds;
    }

    public String getCurrentOperationalArea() {
        return allSharedPreferences.getPreference(CURRENT_OPERATIONAL_AREA);
    }

    public String getCurrentOperationalAreaId() {
        return allSharedPreferences.getPreference(CURRENT_OPERATIONAL_AREA_ID);
    }

    public void setCurrentDistrict(String district) {
        allSharedPreferences.savePreference(CURRENT_DISTRICT, district);
    }

    public String getCurrentDistrict() {
        return allSharedPreferences.getPreference(CURRENT_DISTRICT);
    }

    public void setCurrentProvince(String province) {
        allSharedPreferences.savePreference(CURRENT_PROVINCE, province);
    }

    public String getCurrentProvince() {
        return allSharedPreferences.getPreference(CURRENT_PROVINCE);
    }

    public void setCurrentPlan(String campaign) {
        allSharedPreferences.savePreference(CURRENT_PLAN, campaign);
    }

    public String getCurrentPlan() {
        return allSharedPreferences.getPreference(CURRENT_PLAN);
    }

    public void setCurrentPlanId(String campaignId) {
        allSharedPreferences.savePreference(CURRENT_PLAN_ID, campaignId);
    }

    public String getCurrentPlanId() {
        return allSharedPreferences.getPreference(CURRENT_PLAN_ID);
    }

    public String getPreferenceValue(String key) {
        return allSharedPreferences.getPreference(key);
    }

    public void setCurrentFacilityLevel(String facilityLevel) {
        allSharedPreferences.savePreference(FACILITY_LEVEL, facilityLevel);
    }


    public String getCurrentFacilityLevel() {
        return allSharedPreferences.getPreference(FACILITY_LEVEL);
    }

    public void setInterventionTypeForPlan(String planId, String interventionType) {
        allSharedPreferences.savePreference(planId, interventionType);
    }

    public String getInterventionTypeForPlan(String planId) {
        return allSharedPreferences.getPreference(planId);
    }

    public boolean isKeycloakConfigured() {
        return allSharedPreferences.getPreferences().getBoolean(AccountHelper.CONFIGURATION_CONSTANTS.IS_KEYCLOAK_CONFIGURED, false);
    }
}
