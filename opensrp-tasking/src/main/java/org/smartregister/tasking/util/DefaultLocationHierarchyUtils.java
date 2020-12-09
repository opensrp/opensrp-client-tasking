package org.smartregister.tasking.util;

import androidx.annotation.NonNull;

import org.smartregister.tasking.BuildConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultLocationHierarchyUtils {

    @NonNull
    public static List<String> getLocationLevels() {
        return new ArrayList<>(Arrays.asList(BuildConfig.LOCATION_LEVELS));
    }

    @NonNull
    public static List<String> getFacilityLevels() {
        return new ArrayList<>(Arrays.asList(BuildConfig.HEALTH_FACILITY_LEVELS));
    }

}
