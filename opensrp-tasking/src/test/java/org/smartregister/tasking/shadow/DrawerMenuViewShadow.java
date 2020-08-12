package org.smartregister.tasking.shadow;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Created by samuelgithengi on 1/27/20.
 */
@Implements(DrawerMenuViewShadow.class)
public class DrawerMenuViewShadow {

    @Implementation
    public void initializeDrawerLayout() {//Do nothing
    }

    @Implementation
    public void onResume() {//Do nothing
    }

}
