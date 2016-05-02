package com.thunsaker.brevos.ui;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.thunsaker.R;
import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.brevos.adapters.HistoryListAdapter;
import com.thunsaker.brevos.app.BaseBrevosActivity;
import com.thunsaker.brevos.app.BrevosApp;
import com.thunsaker.brevos.data.api.LinkHistoryItem;
import com.thunsaker.brevos.data.events.GetUserHistoryEvent;
import com.thunsaker.brevos.services.BitlyTasks;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class LinkSearchActivity extends BaseBrevosActivity { //implements LinkFragment.OnFragmentInteractionListener {

    @Inject @ForApplication Context mContext;
    @Inject SearchManager mSearchManager;
    @Inject EventBus mBus;

    @BindView(R.id.editTextSearchQuery) @Nullable EditText mEditTextSearch;
    @BindView(R.id.imageButtonSearch) @Nullable ImageButton mButtonSearch;

    @BindView(R.id.checkBoxSearchArchiveOnly) CheckBox mCheckBoxArchive;
    @BindView(R.id.checkBoxSearchPrivateOnly) CheckBox mCheckBoxPrivate;
    @BindView(R.id.frameLayoutHistorySearchListContainer) FrameLayout mListWrapper;
    @BindView(R.id.listViewHistorySearchResults) ListView mListViewResults;
    @BindView(R.id.linearLayoutHistorySearchResultsEmpty) LinearLayout mEmptyList;
    @BindView(R.id.linearLayoutHistorySearchNoResults) LinearLayout mNoResults;

    private int mCount = 20;
    public static int mOffsetQuantity = 0;

    public String searchQuery;
    private List<LinkHistoryItem> mList;
    private HistoryListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_search);

        SetupActionBar();

        ButterKnife.bind(this);

        mListViewResults.setAdapter(mAdapter);
        mListViewResults.setEmptyView(mEmptyList);

        mCheckBoxPrivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mCheckBoxArchive.isChecked())
                    mCheckBoxArchive.setChecked(false);

                mCheckBoxPrivate.setChecked(isChecked);
            }
        });

        mCheckBoxArchive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mCheckBoxPrivate.isChecked())
                    mCheckBoxPrivate.setChecked(false);

                mCheckBoxArchive.setChecked(isChecked);
            }
        });

        mBus.register(this);
    }

    private void SetupActionBar() {
        ActionBar ab = getSupportActionBar();
        ab.setIcon(getResources().getDrawable(R.drawable.ic_launcher_flat_white));
        ab.setDisplayUseLogoEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.ic_up_affordance_white));
        ab.setDisplayShowCustomEnabled(true);
        ab.setCustomView(R.layout.search_layout_actionview);

        View view = ab.getCustomView();
        if(view != null) {
            EditText editTextSearch = (EditText) view.findViewById(R.id.editTextSearchQuery);
            if(editTextSearch != null) {
                editTextSearch.requestFocus();
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                editTextSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if(actionId == 3) {
                            StartSearch();
                            return true;
                        }
                        return false;
                    }
                });
            }
        }
    }

    @OnClick(R.id.imageButtonSearch)
    public void searchButtonClick() {
        StartSearch();
    }

    private void StartSearch() {
        mEditTextSearch.clearFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        if(mList != null)
            mList.clear();

        if(mAdapter != null)
            mAdapter.notifyDataSetInvalidated();

        if(mEditTextSearch != null && mEditTextSearch.getText().toString().trim().length() > 0) {
            searchQuery = mEditTextSearch.getText().toString().trim();
        }

        if(searchQuery.length() > 0) {
            mList = null;
            mListWrapper.setVisibility(View.VISIBLE);

            int privateFlag = BitlyTasks.HISTORY_PRIVATE_DEFAULT;
            if(mCheckBoxPrivate.isChecked())
                privateFlag = BitlyTasks.HISTORY_PRIVATE_ONLY;

            int archiveFlag = BitlyTasks.HISTORY_ARCHIVE_DEFAULT;
            if(mCheckBoxArchive.isChecked())
                archiveFlag = BitlyTasks.HISTORY_ARCHIVE_ONLY;

            // TODO: DropDown for search
//            switch (mSpinnerPrivate.getSelectedItemPosition()) {
//                case 0:
//                    privateFlag = BitlyTasks.HISTORY_PRIVATE_EXCLUDE;
//                    break;
//                case 2:
//                    privateFlag = BitlyTasks.HISTORY_PRIVATE_ONLY;
//                    break;
//                default:
//                    privateFlag = BitlyTasks.HISTORY_PRIVATE_INCLUDE;
//                    break;
//            }
//
//            switch (mSpinnerArchive.getSelectedItemPosition()) {
//                case 1:
//                    archiveFlag = BitlyTasks.HISTORY_ARCHIVE_INCLUDE;
//                    break;
//                case 2:
//                    archiveFlag = BitlyTasks.HISTORY_ARCHIVE_ONLY;
//                    break;
//                default:
//                    archiveFlag = BitlyTasks.HISTORY_ARCHIVE_EXCLUDE;
//                    break;
//            }

            BitlyTasks bitlyTasks = new BitlyTasks((BrevosApp) mContext);
            bitlyTasks.new GetUserHistory(mCount, mOffsetQuantity * mCount, searchQuery, BitlyTasks.HISTORY_LIST_TYPE_SEARCH, privateFlag, archiveFlag).execute();
        }
    }

    public void onEvent(GetUserHistoryEvent event) {
        if (event != null) {
            if (event.listType == BitlyTasks.HISTORY_LIST_TYPE_SEARCH) {
//                if (mSwipeLayout != null)
//                    mSwipeLayout.setRefreshing(false);

                if (event.userHistoryList != null && event.userHistoryList.size() > 0) {
                    mList = event.userHistoryList;
                    mAdapter = new HistoryListAdapter(mContext, mList, BitlyTasks.HISTORY_LIST_TYPE_SEARCH);
                    mListViewResults.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();
                    mNoResults.setVisibility(View.GONE);
                } else {
                    mListWrapper.setVisibility(View.GONE);
                    mNoResults.setVisibility(View.VISIBLE);
                    mNoResults.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in));
                }
            }
        } else {
            Toast.makeText(mContext, getString(R.string.error_loading_search), Toast.LENGTH_SHORT).show();
        }
    }
}