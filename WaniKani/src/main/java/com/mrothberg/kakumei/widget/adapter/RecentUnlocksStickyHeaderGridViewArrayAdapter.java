package com.mrothberg.kakumei.widget.adapter;

//import com.tonicartos.widget.stickygridheaders.StickyGridHeadersSimpleArrayAdapter;


/**
 * Created by xihsa_000 on 3/14/14.
 */

public class RecentUnlocksStickyHeaderGridViewArrayAdapter {
//public class RecentUnlocksStickyHeaderGridViewArrayAdapter extends StickyGridHeadersSimpleArrayAdapter<UnlockItem> {

//
//    Context context;
//    Typeface typeface;
//
//    View mUnlockType;
//    TextView mUnlockCharacter;
//    ImageView mUnlockCharacterImage;
//    TextView mUnlockTime;
//
//    int headerResourceId;
//
//    private List<UnlockItem> items;
//
//    public RecentUnlocksStickyHeaderGridViewArrayAdapter(Context context, List<UnlockItem> objects, int headerResId, int itemResId) {
//        super(context, objects, headerResId, itemResId);
//        this.items = objects;
//        this.context = context;
//        this.typeface = new Fonts().getKanjiFont(context);
//        this.headerResourceId = headerResId;
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        View v = convertView;
//
//        UnlockItem item = items.get(position);
//
//        if (v == null) {
//            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            v = inflater.inflate(R.layout.item_recent_unlock_grid, null);
//        }
//
//        ((FrameLayout) v).setLayoutAnimation(Animations.FadeInController());
//
//        mUnlockType = v.findViewById(R.id.item_recent_unlock_type);
//        mUnlockCharacter = (TextView) v.findViewById(R.id.item_recent_unlock_character);
//        mUnlockCharacterImage = (ImageView) v.findViewById(R.id.item_recent_unlock_character_image);
//        mUnlockTime = (TextView) v.findViewById(R.id.item_recent_unlock_time);
//
//        mUnlockCharacter.setTypeface(typeface);
//
//        if (item.getType() == BaseItem.ItemType.RADICAL) {
//            mUnlockType.setBackgroundResource(R.drawable.oval_radical);
//        }
//
//        if (item.getType() == BaseItem.ItemType.KANJI) {
//            mUnlockType.setBackgroundResource(R.drawable.oval_kanji);
//        }
//
//        if (item.getType() == BaseItem.ItemType.VOCABULARY) {
//            mUnlockType.setBackgroundResource(R.drawable.oval_vocabulary);
//        }
//
//        if (item.getImage() == null) {
//            mUnlockCharacter.setVisibility(View.VISIBLE);
//            mUnlockCharacterImage.setVisibility(View.GONE);
//            mUnlockCharacter.setText(item.getCharacter());
//        } else {
//            mUnlockCharacter.setVisibility(View.GONE);
//            mUnlockCharacterImage.setVisibility(View.VISIBLE);
//            Picasso.with(context)
//                    .load(item.getImage())
//                    .into(mUnlockCharacterImage);
//            mUnlockCharacterImage.setColorFilter(context.getResources().getColor(R.color.text_gray), Mode.SRC_ATOP);
//        }
//
//        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
//        mUnlockTime.setText(sdf.format(item.getUnlockDate()));
//
//        return v;
//    }
//
//    @Override
//    public long getHeaderId(int position) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(new Date(getItem(position).getUnlockDate()));
//        calendar.set(Calendar.HOUR, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MILLISECOND, 0);
//
//        return calendar.getTimeInMillis();
//    }
//
//    @Override
//    public View getHeaderView(int position, View convertView, ViewGroup parent) {
//        HeaderViewHolder holder;
//        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//        if (convertView == null) {
//            convertView = inflater.inflate(headerResourceId, parent, false);
//            holder = new HeaderViewHolder();
//            holder.textView = (TextView) convertView.findViewById(R.id.header_text);
//            convertView.setTag(holder);
//        } else {
//            holder = (HeaderViewHolder) convertView.getTag();
//        }
//
//        UnlockItem item = getItem(position);
//
//        SimpleDateFormat sdf = new SimpleDateFormat("d MMMM, yyyy");
//
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(new Date(item.getUnlockDate()));
//        calendar.set(Calendar.HOUR, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MILLISECOND, 0);
//
//        holder.textView.setText(sdf.format(calendar.getTimeInMillis()));
//
//        return convertView;
//    }
//
//    protected class HeaderViewHolder {
//        public TextView textView;
//    }
}