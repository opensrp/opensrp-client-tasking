package org.smartregister.tasking.sample.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

import org.smartregister.tasking.activity.TaskingHomeActivity;
import org.smartregister.tasking.util.PreferencesUtil;

import java.util.UUID;

public class SampleTaskingHomeActivity extends TaskingHomeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //added to prevent drawer locking
        PreferencesUtil preferencesUtil = PreferencesUtil.getInstance();
        preferencesUtil.setCurrentOperationalArea("Test");
        preferencesUtil.setCurrentPlan("test");
        preferencesUtil.setCurrentPlanId(UUID.randomUUID().toString());
        //
    }

    @Override
    public void setGeoJsonSource(@NonNull FeatureCollection featureCollection, Feature operationalArea, boolean isChangeMapPosition) {

    }

}
