package org.smartregister.tasking.sample;

import com.mapbox.mapboxsdk.Mapbox;

import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.commonregistry.CommonFtsObject;
import org.smartregister.configurableviews.ConfigurableViewsLibrary;
import org.smartregister.dto.UserAssignmentDTO;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.receiver.ValidateAssignmentReceiver;
import org.smartregister.repository.Repository;
import org.smartregister.tasking.TaskingLibrary;
import org.smartregister.tasking.sample.configuration.SampleSyncConfiguration;
import org.smartregister.tasking.sample.configuration.SampleTaskingLibraryConfiguration;
import org.smartregister.tasking.sample.repository.SampleRepository;
import org.smartregister.tasking.util.TaskingLibraryConfiguration;
import org.smartregister.view.activity.DrishtiApplication;

import java.util.ArrayList;
import java.util.Arrays;

import io.ona.kujaku.KujakuLibrary;
import timber.log.Timber;

public class SampleApplication extends DrishtiApplication implements ValidateAssignmentReceiver.UserAssignmentListener {

    private static CommonFtsObject commonFtsObject;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        context = Context.getInstance();
        context.updateApplicationContext(getApplicationContext());
        context.updateCommonFtsObject(createCommonFtsObject());
        CoreLibrary.init(context, new SampleSyncConfiguration());

        //Auto login by default
        context.session().start(context.session().lengthInMilliseconds());
        context.configuration().getDrishtiApplication().setPassword(SampleRepository.PASSWORD.getBytes());
        context.session().setPassword(SampleRepository.PASSWORD.getBytes());

        TaskingLibraryConfiguration taskingLibraryConfiguration = new SampleTaskingLibraryConfiguration();
        TaskingLibrary.init(taskingLibraryConfiguration);
        getRepository();

        ConfigurableViewsLibrary.init(context);
        SyncStatusBroadcastReceiver.init(this);

        LocationHelper.init(new ArrayList<>(Arrays.asList(BuildConfig.ALLOWED_LEVELS)), BuildConfig.DEFAULT_LOCATION_LEVEL);

        ValidateAssignmentReceiver.init(this);
        ValidateAssignmentReceiver.getInstance().addListener(this);

        Mapbox.getInstance(getApplicationContext(), "sample_key");
        KujakuLibrary.init(getApplicationContext());

        initializeTestLocationData();
    }

    public void initializeTestLocationData() {
        String strLocationHierarchy = "{\"locationsHierarchy\":{\"map\":{\"87045b8a-7e21-4adb-9ced-80ad301ba8dc\":{\"children\":{\"15ca3b5d-7d0a-442d-8767-58c776790267\":{\"children\":{\"f123ffe6-7f35-452a-aede-e1921aed8dda\":{\"children\":{\"446f641d-319b-42b5-990d-494add77c8d5\":{\"id\":\"446f641d-319b-42b5-990d-494add77c8d5\",\"label\":\"CommuneA\",\"node\":{\"attributes\":{\"geographicLevel\":0},\"locationId\":\"446f641d-319b-42b5-990d-494add77c8d5\",\"name\":\"CommuneA\",\"parentLocation\":{\"locationId\":\"f123ffe6-7f35-452a-aede-e1921aed8dda\",\"serverVersion\":0,\"voided\":false,\"type\":\"Location\"},\"tags\":[\"Commune\"],\"serverVersion\":0,\"voided\":false,\"type\":\"Location\"},\"parent\":\"f123ffe6-7f35-452a-aede-e1921aed8dda\"}},\"id\":\"f123ffe6-7f35-452a-aede-e1921aed8dda\",\"label\":\"DistrictA\",\"node\":{\"attributes\":{\"geographicLevel\":0},\"locationId\":\"f123ffe6-7f35-452a-aede-e1921aed8dda\",\"name\":\"DistrictA\",\"parentLocation\":{\"locationId\":\"15ca3b5d-7d0a-442d-8767-58c776790267\",\"serverVersion\":0,\"voided\":false,\"type\":\"Location\"},\"tags\":[\"District\"],\"serverVersion\":0,\"voided\":false,\"type\":\"Location\"},\"parent\":\"15ca3b5d-7d0a-442d-8767-58c776790267\"}},\"id\":\"15ca3b5d-7d0a-442d-8767-58c776790267\",\"label\":\"RegionA\",\"node\":{\"attributes\":{\"geographicLevel\":0},\"locationId\":\"15ca3b5d-7d0a-442d-8767-58c776790267\",\"name\":\"RegionA\",\"parentLocation\":{\"locationId\":\"87045b8a-7e21-4adb-9ced-80ad301ba8dc\",\"serverVersion\":0,\"voided\":false,\"type\":\"Location\"},\"tags\":[\"Region\"],\"serverVersion\":0,\"voided\":false,\"type\":\"Location\"},\"parent\":\"87045b8a-7e21-4adb-9ced-80ad301ba8dc\"}},\"id\":\"87045b8a-7e21-4adb-9ced-80ad301ba8dc\",\"label\":\"Madagascar\",\"node\":{\"attributes\":{\"geographicLevel\":0},\"locationId\":\"87045b8a-7e21-4adb-9ced-80ad301ba8dc\",\"name\":\"Madagascar\",\"tags\":[\"Country\"],\"serverVersion\":0,\"voided\":false,\"type\":\"Location\"}}},\"parentChildren\":{\"f123ffe6-7f35-452a-aede-e1921aed8dda\":[\"446f641d-319b-42b5-990d-494add77c8d5\"],\"15ca3b5d-7d0a-442d-8767-58c776790267\":[\"f123ffe6-7f35-452a-aede-e1921aed8dda\"],\"87045b8a-7e21-4adb-9ced-80ad301ba8dc\":[\"15ca3b5d-7d0a-442d-8767-58c776790267\"]}}}";
        context.allSettings().saveANMLocation(strLocationHierarchy);
        context.allSettings().put("dfltLoc-", "446f641d-319b-42b5-990d-494add77c8d5");
        context.allSharedPreferences().updateANMUserName("demo");
    }

    private static String[] getFtsTables() {
        return new String[]{};
    }

    private static String[] getFtsSearchFields(String tableName) {
        return new String[]{};
    }

    private static String[] getFtsSortFields(String tableName) {
        return null;
    }

    public static CommonFtsObject createCommonFtsObject() {
        if (commonFtsObject == null) {
            commonFtsObject = new CommonFtsObject(getFtsTables());
            for (String ftsTable : commonFtsObject.getTables()) {
                commonFtsObject.updateSearchFields(ftsTable, getFtsSearchFields(ftsTable));
                commonFtsObject.updateSortFields(ftsTable, getFtsSortFields(ftsTable));
            }
        }
        return commonFtsObject;
    }

    @Override
    public void logoutCurrentUser() {

    }

    @Override
    public Repository getRepository() {
        try {
            if (repository == null) {
                repository = new SampleRepository(getInstance().getApplicationContext(), context);
            }
        } catch (UnsatisfiedLinkError e) {
            Timber.e(e);
        }
        return repository;
    }

    @Override
    public void onUserAssignmentRevoked(UserAssignmentDTO userAssignmentDTO) {

    }
}
