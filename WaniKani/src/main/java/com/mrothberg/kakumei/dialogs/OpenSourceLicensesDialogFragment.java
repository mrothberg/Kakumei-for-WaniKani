package com.mrothberg.kakumei.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.webkit.WebView;

import com.mrothberg.kakumei.R;

/**
 * Created by Hikari on 8/24/14.
 */
public class OpenSourceLicensesDialogFragment extends DialogFragment {

    private static final String LICENSES_URL = "file:///android_asset/licenses.html";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        WebView webView = (WebView) inflater.inflate(R.layout.dialog_open_source_licenses, null);
        webView.loadUrl(LICENSES_URL);

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.pref_title_open_source_licences)
                .setView(webView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .create();
    }
}
