package org.smartregister.tasking.util;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.mapbox.geojson.Feature;
import com.mapbox.mapboxsdk.annotations.BubbleLayout;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.jetbrains.annotations.Nullable;
import org.smartregister.tasking.R;
import org.smartregister.tasking.contract.MapCalloutFeature;
import org.smartregister.tasking.task.GenerateViewIconTask;

import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

/**
 * Created by Ephraim Kigamba - nek.eam@gmail.com on 18-01-2021.
 */
public class KujakuFeatureCalloutPlugin implements MapCalloutFeature.Plugin, MapCalloutFeature.ImageGenTask, MapboxMap.OnMapClickListener {


    public static final String CALLOUT_LAYER_ID = "kujaku.layout.layer.feature.callout";
    public static final String SOURCE_ID = "mapbox.poi";
    public static final String PROPERTY_SELECTED = "selected";
    public static final String PROPERTY_TITLE = "title";
    public static final String PROPERTY_FAVOURITE = "favourite";
    public static final String PROPERTY_STYLE = "style";

    private MapCalloutFeature.MapView mapView;

    private HashMap<String, View> viewMap = new HashMap<>(0);
    private Style style;

    public KujakuFeatureCalloutPlugin(@NonNull MapCalloutFeature.MapView mapView, @NonNull Style style) {
        this.mapView = mapView;
        this.style = style;
    }

    public void setupOnMap(@NonNull String featureSourceId) {
        Style style = getStyle();

        if (style != null) {
            GeoJsonSource geoJsonSource = (GeoJsonSource) style.getSourceAs(featureSourceId);

            if (geoJsonSource != null) {
                setupOnMap(geoJsonSource);
            } else {
                Timber.e(new Exception(), "Could not setup %s! GeoJsonSource could not be found", KujakuFeatureCalloutPlugin.class.getName());
            }
        }
    }

    public void setupOnMap(@NonNull GeoJsonSource geoJsonSource) {
        List<Feature> featureList = geoJsonSource.querySourceFeatures(Expression.all());
        setupOnMap(featureList, geoJsonSource.getId());
    }

