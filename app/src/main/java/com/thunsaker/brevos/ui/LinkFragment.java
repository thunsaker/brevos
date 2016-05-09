package com.thunsaker.brevos.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.thunsaker.R;
import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.brevos.adapters.HistoryListAdapter;
import com.thunsaker.brevos.app.BaseBrevosFragment;
import com.thunsaker.brevos.app.BrevosApp;
import com.thunsaker.brevos.data.api.LinkHistoryItem;
import com.thunsaker.brevos.data.events.GetClicksEvent;
import com.thunsaker.brevos.data.events.GetUserHistoryEvent;
import com.thunsaker.brevos.services.BitlyClient;
import com.thunsaker.brevos.services.BitlyTasks;

import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

public class LinkFragment extends BaseBrevosFragment
        implements AbsListView.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener,
        AbsListView.OnScrollListener {

    @Inject @ForApplication Context mContext;
    @Inject EventBus mBus;

    private static final String ARG_COUNT = "count";
    private static final String ARG_LIST_TYPE = "type";

    private int mCount;
    public static int mOffsetQuantity = 0;
    private int mListType;
    private List<LinkHistoryItem> mList;
    public boolean mNewList = true;

    private OnFragmentInteractionListener mClickListener;
    private OnFragmentListViewScrollListener mScrollListener;

    private AbsListView mListView;
    private HistoryListAdapter mAdapter;
    private SwipeRefreshLayout mSwipeLayout;
    private int refreshCount;

    /**
     *
     * @param count Number of items to load
     * @param listType List Type either {@link com.thunsaker.brevos.services.BitlyTasks#HISTORY_LIST_TYPE_DEFAULT} or
     *                 {@link com.thunsaker.brevos.services.BitlyTasks#HISTORY_LIST_TYPE_COMPACT} or
     *                 {@link com.thunsaker.brevos.services.BitlyTasks#HISTORY_LIST_TYPE_SEARCH}
     * @return {@link com.thunsaker.brevos.ui.LinkFragment}
     */
    public static LinkFragment newInstance(int count, int listType) {
        LinkFragment fragment = new LinkFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COUNT, count);
        args.putInt(ARG_LIST_TYPE, listType);
        fragment.setArguments(args);
        return fragment;
    }

    public LinkFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mCount = getArguments().getInt(ARG_COUNT);
            mListType = getArguments().getInt(ARG_LIST_TYPE);
        }

        mBus.register(this);

        if(mSwipeLayout != null)
            mSwipeLayout.setRefreshing(true);

        if(mListType == BitlyTasks.HISTORY_LIST_TYPE_DEFAULT)
            setHasOptionsMenu(true);
        else
            setHasOptionsMenu(false);

        BitlyTasks mBitlyTasks = new BitlyTasks((BrevosApp) getActivity().getApplication());
        mBitlyTasks.new GetUserHistory(mCount, mOffsetQuantity * mCount, "", mListType).execute();
        mNewList = true;

        // TODO: Revisit Trending?
