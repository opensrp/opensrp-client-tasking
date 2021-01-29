package org.smartregister.tasking.interactor;

import org.smartregister.domain.Location;
import org.smartregister.repository.LocationRepository;
import org.smartregister.tasking.TaskingLibrary;
import org.smartregister.tasking.contract.AvailableOfflineMapsContract;
import org.smartregister.tasking.model.OfflineMapModel;
import org.smartregister.util.AppExecutors;
import org.smartregister.view.activity.DrishtiApplication;

import java.util.ArrayList;
import java.util.List;

public class AvailableOfflineMapsInteractor implements AvailableOfflineMapsContract.Interactor {

    private AppExecutors appExecutors;

    private LocationRepository locationRepository;

    private AvailableOfflineMapsContract.Presenter presenter;

    public AvailableOfflineMapsInteractor(AvailableOfflineMapsContract.Presenter presenter) {
        this.presenter = presenter;
        appExecutors = TaskingLibrary.getInstance().getTaskingLibraryConfiguration().getAppExecutors();
        locationRepository = DrishtiApplication.getInstance().getContext().getLocationRepository();
    }


    @Override
    public void fetchAvailableOAsForMapDownLoad(final List<String> locationIds) {

        Runnable runnable = new Runnable() {
            public void run() {
                List<Location> operationalAreas = locationRepository.getLocationsByIds(locationIds, false);

                appExecutors.mainThread().execute(() -> {
                    presenter.onFetchAvailableOAsForMapDownLoad(populateOfflineMapModelList(operationalAreas));
                });
            }
        };

        appExecutors.diskIO().execute(runnable);

    }

    public List<OfflineMapModel> populateOfflineMapModelList(List<Location> locations) {
        List<OfflineMapModel> offlineMapModels = new ArrayList<>();
        for (Location location: locations) {
            OfflineMapModel offlineMapModel = new OfflineMapModel();
            offlineMapModel.setLocation(location);
            offlineMapModels.add(offlineMapModel);
        }

        return offlineMapModels;
    }

}
