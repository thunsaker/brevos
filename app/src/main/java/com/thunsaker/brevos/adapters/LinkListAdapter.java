package com.thunsaker.brevos.adapters;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.thunsaker.R;
import com.thunsaker.android.common.util.Util;
import com.thunsaker.brevos.data.api.LinkHistoryItem;

import java.util.List;

import javax.inject.Inject;

public class LinkListAdapter extends RecyclerView.Adapter<LinkListAdapter.ViewHolder> {
    @Inject
    Picasso mPicasso;

    public Context mContext;
    public List<LinkHistoryItem> mItems;

    public LinkListAdapter(List<LinkHistoryItem> items) {
        mItems = items;
    }

    public void add(int position, LinkHistoryItem item) {
        mItems.add(position, item);
        notifyItemInserted(position);
    }

    public void addAll(List<LinkHistoryItem> items) {
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    public void remove(LinkHistoryItem item) {
        int position = mItems.indexOf(item);
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.link_history_item_compact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final LinkHistoryItem item = mItems.get(position);
        
        final String title = item.title != null && item.title.length() > 0
                ? item.title : null;
        final String shortUrl = item.link != null && item.link.length() > 0
                ? item.link.trim() : null;
        final String aggregateUrl =
                item.aggregate_link != null && item.aggregate_link.length() > 0
                ? item.aggregate_link.trim() : null;
        final String longUrl = item.long_url != null && item.long_url.length() > 0
                ? item.long_url : null;

        holder.itemView.setBackgroundResource(R.drawable.ripple_green);
        
        if (title != null) {
            holder.textViewTitle.setText(title);
            holder.textViewTitle.setVisibility(View.VISIBLE);
        } else {
            if (shortUrl != null) {
                holder.textViewTitle.setText(shortUrl.replace("http://", "").replace("https://", ""));
                holder.textViewTitle.setVisibility(View.VISIBLE);
            }
        }

        if (longUrl != null) {
            holder.textViewLongUrl.setText(longUrl.replace("http://", "").replace("https://", ""));
        }

        // TODO: Wire up the click count again
        holder.textViewClickCount.setText(String.format("%s", Util.randInt(0, 100)));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void clear() {
        mItems.clear();
        notifyDataSetChanged();
    }

    private static OnItemClickListener listener;
    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewTitle;
        public TextView textViewLongUrl;
        public TextView textViewClickCount;
        public RelativeLayout relativeLayoutWrapper;

        public ViewHolder(View view) {
            super(view);

            textViewTitle = (TextView) view.findViewById(R.id.textViewHistoryTitle);
            textViewLongUrl = (TextView) view.findViewById(R.id.textViewHistoryUrl);
            textViewClickCount = (TextView) view.findViewById(R.id.textViewHistoryClicks);
            relativeLayoutWrapper =
                    (RelativeLayout) view.findViewById(R.id.relativeLayoutHistoryWrapper);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener != null)
                        listener.onItemClick(view, getLayoutPosition());
                }
            });

            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        view.findViewById(R.id.relativeLayoutHistoryWrapper)
                                .getBackground()
                                .setHotspot(motionEvent.getX(), motionEvent.getY());
                    }
                    return false;
                }
            });
        }
    }
}
