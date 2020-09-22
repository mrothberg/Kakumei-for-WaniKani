package tr.xip.wanikani.widget.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.codewaves.stickyheadergrid.StickyHeaderGridAdapter;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import tr.xip.wanikani.R;
import tr.xip.wanikani.app.activity.ItemDetailsActivity;
import tr.xip.wanikani.utils.Animations;
import tr.xip.wanikani.utils.Fonts;
import tr.xip.wanikani.wkamodels.BaseItem;

public class KanjiAdapter extends StickyHeaderGridAdapter {

    Map<Integer, List<BaseItem>> map = new LinkedHashMap<>();
    List<Integer> levelsZeroIndexed;
    Typeface typeface;
    Context context;
    public static final String TAG = "KanjiAdapter";

    public KanjiAdapter(Context context, List<BaseItem> list) {
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
    public StickyHeaderGridAdapter.HeaderViewHolder onCreateHeaderViewHolder(ViewGroup parent, int headerType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_level, parent, false);
        return new KanjiLevelViewHolder(view);
    }

    @Override
    public ItemViewHolder onCreateItemViewHolder(ViewGroup parent, int itemType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_kanji, parent, false);
        return new KanjiViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(HeaderViewHolder viewHolder, int section) {
        final KanjiLevelViewHolder holder = (KanjiLevelViewHolder) viewHolder;
        holder.textView.setText(levelsZeroIndexed.get(section).toString());
    }

    @Override
    public void onBindItemViewHolder(ItemViewHolder viewHolder, int section, int offset) {
        final KanjiViewHolder holder = (KanjiViewHolder) viewHolder;

        int levelZeroIndexed = levelsZeroIndexed.get(section);
        BaseItem kanjiItem = map.get(levelZeroIndexed).get(offset);

        holder.character.setText(kanjiItem.getCharacter());
        holder.character.setTypeface(typeface);

        holder.card.setLayoutAnimation(Animations.FadeInController());

        if (!kanjiItem.isUnlocked()) {
            holder.kanjiInnerLayout.setBackgroundColor(context.getResources().getColor(android.R.color.white));
            holder.status.setBackgroundResource(R.drawable.pattern_diagonal_xml);
        } else if (kanjiItem.isBurned()) {
            holder.kanjiInnerLayout.setBackgroundColor(context.getResources().getColor(R.color.wanikani_burned));
            holder.status.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        } else {
            holder.kanjiInnerLayout.setBackgroundColor(context.getResources().getColor(android.R.color.white));
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

        if (kanjiItem.getImportantReading().equals("onyomi"))
            holder.reading.setText(kanjiItem.getOnyomi());
        else
            holder.reading.setText(kanjiItem.getKunyomi());

        holder.reading.setTypeface(typeface);

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

    protected static class KanjiViewHolder extends ItemViewHolder {
        public FrameLayout card;
        public RelativeLayout kanjiInnerLayout;
        public TextView character;
        public TextView meaning;
        public TextView reading;
        public View status;
        public View srs;

        public KanjiViewHolder(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.item_kanji_card);
            kanjiInnerLayout = itemView.findViewById(R.id.item_kanji_inner_layout);
            status = itemView.findViewById(R.id.item_kanji_status);
            character = itemView.findViewById(R.id.item_kanji_character);
            meaning = itemView.findViewById(R.id.item_kanji_meaning);
            reading = itemView.findViewById(R.id.item_kanji_reading);
            srs = itemView.findViewById(R.id.item_kanji_srs_level);
        }
    }

    protected static class KanjiLevelViewHolder extends HeaderViewHolder {
        public TextView textView;

        public KanjiLevelViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.header_level);
        }
    }
}
