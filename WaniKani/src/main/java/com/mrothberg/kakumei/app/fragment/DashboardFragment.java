package com.mrothberg.kakumei.app.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.io.Serializable;
import java.util.List;

import com.mrothberg.kakumei.R;
import com.mrothberg.kakumei.app.activity.MainActivity;
import com.mrothberg.kakumei.app.activity.ProgressDetailsActivity;
import com.mrothberg.kakumei.app.fragment.card.AvailableCard;
import com.mrothberg.kakumei.app.fragment.card.CriticalItemsCard;
import com.mrothberg.kakumei.app.fragment.card.MessageCard;
import com.mrothberg.kakumei.app.fragment.card.NotificationsCard;
import com.mrothberg.kakumei.app.fragment.card.ProgressCard;
import com.mrothberg.kakumei.app.fragment.card.RecentUnlocksCard;
import com.mrothberg.kakumei.app.fragment.card.ReviewsCard;
import com.mrothberg.kakumei.app.fragment.card.SRSCard;
import com.mrothberg.kakumei.app.fragment.card.VacationModeCard;
import com.mrothberg.kakumei.client.WaniKaniAPIV1Interface;
import com.mrothberg.kakumei.content.receiver.BroadcastIntents;
import com.mrothberg.kakumei.database.DatabaseManager;
import com.mrothberg.kakumei.managers.PrefManager;
import com.mrothberg.kakumei.wkamodels.Notification;
import com.mrothberg.kakumei.apimodels.UserData;

public class DashboardFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener,
        AvailableCard.AvailableCardListener,
        ReviewsCard.ReviewsCardListener,
        SRSCard.StatusCardListener,
        ProgressCard.ProgressCardListener,
        RecentUnlocksCard.RecentUnlocksCardListener,
        CriticalItemsCard.CriticalItemsCardListener,
        MessageCard.MessageCardListener,
        View.OnClickListener {

    public static final String SYNC_RESULT_SUCCESS = "success";
    public static final String SYNC_RESULT_FAILED = "failed";
    public static final String WANIKANI_API_EXTRA = "WaniKaniAPI";
    View rootView;
    AppCompatActivity activity;
    boolean isAvailableCardSynced = false;
    boolean isReviewsCardSynced = false;
    boolean isStatusCardSynced = false;
    boolean isProgressCardSynced = false;
    boolean isRecentUnlocksCardSynced = false;
    boolean isCriticalItemsCardSynced = false;
    boolean isAvailableCardSyncedSuccess = false;
    boolean isReviewsCardSyncedSuccess = false;
    boolean isStatusCardSyncedSuccess = false;
    boolean isProgressCardSyncedSuccess = false;
    boolean isRecentUnlocksCardSyncedSuccess = false;
    boolean isCriticalItemsCardSyncedSuccess = false;
    LinearLayout mAvailableHolder;
    CardView mReviewsHolder;
    CardView mProgressHolder;
    LinearLayout mCriticalItemsFragmentHolder;
    LinearLayout mRecentUnlocksFragmentHolder;
    CardView mMessageCardHolder;
    CardView mNotificationsCardHolder;
    CardView mVacationModeCardHolder;
    FrameLayout mVacationModeCard;
    FrameLayout mReviewsCard;
    FrameLayout mProgressCard;
    private Context context;
    private SwipeRefreshLayout mSwipeToRefreshLayout;
    private WaniKaniAPIV1Interface waniKaniAPI;

    private BroadcastReceiver mSyncCalled = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mSwipeToRefreshLayout.setRefreshing(true);
        }
    };
    private BroadcastReceiver mRetrofitConnectionErrorReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            showMessage(MESSAGE_TYPE.ERROR_NO_CONNECTION);
        }
    };
    private BroadcastReceiver mRetrofitUnknownErrorReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            showMessage(MESSAGE_TYPE.ERROR_UNKNOWN);
        }
    };
    private BroadcastReceiver mNotificationsReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            showNotificationIfExists();
        }
    };

    public void setWaniKaniAPI(WaniKaniAPIV1Interface waniKaniAPI) {
        this.waniKaniAPI = waniKaniAPI;
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceivers();
    }

    @Override
    public void onPause() {
        unregisterReceivers();
        super.onPause();
    }

    @Override
    public void onCreate(Bundle paramBundle) {
        this.context = getActivity();
        super.onCreate(paramBundle);
    }

    @Override
    public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle) {

        rootView = paramLayoutInflater.inflate(R.layout.fragment_dashboard, paramViewGroup, false);

        activity = (AppCompatActivity) getActivity();

        mSwipeToRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.dashboard_swipe_refresh);
        mSwipeToRefreshLayout.setOnRefreshListener(this);
        mSwipeToRefreshLayout.setColorSchemeResources(R.color.swipe_refresh);

        mAvailableHolder = (LinearLayout) rootView.findViewById(R.id.fragment_dashboard_available_holder);
        mReviewsHolder = (CardView) rootView.findViewById(R.id.fragment_dashboard_reviews_holder);
        mProgressHolder = (CardView) rootView.findViewById(R.id.fragment_dashboard_progress_holder);
        mRecentUnlocksFragmentHolder = (LinearLayout) rootView.findViewById(R.id.fragment_dashboard_recent_unlocks_holder);
        mCriticalItemsFragmentHolder = (LinearLayout) rootView.findViewById(R.id.fragment_dashboard_critical_items_holder);

        mMessageCardHolder = (CardView) rootView.findViewById(R.id.fragment_dashboard_message_card_holder);
        mNotificationsCardHolder = (CardView) rootView.findViewById(R.id.fragment_dashboard_notifications_card_holder);
        mVacationModeCardHolder = (CardView) rootView.findViewById(R.id.fragment_dashboard_vacation_mode_card_holder);

        mVacationModeCard = (FrameLayout) rootView.findViewById(R.id.fragment_dashboard_vacation_mode_card);
        mReviewsCard = (FrameLayout) rootView.findViewById(R.id.fragment_dashboard_reviews_card);
        mProgressCard = (FrameLayout) rootView.findViewById(R.id.fragment_dashboard_progress_card);

        mProgressHolder.setOnClickListener(this);

        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        VacationModeCard vacationModeCard = new VacationModeCard();
        AvailableCard availableCard = new AvailableCard();
        availableCard.setWaniKaniAPI(waniKaniAPI);

        ReviewsCard reviewsCard = new ReviewsCard();
        reviewsCard.setWaniKaniAPI(waniKaniAPI);

        SRSCard statusCard = new SRSCard();
        statusCard.setWaniKaniAPI(waniKaniAPI);
        ProgressCard progressCard = new ProgressCard();
        progressCard.setWaniKaniAPI(waniKaniAPI);
