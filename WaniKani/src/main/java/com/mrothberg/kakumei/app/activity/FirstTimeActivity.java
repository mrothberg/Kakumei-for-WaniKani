package com.mrothberg.kakumei.app.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.mrothberg.kakumei.R;
import com.mrothberg.kakumei.client.WaniKaniAPIV1Interface;
import com.mrothberg.kakumei.client.WaniKaniApiV2;
import com.mrothberg.kakumei.client.WaniKaniServiceV2Builder;
import com.mrothberg.kakumei.database.DatabaseHelper;
import com.mrothberg.kakumei.database.DatabaseManager;
import com.mrothberg.kakumei.dialogs.HowToGetKeyDialogFragment;
import com.mrothberg.kakumei.managers.PrefManager;

public class FirstTimeActivity extends AppCompatActivity {
    public static final String TAG = "FirstTimeActivity";
    EditText mApiKey;
    Button mHowTo;
    Button mSignIn;

    ViewSwitcher mViewSwitcher;

    Context context;

    WaniKaniAPIV1Interface waniKaniAPIV1Interface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_time);

        context = this;

        mApiKey = (EditText) findViewById(R.id.first_time_api_key);
        mHowTo = (Button) findViewById(R.id.first_time_how_to_api_key);
        mSignIn = (Button) findViewById(R.id.first_time_sign_in);

        mViewSwitcher = (ViewSwitcher) findViewById(R.id.firt_time_view_switcher);
        mViewSwitcher.setInAnimation(context, R.anim.abc_fade_in);
        mViewSwitcher.setOutAnimation(context, R.anim.abc_fade_out);

        mHowTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new HowToGetKeyDialogFragment().show(getSupportFragmentManager(), "how-to-get-key");
            }
        });

        mSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(mApiKey.getText().toString())) {
                    Toast.makeText(context, R.string.error_enter_api_key, Toast.LENGTH_SHORT).show();
                } else {
                    if (mViewSwitcher.getDisplayedChild() == 0) {
                        mViewSwitcher.showNext();
                    }

                    //TODO: rethink api key initialization - static calls :(
                    String apiKey = mApiKey.getText().toString();
                    PrefManager.setApiKey(apiKey);
                    waniKaniAPIV1Interface = new WaniKaniApiV2(new WaniKaniServiceV2Builder(apiKey));
                    waniKaniAPIV1Interface.getUser().whenComplete((userRequest, throwable) -> {
                        if(throwable != null || userRequest == null) {
                            Log.e(TAG, "Error making getUser() call", throwable);

                            if (mViewSwitcher.getDisplayedChild() == 1) {
                                mViewSwitcher.showPrevious();
                            }
                            Toast.makeText(context, R.string.error_invalid_api_key, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        userRequest.data.save();
                        startActivity(new Intent(context, MainActivity.class));
                        finish();
                    });

                    //TODO: notifications
//                    WaniKaniApi.getUser(mApiKey.getText().toString()).enqueue(new ThroughDbCallback<Request<User>, User>() {
//                        @Override
//                        public void onResponse(Call<Request<User>> call, Response<Request<User>> response) {
//                            super.onResponse(call, response);
//                            if (response.isSuccessful() && response.body().user_information != null) {
//                                PrefManager.setApiKey(mApiKey.getText().toString());
//                                PrefManager.setFirstLaunch(false);
//                                WaniKaniApi.init();
//                                startActivity(new Intent(context, MainActivity.class));
//
//                                // Set an alarm for notifications for the first time
//                                new NotificationScheduler(context).schedule();
//
//                                finish();
//                            } else {
//                                onFailure(call, null);
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(Call<Request<User>> call, Throwable t) {
//                            super.onFailure(call, t);
//
//                            if (mViewSwitcher.getDisplayedChild() == 1) {
//                                mViewSwitcher.showPrevious();
//                            }
//                            Toast.makeText(context, R.string.error_invalid_api_key, Toast.LENGTH_SHORT).show();
//                        }
//                    });
                }
            }
        });
    }
}