package org.smartregister.tasking;

/**
 * Created by samuelgithengi on 6/10/20.
 */
public class TaskingLibrary {

    private static TaskingLibrary instance;

    public void init() {
        instance = new TaskingLibrary();
    }

    public static TaskingLibrary getInstance() {
        return instance;
    }
}
