package tr.xip.wanikani.app.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.codewaves.stickyheadergrid.StickyHeaderGridLayoutManager;

import java.util.Collections;
import java.util.Comparator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tr.xip.wanikani.R;
import tr.xip.wanikani.apimodels.UserData;
import tr.xip.wanikani.apimodels.UserRequest;
import tr.xip.wanikani.client.WaniKaniAPIV1Interface;
import tr.xip.wanikani.client.WaniKaniApiV2;
import tr.xip.wanikani.database.DatabaseManager;
import tr.xip.wanikani.dialogs.LegendDialogFragment;
import tr.xip.wanikani.dialogs.LevelPickerDialogFragment;
import tr.xip.wanikani.managers.PrefManager;
import tr.xip.wanikani.utils.Utils;
import tr.xip.wanikani.widget.adapter.RadicalsAdapter;
import tr.xip.wanikani.wkamodels.BaseItem;
import tr.xip.wanikani.wkamodels.RadicalsList;

public class RadicalsFragment extends Fragment implements LevelPickerDialogFragment.LevelDialogListener,
        SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = "RadicalsFragment";
    Context context;

    TextView mMessageTitle;
    TextView mMessageSummary;
    ImageView mMessageIcon;
    ViewFlipper mMessageFlipper;

    private RecyclerView mRecycler;
    private StickyHeaderGridLayoutManager mLayoutManager;

    ViewFlipper mListFlipper;

    LevelPickerDialogFragment mLevelPickerDialog;

    RadicalsAdapter mRadicalsAdapter;

    View rootView;

    String level = "";

    MenuItem mLevelItem;

    WaniKaniAPIV1Interface waniKaniAPI;

    private SwipeRefreshLayout mMessageSwipeRefreshLayout;

    public void setWaniKaniAPI(WaniKaniAPIV1Interface waniKaniAPI) {
        this.waniKaniAPI = waniKaniAPI;
    }

    private void showLegend() {
        new LegendDialogFragment().show(getActivity().getSupportFragmentManager(), "legend-dialog");
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        context = getActivity();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mLevelPickerDialog == null)
            mLevelItem.setVisible(false);
        else
            mLevelItem.setVisible(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_radicals, menu);
        mLevelItem = menu.findItem(R.id.action_level);
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        rootView = layoutInflater.inflate(R.layout.fragment_radicals, viewGroup, false);

        mMessageSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.radicals_message_swipe_refresh);
        mMessageSwipeRefreshLayout.setOnRefreshListener(this);
        mMessageSwipeRefreshLayout.setColorSchemeResources(R.color.swipe_refresh);

        mRecycler = rootView.findViewById(R.id.radical_recycler_view);

        //TODO: There's probably a better way to do this, maybe using dimensions?
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        int radicalItemWidth = 100;
        int numColumns = (int) (screenWidthDp / radicalItemWidth + 0.5); // +0.5 for correct rounding to int.
        mLayoutManager = new StickyHeaderGridLayoutManager(numColumns);
        mRecycler.setLayoutManager(mLayoutManager);

        mListFlipper = (ViewFlipper) rootView.findViewById(R.id.radicals_list_flipper);
        mMessageFlipper = (ViewFlipper) rootView.findViewById(R.id.radicals_message_flipper);

        mMessageIcon = (ImageView) rootView.findViewById(R.id.radicals_message_icon);
        mMessageTitle = (TextView) rootView.findViewById(R.id.radicals_message_title);
        mMessageSummary = (TextView) rootView.findViewById(R.id.radicals_message_summary);

        if (!PrefManager.isLegendLearned()) {
            showLegend();
        }

        fetchLevelAndData();

        setHasOptionsMenu(true);

        return rootView;
    }

    public void fetchLevelAndData() {
        UserData user = DatabaseManager.getUserV2();

        if (user != null) {
            setLevel(user.level);
            fetchData();
        } else {
            WaniKaniApiV2.getUser().enqueue(new Callback<UserRequest>() {
                @Override
                public void onResponse(Call<UserRequest> call, Response<UserRequest> response) {
                    if (response.isSuccessful() && response.body().data != null) {
                        setLevel(response.body().data.level);
                        fetchData();
                    }
                }

                @Override
                public void onFailure(Call<UserRequest> call, Throwable t) {

                }
            });
        }
    }

    private void setLevel(int level) {
        this.level = level + "";
        mLevelPickerDialog = new LevelPickerDialogFragment();
    }

    public void fetchData() {
        if (mListFlipper.getDisplayedChild() == 1)
            mListFlipper.showPrevious();

        waniKaniAPI.getRadicalsList(level).whenComplete((radicalsList, throwable) -> {
            if(throwable != null || radicalsList == null || radicalsList.isEmpty()) {
                Log.e(TAG, "Failure retrieving radicals list for level: " + level, throwable);
                attemptToLoadRadicalsFromDB();
            } else {
                loadRadicalsList(radicalsList);
            }

            ((AppCompatActivity) context).invalidateOptionsMenu();

            if (mListFlipper.getDisplayedChild() == 0)
                mListFlipper.showNext();

            mMessageSwipeRefreshLayout.setRefreshing(false);
        });
    }

    private void attemptToLoadRadicalsFromDB() {
        RadicalsList list = new RadicalsList();
        list.addAll(DatabaseManager.getItems(BaseItem.ItemType.RADICAL, Utils.convertStringArrayToIntArray(level.split(","))));

        if (list.size() != 0) {
            loadRadicalsList(list);
        } else {
            mMessageIcon.setImageResource(R.drawable.ic_error_red_36dp);
            mMessageTitle.setText(R.string.no_items_title);
            mMessageSummary.setText(R.string.no_items_summary);

            if (mMessageFlipper.getDisplayedChild() == 0) {
                mMessageFlipper.showNext();
            }
        }
    }

    private void loadRadicalsList(RadicalsList list) {
        Collections.sort(list, new Comparator<BaseItem>() {
            public int compare(BaseItem item1, BaseItem item2) {
                return Float.valueOf((item1.getLevel() + "")).compareTo(Float.valueOf(item2.getLevel() + ""));
            }
        });

        mRadicalsAdapter = new RadicalsAdapter(context, list);
        mRecycler.setAdapter(mRadicalsAdapter);

        if (mMessageFlipper.getDisplayedChild() == 1)
            mMessageFlipper.showPrevious();
    }

    @Override
    public void onLevelDialogPositiveClick(DialogFragment dialog, String level) {
        this.level = level;
        fetchData();
    }

    @Override
    public void onLevelDialogResetClick(DialogFragment dialogFragment, String level) {
        fetchLevelAndData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_level:
                showLevelDialog();
                break;
            case R.id.action_legend:
                showLegend();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showLevelDialog() {
        if (mLevelPickerDialog != null) {
            mLevelPickerDialog.init(this.getId(), level);
            mLevelPickerDialog.show(getActivity().getSupportFragmentManager(), "LevelPickerDialogFragment");
        }
    }

    @Override
    public void onRefresh() {
        fetchData();
    }
}