package com.mrothberg.kakumei.app.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.ArrayList;
import java.util.List;

import com.mrothberg.kakumei.R;
import com.mrothberg.kakumei.app.fragment.DashboardFragment;
import com.mrothberg.kakumei.app.fragment.card.ProgressCard;
import com.mrothberg.kakumei.app.fragment.card.ProgressCardNoTitle;
import com.mrothberg.kakumei.client.WaniKaniAPIV1Interface;
import com.mrothberg.kakumei.database.DatabaseManager;
import com.mrothberg.kakumei.wkamodels.BaseItem;
import com.mrothberg.kakumei.wkamodels.ItemsList;
import com.mrothberg.kakumei.wkamodels.KanjiList;
import com.mrothberg.kakumei.wkamodels.RadicalsList;
import com.mrothberg.kakumei.apimodels.UserData;
import com.mrothberg.kakumei.widget.adapter.RemainingKanjiAdapter;
import com.mrothberg.kakumei.widget.adapter.RemainingRadicalsAdapter;

/**
 * Created by Hikari on 9/18/14.
 */
public class ProgressDetailsActivity extends AppCompatActivity implements ProgressCard.ProgressCardListener {
    private final String TAG = "ProgressDetailsActivity";
    Toolbar mToolbar;

    List<BaseItem> mRemainingRadicals = new ArrayList<>();
    List<BaseItem> mRemainingKanji = new ArrayList<>();

    GridView mRadicalsGrid;
    GridView mKanjiGrid;

    ViewFlipper mRadicalsFlipper;
    ViewFlipper mKanjiFlipper;

    ViewFlipper mRadicalsMessageFlipper;
    ViewFlipper mKanjiMessageFlipper;

    TextView mRadicalsMessageText;
    TextView mKanjiMessageText;

    CardView mRadicalsCard;
    CardView mKanjiCard;

    private LoadState radicalsLoaded = LoadState.NOT_LOADED;
    private LoadState kanjiLoaded = LoadState.NOT_LOADED;

