package com.mrothberg.kakumei.widget.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.codewaves.stickyheadergrid.StickyHeaderGridAdapter;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mrothberg.kakumei.R;
import com.mrothberg.kakumei.app.activity.ItemDetailsActivity;
import com.mrothberg.kakumei.utils.Animations;
import com.mrothberg.kakumei.utils.Fonts;
import com.mrothberg.kakumei.wkamodels.BaseItem;

public class VocabularyAdapter extends StickyHeaderGridAdapter {
    Map<Integer, List<BaseItem>> map = new LinkedHashMap<>();
    List<Integer> levelsZeroIndexed;
    Typeface typeface;
    Context context;
    public static final String TAG = "VocabularyAdapter";

    public VocabularyAdapter(Context context, List<BaseItem> list) {
        this.context = context;
        this.typeface = new Fonts().getKanjiFont(context);

        //TODO: clean this up
        for (BaseItem item : list) {
            int level = item.getLevel();

            if (map.get(level) != null) {
                map.get(level).add(item);
            } else {
                List<BaseItem> newList = new ArrayList<>();
                newList.add(item);
                map.put(level, newList);
            }
        }

        this.levelsZeroIndexed = new ArrayList<>(map.keySet());
    }

    @Override
    public int getSectionCount() {
        return map.size();
    }

    @Override
    public int getSectionItemCount(int section) {
        int levelZeroIndexed = levelsZeroIndexed.get(section);
        return map.get(levelZeroIndexed).size();
    }

    @Override
    public HeaderViewHolder onCreateHeaderViewHolder(ViewGroup parent, int headerType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_level, parent, false);
        return new VocabLevelHeaderViewHolder(view);
    }

    @Override
    public ItemViewHolder onCreateItemViewHolder(ViewGroup parent, int itemType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vocabulary, parent, false);
        return new VocabItemViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(HeaderViewHolder viewHolder, int section) {
        final VocabLevelHeaderViewHolder holder = (VocabLevelHeaderViewHolder) viewHolder;
        holder.textView.setText(levelsZeroIndexed.get(section).toString());
    }

    @Override
    public void onBindItemViewHolder(ItemViewHolder viewHolder, int section, int offset) {
        final VocabItemViewHolder holder = (VocabItemViewHolder) viewHolder;

        int levelZeroIndexed = levelsZeroIndexed.get(section);
        BaseItem kanjiItem = map.get(levelZeroIndexed).get(offset);

        holder.card.setLayoutAnimation(Animations.FadeInController());

        holder.character.setText(kanjiItem.getCharacter());
        holder.character.setTypeface(typeface);

        if (!kanjiItem.isUnlocked()) {
            holder.vocabInnerLayout.setBackgroundColor(context.getResources().getColor(android.R.color.white));
            holder.status.setBackgroundResource(R.drawable.pattern_diagonal_xml);
        } else if (kanjiItem.isBurned()) {
            holder.vocabInnerLayout.setBackgroundColor(context.getResources().getColor(R.color.wanikani_burned));
            holder.status.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        } else {
            holder.vocabInnerLayout.setBackgroundColor(context.getResources().getColor(android.R.color.white));
            holder.status.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        }

        if (kanjiItem.isUnlocked()) {
            if (kanjiItem.getSrsLevel().equals("apprentice"))
                holder.srs.setBackgroundResource(R.drawable.oval_apprentice);
            if (kanjiItem.getSrsLevel().equals("guru"))
                holder.srs.setBackgroundResource(R.drawable.oval_guru);
            if (kanjiItem.getSrsLevel().equals("master"))
                holder.srs.setBackgroundResource(R.drawable.oval_master);
            if (kanjiItem.getSrsLevel().equals("enlighten"))
                holder.srs.setBackgroundResource(R.drawable.oval_enlightened);
            if (kanjiItem.getSrsLevel().equals("burned"))
                holder.srs.setBackgroundResource(R.drawable.oval_burned);
        } else
            holder.srs.setBackgroundResource(R.drawable.oval_disabled);

        String readingsString = kanjiItem.getKana();
        if (readingsString != null) {
            String[] readings = readingsString.split(",");
            holder.kana.setText(readings[0]);
            holder.kana.setTypeface(typeface);
        }

        String[] meanings = kanjiItem.getMeaning().split(",");

        holder.meaning.setText(WordUtils.capitalize(meanings[0]));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int section = getAdapterPositionSection(holder.getAdapterPosition());
                int offset = getItemSectionOffset(section, holder.getAdapterPosition());

                int level = levelsZeroIndexed.get(section);
                BaseItem item = map.get(level).get(offset);

                Intent intent = new Intent(context, ItemDetailsActivity.class);
                intent.putExtra(ItemDetailsActivity.ARG_ITEM, item);
                context.startActivity(intent);
            }
        });
    }

    protected static class VocabItemViewHolder extends ItemViewHolder {
        public FrameLayout card;
        public LinearLayout vocabInnerLayout;
        public TextView character;
        public TextView meaning;
        public TextView kana;
        public View status;
        public View srs;

        public VocabItemViewHolder(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.item_vocabulary_card);
            vocabInnerLayout = itemView.findViewById(R.id.item_vocabulary_inner_layout);
            status = itemView.findViewById(R.id.item_vocabulary_status);
            character = itemView.findViewById(R.id.item_vocabulary_character);
            meaning = itemView.findViewById(R.id.item_vocabulary_meaning);
            kana = itemView.findViewById(R.id.item_vocabulary_kana);
            srs = itemView.findViewById(R.id.item_vocabulary_srs_level);
        }
    }

    protected static class VocabLevelHeaderViewHolder extends HeaderViewHolder {
        public TextView textView;

        public VocabLevelHeaderViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.header_level);
        }
    }
}
