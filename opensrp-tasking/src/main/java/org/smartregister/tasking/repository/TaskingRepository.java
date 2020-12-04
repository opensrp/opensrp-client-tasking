package org.smartregister.tasking.repository;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteException;

import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.repository.BaseRepository;
import org.smartregister.tasking.model.StructureDetails;
import org.smartregister.tasking.util.Constants;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static org.smartregister.tasking.util.Constants.DatabaseKeys.EVENT_TABLE;
import static org.smartregister.tasking.util.Constants.DatabaseKeys.TASK_TABLE;
import static org.smartregister.tasking.util.TaskingConstants.CONFIGURATION.CODE;
import static org.smartregister.tasking.util.TaskingConstants.DatabaseKeys.ID;
import static org.smartregister.tasking.util.TaskingConstants.DatabaseKeys.PLAN_ID;
import static org.smartregister.tasking.util.TaskingConstants.DatabaseKeys.STRUCTURES_TABLE;
import static org.smartregister.tasking.util.TaskingConstants.DatabaseKeys.STRUCTURE_SYNC_STATUS;
import static org.smartregister.tasking.util.TaskingConstants.DatabaseKeys.SYNC_STATUS;
import static org.smartregister.tasking.util.TaskingConstants.DatabaseKeys.TASK_SYNC_STATUS;
import static org.smartregister.tasking.util.TaskingConstants.Intervention.CASE_CONFIRMATION;
import static org.smartregister.tasking.util.TaskingConstants.Tables.CLIENT_TABLE;
import static org.smartregister.tasking.util.TaskingConstants.Tables.STRUCTURE_TABLE;

public class TaskingRepository extends BaseRepository {

    public Map<String, StructureDetails> getStructureName(String parentId) {
        Cursor cursor = null;
        Map<String, StructureDetails> structureNames = new HashMap<>();
        try {
            String query = getStructureNamesSelect(String.format("%s=?",
                    Constants.DatabaseKeys.PARENT_ID)).concat(String.format(" GROUP BY %s.%s", STRUCTURES_TABLE, ID));
            Timber.d(query);
            cursor = getReadableDatabase().rawQuery(query, new String[]{parentId});
            while (cursor.moveToNext()) {
                structureNames.put(cursor.getString(0), new StructureDetails(cursor.getString(1), cursor.getString(2)));
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return structureNames;
    }

    protected String getStructureNamesSelect(String mainCondition) {
        return "";
    }


    public String getIndexCaseStructure(String planId) {
        Cursor cursor = null;
        String structureId = null;
        try {
            String query = getMemberTasksSelect(String.format("%s=? AND %s=? ",
                    PLAN_ID, CODE), new String[]{});
            Timber.d(query);
            cursor = getReadableDatabase().rawQuery(query, new String[]{planId, CASE_CONFIRMATION});
            if (cursor.moveToNext()) {
                structureId = cursor.getString(0);
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return structureId;
    }

    protected String getMemberTasksSelect(String mainCondition, String[] memberColumns) {
        SmartRegisterQueryBuilder queryBuilder = new SmartRegisterQueryBuilder();
        queryBuilder.selectInitiateMainTable(Constants.DatabaseKeys.STRUCTURES_TABLE, memberColumns, Constants.DatabaseKeys.ID);
        queryBuilder.customJoin(String.format(" JOIN %s ON %s.%s = %s.%s ",
                Constants.TABLE_NAME.FAMILY_MEMBER, Constants.TABLE_NAME.FAMILY_MEMBER, Constants.DatabaseKeys.STRUCTURE_ID, Constants.DatabaseKeys.STRUCTURES_TABLE, Constants.DatabaseKeys.ID));
        queryBuilder.customJoin(String.format(" JOIN %s ON %s.%s = %s.%s ",
                Constants.Tables.TASK_TABLE, Constants.Tables.TASK_TABLE, Constants.DatabaseKeys.FOR, Constants.TABLE_NAME.FAMILY_MEMBER, Constants.DatabaseKeys.BASE_ENTITY_ID));
        return queryBuilder.mainCondition(mainCondition);
    }

    public int checkSynced() {
        String syncQuery = String.format("SELECT %s FROM %s WHERE %s <> ?\n", SYNC_STATUS, CLIENT_TABLE, SYNC_STATUS) +
                "UNION ALL\n" +
                String.format("SELECT %s FROM %s WHERE %s <> ? AND %s <> ?\n", SYNC_STATUS, EVENT_TABLE, SYNC_STATUS, SYNC_STATUS) +
                "UNION ALL\n" +
                String.format("SELECT %s FROM %s WHERE %s <> ?\n", TASK_SYNC_STATUS, TASK_TABLE, TASK_SYNC_STATUS) +
                "UNION ALL\n" +
                String.format("SELECT %s FROM %s WHERE %s <> ?\n", STRUCTURE_SYNC_STATUS, STRUCTURE_TABLE, STRUCTURE_SYNC_STATUS);

        try (Cursor syncCursor = getReadableDatabase().rawQuery(syncQuery, new String[]{TYPE_Synced, TYPE_Synced, TYPE_Task_Unprocessed, TYPE_Synced, TYPE_Synced})) {
            if (syncCursor != null) {
                return syncCursor.getCount();
            }
        } catch (SQLiteException e) {
            Timber.e(e);
        }
        return -1;
    }

}
