package com.mrothberg.kakumei.app.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.util.DisplayMetrics;
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

import com.mrothberg.kakumei.R;
import com.mrothberg.kakumei.apimodels.UserData;
import com.mrothberg.kakumei.client.WaniKaniAPIV1Interface;
import com.mrothberg.kakumei.database.DatabaseManager;
import com.mrothberg.kakumei.dialogs.LegendDialogFragment;
import com.mrothberg.kakumei.dialogs.LevelPickerDialogFragment;
import com.mrothberg.kakumei.managers.PrefManager;
import com.mrothberg.kakumei.utils.Utils;
import com.mrothberg.kakumei.widget.adapter.KanjiAdapter;
import com.mrothberg.kakumei.wkamodels.BaseItem;
import com.mrothberg.kakumei.wkamodels.ItemsList;
import com.mrothberg.kakumei.wkamodels.KanjiList;

public class KanjiFragment extends Fragment implements LevelPickerDialogFragment.LevelDialogListener,
        SwipeRefreshLayout.OnRefreshListener {

    Context context;

    TextView mMessageTitle;
    TextView mMessageSummary;
    ImageView mMessageIcon;
    ViewFlipper mMessageFlipper;

    private RecyclerView mRecycler;
    private StickyHeaderGridLayoutManager mLayoutManager;

    ViewFlipper mListFlipper;

    LevelPickerDialogFragment mLevelPickerDialog;

    KanjiAdapter mKanjiAdapter;
    View rootView;

    String level = "";

    MenuItem mLevelItem;

    WaniKaniAPIV1Interface waniKaniAPI;

    private SwipeRefreshLayout mMessageSwipeRefreshLayout;
    private final static String TAG = "KanjiFragment";

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
        rootView = layoutInflater.inflate(R.layout.fragment_kanji, viewGroup, false);

        mMessageSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.kanji_message_swipe_refresh);
        mMessageSwipeRefreshLayout.setOnRefreshListener(this);
        mMessageSwipeRefreshLayout.setColorSchemeResources(R.color.swipe_refresh);

        mRecycler = rootView.findViewById(R.id.kanji_recycler_view);

        //TODO: There's probably a better way to do this, maybe using dimensions?
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        int kanjiItemWidth = 100;
        int numColumns = (int) (screenWidthDp / kanjiItemWidth + 0.5); // +0.5 for correct rounding to int.
        mLayoutManager = new StickyHeaderGridLayoutManager(numColumns);
        mRecycler.setLayoutManager(mLayoutManager);

        mListFlipper = (ViewFlipper) rootView.findViewById(R.id.kanji_list_flipper);
        mMessageFlipper = (ViewFlipper) rootView.findViewById(R.id.kanji_message_flipper);

        mMessageIcon = (ImageView) rootView.findViewById(R.id.kanji_message_icon);
        mMessageTitle = (TextView) rootView.findViewById(R.id.kanji_message_title);
        mMessageSummary = (TextView) rootView.findViewById(R.id.kanji_message_summary);

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
            waniKaniAPI.getUser().whenComplete((userRequest, throwable) -> {
               if(throwable != null) {
                   setLevel(userRequest.data.level);
                   fetchData();
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

        //TODO: This logic and helper methods can be improved.
        //      The other fragments (Radicals and Vocab) contain the same logic
        waniKaniAPI.getKanjiList(level).whenComplete((kanjiList, throwable) -> {
            if(throwable != null || kanjiList == null || kanjiList.isEmpty()) {
                attemptToLoadKanjiFromDB();
            } else {
                loadKanjiList(kanjiList);
            }

            ((AppCompatActivity) context).invalidateOptionsMenu();

            if (mListFlipper.getDisplayedChild() == 0)
                mListFlipper.showNext();

            mMessageSwipeRefreshLayout.setRefreshing(false);
        });
    }

    private void attemptToLoadKanjiFromDB() {
        KanjiList kanjiList = new KanjiList();
        kanjiList.addAll(DatabaseManager.getItems(BaseItem.ItemType.KANJI, Utils.convertStringArrayToIntArray(level.split(","))));

        if (!kanjiList.isEmpty()) {
            loadKanjiList(kanjiList);
        } else {
            mMessageIcon.setImageResource(R.drawable.ic_error_red_36dp);
            mMessageTitle.setText(R.string.no_items_title);
            mMessageSummary.setText(R.string.no_items_summary);

            if (mMessageFlipper.getDisplayedChild() == 0) {
                mMessageFlipper.showNext();
            }
        }
    }

    private void loadKanjiList(ItemsList list) {
            //TODO: The Kanji Adapter should prob do this sort
            Collections.sort(list, new Comparator<BaseItem>() {
                public int compare(BaseItem item1, BaseItem item2) {
                    return Float.valueOf((item1.getLevel() + "")).compareTo(Float.valueOf(item2.getLevel() + ""));
                }
            });

            mKanjiAdapter = new KanjiAdapter(context, list);
            mRecycler.setAdapter(mKanjiAdapter);

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