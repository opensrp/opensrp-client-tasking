package org.smartregister.tasking.util;

import org.smartregister.tasking.activity.OfflineMapsActivity;
import org.smartregister.tasking.activity.TaskRegisterActivity;
import org.smartregister.tasking.activity.TaskingHomeActivity;
import org.smartregister.view.activity.BaseRegisterActivity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActivityConfiguration {
    private Class<? extends TaskRegisterActivity> taskRegisterActivity;

    private Class<? extends OfflineMapsActivity> offlineMapsActivity;

    private Class<? extends TaskingHomeActivity> taskingHomeActivity;

    private Class<? extends BaseRegisterActivity> familyRegisterActivity;

}
