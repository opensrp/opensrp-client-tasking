package org.smartregister.tasking.task;

/**
 * Created by Ephraim Kigamba - nek.eam@gmail.com on 18-01-2021.
 */

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.mapbox.geojson.Feature;

import org.smartregister.tasking.contract.MapCalloutFeature;

import java.util.HashMap;
import java.util.List;

/**
 * AsyncTask to generate Bitmap from Views to be used as iconImage in a SymbolLayer.
 * <p>
 * Call be optionally be called to update the underlying data source after execution.
 * </p>
 * <p>
 * Generating Views on background thread since we are not going to be adding them to the view hierarchy.
 * </p>
 */
public class GenerateViewIconTask extends AsyncTask<List<Feature>, Void, HashMap<String, Bitmap>> {

    private MapCalloutFeature.ImageGenTask imageGenTask;

    public GenerateViewIconTask(MapCalloutFeature.ImageGenTask imageGenTask) {
        this.imageGenTask = imageGenTask;
    }

    @SafeVarargs
    @SuppressWarnings("WrongThread")
    @Override
    protected final HashMap<String, Bitmap> doInBackground(List<Feature>... params) {
        if (params.length > 0) {
            return imageGenTask.generateCalloutBitmaps(params[0]);
        }

        return null;
    }

    @Override
    protected void onPostExecute(HashMap<String, Bitmap> bitmapHashMap) {
        super.onPostExecute(bitmapHashMap);
        imageGenTask.updateMapWithCalloutBitmaps(bitmapHashMap);
    }


}