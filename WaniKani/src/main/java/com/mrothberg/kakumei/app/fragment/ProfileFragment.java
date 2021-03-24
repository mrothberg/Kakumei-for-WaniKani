package com.mrothberg.kakumei.app.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.mrothberg.kakumei.R;
import com.mrothberg.kakumei.app.activity.MainActivity;
import com.mrothberg.kakumei.client.WaniKaniAPIV1Interface;
import com.mrothberg.kakumei.database.DatabaseManager;
import com.mrothberg.kakumei.managers.PrefManager;
import com.mrothberg.kakumei.apimodels.UserData;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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

    private WaniKaniAPIV1Interface waniKaniAPIV1Interface;

    public ProfileFragment(WaniKaniAPIV1Interface waniKaniAPIV1Interface) {
        this.waniKaniAPIV1Interface = waniKaniAPIV1Interface;
    }

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
        waniKaniAPIV1Interface.getUser().whenComplete((userRequest, throwable) -> {
            if(throwable != null) {
                UserData user = DatabaseManager.getUserV2();
                if (user != null) {
                    load(user);
                }
                return;
            }

            load(userRequest.data);
        });
    }

    private void load(UserData user) {
        mUsername.setText(user.username);
        mLevel.setText(user.level + "");
        String creationDate = user.started_at;
        final DateFormat iso8601Parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        iso8601Parser.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date startDate = iso8601Parser.parse(creationDate);
            creationDate = startDate.toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mCreationDate.setText(creationDate);
        mSubscriptionType.setText(user.subscription.type);

        if (mViewFlipper.getDisplayedChild() == 1) {
            mViewFlipper.showPrevious();
        }

        if (PrefManager.isProfileFirstTime()) {
            PrefManager.setProfileFirstTime(false);
        }
    }
}

