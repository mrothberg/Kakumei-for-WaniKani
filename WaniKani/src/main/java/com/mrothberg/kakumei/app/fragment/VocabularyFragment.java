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
import com.mrothberg.kakumei.widget.adapter.VocabularyAdapter;
import com.mrothberg.kakumei.wkamodels.BaseItem;
import com.mrothberg.kakumei.wkamodels.ItemsList;
import com.mrothberg.kakumei.wkamodels.VocabularyList;

public class VocabularyFragment extends Fragment implements LevelPickerDialogFragment.LevelDialogListener,
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

    VocabularyAdapter mVocabularyAdapter;

    View rootView;

    String level = "";

    MenuItem mLevelItem;

    private SwipeRefreshLayout mMessageSwipeRefreshLayout;

    WaniKaniAPIV1Interface wanikaniAPI;
    private final String TAG = "VocabularyFragment";

    public void setWaniKaniAPI(WaniKaniAPIV1Interface waniKaniAPI) {
        this.wanikaniAPI = waniKaniAPI;
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
        rootView = layoutInflater.inflate(R.layout.fragment_vocabulary, viewGroup, false);

        mMessageSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.vocabulary_message_swipe_refresh);
        mMessageSwipeRefreshLayout.setOnRefreshListener(this);
        mMessageSwipeRefreshLayout.setColorSchemeResources(R.color.swipe_refresh);

        mRecycler = rootView.findViewById(R.id.vocabulary_recycler_view);

        //TODO: There's probably a better way to do this, maybe using dimensions?
        // Especially this fragment and how it will look on tablets (too many small columns)
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        int vocabItemWidth = 300;
        int numColumns = (int) (screenWidthDp / vocabItemWidth + 0.5); // +0.5 for correct rounding to int.
        mLayoutManager = new StickyHeaderGridLayoutManager(numColumns);
        mRecycler.setLayoutManager(mLayoutManager);

        mListFlipper = (ViewFlipper) rootView.findViewById(R.id.vocabulary_list_flipper);
        mMessageFlipper = (ViewFlipper) rootView.findViewById(R.id.vocabulary_message_flipper);

        mMessageIcon = (ImageView) rootView.findViewById(R.id.vocabulary_message_icon);
        mMessageTitle = (TextView) rootView.findViewById(R.id.vocabulary_message_title);
        mMessageSummary = (TextView) rootView.findViewById(R.id.vocabulary_message_summary);

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
            wanikaniAPI.getUser().whenComplete((userRequest, throwable) -> {
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

        wanikaniAPI.getVocabularyList(level).whenComplete((vocabularyList, throwable) -> {
            if(throwable != null || vocabularyList == null || vocabularyList.isEmpty()) {
                attemptToLoadVocabularyFromDB();
            } else {
                loadVocabularyList(vocabularyList);
            }

            ((AppCompatActivity) context).invalidateOptionsMenu();

            if (mListFlipper.getDisplayedChild() == 0)
                mListFlipper.showNext();

            mMessageSwipeRefreshLayout.setRefreshing(false);
        });
    }

    private void attemptToLoadVocabularyFromDB() {
        VocabularyList vocabularyList = new VocabularyList();
        vocabularyList.addAll(DatabaseManager.getItems(BaseItem.ItemType.VOCABULARY, Utils.convertStringArrayToIntArray(level.split(","))));

        if (vocabularyList.size() != 0) {
            loadVocabularyList(vocabularyList);
        } else {
            mMessageIcon.setImageResource(R.drawable.ic_error_red_36dp);
            mMessageTitle.setText(R.string.no_items_title);
            mMessageSummary.setText(R.string.no_items_summary);

            if (mMessageFlipper.getDisplayedChild() == 0) {
                mMessageFlipper.showNext();
            }
        }
    }

    private void loadVocabularyList(ItemsList list) {
        Collections.sort(list, new Comparator<BaseItem>() {
            public int compare(BaseItem item1, BaseItem item2) {
                return Float.valueOf((item1.getLevel() + "")).compareTo(Float.valueOf(item2.getLevel() + ""));
            }
        });

        mVocabularyAdapter = new VocabularyAdapter(context, list);
        mRecycler.setAdapter(mVocabularyAdapter);

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