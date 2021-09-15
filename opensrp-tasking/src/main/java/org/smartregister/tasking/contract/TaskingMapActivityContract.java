package org.smartregister.tasking.contract;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Event;
import org.smartregister.domain.Task;
import org.smartregister.tasking.model.CardDetails;
import org.smartregister.tasking.model.TaskDetails;
import org.smartregister.tasking.model.TaskFilterParams;
import org.smartregister.tasking.util.TaskingJsonFormUtils;

import java.util.List;

/**
 * Created by samuelgithengi on 11/27/18.
 */
public interface TaskingMapActivityContract {

    interface View extends UserLocationContract.UserLocationView, BaseDrawerContract.DrawerActivity {

        void showProgressDialog(@StringRes int title, @StringRes int message, Object... formatArgs);

        void hideProgressDialog();

        Context getContext();

        Presenter getPresenter();

        int getLayoutId();

        void closeCardView(int id);

        void closeAllCardViews();

        void positionMyLocationAndLayerSwitcher();

        void openFilterTaskActivity(TaskFilterParams filterParams);

        void openTaskRegister(TaskFilterParams filterParams);

        void openStructureProfile(CommonPersonObjectClient family);

        void registerFamily();

        void setGeoJsonSource(@NonNull FeatureCollection featureCollection, Feature operationalArea, boolean changeMapPosition);

        void displayNotification(int title, @StringRes int message, Object... formatArgs);

        void displayNotification(String message);

        void openCardView(CardDetails cardDetails);

        void startJsonForm(JSONObject form);

        void displaySelectedFeature(Feature feature, LatLng clickedPoint);

        void displaySelectedFeature(Feature feature, LatLng clickedPoint, double zoomlevel);

        void clearSelectedFeature();

        void displayToast(@StringRes int resourceId);

        TaskingJsonFormUtils getJsonFormUtils();

        void focusOnUserLocation(boolean focusOnUserLocation);

        boolean isMyLocationComponentActive();

        void displayMarkStructureInactiveDialog();

        void setNumberOfFilters(int numberOfFilters);

        void setSearchPhrase(String searchPhrase);

        void toggleProgressBarView(boolean syncing);

        void setOperationalArea(String operationalArea);
    }

    interface Presenter extends BaseContract.BasePresenter {

        void onStructuresFetched(JSONObject structuresGeoJson, Feature operationalArea, List<TaskDetails> taskDetailsList);

        void onStructuresFetched(JSONObject structuresGeoJson, Feature operationalArea, List<TaskDetails> taskDetailsList, String point, Boolean locationComponentActive);

        void onDrawerClosed();

        void startForm(Feature feature, CardDetails cardDetails, String interventionType);

        void startForm(Feature feature, CardDetails cardDetails, String interventionType, Event event);

        void startForm(String formName, Feature feature, String sprayStatus, String familyHead, Event event);

        void startForm(JSONObject formJson);

        void onUndoInterventionStatus(String interventionType);

        void saveJsonForm(String json);

        void resetFeatureTasks(String structureId, Task task);

        void onStructureAdded(Feature feature, JSONArray featureCoordinates, double zoomlevel);

        void onFormSaveFailure(String eventType);

        void onCardDetailsFetched(CardDetails cardDetails);

        void onInterventionFormDetailsFetched(CardDetails finalCardDetails);

        void onInterventionTaskInfoReset(boolean success);

        void onAddStructureClicked(boolean myLocationComponentActive);

        void onAddStructureClicked(boolean myLocationComponentActive, String point);

        Feature getSelectedFeature();

        @StringRes
        int getInterventionLabel();

        void onMarkStructureInactiveConfirmed();

        void onStructureMarkedInactive();

        void onMarkStructureIneligibleConfirmed();

        void onStructureMarkedIneligible();

        void onMapReady();

        void onMapClicked(MapboxMap mapboxMap, LatLng point, boolean isLongclick);

        void onFeatureSelected(Feature feature, boolean isLongclick);

        void onFeatureSelectedByClick(Feature feature);

        void onFeatureSelectedByLongClick(Feature feature);

        void validateUserLocation();

        void onFilterTasksClicked();

        void onOpenTaskRegisterClicked();

        void setTaskFilterParams(TaskFilterParams filterParams);

        void onEventFound(Event event);
      
        void findLastEvent(String featureId, String eventType);
      
        void onFociBoundaryLongClicked();

        void searchTasks(String searchPhrase);

        TaskingMapActivityContract.View getView();
    }
}
