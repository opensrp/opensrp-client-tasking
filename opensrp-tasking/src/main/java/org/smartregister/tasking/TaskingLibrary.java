package org.smartregister.tasking;

import androidx.annotation.NonNull;

import org.smartregister.util.AppExecutors;

/**
 * Created by samuelgithengi on 6/10/20.
 */
public class TaskingLibrary {

    private static TaskingLibrary instance;
    private AppExecutors appExecutors;

    public void init() {
        instance = new TaskingLibrary();
    }

    public static TaskingLibrary getInstance() {
        return instance;
    }

    @NonNull
    public AppExecutors getAppExecutors() {
        if (appExecutors == null) {
            appExecutors = new AppExecutors();
        }

        return appExecutors;
    }
}
