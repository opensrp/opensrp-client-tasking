package org.smartregister.tasking.util;

import androidx.annotation.NonNull;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Geometry;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.turf.TurfMeasurement;

import java.util.List;

/**
 * Created by Ephraim Kigamba - nek.eam@gmail.com on 20-01-2021.
 */
public class TaskingCoordinateUtils {

    public static LatLng getCenter(List<Feature> features) {
        double[] bbox = null;

        for (Feature feature: features) {
            Geometry featureGeometry = feature.geometry();
            if (featureGeometry != null) {
                double[] featureBbox = TurfMeasurement.bbox(featureGeometry);

                if (bbox == null) {
                    bbox = featureBbox;
                } else {
                    if (featureBbox[0] < bbox[0]) {
                        bbox[0] = featureBbox[0];
                    }

                    if (featureBbox[1] < bbox[1]) {
                        bbox[1] = featureBbox[1];
                    }

                    if (featureBbox[2] > bbox[2]) {
                        bbox[2] = featureBbox[2];
                    }

                    if (featureBbox[3] > bbox[3]) {
                        bbox[3] = featureBbox[3];
                    }
                }
            }
        }

        return getCenter(bbox);
    }

    /**
     * Generates the center from the {@link Geometry} of a given {@link Feature} for {@link Geometry}
     * of types {@link com.mapbox.geojson.MultiPolygon}, {@link com.mapbox.geojson.Polygon} and
     * {@link com.mapbox.geojson.MultiPoint}
     *
     * @param bbox
     * @return
     */
    public static LatLng getCenter(@NonNull double[] bbox) {
        return LatLngBounds.from(bbox[3], bbox[2], bbox[1], bbox[0]).getCenter();
    }
}
