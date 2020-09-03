package tr.xip.wanikani.app.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.tonicartos.widget.stickygridheaders.StickyGridHeadersGridView;

import java.util.Collections;
import java.util.Comparator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tr.xip.wanikani.R;
import tr.xip.wanikani.app.activity.ItemDetailsActivity;
import tr.xip.wanikani.client.WaniKaniAPIV1Interface;
import tr.xip.wanikani.client.WaniKaniApiV2;
import tr.xip.wanikani.database.DatabaseManager;
import tr.xip.wanikani.dialogs.LegendDialogFragment;
import tr.xip.wanikani.dialogs.LevelPickerDialogFragment;
import tr.xip.wanikani.managers.PrefManager;
import tr.xip.wanikani.wkamodels.BaseItem;
import tr.xip.wanikani.wkamodels.ItemsList;
import tr.xip.wanikani.wkamodels.KanjiList;
import tr.xip.wanikani.apimodels.UserData;
import tr.xip.wanikani.apimodels.UserRequest;
import tr.xip.wanikani.utils.Utils;
import tr.xip.wanikani.widget.adapter.KanjiAdapter;

public class KanjiFragment extends Fragment implements LevelPickerDialogFragment.LevelDialogListener,
        SwipeRefreshLayout.OnRefreshListener {

    Context context;

    TextView mMessageTitle;
    TextView mMessageSummary;
    ImageView mMessageIcon;
    ViewFlipper mMessageFlipper;

    StickyGridHeadersGridView mGrid;
    ViewFlipper mListFlipper;

    LevelPickerDialogFragment mLevelPickerDialog;

    KanjiAdapter mKanjiAdapter;

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
        rootView = layoutInflater.inflate(R.layout.fragment_kanji, viewGroup, false);

        mMessageSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.kanji_message_swipe_refresh);
        mMessageSwipeRefreshLayout.setOnRefreshListener(this);
        mMessageSwipeRefreshLayout.setColorSchemeResources(R.color.swipe_refresh);

        mGrid = (StickyGridHeadersGridView) rootView.findViewById(R.id.kanji_grid);
        mGrid.setOnItemClickListener(new gridItemClickListener());

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

            mGrid.setAdapter(new ArrayAdapter(context, R.layout.item_radical));

            if (mMessageFlipper.getDisplayedChild() == 0) {
                mMessageFlipper.showNext();
            }
        }
    }

    private void loadKanjiList(ItemsList list) {
            Collections.sort(list, new Comparator<BaseItem>() {
                public int compare(BaseItem item1, BaseItem item2) {
                    return Float.valueOf((item1.getLevel() + "")).compareTo(Float.valueOf(item2.getLevel() + ""));
                }
            });

            mKanjiAdapter = new KanjiAdapter(context, list, R.layout.header_level, R.layout.item_kanji);
            mGrid.setAdapter(mKanjiAdapter);

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

    private class gridItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            BaseItem kanjiItem = mKanjiAdapter.getItem(position);

            Intent intent = new Intent(getActivity(), ItemDetailsActivity.class);
            intent.putExtra(ItemDetailsActivity.ARG_ITEM, kanjiItem);
            getActivity().startActivity(intent);
        }
    }

}