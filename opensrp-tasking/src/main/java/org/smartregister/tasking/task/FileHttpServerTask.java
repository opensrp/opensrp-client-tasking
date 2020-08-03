package org.smartregister.tasking.task;

import android.content.Context;
import android.os.AsyncTask;

import org.smartregister.tasking.util.Constants;
import org.smartregister.tasking.util.OfflineMapHelper;

/**
 * Created by Richard Kareko on 2/4/20.
 */

public class FileHttpServerTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private String mapStyleAssetPath;

    public FileHttpServerTask(Context context, String mapStyleAssetPath) {
        this.context = context;
        this.mapStyleAssetPath = mapStyleAssetPath;
    }

    @Override
    protected Void doInBackground(Void... params) {
        OfflineMapHelper.initializeFileHTTPServer(context, Constants.DG_ID_PLACEHOLDER, mapStyleAssetPath);
        return null;
    }
}
