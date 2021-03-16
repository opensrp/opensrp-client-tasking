package org.smartregister.tasking.contract;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mapbox.geojson.Feature;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Ephraim Kigamba - nek.eam@gmail.com on 19-01-2021.
 */
public interface MapCalloutFeature {

    interface MapView {

        void refreshCalloutSource();

        Context getContext();

        MapboxMap getMapboxMap();
    }

    interface Plugin {

        void setupOnMap(@NonNull String featureSourceId);

        void setImageGenResults(@NonNull Style style, HashMap<String, View> viewMap, HashMap<String, Bitmap> bitmapHashMap);

        String getLabelProperty();

        @Nullable
        Style getStyle();
    }

    interface ImageGenTask {

        void updateMapWithCalloutBitmaps(HashMap<String, Bitmap> bitmapHashMap);

        @Nullable
        HashMap<String, Bitmap> generateCalloutBitmaps(List<Feature> featureList);

        @Nullable
        Plugin getPlugin();

        @Nullable
        MapView getMapView();

        boolean isRefreshSource();

        @Nullable
        Style getStyle();

    }
}
