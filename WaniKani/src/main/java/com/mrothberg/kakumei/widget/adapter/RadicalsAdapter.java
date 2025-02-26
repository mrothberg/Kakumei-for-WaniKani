package com.mrothberg.kakumei.widget.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.codewaves.stickyheadergrid.StickyHeaderGridAdapter;
import com.squareup.picasso.Picasso;

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

public class RadicalsAdapter extends StickyHeaderGridAdapter {

    Map<Integer, List<BaseItem>> map = new LinkedHashMap<>();
    List<Integer> levelsZeroIndexed;
    Typeface typeface;
    Context context;
    public static final String TAG = "RadicalsAdapter";

    public RadicalsAdapter(Context context, List<BaseItem> list) {
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
        return new RadicalLevelHeaderViewHolder(view);
    }

    @Override
    public ItemViewHolder onCreateItemViewHolder(ViewGroup parent, int itemType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_radical, parent, false);
        return new RadicalItemViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(HeaderViewHolder viewHolder, int section) {
        final RadicalLevelHeaderViewHolder holder = (RadicalLevelHeaderViewHolder) viewHolder;
        holder.textView.setText(levelsZeroIndexed.get(section).toString());
    }

    @Override
    public void onBindItemViewHolder(ItemViewHolder viewHolder, int section, int offset) {
        final RadicalItemViewHolder holder = (RadicalItemViewHolder) viewHolder;

        int levelZeroIndexed = levelsZeroIndexed.get(section);
        BaseItem radicalItem = map.get(levelZeroIndexed).get(offset);

        holder.card.setLayoutAnimation(Animations.FadeInController());

        holder.character.setTypeface(this.typeface);

        if (radicalItem.getImage() == null) {
            holder.character.setVisibility(View.VISIBLE);
            holder.image.setVisibility(View.GONE);
            holder.character.setText(radicalItem.getCharacter());
        } else {
            holder.character.setVisibility(View.GONE);
            holder.image.setVisibility(View.VISIBLE);
            Picasso.with(context)
                    .load(radicalItem.getImage())
                    .into(holder.image);

            holder.image.setColorFilter(context.getResources().getColor(R.color.text_gray), PorterDuff.Mode.SRC_ATOP);
        }

        if (!radicalItem.isUnlocked()) {
            holder.radicalInnerLayout.setBackgroundColor(context.getResources().getColor(android.R.color.white));
            holder.status.setBackgroundResource(R.drawable.pattern_diagonal_xml);
        } else if (radicalItem.isBurned()) {
            holder.radicalInnerLayout.setBackgroundColor(context.getResources().getColor(R.color.wanikani_burned));
            holder.status.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        } else {
            holder.radicalInnerLayout.setBackgroundColor(context.getResources().getColor(android.R.color.white));
            holder.status.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        }

        if (radicalItem.isUnlocked() && !radicalItem.getSrsLevel().equals("")) {
            if (radicalItem.getSrsLevel().equals("apprentice"))
                holder.srs.setBackgroundResource(R.drawable.oval_apprentice);
            if (radicalItem.getSrsLevel().equals("guru"))
                holder.srs.setBackgroundResource(R.drawable.oval_guru);
            if (radicalItem.getSrsLevel().equals("master"))
                holder.srs.setBackgroundResource(R.drawable.oval_master);
            if (radicalItem.getSrsLevel().equals("enlighten"))
                holder.srs.setBackgroundResource(R.drawable.oval_enlightened);
            if (radicalItem.getSrsLevel().equals("burned"))
                holder.srs.setBackgroundResource(R.drawable.oval_burned);
        } else
            holder.srs.setBackgroundResource(R.drawable.oval_disabled);

        holder.meaning.setText(WordUtils.capitalize((radicalItem.getMeaning())));

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

    protected static class RadicalItemViewHolder extends ItemViewHolder {
        public FrameLayout card;
        public RelativeLayout radicalInnerLayout;
        public TextView character;
        public ImageView image;
        public TextView meaning;
        public View status;
        public View srs;

        public RadicalItemViewHolder(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.item_radical_card);
            radicalInnerLayout = itemView.findViewById(R.id.item_radical_inner_layout);
            status = itemView.findViewById(R.id.item_radical_status);
            character = itemView.findViewById(R.id.item_radical_character);
            image = itemView.findViewById(R.id.item_radical_character_image);
            meaning = itemView.findViewById(R.id.item_radical_meaning);
            srs = itemView.findViewById(R.id.item_radical_srs_level);
        }
    }

    protected static class RadicalLevelHeaderViewHolder extends HeaderViewHolder {
        public TextView textView;

        public RadicalLevelHeaderViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.header_level);
        }
    }
}