    public void setupOnMap(@NonNull List<Feature> featureList, @NonNull String geoJsonSourceId) {
        setupCalloutLayer(style, geoJsonSourceId);

        GenerateViewIconTask generateViewIconTask = new GenerateViewIconTask(this);

        generateViewIconTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, featureList);
        mapView.getMapboxMap().addOnMapClickListener(this);
    }

    protected void setupCalloutLayer(@NonNull Style style, @NonNull String featureSourceId) {
        Layer calloutLayer = style.getLayer(CALLOUT_LAYER_ID);

        if (calloutLayer == null) {
            style.addLayer(new SymbolLayer(CALLOUT_LAYER_ID, featureSourceId)
                            .withProperties(
                                    /* show image with id title based on the value of the title feature property */
                                    PropertyFactory.iconImage(String.format("{%s}", getLabelProperty())),

                                    /* set anchor of icon to bottom-left */
                                    PropertyFactory.iconAnchor(Property.ICON_ANCHOR_BOTTOM_LEFT),

                                    /* offset icon slightly to match bubble layout */
                                    PropertyFactory.iconOffset(new Float[]{-20.0f, -10.0f}),
                                    PropertyFactory.iconAllowOverlap(true),
                                    PropertyFactory.iconIgnorePlacement(true)
                            )

                    /* add a filter to show only when selected feature property is true */
                    //.withFilter(Expression.eq(Expression.literal(PROPERTY_SELECTED), true))
            );
        }
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        MapboxMap mapboxMap = mapView.getMapboxMap();

        if (mapboxMap != null) {
            PointF screenPoint = mapboxMap.getProjection().toScreenLocation(point);
            List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint, CALLOUT_LAYER_ID);
            if (!features.isEmpty()) {
                // we received a click event on the callout layer
                Feature feature = features.get(0);
                PointF symbolScreenPoint = mapboxMap.getProjection().toScreenLocation(convertToLatLng(feature));
                handleClickCallout(feature, screenPoint, symbolScreenPoint);

                return true;
            } else {
                // we didn't find a click event on callout layer, try clicking maki layer
                //handleClickIcon(screenPoint);
                return false;
            }
        }

        return false;
    }

    private LatLng convertToLatLng(Feature feature) {
        com.mapbox.geojson.Point symbolPoint = (com.mapbox.geojson.Point) feature.geometry();
        return new LatLng(symbolPoint.latitude(), symbolPoint.longitude());
    }


    private void handleClickCallout(Feature feature, PointF screenPoint, PointF symbolScreenPoint) {
        View view = viewMap.get(feature.getStringProperty(PROPERTY_TITLE));

        if (view != null) {
            View textContainer = view.findViewById(R.id.text_container);

            // create hitbox for textView
            Rect hitRectText = new Rect();
            textContainer.getHitRect(hitRectText);

            // move hitbox to location of symbol
            hitRectText.offset((int) symbolScreenPoint.x, (int) symbolScreenPoint.y);

            // offset vertically to match anchor behaviour
            hitRectText.offset(0, -view.getMeasuredHeight());

            // hit test if clicked point is in textview hitbox
            if (hitRectText.contains((int) screenPoint.x, (int) screenPoint.y)) {
                // user clicked on text
                String callout = feature.getStringProperty("call-out");
                Toast.makeText(mapView.getContext(), callout, Toast.LENGTH_LONG).show();
            } else {
                // user clicked on icon
            /*List<Feature> featureList = featureCollection.getFeatures();
            for (int i = 0; i < featureList.size(); i++) {
                if (featureList.get(i).getStringProperty(PROPERTY_TITLE).equals(feature.getStringProperty(PROPERTY_TITLE))) {
                    toggleFavourite(i);
                }
            }*/
            }
        }
    }

    @Override
    public void setImageGenResults(@NonNull Style style, HashMap<String, View> viewMap, HashMap<String, Bitmap> bitmapHashMap) {
        this.viewMap = viewMap;

        if (style != null) {
            style.addImages(bitmapHashMap);
        } else {
            Timber.e(new Exception(), "Style could not be updated with images. Style is NULL");
        }
    }

    @Override
    public String getLabelProperty() {
        return Constants.DatabaseKeys.STRUCTURE_NAME;
    }

    @Nullable
    public HashMap<String, Bitmap> generateCalloutBitmaps(List<Feature> featureList) {
        MapCalloutFeature.MapView activity = getMapView();
        String labelProperty = getPlugin().getLabelProperty();
        if (activity != null && labelProperty != null) {
            HashMap<String, Bitmap> imagesMap = new HashMap<>();
            LayoutInflater inflater = LayoutInflater.from(activity.getContext());

            for (Feature feature : featureList) {
                View view = inflater.inflate(R.layout.layout_map_feature_callout, null);

                String name = feature.getStringProperty(labelProperty);
                TextView titleTv = view.findViewById(R.id.title);
                titleTv.setText(name);

                String priority = feature.getStringProperty(TaskingConstants.Properties.TASK_PRIORITY);
                BubbleLayout bubbleLayout = view.findViewById(R.id.calloutLayout);

                int bubbleColorRes = -1;

                if (priority != null) {
                    switch (priority) {
                        case "0":
                            bubbleColorRes = R.color.map_tasking_red;
                            break;

                        case "1":
                            bubbleColorRes = R.color.map_tasking_orange;
                            break;

                        case "2":
                            bubbleColorRes = R.color.map_tasking_blue;
                            break;

                        case "3":
                            bubbleColorRes = R.color.map_tasking_blue;
                            break;
                    }
                }

                if (bubbleColorRes != -1) {
                    int bubbleColor = ContextCompat.getColor(getMapView().getContext(), bubbleColorRes);
                    bubbleLayout.setBubbleColor(bubbleColor);
                }

                Bitmap bitmap = SymbolGenerator.generate(view);
                imagesMap.put(name, bitmap);
                viewMap.put(name, view);
            }

            return imagesMap;
        } else {
            return null;
        }
    }

    @androidx.annotation.Nullable
    @Override
    public MapCalloutFeature.Plugin getPlugin() {
        return this;
    }

    @androidx.annotation.Nullable
    @Override
    public MapCalloutFeature.MapView getMapView() {
        return mapView;
    }

    @Override
    public boolean isRefreshSource() {
        return true;
    }

    @Override
    public void updateMapWithCalloutBitmaps(HashMap<String, Bitmap> bitmapHashMap) {
        Style style = getStyle();

        if (style != null) {
            MapCalloutFeature.Plugin plugin = getPlugin();
            if (plugin != null && bitmapHashMap != null) {
                plugin.setImageGenResults(style, viewMap, bitmapHashMap);
            }

            MapCalloutFeature.MapView activity = getMapView();
            if (bitmapHashMap != null && activity != null && isRefreshSource()) {
                activity.refreshCalloutSource();
            }
        }
    }

    @Override
    public Style getStyle() {
        return style;
    }
}
