package org.smartregister.tasking.viewholder;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import org.smartregister.tasking.R;
import org.smartregister.tasking.TaskingLibrary;
import org.smartregister.tasking.model.CardDetails;
import org.smartregister.tasking.model.TaskDetails;
import org.smartregister.tasking.util.Constants;
import org.smartregister.tasking.util.PreferencesUtil;


/**
 * Created by samuelgithengi on 3/12/19.
 */
public class PrioritizedTaskRegisterViewHolder extends RecyclerView.ViewHolder {

    private Context context;

    private ImageView taskIconImageView;

    private ImageView taskActionImageView;

    private TextView taskEntityNameTv;

    private TextView taskTitleTv;

    private TextView taskRelativeTimeAssignedTv;

    private TextView taskActionDescriptionTv;

    private PreferencesUtil prefsUtil = PreferencesUtil.getInstance();

    public PrioritizedTaskRegisterViewHolder(@NonNull View itemView) {
        super(itemView);
        context = itemView.getContext();
        taskIconImageView = itemView.findViewById(R.id.prioritized_register_row_task_icon);
        taskActionImageView = itemView.findViewById(R.id.prioritized_register_row_iv_row_action);
        taskEntityNameTv = itemView.findViewById(R.id.prioritized_register_row_task_name_or_person);
        taskTitleTv = itemView.findViewById(R.id.prioritized_register_row_task_title);
        taskActionDescriptionTv = itemView.findViewById(R.id.prioritized_register_row_txt_action_description);
        taskRelativeTimeAssignedTv = itemView.findViewById(R.id.prioritized_register_row_task_assigned_relative_time);
    }


    public void setAction(@DrawableRes int iconResource, String actionDescription, View.OnClickListener onClickListener) {
        taskActionImageView.setImageDrawable(context.getResources().getDrawable(iconResource));
        taskActionDescriptionTv.setText(actionDescription);
        taskActionImageView.setOnClickListener(onClickListener);
        taskActionDescriptionTv.setOnClickListener(onClickListener);
    }


    public void setTaskIcon(@DrawableRes int iconResource) {
        taskIconImageView.setImageDrawable(context.getResources().getDrawable(iconResource));
    }

    public void setTaskEntityName(String taskName) {
        taskEntityNameTv.setText(taskName);
    }


    public void setTaskTitle(String taskTitle) {
        taskTitleTv.setText(taskTitle);
    }

    public void setTaskRelativeTimeAssigned(String taskAssignedTime) {
        taskRelativeTimeAssignedTv.setText(taskAssignedTime);
    }

    public void setItemViewListener(TaskDetails task, View.OnClickListener onClickListener) {
        itemView.setOnClickListener(onClickListener);
        itemView.setTag(R.id.task_details, task);
    }


    private Pair<Drawable, String> getActionDrawable(TaskDetails task) {
        return TaskingLibrary.getInstance().getTaskingLibraryConfiguration().getActionDrawable(context, task);
    }
}
