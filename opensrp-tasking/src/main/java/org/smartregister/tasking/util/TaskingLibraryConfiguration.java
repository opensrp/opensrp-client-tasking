package org.smartregister.tasking.util;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.domain.tag.FormTag;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.tasking.BuildConfig;
import org.smartregister.tasking.model.BaseTaskDetails;
import org.smartregister.tasking.model.TaskDetails;
import org.smartregister.util.DatabaseMigrationUtils;
import org.smartregister.util.RecreateECUtil;
import org.smartregister.view.activity.DrishtiApplication;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

/**
 * Created by Ephraim Kigamba - nek.eam@gmail.com on 06-08-2020.
 */
public abstract class TaskingLibraryConfiguration {

    @NonNull
    public abstract Pair<Drawable, String> getActionDrawable(TaskDetails task);

    public abstract int getInterventionLabel();

    @NonNull
    public abstract Float getLocationBuffer();

    public abstract void startImmediateSync();

    public abstract boolean validateFarStructures();

    public abstract int getResolveLocationTimeoutInSeconds();

    public abstract String getAdminPasswordNotNearStructures();


    public abstract boolean isFocusInvestigation();

    public abstract boolean isMDA();

    public abstract String getCurrentLocationId();

    public abstract String getCurrentOperationalAreaId();

    public abstract Integer getDatabaseVersion();

    public abstract void tagEventTaskDetails(List<Event> events, SQLiteDatabase sqLiteDatabase);

    /**
     * Uses the server setting "DISPLAY_DISTANCE_SCALE" to determine whether to display the distance scale
     * If this variable is not available on the server the value is retrieved from BuildConfig.DISPLAY_DISTANCE_SCALE
     *
     * @return displayDistanceScale
     */
    public abstract Boolean displayDistanceScale();

    public abstract String getFormName(@NonNull String encounterType, @Nullable String taskCode);

    public abstract boolean resetTaskInfo(@NonNull SQLiteDatabase db, @NonNull BaseTaskDetails taskDetails);

    public abstract boolean archiveClient(String baseEntityId, boolean isFamily);

}
