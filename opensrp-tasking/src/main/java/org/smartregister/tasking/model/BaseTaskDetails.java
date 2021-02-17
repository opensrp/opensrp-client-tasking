package org.smartregister.tasking.model;

import androidx.annotation.NonNull;

import org.smartregister.domain.Task;

import java.io.Serializable;

/**
 * Created by samuelgithengi on 4/11/19.
 */
public class BaseTaskDetails implements Serializable {

    private String taskId;

    // This holds a data item that can be used to generate
    // the form to which the task should be attributed
    private String taskCode;

    // This holds the entity id eg. the location-id or the person-id
    private String taskEntity;

    private String businessStatus;

    private String taskStatus;

    private String structureId;

    private String familyMemberNames;

    private long authoredOn;

    private Task.TaskPriority priority;

    public BaseTaskDetails(@NonNull String taskId) {
        this.taskId = taskId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskCode() {
        return taskCode;
    }

    public void setTaskCode(String taskCode) {
        this.taskCode = taskCode;
    }

    public String getTaskEntity() {
        return taskEntity;
    }

    public void setTaskEntity(String taskEntity) {
        this.taskEntity = taskEntity;
    }

    public String getBusinessStatus() {
        return businessStatus;
    }

    public void setBusinessStatus(String businessStatus) {
        this.businessStatus = businessStatus;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getStructureId() {
        return structureId;
    }

    public void setStructureId(String structureId) {
        this.structureId = structureId;
    }

    public String getFamilyMemberNames() {
        return familyMemberNames;
    }

    public void setFamilyMemberNames(String familyMemberNames) {
        this.familyMemberNames = familyMemberNames;
    }

    public long getAuthoredOn() {
        return authoredOn;
    }

    public void setAuthoredOn(long authoredOn) {
        this.authoredOn = authoredOn;
    }

    public Task.TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(Task.TaskPriority priority) {
        this.priority = priority;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BaseTaskDetails))
            return false;
        BaseTaskDetails other = (BaseTaskDetails) obj;
        return getTaskId().equals(other.getTaskId());
    }
}