//        RecentUnlocksCard recentUnlocksCard = new RecentUnlocksCard();
//        CriticalItemsCard criticalItemsCard = new CriticalItemsCard();

        availableCard.setListener(this, getActivity());
        reviewsCard.setListener(this, getActivity());
        statusCard.setListener(this, getActivity());
        progressCard.setListener(this, getActivity());
//        recentUnlocksCard.setListener(this, getActivity());
//        criticalItemsCard.setListener(this, getActivity());

        transaction.replace(R.id.fragment_dashboard_vacation_mode_card, vacationModeCard);
        transaction.replace(R.id.fragment_dashboard_available_card, availableCard);
        transaction.replace(R.id.fragment_dashboard_reviews_card, reviewsCard);
        transaction.replace(R.id.fragment_dashboard_status_card, statusCard);
        transaction.replace(R.id.fragment_dashboard_progress_card, progressCard);
//        transaction.replace(R.id.fragment_dashboard_recent_unlocks_card, recentUnlocksCard);
//        transaction.replace(R.id.fragment_dashboard_critical_items_card, criticalItemsCard);
        transaction.commit();

        if (!MainActivity.isFirstSyncDashboardDone) {
            Intent intent = new Intent(BroadcastIntents.SYNC());
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
            checkVacationMode();
            MainActivity.isFirstSyncDashboardDone = true;
        } else {
            Intent intent = new Intent(BroadcastIntents.SYNC());
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
            checkVacationMode();
        }

        showNotificationIfExists();

        setRefreshing();

        return rootView;
    }

    private void setRefreshing() {
        if (mSwipeToRefreshLayout != null)
            mSwipeToRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeToRefreshLayout.setRefreshing(true);
                }
            });
    }

    private void updateSyncStatus() {
        //TODO: support recent unlocks and critical items
//        if (isAvailableCardSynced && isReviewsCardSynced && isStatusCardSynced && isProgressCardSynced && isRecentUnlocksCardSynced && isCriticalItemsCardSynced) {
//            mSwipeToRefreshLayout.setRefreshing(false);
//
//            if (isAvailableCardSyncedSuccess && isReviewsCardSyncedSuccess && isStatusCardSyncedSuccess && isRecentUnlocksCardSyncedSuccess
//                    && isCriticalItemsCardSyncedSuccess) {
//                PrefManager.setDashboardLastUpdateDate(System.currentTimeMillis());
//                onMessageCardOkButtonClick();
//            }
//        }

        if (isAvailableCardSynced && isReviewsCardSynced && isStatusCardSynced && isProgressCardSynced) {
            mSwipeToRefreshLayout.setRefreshing(false);

            if (isAvailableCardSyncedSuccess && isReviewsCardSyncedSuccess && isStatusCardSyncedSuccess) {
                PrefManager.setDashboardLastUpdateDate(System.currentTimeMillis());
                onMessageCardOkButtonClick();
            }
        }
    }

    private void registerReceivers() {
        LocalBroadcastManager.getInstance(activity).registerReceiver(mSyncCalled,
                new IntentFilter(BroadcastIntents.SYNC()));
        LocalBroadcastManager.getInstance(activity).registerReceiver(mRetrofitConnectionErrorReceiver,
                new IntentFilter(BroadcastIntents.RETROFIT_ERROR_CONNECTION()));
        LocalBroadcastManager.getInstance(activity).registerReceiver(mRetrofitUnknownErrorReceiver,
                new IntentFilter(BroadcastIntents.RETROFIT_ERROR_UNKNOWN()));
        LocalBroadcastManager.getInstance(activity).registerReceiver(mNotificationsReceiver,
                new IntentFilter(BroadcastIntents.NOTIFICATION()));
    }

    private void unregisterReceivers() {
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(mSyncCalled);
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(mRetrofitConnectionErrorReceiver);
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(mRetrofitUnknownErrorReceiver);
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(mNotificationsReceiver);
    }

    private void showMessage(MESSAGE_TYPE msgType) {
        if (getActivity() == null) return;

        try {
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            MessageCard fragment = new MessageCard();
            fragment.setListener(this);

            String title = "";
            String prefix = "";

            if (msgType == MESSAGE_TYPE.ERROR_NO_CONNECTION) {
                title = getString(R.string.error_no_connection);
                prefix = getString(R.string.content_last_updated) + " ";
            }

            if (msgType == MESSAGE_TYPE.ERROR_UNKNOWN) {
                title = getString(R.string.error_unknown_error);
                prefix = getString(R.string.content_last_updated) + " ";
            }

            Bundle args = new Bundle();
            args.putString(MessageCard.ARG_TITLE, title);
            args.putString(MessageCard.ARG_PREFIX, prefix);
            args.putLong(MessageCard.ARG_TIME, PrefManager.getDashboardLastUpdateTime());
            fragment.setArguments(args);

            transaction.replace(R.id.fragment_dashboard_message_card, fragment).commit();

            mMessageCardHolder.setVisibility(View.VISIBLE);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            // Probably http://www.androiddesignpatterns.com/2013/08/fragment-transaction-commit-state-loss.html
            // Ignore. No need to show message if Activity has been killed.
        }
    }

    private void showNotificationIfExists() {
        List<Notification> notifications = DatabaseManager.getNotifications();

        if (notifications != null && notifications.size() != 0) {
            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
            Bundle args = new Bundle();
            args.putSerializable(NotificationsCard.ARG_NOTIFICATIONS, (Serializable) notifications);
            NotificationsCard card = new NotificationsCard();
            card.setArguments(args);
            transaction.replace(R.id.fragment_dashboard_notifications_card, card).commit();
            mNotificationsCardHolder.setVisibility(View.VISIBLE);
        } else {
            mNotificationsCardHolder.setVisibility(View.GONE);
        }
    }

    private void setCriticalItemsFragmentHeight(int height) {
        ViewGroup.LayoutParams params = mCriticalItemsFragmentHolder.getLayoutParams();
        params.height = height;
        mCriticalItemsFragmentHolder.setLayoutParams(params);
    }

    private void setRecentUnlocksFragmentHeight(int height) {
        ViewGroup.LayoutParams params = mRecentUnlocksFragmentHolder.getLayoutParams();
        params.height = height;
        mRecentUnlocksFragmentHolder.setLayoutParams(params);
    }

    private void checkVacationMode() {
        UserData user = DatabaseManager.getUserV2();
        if (user == null) return;

//        if (user.isVacationModeActive()) {
//            mAvailableHolder.setVisibility(View.GONE);
//            mReviewsHolder.setVisibility(View.GONE);
//            mVacationModeCardHolder.setVisibility(View.VISIBLE);
//        } else {
            mAvailableHolder.setVisibility(View.VISIBLE);
            mReviewsHolder.setVisibility(View.VISIBLE);
            mVacationModeCardHolder.setVisibility(View.GONE);
//        }
    }

    @Override
    public void onAvailableCardSyncFinishedListener(String result) {
        if (result.equals(SYNC_RESULT_SUCCESS))
            isAvailableCardSyncedSuccess = true;

        if (result.equals(SYNC_RESULT_FAILED))
            isAvailableCardSyncedSuccess = false;

        isAvailableCardSynced = true;
        updateSyncStatus();
    }

    @Override
    public void onReviewsCardSyncFinishedListener(String result) {
        if (result.equals(SYNC_RESULT_SUCCESS))
            isReviewsCardSyncedSuccess = true;

        if (result.equals(SYNC_RESULT_FAILED))
            isReviewsCardSyncedSuccess = false;

        isReviewsCardSynced = true;
        updateSyncStatus();
    }

    @Override
    public void onStatusCardSyncFinishedListener(String result) {
        if (result.equals(SYNC_RESULT_SUCCESS))
            isStatusCardSyncedSuccess = true;

        if (result.equals(SYNC_RESULT_FAILED))
            isStatusCardSyncedSuccess = false;

        isStatusCardSynced = true;
        updateSyncStatus();
    }

    @Override
    public void onProgressCardSyncFinishedListener(String result) {
        if (result.equals(SYNC_RESULT_SUCCESS))
            isProgressCardSyncedSuccess = true;

        if (result.equals(SYNC_RESULT_FAILED))
            isProgressCardSyncedSuccess = false;

        isProgressCardSynced = true;
        updateSyncStatus();
    }

    @Override
    public void onRecentUnlocksCardSyncFinishedListener(int height, String result) {
        if (result.equals(SYNC_RESULT_SUCCESS)) {
            isRecentUnlocksCardSyncedSuccess = true;
        }

        if (result.equals(SYNC_RESULT_FAILED)) {
            isRecentUnlocksCardSyncedSuccess = false;
        }

        setRecentUnlocksFragmentHeight(height);
        isRecentUnlocksCardSynced = true;
        updateSyncStatus();
    }

    @Override
    public void onCriticalItemsCardSyncFinishedListener(int height, String result) {
        if (result.equals(SYNC_RESULT_SUCCESS)) {
            isCriticalItemsCardSyncedSuccess = true;
        }

        if (result.equals(SYNC_RESULT_FAILED)) {
            isCriticalItemsCardSyncedSuccess = false;
        }

        setCriticalItemsFragmentHeight(height);
        isCriticalItemsCardSynced = true;
        updateSyncStatus();
    }

    @Override
    public void onMessageCardOkButtonClick() {
        mMessageCardHolder.setVisibility(View.GONE);
    }

    @Override
    public void onRefresh() {
        isAvailableCardSynced = false;
        isReviewsCardSynced = false;
        isStatusCardSynced = false;
        isProgressCardSynced = false;
        isRecentUnlocksCardSynced = false;
        isCriticalItemsCardSynced = false;

        isAvailableCardSyncedSuccess = false;
        isReviewsCardSyncedSuccess = false;
        isStatusCardSyncedSuccess = false;
        isProgressCardSyncedSuccess = false;
        isRecentUnlocksCardSyncedSuccess = false;
        isCriticalItemsCardSyncedSuccess = false;

        showNotificationIfExists();

        Intent intent = new Intent(BroadcastIntents.SYNC());
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
        checkVacationMode();
    }

    @Override
    public void onClick(View view) {
        if (view == mReviewsHolder) {
            // TODO - Handle reviews card stuff
        }
        if (view == mProgressHolder) {
            Intent intent = new Intent(getActivity(), ProgressDetailsActivity.class);
            intent.putExtra(WANIKANI_API_EXTRA, waniKaniAPI);
            startActivity(intent);
        }
    }

    enum MESSAGE_TYPE {
        ERROR_NO_CONNECTION,
        ERROR_UNKNOWN
    }
}