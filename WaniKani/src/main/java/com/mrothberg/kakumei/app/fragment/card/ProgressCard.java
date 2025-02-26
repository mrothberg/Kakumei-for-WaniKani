package com.mrothberg.kakumei.app.fragment.card;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mrothberg.kakumei.R;
import com.mrothberg.kakumei.app.fragment.DashboardFragment;
import com.mrothberg.kakumei.client.WaniKaniAPIV1Interface;
import com.mrothberg.kakumei.content.receiver.BroadcastIntents;
import com.mrothberg.kakumei.database.DatabaseManager;
import com.mrothberg.kakumei.wkamodels.LevelProgression;
import com.mrothberg.kakumei.apimodels.UserData;
import com.mrothberg.kakumei.utils.Utils;

/**
 * Created by xihsa_000 on 3/13/14.
 */
public class ProgressCard extends Fragment {
    Utils utils;

    View rootView;

    Context context;

    ProgressCardListener mListener;

    TextView mUserLevel;
    TextView mRadicalPercentage;
    TextView mRadicalsProgress;
    TextView mRadicalsTotal;
    TextView mKanjiPercentage;
    TextView mKanjiProgress;
    TextView mKanjiTotal;

    ProgressBar mRadicalProgressBar;
    ProgressBar mKanjiProgressBar;

    LinearLayout mCard;

    WaniKaniAPIV1Interface waniKaniAPI;

    public void setWaniKaniAPI(WaniKaniAPIV1Interface waniKaniAPI) {
        this.waniKaniAPI = waniKaniAPI;
    }

    public void setListener(ProgressCardListener listener, Context context) {
        mListener = listener;
        LocalBroadcastManager.getInstance(context).registerReceiver(mDoLoad,
                new IntentFilter(BroadcastIntents.SYNC()));
    }

    private BroadcastReceiver mDoLoad = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            load();
        }
    };

    @Override
    public void onCreate(Bundle state) {
        utils = new Utils(getActivity());
        super.onCreate(state);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.card_progress, null);

        context = getActivity();

        mUserLevel = (TextView) rootView.findViewById(R.id.card_progress_level);
        mRadicalPercentage = (TextView) rootView.findViewById(R.id.card_progress_radicals_percentage);
        mRadicalsProgress = (TextView) rootView.findViewById(R.id.card_progress_radicals_progress);
        mRadicalsTotal = (TextView) rootView.findViewById(R.id.card_progress_radicals_total);
        mKanjiPercentage = (TextView) rootView.findViewById(R.id.card_progress_kanji_percentage);
        mKanjiProgress = (TextView) rootView.findViewById(R.id.card_progress_kanji_progress);
        mKanjiTotal = (TextView) rootView.findViewById(R.id.card_progress_kanji_total);

        mRadicalProgressBar = (ProgressBar) rootView.findViewById(R.id.card_progress_radicals_progress_bar);
        mKanjiProgressBar = (ProgressBar) rootView.findViewById(R.id.card_progress_kanji_progress_bar);

        mCard = (LinearLayout) rootView.findViewById(R.id.card_progress_card);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mDoLoad);
    }

    //TODO: progress card no title calls this when progress card clicked on.
    //TODO: rethink user stuff here
    public void load() {
        UserData user = DatabaseManager.getUserV2();

        waniKaniAPI.getCurrentLevelProgression().whenComplete((levelProgression, throwable) -> {

            if(throwable != null) {
                levelProgression = DatabaseManager.getLevelProgression();
            }

            if(user != null && levelProgression != null) {
                displayData(user, levelProgression);
            } else {
                mListener.onProgressCardSyncFinishedListener(DashboardFragment.SYNC_RESULT_FAILED);
            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void displayData(UserData user, LevelProgression progression) {
        mUserLevel.setText(user.level + "");
        mRadicalPercentage.setText(progression.getRadicalsPercentage() + "");
        mRadicalsProgress.setText(progression.radicals_progress + "");
        mRadicalsTotal.setText(progression.radicals_total + "");
        mKanjiPercentage.setText(progression.getKanjiPercentage() + "");
        mKanjiProgress.setText(progression.kanji_progress + "");
        mKanjiTotal.setText(progression.kanji_total + "");

        mRadicalProgressBar.setProgress(progression.getRadicalsPercentage());
        mKanjiProgressBar.setProgress(progression.getKanjiPercentage());

        mListener.onProgressCardSyncFinishedListener(DashboardFragment.SYNC_RESULT_SUCCESS);
    }

    public interface ProgressCardListener {
        public void onProgressCardSyncFinishedListener(String result);
    }
}
