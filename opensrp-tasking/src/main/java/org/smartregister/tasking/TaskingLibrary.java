package org.smartregister.tasking;

/**
 * Created by samuelgithengi on 6/10/20.
 */
public class TaskingLibrary {

    private static TaskingLibrary instance;

    private float locationBuffer;

    public void init() {
        instance = new TaskingLibrary();
    }

    public static TaskingLibrary getInstance() {
        return instance;
    }


    public float getLocationBuffer() {
        return locationBuffer;
    }

    public void setLocationBuffer(float locationBuffer) {
        this.locationBuffer = locationBuffer;
    }
}
