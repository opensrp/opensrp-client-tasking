package org.smartregister.tasking.util;

import org.smartregister.tasking.activity.OfflineMapsActivity;
import org.smartregister.tasking.activity.TaskRegisterActivity;
import org.smartregister.tasking.activity.TaskingMapActivity;
import org.smartregister.view.activity.BaseRegisterActivity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActivityConfiguration {
    private Class<? extends TaskRegisterActivity> taskRegisterActivity;

    private Class<? extends OfflineMapsActivity> offlineMapsActivity;

    private Class<? extends TaskingMapActivity> taskingMapActivity;

    private Class<? extends BaseRegisterActivity> familyRegisterActivity;

}
