package org.smartregister.tasking.util;

import android.app.Activity;
import android.content.Intent;

import com.vijay.jsonwizard.activities.FormConfigurationJsonFormActivity;

import org.json.JSONObject;

import timber.log.Timber;

/**
 * Created by Ephraim Kigamba - nek.eam@gmail.com on 27-07-2020.
 */
public class TaskingJsonFormUtils {



    public void startJsonForm(JSONObject form, Activity context) {
        startJsonForm(form, context, TaskingConstants.RequestCode.REQUEST_CODE_GET_JSON);
    }

    public void startJsonForm(JSONObject form, Activity context, int requestCode) {
        Intent intent = new Intent(context, FormConfigurationJsonFormActivity.class);
        try {
            intent.putExtra(TaskingConstants.JSON_FORM_PARAM_JSON, form.toString());
            context.startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            Timber.e(e);
        }
    }
}
