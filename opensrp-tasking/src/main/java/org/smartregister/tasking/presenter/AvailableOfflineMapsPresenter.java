package org.smartregister.tasking.presenter;

import org.smartregister.tasking.contract.AvailableOfflineMapsContract;
import org.smartregister.tasking.interactor.AvailableOfflineMapsInteractor;
import org.smartregister.tasking.model.OfflineMapModel;

import java.util.List;

public class AvailableOfflineMapsPresenter implements AvailableOfflineMapsContract.Presenter {

    private AvailableOfflineMapsContract.Interactor interactor;
    private AvailableOfflineMapsContract.View view;

    public AvailableOfflineMapsPresenter(AvailableOfflineMapsContract.View view) {
        this.view = view;
        this.interactor = new AvailableOfflineMapsInteractor(this);
    }

    @Override
    public void fetchAvailableOAsForMapDownLoad(List<String> locationIds) {
        interactor.fetchAvailableOAsForMapDownLoad(locationIds);
    }

    @Override
    public void onFetchAvailableOAsForMapDownLoad(List<OfflineMapModel> offlineMapModels) {
        view.setOfflineMapModelList(offlineMapModels);
    }

    @Override
    public void onDownloadStarted(String operationalAreaId) {
        view.disableCheckBox(operationalAreaId);

    }

    @Override
    public void onDownloadComplete(String operationalAreaId) {
        view.moveDownloadedOAToDownloadedList(operationalAreaId);
    }

    @Override
    public void onDownloadStopped(String operationalAreaId) {
        view.removeOperationalAreaToDownload(operationalAreaId);
        view.enableCheckBox(operationalAreaId);
    }

}
