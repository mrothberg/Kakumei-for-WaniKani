package com.mrothberg.kakumei.app.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.mrothberg.kakumei.R;
import com.mrothberg.kakumei.apimodels.UserRequest;
import com.mrothberg.kakumei.app.fragment.DashboardFragment;
import com.mrothberg.kakumei.app.fragment.KanjiFragment;
import com.mrothberg.kakumei.app.fragment.NavigationDrawerFragment;
import com.mrothberg.kakumei.app.fragment.RadicalsFragment;
import com.mrothberg.kakumei.app.fragment.VocabularyFragment;
import com.mrothberg.kakumei.app.fragment.card.AvailableCard;
import com.mrothberg.kakumei.client.WaniKaniApiV2;
import com.mrothberg.kakumei.content.receiver.BroadcastIntents;
import com.mrothberg.kakumei.database.DatabaseManager;
import com.mrothberg.kakumei.managers.PrefManager;
import com.mrothberg.kakumei.wkamodels.Notification;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    public static final String STATE_ACTIONBAR_TITLE = "action_bar_title";

    public static boolean isFirstSyncDashboardDone = false;
    public static boolean isFirstSyncProfileDone = false;

    public static CharSequence mTitle;

    ActionBar mActionBar;
    Toolbar mToolbar;

    private NavigationDrawerFragment mNavigationDrawerFragment;

    private WaniKaniApiV2 waniKaniAPI = new WaniKaniApiV2();

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (PrefManager.isFirstLaunch()) {
            startActivity(new Intent(this, FirstTimeActivity.class));
            finish();
        } else {
            setContentView(R.layout.activity_main);
            handleNotification(getIntent());

            mToolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(mToolbar);

            mActionBar = getSupportActionBar();

            if (savedInstanceState != null) {
                mTitle = savedInstanceState.getString(STATE_ACTIONBAR_TITLE);
                mActionBar.setTitle(mTitle.toString());
            }

            mNavigationDrawerFragment = (NavigationDrawerFragment)
                    getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

            mNavigationDrawerFragment.setUp(
                    R.id.navigation_drawer_holder,
                    (DrawerLayout) findViewById(R.id.drawer_layout));
        }

        WaniKaniApiV2.getUser().enqueue(new Callback<UserRequest>() {
            @Override
            public void onResponse(Call<UserRequest> call, Response<UserRequest> response) {
                if (response.isSuccessful() && response.body().data != null) {
                    DatabaseManager.saveUserV2(response.body().data);
                }
            }

            @Override
            public void onFailure(Call<UserRequest> call, Throwable t) {

            }
        });

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = null;

        switch (position) {
            case 0:
                DashboardFragment df = new DashboardFragment();
                df.setWaniKanAPI(waniKaniAPI);
                fragment = df;
                mTitle = getString(R.string.title_dashboard);
                break;
            case 1:
                fragment = new RadicalsFragment();
                ((RadicalsFragment) fragment).setWaniKaniAPI(waniKaniAPI);
                mTitle = getString(R.string.title_radicals);
                break;
            case 2:
                fragment = new KanjiFragment();
                ((KanjiFragment) fragment).setWaniKaniAPI(waniKaniAPI);
                mTitle = getString(R.string.title_kanji);
                break;
            case 3:
                fragment = new VocabularyFragment();
                ((VocabularyFragment) fragment).setWaniKaniAPI(waniKaniAPI);
                mTitle = getString(R.string.title_vocabulary);
                break;
        }

        if (fragment != null)
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
    }

    public void restoreActionBar() {
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        mActionBar.setTitle(mTitle.toString());
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.global, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AvailableCard.BROWSER_REQUEST) {
            Intent intent = new Intent(BroadcastIntents.SYNC());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_ACTIONBAR_TITLE, mTitle.toString());
    }

    private void handleNotification(Intent intent) {
        if (intent == null || intent.getExtras() == null) return;

        Bundle bundle = getIntent().getExtras();

        String idString = bundle.getString(Notification.DATA_NOTIFICATION_ID);

        if (idString == null) return; // Return if no id - basically not a notification Intent

        int id = Integer.valueOf(idString);
        String title = bundle.getString(Notification.DATA_NOTIFICATION_TITLE);
        String shortText = bundle.getString(Notification.DATA_NOTIFICATION_SHORT_TEXT);
        String text = bundle.getString(Notification.DATA_NOTIFICATION_TEXT);
        String image = bundle.getString(Notification.DATA_NOTIFICATION_IMAGE);
        String actionUrl = bundle.getString(Notification.DATA_NOTIFICATION_ACTION_URL);
        String actionText = bundle.getString(Notification.DATA_NOTIFICATION_ACTION_TEXT);

        DatabaseManager.saveNotification(new Notification(
                id,
                title,
                shortText,
                text,
                image,
                actionUrl,
                actionText,
                false
        ));

        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(BroadcastIntents.NOTIFICATION()));
    }
}
