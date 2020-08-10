package org.smartregister.tasking.contract;

import androidx.annotation.NonNull;

import com.mapbox.geojson.Feature;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Task;

/**
 * Created by samuelgithengi on 3/25/19.
 */
public interface BaseContract {

    interface BasePresenter {

        void onFormSaved(@NonNull String structureId,
                         String taskID, @NonNull Task.TaskStatus taskStatus, @NonNull String businessStatus, String interventionType);

        void onStructureAdded(Feature feature, JSONArray featureCoordinates, double zoomlevel);

        void onFormSaveFailure(String eventType);

        void onFamilyFound(CommonPersonObjectClient finalFamily);
    }

    interface BaseInteractor {

        void saveJsonForm(String json);

        org.smartregister.domain.Event saveEvent(JSONObject jsonForm, String encounterType, String bindType) throws JSONException;

        void saveRegisterStructureForm(JSONObject jsonForm);

        void saveMemberForm(JSONObject jsonForm, String eventType, String intervention);

        void saveCaseConfirmation(JSONObject jsonForm, String eventType);

        void saveLocationInterventionForm(JSONObject jsonForm);
    }
}
