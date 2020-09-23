package com.mrothberg.kakumei.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.mrothberg.kakumei.R;
import com.mrothberg.kakumei.managers.PrefManager;

/**
 * Created by Hikari on 8/18/14.
 */
public class LegendDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.content_radicals_legend)
                .setView(R.layout.dialog_legend)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                        PrefManager.setLegendLearned(true);
                    }
                })
                .create();
    }
}