    private WaniKaniAPIV1Interface waniKaniAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_details);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRadicalsGrid = (GridView) findViewById(R.id.progress_details_radicals_grid);
        mKanjiGrid = (GridView) findViewById(R.id.progress_details_kanji_grid);

        mRadicalsFlipper = (ViewFlipper) findViewById(R.id.progress_details_radicals_flipper);
        mKanjiFlipper = (ViewFlipper) findViewById(R.id.progress_details_kanji_flipper);

        mRadicalsMessageFlipper = (ViewFlipper) findViewById(R.id.progress_details_radicals_message_flipper);
        mKanjiMessageFlipper = (ViewFlipper) findViewById(R.id.progress_details_kanji_message_flipper);

        mRadicalsMessageText = (TextView) findViewById(R.id.progress_details_radicals_message_text);
        mKanjiMessageText = (TextView) findViewById(R.id.progress_details_kanji_message_text);

        mRadicalsGrid.setOnItemClickListener(new RadicalsItemClickListener());
        mKanjiGrid.setOnItemClickListener(new KanjiItemClickListener());

        mRadicalsCard = (CardView) findViewById(R.id.progress_details_radicals_card);
        mKanjiCard = (CardView) findViewById(R.id.progress_details_kanji_card);

        Fragment mProgressCard = getSupportFragmentManager().
                findFragmentById(R.id.progress_details_progress_card);

        waniKaniAPI = (WaniKaniAPIV1Interface) getIntent().getSerializableExtra(DashboardFragment.WANIKANI_API_EXTRA);
        ((ProgressCardNoTitle) mProgressCard).setWaniKaniAPI(waniKaniAPI);

        //TODO: this data should be shared instead of making the same requests again (as when we send sync intent)
        ((ProgressCardNoTitle) mProgressCard).load();
        ((ProgressCardNoTitle) mProgressCard).setListener(this, this);

        loadData();
    }


    @Override
    public boolean onSupportNavigateUp() {
        super.onBackPressed();
        return true;
    }

    @Override
    public void onProgressCardSyncFinishedListener(String result) {
        /* empty */
    }

    private void loadData() {
        final UserData user = DatabaseManager.getUserV2();
        if (user == null) return;

        //TODO combine getting radicals and kanji lists in 1 api method
        waniKaniAPI.getRadicalsList(Integer.toString(user.level)).whenComplete((radicalItems, throwable) -> {
            if(throwable != null || radicalItems == null) {
                Log.e(TAG, "throwable", throwable);
                radicalItems = new RadicalsList();
                radicalItems.addAll(DatabaseManager.getItems(BaseItem.ItemType.RADICAL, new int[] {user.level}));
            }

            if (radicalItems.size() != 0) {
                loadRemainingRadicalList(radicalItems);
            } else {
                radicalsLoaded = LoadState.FAILED;
            }
        });

        waniKaniAPI.getKanjiList(Integer.toString(user.level)).whenComplete((kanjiList, throwable) -> {
            if(throwable != null || kanjiList == null) {
                Log.e("ProgressDetailsActivity", "throwable", throwable);
                kanjiList = new KanjiList();
                kanjiList.addAll(DatabaseManager.getItems(BaseItem.ItemType.KANJI, new int[] {user.level}));
            }

            if(kanjiList.size() != 0) {
                loadRemainingKanjiList(kanjiList);
            } else {
                kanjiLoaded = LoadState.FAILED;
            }

            displayData();
        });
    }

    //TODO: combine loadKanjiList() and loadRadicalList()
    private void loadRemainingKanjiList(ItemsList list) {
        for (BaseItem item : list) {
            if (item.isUnlocked()) {
                // TODO: existing bug - this should be checking if the item is passed, not what stage it's on
                //  as you can drop a radical/kanji down to apprentice but WaniKani considers it passed
                //  if previously guru'd
                if (item.getSrsLevel().equals("apprentice")) {
                    mRemainingKanji.add(item);
                }
            } else {
                mRemainingKanji.add(item);
            }
        }
        kanjiLoaded = LoadState.SUCCESS;
    }

    void loadRemainingRadicalList(ItemsList list) {
        for (BaseItem item : list) {
            if (item.isUnlocked()) {
                if (item.getSrsLevel().equals("apprentice")) {
                    mRemainingRadicals.add(item);
                }
            } else {
                mRemainingRadicals.add(item);
            }
        }
        radicalsLoaded = LoadState.SUCCESS;
    }

    private void displayData() {
        if (radicalsLoaded != LoadState.NOT_LOADED && kanjiLoaded != LoadState.NOT_LOADED) {
            if (radicalsLoaded == LoadState.SUCCESS) {
                if (mRemainingRadicals.size() > 0) {
                    mRadicalsGrid.setAdapter(
                            new RemainingRadicalsAdapter(
                                    ProgressDetailsActivity.this,
                                    R.layout.item_radical_remaining,
                                    mRemainingRadicals
                            )
                    );
                    mRadicalsCard.setVisibility(View.VISIBLE);
                } else {
                    mRadicalsCard.setVisibility(View.GONE);
                }
            } else {
                mRadicalsMessageText.setText(R.string.error_loading_items);
                if (mRadicalsMessageFlipper.getDisplayedChild() == 0)
                    mRadicalsMessageFlipper.showNext();
            }

            if (kanjiLoaded == LoadState.SUCCESS) {
                if (mRemainingKanji.size() > 0) {
                    mKanjiGrid.setAdapter(
                            new RemainingKanjiAdapter(
                                    ProgressDetailsActivity.this,
                                    R.layout.item_kanji_remaining,
                                    mRemainingKanji
                            )
                    );
                    mKanjiCard.setVisibility(View.VISIBLE);
                } else {
                    mKanjiCard.setVisibility(View.GONE);
                }
            } else {
                mKanjiMessageText.setText(R.string.error_loading_items);
                if (mKanjiMessageFlipper.getDisplayedChild() == 0)
                    mKanjiMessageFlipper.showNext();
            }

            if (mRadicalsFlipper.getDisplayedChild() == 0)
                mRadicalsFlipper.showNext();

            if (mKanjiFlipper.getDisplayedChild() == 0)
                mKanjiFlipper.showNext();
        }
    }

    private class RadicalsItemClickListener implements android.widget.AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            BaseItem item = mRemainingRadicals.get(position);

            Intent intent = new Intent(ProgressDetailsActivity.this, ItemDetailsActivity.class);
            intent.putExtra(ItemDetailsActivity.ARG_ITEM, item);
            startActivity(intent);
        }
    }

    private class KanjiItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            BaseItem item = mRemainingKanji.get(position);

            Intent intent = new Intent(ProgressDetailsActivity.this, ItemDetailsActivity.class);
            intent.putExtra(ItemDetailsActivity.ARG_ITEM, item);
            startActivity(intent);
        }
    }

    private enum LoadState {
        NOT_LOADED, SUCCESS, FAILED
    }
}
