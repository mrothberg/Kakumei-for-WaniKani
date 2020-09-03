package tr.xip.wanikani.app.fragment.card;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import tr.xip.wanikani.R;
import tr.xip.wanikani.app.fragment.DashboardFragment;
import tr.xip.wanikani.client.WaniKaniAPIV1Interface;
import tr.xip.wanikani.content.receiver.BroadcastIntents;
import tr.xip.wanikani.database.DatabaseManager;
import tr.xip.wanikani.managers.PrefManager;
import tr.xip.wanikani.wkamodels.StudyQueue;
import tr.xip.wanikani.apimodels.UserData;
import tr.xip.wanikani.utils.Utils;
import tr.xip.wanikani.widget.RelativeTimeTextView;

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
                    if (PrefManager.isUseSpecificDates()) {
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
