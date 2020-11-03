package org.smartregister.tasking.view.dialog;

/**
 * Created by Ephraim Kigamba - nek.eam@gmail.com on 27-07-2020.
 */

import android.view.View;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import androidx.annotation.Nullable;
import org.smartregister.tasking.R;
import org.smartregister.view.activity.BaseRegisterActivity;
import timber.log.Timber;

@SuppressLint({"ValidFragment"})
public class NoMatchDialogFragment extends DialogFragment {
    private final NoMatchDialogFragment.NoMatchDialogActionHandler noMatchDialogActionHandler = new NoMatchDialogFragment.NoMatchDialogActionHandler();
    private final BaseRegisterActivity baseRegisterActivity;
    private final String uniqueId;

    public NoMatchDialogFragment(BaseRegisterActivity baseRegisterActivity, String uniqueId) {
        this.uniqueId = uniqueId;
        this.baseRegisterActivity = baseRegisterActivity;
    }

    @Nullable
    public static NoMatchDialogFragment launchDialog(BaseRegisterActivity activity, String dialogTag, String whoAncId) {
        NoMatchDialogFragment noMatchDialogFragment = new NoMatchDialogFragment(activity, whoAncId);
        if (activity != null) {
            FragmentTransaction fragmentTransaction = activity.getFragmentManager().beginTransaction();
            Fragment prev = activity.getFragmentManager().findFragmentByTag(dialogTag);
            if (prev != null) {
                fragmentTransaction.remove(prev);
            }

            fragmentTransaction.addToBackStack((String)null);
            noMatchDialogFragment.show(fragmentTransaction, dialogTag);
            return noMatchDialogFragment;
        } else {
            return null;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setStyle(1, 16973939);
    }

    @Nullable
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup dialogView = (ViewGroup)inflater.inflate(R.layout.dialog_no_match, container, false);
        Button cancel = (Button)dialogView.findViewById(R.id.cancel_no_match_dialog);
        cancel.setOnClickListener(this.noMatchDialogActionHandler);
        Button advancedSearch = (Button)dialogView.findViewById(R.id.go_to_advanced_search);
        advancedSearch.setOnClickListener(this.noMatchDialogActionHandler);
        return dialogView;
    }

    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);
        this.baseRegisterActivity.setSearchTerm("");
    }

    private class NoMatchDialogActionHandler implements OnClickListener {
        private NoMatchDialogActionHandler() {
        }

        public void onClick(View view) {
            if (view.getId() == R.id.cancel_no_match_dialog) {
                NoMatchDialogFragment.this.dismiss();
                NoMatchDialogFragment.this.baseRegisterActivity.setSearchTerm("");
            } else if (view.getId() == R.id.go_to_advanced_search) {
                NoMatchDialogFragment.this.baseRegisterActivity.setSearchTerm("");
                this.goToAdvancedSearch(NoMatchDialogFragment.this.uniqueId);
                NoMatchDialogFragment.this.baseRegisterActivity.setSelectedBottomBarMenuItem(R.id.action_search);
                NoMatchDialogFragment.this.dismiss();
            }

        }

        private void goToAdvancedSearch(String uniqueId) {
            Timber.i(uniqueId, new Object[0]);
        }
    }
}
