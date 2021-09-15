package org.smartregister.tasking.interactor;

import org.smartregister.domain.Location;
import org.smartregister.tasking.TaskingLibrary;
import org.smartregister.tasking.contract.AvailableOfflineMapsContract;
import org.smartregister.tasking.model.OfflineMapModel;
import org.smartregister.tasking.util.TaskingLibraryConfiguration;
import org.smartregister.util.AppExecutors;

import java.util.ArrayList;
import java.util.List;

public class AvailableOfflineMapsInteractor implements AvailableOfflineMapsContract.Interactor {

    private AppExecutors appExecutors;

    private AvailableOfflineMapsContract.Presenter presenter;

    private TaskingLibraryConfiguration taskingLibraryConfiguration;

    public AvailableOfflineMapsInteractor(AvailableOfflineMapsContract.Presenter presenter) {
        this.presenter = presenter;
        appExecutors = TaskingLibrary.getInstance().getTaskingLibraryConfiguration().getAppExecutors();
        taskingLibraryConfiguration = TaskingLibrary.getInstance().getTaskingLibraryConfiguration();
    }


    @Override
    public void fetchAvailableOAsForMapDownLoad(final List<String> locationIds) {
        Runnable runnable = new Runnable() {
            public void run() {
                List<Location> operationalAreas = taskingLibraryConfiguration.getLocationsIdsForDownload(locationIds);
                appExecutors.mainThread().execute(() -> {
                    presenter.onFetchAvailableOAsForMapDownLoad(populateOfflineMapModelList(operationalAreas));
                });
            }
        };
        appExecutors.diskIO().execute(runnable);
    }

    public List<OfflineMapModel> populateOfflineMapModelList(List<Location> locations) {
        List<OfflineMapModel> offlineMapModels = new ArrayList<>();
        for (Location location : locations) {
            OfflineMapModel offlineMapModel = new OfflineMapModel();
            offlineMapModel.setLocation(location);
            offlineMapModels.add(offlineMapModel);
        }

        return offlineMapModels;
    }

}
