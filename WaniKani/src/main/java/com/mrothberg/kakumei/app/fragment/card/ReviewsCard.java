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
import android.widget.TextView;

import java.text.SimpleDateFormat;

import com.mrothberg.kakumei.R;
import com.mrothberg.kakumei.apimodels.UserData;
import com.mrothberg.kakumei.app.fragment.DashboardFragment;
import com.mrothberg.kakumei.client.WaniKaniAPIV1Interface;
import com.mrothberg.kakumei.content.receiver.BroadcastIntents;
import com.mrothberg.kakumei.database.DatabaseManager;
import com.mrothberg.kakumei.managers.PrefManager;
import com.mrothberg.kakumei.utils.Utils;
import com.mrothberg.kakumei.widget.RelativeTimeTextView;
import com.mrothberg.kakumei.wkamodels.StudyQueue;

/**
 * Created by xihsa_000 on 3/13/14.
 */
public class ReviewsCard extends Fragment {
    Context context;
    Utils utils;

    View rootView;

    ReviewsCardListener mListener;

    RelativeTimeTextView mNextReview;
    TextView mNextHour;
    TextView mNextDay;

    LinearLayout mMoreReviewsHolder;
    TextView mMoreReviews;

    public void setWaniKaniAPI(WaniKaniAPIV1Interface waniKaniAPI) {
        this.waniKaniAPI = waniKaniAPI;
    }

    private WaniKaniAPIV1Interface waniKaniAPI;
    private BroadcastReceiver mDoLoad = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            waniKaniAPI.getStudyQueue().whenComplete((studyQueue, throwable) -> {
                UserData user = DatabaseManager.getUserV2();

                if(throwable != null) {
                    studyQueue = DatabaseManager.getStudyQueue();
                }

                displayData(user, studyQueue);
            });
        }
    };

    public void setListener(ReviewsCardListener listener, Context context) {
        mListener = listener;
        LocalBroadcastManager.getInstance(context).registerReceiver(mDoLoad,
                new IntentFilter(BroadcastIntents.SYNC()));
    }

    @Override
    public void onCreate(Bundle state) {
        utils = new Utils(getActivity());
        super.onCreate(state);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.card_reviews, null);

        context = getActivity();

        mNextReview = (RelativeTimeTextView) rootView.findViewById(R.id.card_reviews_next_review);
        mNextHour = (TextView) rootView.findViewById(R.id.card_reviews_next_hour);
        mNextDay = (TextView) rootView.findViewById(R.id.card_reviews_next_day);

        mMoreReviewsHolder = (LinearLayout) rootView.findViewById(R.id.card_reviews_more_reviews_holder);
        mMoreReviews = (TextView) rootView.findViewById(R.id.card_reviews_more_reviews);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mDoLoad);
    }

    @SuppressLint("SetTextI18n,SimpleDateFormat")
    private void displayData(UserData user, StudyQueue queue) {
        if (user != null && queue != null) {
            //TODO implement vacation mode
//            if (!user.isVacationModeActive()) {
                mNextHour.setText(queue.reviews_available_next_hour + "");
                mNextDay.setText(queue.reviews_available_next_day + "");

                SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM HH:mm");

                if (queue.reviews_available != 0) {
                    mNextReview.setText(R.string.card_content_reviews_available_now);
                } else {
                    if(queue.next_review_date == 0) {
                        mNextReview.setText(R.string.card_content_reviews_not_available);
                    } else if (PrefManager.isUseSpecificDates()) {
                        mNextReview.setText(sdf.format(queue.getNextReviewDateInMillis()));
                    } else {
                        mNextReview.setReferenceTime(queue.getNextReviewDateInMillis());
                    }
                }

                mListener.onReviewsCardSyncFinishedListener(DashboardFragment.SYNC_RESULT_SUCCESS);
//            } else {
//                // Vacation mode is handled in DashboardFragment
//                mListener.onReviewsCardSyncFinishedListener(DashboardFragment.SYNC_RESULT_SUCCESS);
//            }
        } else {
            mListener.onReviewsCardSyncFinishedListener(DashboardFragment.SYNC_RESULT_FAILED);
        }
    }

    public interface ReviewsCardListener {
        public void onReviewsCardSyncFinishedListener(String result);
    }
}
