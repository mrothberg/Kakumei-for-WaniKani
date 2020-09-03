package tr.xip.wanikani.app.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tr.xip.wanikani.R;
import tr.xip.wanikani.client.WaniKaniApiV2;
import tr.xip.wanikani.dialogs.HowToGetKeyDialogFragment;
import tr.xip.wanikani.managers.PrefManager;
import tr.xip.wanikani.apimodels.UserRequest;

public class FirstTimeActivity extends AppCompatActivity {
    public static final String TAG = "FirstTimeActivity";
    EditText mApiKey;
    Button mHowTo;
    Button mSignIn;

    ViewSwitcher mViewSwitcher;

    Context context;

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
                    PrefManager.setApiKey(mApiKey.getText().toString());
                    WaniKaniApiV2.init();
                    //TODO: DI interface instead of this static call
                    WaniKaniApiV2.getUser().enqueue(new Callback<UserRequest>() {
                        @Override
                        public void onResponse(Call<UserRequest> call, Response<UserRequest> response) {
                            if(response.isSuccessful() && response.body() != null) {
                                Log.d(TAG, "successful response??");
                                response.body().data.save();
                                PrefManager.setFirstLaunch(false);
                                WaniKaniApiV2.init();

                                startActivity(new Intent(context, MainActivity.class));
                                finish();
                            } else {
                                onFailure(call, null);
                            }
                        }

                        @Override
                        public void onFailure(Call<UserRequest> call, Throwable t) {
                            Log.e(TAG, "Error making getUser() call", t);

                            if (mViewSwitcher.getDisplayedChild() == 1) {
                                mViewSwitcher.showPrevious();
                            }
                            Toast.makeText(context, R.string.error_invalid_api_key, Toast.LENGTH_SHORT).show();
                        }
                    });

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