package org.smartregister.tasking.util;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

public class DefaultLocationHierarchyUtils {

    @NonNull
    public static List<String> getLocationLevels() {
        return Arrays.asList("Country", "Region", "District", "Commune");
    }

    @NonNull
    public static List<String> getFacilityLevels() {
        return Arrays.asList("Country", "Region", "District");
    }

}
