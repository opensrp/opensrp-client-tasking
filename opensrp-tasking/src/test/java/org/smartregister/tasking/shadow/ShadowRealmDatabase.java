package org.smartregister.tasking.shadow;

import android.content.Context;

import androidx.annotation.NonNull;

import org.mockito.Mockito;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import io.ona.kujaku.data.realm.RealmDatabase;

/**
 * Created by Ephraim Kigamba - nek.eam@gmail.com on 14-08-2020.
 */
@Implements(RealmDatabase.class)
public class ShadowRealmDatabase {

    @Implementation
    public static RealmDatabase init(@NonNull Context context) {
        return Mockito.mock(RealmDatabase.class);
    }
}
