package org.smartregister.tasking.receiver;

import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.domain.Task;
import org.smartregister.tasking.BaseUnitTest;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.smartregister.AllConstants.INTENT_KEY.TASK_GENERATED;
import static org.smartregister.AllConstants.INTENT_KEY.TASK_GENERATED_EVENT;

public class TaskGenerationReceiverTest extends BaseUnitTest {

    private TaskGenerationReceiver taskGenerationReceiver;

    @Mock
    private TaskGenerationReceiver.TaskGenerationCallBack taskGenerationCallBack;

    @Mock
    private LocalBroadcastManager localBroadcastManager;

    @Mock
    private Context context;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ReflectionHelpers.setStaticField(LocalBroadcastManager.class, "mInstance", localBroadcastManager);
        taskGenerationReceiver = spy(new TaskGenerationReceiver(taskGenerationCallBack));
    }

    @Test
    public void testOnReceiverShouldInvokeOnTaskCreatedIfTaskIsPresent() {
        Task task = new Task();
        task.setPlanIdentifier(UUID.randomUUID().toString());
        task.setGroupIdentifier(UUID.randomUUID().toString());
        task.setForEntity(UUID.randomUUID().toString());
        task.setStatus(Task.TaskStatus.READY);
        task.setPriority(Task.TaskPriority.ROUTINE);

        Intent intent = new Intent(TASK_GENERATED_EVENT);
        intent.putExtra(TASK_GENERATED, task);

        taskGenerationReceiver.onReceive(context, intent);

        verify(taskGenerationCallBack, only()).onTaskCreatedOrUpdated(eq(task));

        verify(localBroadcastManager, only()).unregisterReceiver(eq(taskGenerationReceiver));
    }


    @Test
    public void testOnReceiverShouldNotInvokeOnTaskCreatedIfTaskAbsent() {
        Intent intent = new Intent(TASK_GENERATED_EVENT);
        taskGenerationReceiver.onReceive(context, intent);

        verify(taskGenerationCallBack, never()).onTaskCreatedOrUpdated(any(Task.class));

        verify(localBroadcastManager, only()).unregisterReceiver(eq(taskGenerationReceiver));
    }

    @After
    public void tearDown() {
        ReflectionHelpers.setStaticField(LocalBroadcastManager.class, "mInstance", null);
    }
}
