package org.smartregister.tasking.sample.configuration;

import androidx.annotation.NonNull;

import org.smartregister.tasking.configuration.DefaultTaskingLibraryConfiguration;
import org.smartregister.tasking.sample.activity.SampleOfflineMapsActivity;
import org.smartregister.tasking.sample.activity.SampleTaskRegisterActivity;
import org.smartregister.tasking.sample.activity.SampleTaskingHomeActivity;
import org.smartregister.tasking.util.ActivityConfiguration;
import org.smartregister.util.AppExecutors;

public class SampleTaskingLibraryConfiguration extends DefaultTaskingLibraryConfiguration {

    @Override
    public ActivityConfiguration getActivityConfiguration() {
        return ActivityConfiguration.builder()
                .offlineMapsActivity(SampleOfflineMapsActivity.class)
                .taskingHomeActivity(SampleTaskingHomeActivity.class)
                .taskRegisterActivity(SampleTaskRegisterActivity.class)
                .build();
    }

    @NonNull
    @Override
    public AppExecutors getAppExecutors() {
        return new AppExecutors();
    }

    @Override
    public double getOnClickMaxZoomLevel() {
        return 6.0;
    }
}
