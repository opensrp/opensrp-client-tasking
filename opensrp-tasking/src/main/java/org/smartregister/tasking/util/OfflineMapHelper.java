package org.smartregister.tasking.util;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Geometry;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.turf.TurfMeasurement;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.tasking.R;
import org.smartregister.tasking.TaskingLibrary;
import org.smartregister.tasking.server.FileHTTPServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ona.kujaku.data.realm.RealmDatabase;
import io.ona.kujaku.data.realm.objects.MapBoxOfflineQueueTask;
import io.ona.kujaku.downloaders.MapBoxOfflineResourcesDownloader;
import io.ona.kujaku.helpers.OfflineServiceHelper;
import timber.log.Timber;

import static io.ona.kujaku.data.MapBoxDownloadTask.MAP_NAME;
import static org.smartregister.tasking.util.Constants.Map.DOWNLOAD_MAX_ZOOM;
import static org.smartregister.tasking.util.Constants.Map.DOWNLOAD_MIN_ZOOM;

/**
 * Created by Richard Kareko on 1/30/20.
 */

public class OfflineMapHelper {

    @NonNull
    public static Pair<List<String>, Map<String, OfflineRegion>> getOfflineRegionInfo(final OfflineRegion[] offlineRegions) {
        List<String> offlineRegionNames = new ArrayList<>();
        Map<String, OfflineRegion> modelMap = new HashMap<>();

        for (int position = 0; position < offlineRegions.length; position++) {

            byte[] metadataBytes = offlineRegions[position].getMetadata();
            try {
                JSONObject jsonObject = new JSONObject(new String(metadataBytes));
                if (jsonObject.has(MapBoxOfflineResourcesDownloader.METADATA_JSON_FIELD_REGION_NAME)) {
                    String regionName = jsonObject.getString(MapBoxOfflineResourcesDownloader.METADATA_JSON_FIELD_REGION_NAME);
                    offlineRegionNames.add(regionName);
                    modelMap.put(regionName, offlineRegions[position]);
                }

            } catch (JSONException e) {
                Timber.e(e);
            }

        }

        return new Pair(offlineRegionNames, modelMap);
    }

    public static Map<String, MapBoxOfflineQueueTask> populateOfflineQueueTaskMap(RealmDatabase realmDatabase) {
        Map<String, MapBoxOfflineQueueTask> offlineQueueTaskMap = new HashMap<>();

        List<MapBoxOfflineQueueTask> offlineQueueTasks = realmDatabase.getTasks();

        if (offlineQueueTasks == null) {
            return offlineQueueTaskMap;
        }

        for (MapBoxOfflineQueueTask offlineQueueTask : offlineQueueTasks) {

            try {
                if (MapBoxOfflineQueueTask.TASK_TYPE_DOWNLOAD.equals(offlineQueueTask.getTaskType())
                        && MapBoxOfflineQueueTask.TASK_STATUS_DONE == offlineQueueTask.getTaskStatus()) {
                    offlineQueueTaskMap.put(offlineQueueTask.getTask().get(MAP_NAME).toString(), offlineQueueTask);
                }
            } catch (JSONException e) {
                Timber.e(e);
            }
        }

        return offlineQueueTaskMap;
    }

    public static void downloadMap(final Feature operationalAreaFeature, final String mapName, final Context context) {
        Geometry geometry = operationalAreaFeature.geometry();
        if (geometry != null) {
            Runnable runnable = new Runnable() {
                public void run() {
                    double[] bbox = TurfMeasurement.bbox(geometry);

                    double minX = bbox[0];
                    double minY = bbox[1];
                    double maxX = bbox[2];
                    double maxY = bbox[3];

                    double topLeftLat = maxY;
                    double topLeftLng = minX;
                    double bottomRightLat = minY;
                    double bottomRightLng = maxX;
                    double topRightLat = maxY;
                    double topRightLng = maxX;
                    double bottomLeftLat = minY;
                    double bottomLeftLng = minX;

                    String mapboxStyle = context.getString(R.string.localhost_url, FileHTTPServer.PORT);

                    LatLng topLeftBound = new LatLng(topLeftLat, topLeftLng);
                    LatLng topRightBound = new LatLng(topRightLat, topRightLng);
                    LatLng bottomRightBound = new LatLng(bottomRightLat, bottomRightLng);
                    LatLng bottomLeftBound = new LatLng(bottomLeftLat, bottomLeftLng);

                    double maxZoom = DOWNLOAD_MAX_ZOOM;
                    double minZoom = DOWNLOAD_MIN_ZOOM;

                    OfflineServiceHelper.ZoomRange zoomRange = new OfflineServiceHelper.ZoomRange(minZoom, maxZoom);

                    OfflineServiceHelper.requestOfflineMapDownload(context
                            , mapName
                            , mapboxStyle
                            , TaskingLibrary.getInstance().getMapboxAccessToken()
                            , topLeftBound
                            , topRightBound
                            , bottomRightBound
                            , bottomLeftBound
                            , zoomRange
                    );
                }
            };

            TaskingLibrary.getInstance().getAppExecutors().diskIO().execute(runnable);
        }
    }

    public static void initializeFileHTTPServer(Context context, String digitalGlobeIdPlaceholder, String mapStyleAssetPath) {
        try {
            if (StringUtils.isNotBlank(mapStyleAssetPath)) {
                FileHTTPServer httpServer = new FileHTTPServer(context, mapStyleAssetPath, digitalGlobeIdPlaceholder);
                httpServer.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
