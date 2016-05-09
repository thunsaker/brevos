package com.thunsaker.brevos.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.thunsaker.R;
import com.thunsaker.brevos.data.api.LinkHistoryItem;

import java.util.List;

import javax.inject.Inject;

public class LinkListAdapter extends RecyclerView.Adapter<LinkListAdapter.ViewHolder> {
    @Inject
    Picasso mPicasso;

    public Context mContext;
    public List<LinkHistoryItem> mItems;

    public LinkListAdapter(Context context, List<LinkHistoryItem> items) {
        mItems = items;
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

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
