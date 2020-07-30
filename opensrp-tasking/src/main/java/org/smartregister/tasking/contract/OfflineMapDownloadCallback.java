package org.smartregister.tasking.contract;

import org.smartregister.tasking.model.OfflineMapModel;

public interface OfflineMapDownloadCallback {

    void onMapDownloaded(OfflineMapModel offlineMapModel);

    void onOfflineMapDeleted(OfflineMapModel offlineMapModel);
}
