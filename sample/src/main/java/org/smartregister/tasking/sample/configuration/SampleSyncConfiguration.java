package org.smartregister.tasking.sample.configuration;

import org.smartregister.SyncConfiguration;
import org.smartregister.SyncFilter;
import org.smartregister.view.activity.BaseLoginActivity;

import java.util.List;

public class SampleSyncConfiguration extends SyncConfiguration {
    @Override
    public int getSyncMaxRetries() {
        return 0;
    }

    @Override
    public SyncFilter getSyncFilterParam() {
        return null;
    }

    @Override
    public String getSyncFilterValue() {
        return null;
    }

    @Override
    public int getUniqueIdSource() {
        return 0;
    }

    @Override
    public int getUniqueIdBatchSize() {
        return 0;
    }

    @Override
    public int getUniqueIdInitialBatchSize() {
        return 0;
    }

    @Override
    public SyncFilter getEncryptionParam() {
        return null;
    }

    @Override
    public boolean updateClientDetailsTable() {
        return false;
    }

    @Override
    public List<String> getSynchronizedLocationTags() {
        return null;
    }

    @Override
    public String getTopAllowedLocationLevel() {
        return null;
    }

    @Override
    public String getOauthClientId() {
        return null;
    }

    @Override
    public String getOauthClientSecret() {
        return null;
    }

    @Override
    public Class<? extends BaseLoginActivity> getAuthenticationActivity() {
        return null;
    }
}
