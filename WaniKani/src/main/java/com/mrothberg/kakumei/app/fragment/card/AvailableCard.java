package com.mrothberg.kakumei.app.fragment.card;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mrothberg.kakumei.R;
import com.mrothberg.kakumei.app.activity.Browser;
import com.mrothberg.kakumei.app.activity.WebReviewActivity;
import com.mrothberg.kakumei.app.fragment.DashboardFragment;
import com.mrothberg.kakumei.client.WaniKaniAPIV1Interface;
import com.mrothberg.kakumei.content.receiver.BroadcastIntents;
import com.mrothberg.kakumei.database.DatabaseManager;
import com.mrothberg.kakumei.managers.PrefManager;
import com.mrothberg.kakumei.wkamodels.StudyQueue;
import com.mrothberg.kakumei.apimodels.UserData;
import com.mrothberg.kakumei.utils.Utils;

/**
 * Created by xihsa_000 on 3/13/14.
 */
public class AvailableCard extends Fragment {

    public static final int BROWSER_REQUEST = 1;

    View rootView;
    Context context;

    Utils utils;

    ImageView mLessonsGo;
    ImageView mReviewsGo;
    TextView mLessonsAvailable;
    TextView mReviewsAvailable;
    LinearLayout mCard;

    AvailableCardListener mListener;

    private WaniKaniAPIV1Interface waniKaniAPI;

    public void setWaniKaniAPI(WaniKaniAPIV1Interface waniKaniAPIV1Interface) {
        this.waniKaniAPI = waniKaniAPIV1Interface;
    }

    private BroadcastReceiver mDoLoad = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            waniKaniAPI.getStudyQueue().whenComplete((studyQueue, throwable) -> {
                UserData user = DatabaseManager.getUserV2();

                if(throwable != null) {
                    studyQueue = DatabaseManager.getStudyQueue();
                }

                if (user != null && studyQueue != null) {
                    displayData(user, studyQueue);
                } else {
                    mListener.onAvailableCardSyncFinishedListener(DashboardFragment.SYNC_RESULT_FAILED);
                }
            });
        }
    };

    public void setListener(AvailableCardListener listener, Context context) {
        mListener = listener;
        LocalBroadcastManager.getInstance(context).registerReceiver(mDoLoad,
                new IntentFilter(BroadcastIntents.SYNC()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mDoLoad);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle state) {
        utils = new Utils(getActivity());
        super.onCreate(state);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.card_available, parent, false);

        context = getActivity();

        mLessonsGo = (ImageView) rootView.findViewById(R.id.card_available_lessons_go);
        mReviewsGo = (ImageView) rootView.findViewById(R.id.card_available_reviews_go);

        mLessonsGo.setColorFilter(getResources().getColor(R.color.text_gray),
                PorterDuff.Mode.SRC_ATOP);
        mReviewsGo.setColorFilter(getResources().getColor(R.color.text_gray),
                PorterDuff.Mode.SRC_ATOP);

        mLessonsAvailable = (TextView) rootView.findViewById(R.id.card_available_lessons);
        mReviewsAvailable = (TextView) rootView.findViewById(R.id.card_available_reviews);

        mCard = (LinearLayout) rootView.findViewById(R.id.card_available_card);

        setUpParentOnClicks();

        return rootView;
    }

    private void setUpParentOnClicks() {

        mLessonsGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = PrefManager.getWebViewIntent(context);
                intent.setAction(WebReviewActivity.OPEN_ACTION);
                intent.setData(Uri.parse(Browser.LESSON_URL));
                getActivity().startActivityForResult(intent, BROWSER_REQUEST);
            }
        });

        mReviewsGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = PrefManager.getWebViewIntent(context);
                intent.setAction(WebReviewActivity.OPEN_ACTION);
                intent.setData(Uri.parse(Browser.REVIEW_URL));
                getActivity().startActivityForResult(intent, BROWSER_REQUEST);
            }
        });
    }

    private void displayData(UserData user, StudyQueue queue) {
//        if (!user.isVacationModeActive()) {
            if (isAdded()) {
                int lessonsAvailable = queue.lessons_available;
                int reviewsAvailable = queue.reviews_available;
                Resources res = getResources();
                mLessonsAvailable.setText(res.getQuantityString(R.plurals.card_content_available_lessons_capital, lessonsAvailable, lessonsAvailable));
                mReviewsAvailable.setText(res.getQuantityString(R.plurals.card_content_available_reviews_capital, reviewsAvailable, reviewsAvailable));
            }
            mListener.onAvailableCardSyncFinishedListener(DashboardFragment.SYNC_RESULT_SUCCESS);
//        } else {
//            // Vacation mode is handled in DashboardFragment
//            mListener.onAvailableCardSyncFinishedListener(DashboardFragment.SYNC_RESULT_SUCCESS);
//        }
    }

    public interface AvailableCardListener {
        public void onAvailableCardSyncFinishedListener(String result);
    }
}
