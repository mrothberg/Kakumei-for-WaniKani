package com.mrothberg.kakumei.app.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewFlipper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.mrothberg.kakumei.R;
import com.mrothberg.kakumei.app.activity.MainActivity;
import com.mrothberg.kakumei.client.WaniKaniApiV2;
import com.mrothberg.kakumei.database.DatabaseManager;
import com.mrothberg.kakumei.managers.PrefManager;
import com.mrothberg.kakumei.apimodels.UserData;
import com.mrothberg.kakumei.apimodels.UserRequest;

/**
 * Created by xihsa_000 on 3/11/14.
 */
public class ProfileFragment extends Fragment {

    Context context;

    TextView mUsername;
    TextView mLevel;
    TextView mCreationDate;
    TextView mSubscriptionType;

    ViewFlipper mViewFlipper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_profile, container,
                false);

        context = getActivity();

        mUsername = (TextView) rootView.findViewById(R.id.profile_username);
        mLevel = (TextView) rootView.findViewById(R.id.profile_level);
        mCreationDate = (TextView) rootView.findViewById(R.id.profile_creation_date);
        mSubscriptionType = rootView.findViewById(R.id.subscription_type);

        mViewFlipper = (ViewFlipper) rootView.findViewById(R.id.profile_view_flipper);

        if (PrefManager.isProfileFirstTime()) {
            if (mViewFlipper.getDisplayedChild() == 0) {
                mViewFlipper.showNext();
            }
        }

        if (!MainActivity.isFirstSyncProfileDone) {
            fetchData();

            MainActivity.isFirstSyncProfileDone = true;
        } else {
            fetchData();
        }

        return rootView;
    }

    public void fetchData() {
        WaniKaniApiV2.getUser().enqueue(new Callback<UserRequest>() {
            @Override
            public void onResponse(Call<UserRequest> call, Response<UserRequest> response) {
                if (response.isSuccessful() && response.body().data != null) {
                    load(response.body().data);
                } else {
                    onFailure(call, null);
                }
            }

            @Override
            public void onFailure(Call<UserRequest> call, Throwable t) {
                UserData user = DatabaseManager.getUserV2();
                if (user != null) {
                    load(user);
                }
            }

            void load(UserData user) {

                mUsername.setText(user.username);
                mLevel.setText(user.level + "");
//                mCreationDate.setText(new SimpleDateFormat("MMMM d, yyyy").format(user.getCreationDateInMillis()));
                mCreationDate.setText(user.started_at);
                mSubscriptionType.setText(user.subscription.type);

                if (mViewFlipper.getDisplayedChild() == 1) {
                    mViewFlipper.showPrevious();
                }

                if (PrefManager.isProfileFirstTime()) {
                    PrefManager.setProfileFirstTime(false);
                }
            }
        });
    }
}

