package org.smartregister.tasking.repository;

import android.text.TextUtils;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteException;

import org.apache.commons.lang3.ArrayUtils;
import org.smartregister.CoreLibrary;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.domain.Client;
import org.smartregister.domain.Task;
import org.smartregister.repository.BaseRepository;
import org.smartregister.tasking.model.StructureDetails;
import org.smartregister.tasking.util.Constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

import static org.smartregister.domain.Task.INACTIVE_TASK_STATUS;
import static org.smartregister.tasking.util.Constants.DatabaseKeys.EVENT_TABLE;
import static org.smartregister.tasking.util.Constants.DatabaseKeys.TASK_TABLE;
import static org.smartregister.tasking.util.TaskingConstants.CONFIGURATION.CODE;
import static org.smartregister.tasking.util.TaskingConstants.DatabaseKeys.ID;
import static org.smartregister.tasking.util.TaskingConstants.DatabaseKeys.PLAN_ID;
import static org.smartregister.tasking.util.TaskingConstants.DatabaseKeys.GROUPID;
import static org.smartregister.tasking.util.TaskingConstants.DatabaseKeys.STATUS;
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
        return "SELECT * FROM structure WHERE " + mainCondition;
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

    public HashMap<String, Client> getLocationToClient(Set<String> personIds) {
        Cursor cursor = null;
        HashMap<String, Client> locationToClientMap = new HashMap<>();
        try {
            String[] params = personIds.toArray(new String[0]);
            cursor = getReadableDatabase().rawQuery(String.format("SELECT structure_family_relationship.structure_uuid, client.json FROM structure_family_relationship INNER JOIN ec_family_member ON structure_family_relationship.family_base_entity_id = ec_family_member.relational_id INNER JOIN client ON ec_family_member.base_entity_id = client.baseEntityId WHERE ec_family_member.base_entity_id IN (%s)"
                    , TextUtils.join(",", Collections.nCopies(personIds.size(), "?")))
                    , params);
            while (cursor.moveToNext()) {
                String structureUuid = cursor.getString(cursor.getColumnIndex("structure_uuid"));
                String clientJson = cursor.getString(cursor.getColumnIndex("json"));

                if (!TextUtils.isEmpty(clientJson)) {
                    Client client = CoreLibrary.getInstance().context().getEventClientRepository().convert(clientJson, Client.class);
                    locationToClientMap.put(structureUuid, client);
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null)
                cursor.close();
        }

        return locationToClientMap;
    }

    public HashMap<String, Set<Task>> getClientTasksByPlanAndGroup(String planId, String groupId) {
        Cursor cursor = null;
        HashMap<String, Set<Task>> tasks = new HashMap<>();
        try {
            String[] params = new String[]{planId, groupId};
            cursor = getReadableDatabase().rawQuery(String.format("SELECT * FROM %s WHERE %s=? AND %s =? AND %s NOT IN (%s)",
                    TASK_TABLE, PLAN_ID, GROUPID, STATUS,
                    TextUtils.join(",", Collections.nCopies(INACTIVE_TASK_STATUS.length, "?"))),
                    ArrayUtils.addAll(params, INACTIVE_TASK_STATUS));
            while (cursor.moveToNext()) {
                Set<Task> taskSet;
                Task task = CoreLibrary.getInstance().context().getTaskRepository().readCursor(cursor);
                if (tasks.containsKey(task.getForEntity())) {
                    taskSet = tasks.get(task.getForEntity());
                } else {
                    taskSet = new HashSet<>();
                }

                taskSet.add(task);
                tasks.put(task.getForEntity(), taskSet);
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null)
                cursor.close();
        }

        return tasks;
    }

}