//        } else {
//            BitlyTasks mBitlyTasks = new BitlyTasks((BrevosApp)getActivity().getApplication());
//            mBitlyTasks.new GetTrendingLinks(mCount).execute();
//            mNewList = true;
//        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.history, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_filter)
            return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_link, container, false);

        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeLayoutLinkList);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(R.color.primary, R.color.accent);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
        LinearLayout progressWrapper = (LinearLayout) view.findViewById(android.R.id.empty);
        ((AdapterView<ListAdapter>) mListView).setEmptyView(progressWrapper);

        mListView.setOnItemClickListener(this);
        mListView.setOnScrollListener(this);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mClickListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                + " must implement OnFragmentInteractionListener");
        }

        try {
            mScrollListener = (OnFragmentListViewScrollListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentListViewScrollListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mClickListener = null;

        if(mBus.isRegistered(this))
            mBus.unregister(this);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mClickListener)
            mClickListener.onFragmentInteraction(mList.get(position).aggregate_link);
    }

    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyText instanceof TextView)
            ((TextView) emptyView).setText(emptyText);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) { }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if(mScrollListener != null)
            mScrollListener.onFragmentListViewScrollListener(view, firstVisibleItem, visibleItemCount, totalItemCount);
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(String id);
    }

    public interface OnFragmentListViewScrollListener {
        public void onFragmentListViewScrollListener(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount);
    }

    public void onEvent(GetUserHistoryEvent event) {
        if(event != null) {
            if(event.listType != BitlyTasks.HISTORY_LIST_TYPE_SEARCH) {
                if (mSwipeLayout != null)
                    mSwipeLayout.setRefreshing(false);

                if (event.userHistoryList != null && event.userHistoryList.size() > 0) {
                    mList = event.userHistoryList;

                    GetClickCountsForList(event.userHistoryList);

//                    if (event.listType == BitlyTasks.HISTORY_LIST_TYPE_COMPACT) {
//                        if (mNewList) {
//                            mAdapter = new HistoryListAdapter(mContext, mList, event.listType);
//                            if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
//                                ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
//                        }
//
//                        if (mListView.getClass() == ListView.class) {
//                            if (((ListView) mListView).getHeaderViewsCount() == 0) {
//                                View headerView = LayoutInflater.from(mContext).inflate(R.layout.link_list_header, null);
//
//                                Button showButton = (Button) headerView.findViewById(R.id.buttonLinkListHeaderSeeAll);
//                                showButton.setVisibility(View.VISIBLE);
//                                showButton.setOnClickListener(new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        Intent history = new Intent(mContext, HistoryActivity.class);
//                                        history.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                        mContext.startActivity(history);
//                                    }
//                                });
//                                ((ListView) mListView).addHeaderView(headerView, null, false);
//                            }
//                        } else {
//                            View headerView = getActivity().findViewById(R.id.linearLayoutLinkListHeaderWrapper);
//
//                            Button showButton = (Button) headerView.findViewById(R.id.buttonLinkListHeaderSeeAll);
//                            showButton.setVisibility(View.VISIBLE);
//                            showButton.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    Intent history = new Intent(mContext, HistoryActivity.class);
//                                    history.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                    mContext.startActivity(history);
//                                }
//                            });
//
//                            LinearLayout footerWrapper = (LinearLayout) getActivity().findViewById(R.id.linearLayoutHistoryFooterWrapper);
//                            footerWrapper.setVisibility(View.GONE);
//                        }
//                        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
//                            if (mNewList) {
//                                ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
//                            }
//                        }
//                    } else {
                        if (mNewList) {
                            mAdapter = new HistoryListAdapter(mContext, mList, event.listType);
                            if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                                ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
                        } else {
                            for (LinkHistoryItem item : event.userHistoryList) {
                                mAdapter.add(item);
                            }
                        }

                        if (mListView.getClass() == ListView.class) {
                            if (((ListView) mListView).getFooterViewsCount() == 0) {
                                View footerView = LayoutInflater.from(mContext).inflate(R.layout.link_list_footer, null);

                                Button moreButton = (Button) footerView.findViewById(R.id.buttonHistoryLoadMore);
                                moreButton.setText(String.format(getString(R.string.history_load_more), mCount));
                                moreButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mOffsetQuantity++;
                                        BitlyTasks mBitlyTasks = new BitlyTasks((BrevosApp) getActivity().getApplication());
                                        mBitlyTasks.new GetUserHistory(mCount, mOffsetQuantity * mCount, "", BitlyTasks.HISTORY_LIST_TYPE_DEFAULT).execute();
                                        mNewList = false;
                                        mSwipeLayout.setRefreshing(true);
                                    }
                                });
                                ((ListView) mListView).addFooterView(footerView, null, false);
                            }
                        } else {
                            LinearLayout footerWrapper = (LinearLayout) getActivity().findViewById(R.id.linearLayoutHistoryFooterWrapper);
                            footerWrapper.setVisibility(View.VISIBLE);

                            Button moreButton = (Button) getActivity().findViewById(R.id.buttonHistoryLoadMore);
                            moreButton.setText(String.format(getString(R.string.history_load_more), mCount));
                            moreButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mOffsetQuantity++;
                                    BitlyTasks mBitlyTasks = new BitlyTasks((BrevosApp) getActivity().getApplication());
                                    mBitlyTasks.new GetUserHistory(mCount, mOffsetQuantity * mCount, "", BitlyTasks.HISTORY_LIST_TYPE_DEFAULT).execute();
                                    mNewList = false;
                                    mSwipeLayout.setRefreshing(true);
                                }
                            });
                        }

                        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                            if (mNewList) {
                                ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
                            }
                        }
//                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.error_loading_history), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(mContext, getString(R.string.error_loading_history), Toast.LENGTH_SHORT).show();
        }
    }

    private void GetClickCountsForList(List<LinkHistoryItem> userHistoryList) {
        refreshCount = 0;
        for (LinkHistoryItem item : userHistoryList) {
            BitlyTasks mBitlyTasks = new BitlyTasks((BrevosApp) getActivity().getApplication());
            mBitlyTasks.new GetClickCountTotal(item.link, BitlyClient.CLICKS_DESTINATION_LIST, false).execute();
        }
    }

    public void onEvent(GetClicksEvent event) {
        if(event.destination == BitlyClient.CLICKS_DESTINATION_LIST) {
            if(mList != null && mList.size() > 0) {
                for (LinkHistoryItem item : mList) {
                    if(item.link.equals(event.link)) {
                        item.clicks = event.count;
                        if(++refreshCount % 5 == 1) {
                            mAdapter.notifyDataSetChanged();
                        }
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void onRefresh() {
        if(mListType == BitlyTasks.HISTORY_LIST_TYPE_COMPACT) {
            BitlyTasks mBitlyTasks= new BitlyTasks((BrevosApp)getActivity().getApplication());
            mBitlyTasks.new GetUserHistory(MainActivity.COMPACT_LINK_LIST_COUNT, 0, "", BitlyTasks.HISTORY_LIST_TYPE_COMPACT).execute();
        } else {
            BitlyTasks mBitlyTasks= new BitlyTasks((BrevosApp)getActivity().getApplication());
            if(mOffsetQuantity == 0) {
                mBitlyTasks.new GetUserHistory(mCount, mCount * mOffsetQuantity, "", BitlyTasks.HISTORY_LIST_TYPE_DEFAULT).execute();
            } else {
                mBitlyTasks.new GetUserHistory(mCount * mOffsetQuantity, 0, "", BitlyTasks.HISTORY_LIST_TYPE_DEFAULT).execute();
            }
        }

        mNewList = true;

        if(mSwipeLayout != null)
            mSwipeLayout.setRefreshing(true);
    }

//    public interface AddToListHeader {
//        public void AddToListHeader(Bitmark link);
//    }
}