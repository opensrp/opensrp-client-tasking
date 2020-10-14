package org.smartregister.tasking.configuration;

import org.smartregister.tasking.util.TaskingLibraryConfiguration;

/**
 * Created by Ephraim Kigamba - nek.eam@gmail.com on 06-10-2020.
 */
public class TaskRegisterV2Configuration implements TaskingLibraryConfiguration.TaskRegisterConfiguration {
    @Override
    public boolean isV2Design() {
        return true;
    }

    @Override
    public boolean showGroupedTasks() {
        return false;
    }
}
