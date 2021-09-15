package org.smartregister.tasking.repository;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.smartregister.tasking.BaseUnitTest;
import org.smartregister.tasking.model.StructureDetails;

import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.smartregister.repository.BaseRepository.TYPE_Synced;
import static org.smartregister.repository.BaseRepository.TYPE_Task_Unprocessed;
import static org.smartregister.tasking.util.TaskingConstants.Intervention.CASE_CONFIRMATION;

public class TaskingRepositoryTest extends BaseUnitTest {

    private TaskingRepository taskingRepository;

    @Mock
    private SQLiteDatabase database;

    @Mock
    private Cursor cursor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        taskingRepository = spy(new TaskingRepository());
        doReturn(database).when(taskingRepository).getReadableDatabase();
    }

    @Test
    public void testGetStructureNameShouldReturnMapIfFound() {
        String parentId = UUID.randomUUID().toString();

        doReturn("Ampa").when(cursor).getString(0);

        doAnswer(new Answer() {
            int count = -1;

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return ++count == 0;
            }
        }).when(cursor).moveToNext();

        doReturn(cursor).when(database).rawQuery(anyString(), eq(new String[]{parentId}));
        Map<String, StructureDetails> structureDetailsMap = taskingRepository.getStructureName(parentId);
        assertNotNull(structureDetailsMap);
        assertFalse(structureDetailsMap.isEmpty());
        assertNotNull(structureDetailsMap.get("Ampa"));
    }

    @Test
    public void testGetIndexCaseStructureShouldReturnStringIfFound() {
        String planId = UUID.randomUUID().toString();
        String structureId = UUID.randomUUID().toString();

        doReturn(structureId).when(cursor).getString(0);

        doAnswer(new Answer() {
            int count = -1;

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return ++count == 0;
            }
        }).when(cursor).moveToNext();

        doReturn(cursor).when(database).rawQuery(anyString(), eq(new String[]{planId, CASE_CONFIRMATION}));
        String result = taskingRepository.getIndexCaseStructure(planId);
        assertNotNull(result);
        assertEquals(structureId, result);
    }

    @Test
    public void testCheckSyncedShouldReturnCount() {
        doReturn(1).when(cursor).getCount();

        doAnswer(new Answer() {
            int count = -1;

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return ++count == 0;
            }
        }).when(cursor).moveToNext();

        doReturn(cursor).when(database).rawQuery(anyString(), eq(new String[]{TYPE_Synced, TYPE_Synced, TYPE_Task_Unprocessed, TYPE_Synced, TYPE_Synced}));
        int result = taskingRepository.checkSynced();
        assertEquals(1, result);
    }
}