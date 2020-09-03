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
import tr.xip.wanikani.wkamodels.VocabularyList;
import tr.xip.wanikani.apimodels.UserData;
import tr.xip.wanikani.apimodels.UserRequest;
import tr.xip.wanikani.utils.Utils;
import tr.xip.wanikani.widget.adapter.VocabularyAdapter;

public class VocabularyFragment extends Fragment implements LevelPickerDialogFragment.LevelDialogListener,
        SwipeRefreshLayout.OnRefreshListener {

    Context context;

    TextView mMessageTitle;
    TextView mMessageSummary;
    ImageView mMessageIcon;
    ViewFlipper mMessageFlipper;

    StickyGridHeadersGridView mGrid;
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

        mGrid = (StickyGridHeadersGridView) rootView.findViewById(R.id.vocabulary_grid);
        mGrid.setOnItemClickListener(new gridItemClickListener());

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

            mGrid.setAdapter(new ArrayAdapter(context, R.layout.item_radical));

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

        mVocabularyAdapter = new VocabularyAdapter(context, list, R.layout.header_level, R.layout.item_kanji);
        mGrid.setAdapter(mVocabularyAdapter);

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
            BaseItem vocabularyItem = mVocabularyAdapter.getItem(position);
            Intent intent = new Intent(getActivity(), ItemDetailsActivity.class);
            intent.putExtra(ItemDetailsActivity.ARG_ITEM, vocabularyItem);
            getActivity().startActivity(intent);
        }
    }
}