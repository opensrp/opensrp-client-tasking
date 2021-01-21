package org.smartregister.tasking.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.smartregister.tasking.R;
import org.smartregister.tasking.TaskingLibrary;
import org.smartregister.tasking.model.TaskDetails;
import org.smartregister.tasking.viewholder.PrioritizedTaskRegisterViewHolder;
import org.smartregister.tasking.viewholder.TaskRegisterViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by samuelgithengi on 3/20/19.
 */
public class TaskRegisterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<TaskDetails> taskDetails = new ArrayList<>();

    private Context context;

    private View.OnClickListener registerActionHandler;

    private boolean isV2Design;

    public TaskRegisterAdapter(Context context, View.OnClickListener registerActionHandler) {
        this.context = context;
        this.registerActionHandler = registerActionHandler;
        this.isV2Design = TaskingLibrary.getInstance().getTaskingLibraryConfiguration().getTasksRegisterConfiguration().isV2Design();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            View view = LayoutInflater.from(context).inflate(R.layout.task_register_row, parent, false);
            return new TaskRegisterViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.prioritized_task_register_row, parent, false);
            return new PrioritizedTaskRegisterViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        /*TaskDetails task = taskDetails.get(position);
        Float distance = task.getDistanceFromUser();
        String name = task.getStructureName();
        String action = null;
        boolean hasIcon = false;
        if (Intervention.IRS.equals(task.getTaskCode())) {
            if (name == null) {
                name = task.getFamilyName() != null ? task.getFamilyName() : task.getStructureName() != null ? task.getStructureName() : context.getString(R.string.unenumerated_structure);
            }
            action = context.getString(R.string.record_status);
        } else if (Intervention.MOSQUITO_COLLECTION.equals(task.getTaskCode())) {
            name = context.getString(R.string.mosquito_collection_point);
            action = context.getString(R.string.record_mosquito_collection);
        } else if (Intervention.LARVAL_DIPPING.equals(task.getTaskCode())) {
            name = context.getString(R.string.larval_breeding_site);
            action = context.getString(R.string.record_larvacide);
        } else if (Intervention.BCC.equals(task.getTaskCode())) {
            viewHolder.setIcon(R.drawable.ic_bcc);
            name = context.getString(R.string.bcc);
            action = context.getString(R.string.record_bcc);
            hasIcon = true;
        } else if (Intervention.CASE_CONFIRMATION.equals(task.getTaskCode()) && task.getTaskCount() == null) {
            viewHolder.setIcon(R.drawable.ic_classification_details);
            viewHolder.setItemViewListener(task, registerActionHandler);
            name = context.getString(R.string.classification_details);
            action = context.getString(R.string.view);
            hasIcon = true;
        } else if (Intervention.PAOT.equals(task.getTaskCode())) {
            name = context.getString(R.string.card_view_paot);
            if (task.getBusinessStatus() != null) {
                action = CardDetailsUtil.getTranslatedBusinessStatus(task.getBusinessStatus()).replaceAll(" ", "\n");
            }
        } else {
            name = NOT_ELIGIBLE.equals(task.getBusinessStatus()) ? context.getString(R.string.ineligible_location) : task.getFamilyName();
            if (name == null) {
                name = task.getStructureName() != null ? task.getStructureName() : context.getString(R.string.unenumerated_structure);
            }
            if (task.getBusinessStatus() != null) {
                action = CardDetailsUtil.getTranslatedBusinessStatus(task.getBusinessStatus()).replaceAll(" ", "\n");
            }
        }
        viewHolder.setTaskName(name);
        CardDetails cardDetails = new CardDetails(task.getBusinessStatus());
        if (Task.TaskStatus.COMPLETED.name().equals(task.getTaskStatus())) {
            if (task.getBusinessStatus() != null) {
                action = CardDetailsUtil.getTranslatedBusinessStatus(task.getBusinessStatus()).replaceAll(" ", "\n");
            }
            CardDetailsUtil.formatCardDetails(cardDetails);
        }
        viewHolder.setTaskAction(action, task, cardDetails, registerActionHandler);
        viewHolder.setDistanceFromStructure(distance, task.isDistanceFromCenter());
        viewHolder.setTaskDetails(task.getBusinessStatus(), task.getTaskDetails());
        if (hasIcon) {
            viewHolder.hideDistanceFromStructure();
        } else {
            viewHolder.hideIcon();
        }

        if (StringUtils.isNotEmpty(task.getHouseNumber())) {
            viewHolder.showHouseNumber();
            viewHolder.setHouseNumber(context.getString(R.string.numero_sign) + " " + task.getHouseNumber());
        } else {
            viewHolder.hideHouseNumber();
        }*/

        TaskingLibrary.getInstance().getTaskingLibraryConfiguration().onTaskRegisterBindViewHolder(context, viewHolder, registerActionHandler, taskDetails.get(position), position);
    }

    @Override
    public int getItemCount() {
        return taskDetails.size();
    }

    public int getOverdueTasksCount() {
        int overdueTasksCount = 0;
        for (TaskDetails taskDetails: taskDetails) {
            // TODO: Check if the task is overdue here
        }

        return overdueTasksCount;
    }

    public void setTaskDetails(List<TaskDetails> taskDetails) {
        this.taskDetails = taskDetails;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return isV2Design ? 2 : 1;
    }


}
