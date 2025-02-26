package com.mrothberg.kakumei.widget.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import com.mrothberg.kakumei.R;
import com.mrothberg.kakumei.wkamodels.BaseItem;
import com.mrothberg.kakumei.wkamodels.CriticalItem;

/**
 * Created by xihsa_000 on 3/14/14.
 */
public class CriticalItemsAdapter extends ArrayAdapter<CriticalItem> {

    Context context;
    Typeface typeface;

    View mItemType;
    TextView mItemCharacter;
    ImageView mItemCharacterImage;
    TextView mItemPercentage;

    private List<CriticalItem> items;

    public CriticalItemsAdapter(Context context, int textViewResourceId, List<CriticalItem> objects, Typeface typeface) {
        super(context, textViewResourceId, objects);
        this.items = objects;
        this.context = context;
        this.typeface = typeface;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        CriticalItem item = items.get(position);

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.item_critical, null);
        }

        mItemType = v.findViewById(R.id.item_critical_type);
        mItemCharacter = (TextView) v.findViewById(R.id.item_critical_character);
        mItemCharacterImage = (ImageView) v.findViewById(R.id.item_critical_character_image);
        mItemPercentage = (TextView) v.findViewById(R.id.item_critical_percentage);

        mItemCharacter.setTypeface(typeface);

        if (item.getType() == BaseItem.ItemType.RADICAL) {
            mItemType.setBackgroundResource(R.drawable.oval_radical);
        }

        if (item.getType() == BaseItem.ItemType.KANJI) {
            mItemType.setBackgroundResource(R.drawable.oval_kanji);
        }

        if (item.getType() == BaseItem.ItemType.VOCABULARY) {
            mItemType.setBackgroundResource(R.drawable.oval_vocabulary);
        }

        if (item.getImage() == null) {
            mItemCharacter.setVisibility(View.VISIBLE);
            mItemCharacterImage.setVisibility(View.GONE);
            mItemCharacter.setText(item.getCharacter());
        } else {
            mItemCharacter.setVisibility(View.GONE);
            mItemCharacterImage.setVisibility(View.VISIBLE);
            Picasso.with(context)
                    .load(item.getImage())
                    .into(mItemCharacterImage);
            mItemCharacterImage.setColorFilter(context.getResources().getColor(R.color.text_gray), PorterDuff.Mode.SRC_ATOP);
        }

        mItemCharacter.setText(item.getCharacter());
        mItemPercentage.setText(item.getPercentage() + "");

        return v;
    }
}