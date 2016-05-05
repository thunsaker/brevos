package com.thunsaker.brevos.ui;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
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

public class LinkSearchActivity extends BaseBrevosActivity {

    @Inject
    @ForApplication
    Context mContext;

    @Inject
    SearchManager mSearchManager;

    @Inject
    EventBus mBus;

    @BindView(R.id.linearLayoutHistorySearchMainWrapper) LinearLayout mLayoutWrapper;
    @BindView(R.id.search_view) SearchView mSearchView;
    @BindView(R.id.imageButtonSearch) ImageButton mButtonSearchBack;
    @BindView(R.id.frameLayoutSearchCloseContainer) FrameLayout mButtonClearSearchContainer;
    @BindView(R.id.imageButtonSearchClear) ImageButton mButtonClearSearch;

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

        ButterKnife.bind(this);

        setupSearchView();

//        mButtonSearchBack.setImageDrawable(
//                VectorDrawableCompat.create(getResources(), R.drawable.back_arrow, getTheme()));

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

    private void setupSearchView() {
        mSearchView.setIconified(false);
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setFocusable(true);
        mSearchView.requestFocusFromTouch();

        mSearchView.setSearchableInfo(mSearchManager.getSearchableInfo(getComponentName()));
        mSearchView.setQueryHint(getString(R.string.search_hint));
        mSearchView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        mSearchView.setImeOptions(mSearchView.getImeOptions() | EditorInfo.IME_ACTION_SEARCH |
                EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchQuery = query;
                StartSearch(true);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(mButtonClearSearchContainer.getVisibility() == View.GONE)
                    mButtonClearSearchContainer.setVisibility(View.VISIBLE);

                if(newText.length() >= 3) {
                    searchQuery = newText;
                    StartSearch(false);
                }
                return false;
            }
        });
    }

    private void StartSearch(boolean hideKeyboard) {
        if(hideKeyboard) {
            mSearchView.clearFocus();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

        if(mList != null)
            mList.clear();

        if(mAdapter != null)
            mAdapter.notifyDataSetInvalidated();

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
            bitlyTasks.new GetUserHistory(mCount, mOffsetQuantity * mCount, searchQuery,
                    BitlyTasks.HISTORY_LIST_TYPE_SEARCH, privateFlag, archiveFlag).execute();
        }
    }

    public void onEvent(GetUserHistoryEvent event) {
        if (event != null) {
            if (event.listType == BitlyTasks.HISTORY_LIST_TYPE_SEARCH) {
                if (event.userHistoryList != null && event.userHistoryList.size() > 0) {
                    mList = event.userHistoryList;
                    mAdapter = new HistoryListAdapter(mContext, mList,
                            BitlyTasks.HISTORY_LIST_TYPE_SEARCH);
                    mListViewResults.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();
                    mNoResults.setVisibility(View.GONE);
                } else {
                    mListWrapper.setVisibility(View.GONE);
                    mNoResults.setVisibility(View.VISIBLE);
                    mNoResults.startAnimation(
                            AnimationUtils.loadAnimation(mContext, R.anim.fade_in));
                }
            }
        } else {
            Toast.makeText(mContext, getString(R.string.error_loading_search), Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.imageButtonSearch)
    protected void closeSearch() {
        finish();
    }

    @OnClick(R.id.imageButtonSearchClear)
    protected void clearSearch() {
        mSearchView.setQuery("", false);
        if(mList != null)
            mList.clear();
        if(mAdapter != null)
            mAdapter.notifyDataSetChanged();

        mButtonClearSearchContainer.setVisibility(View.GONE);

        Snackbar.make(mLayoutWrapper, "Clearing Search", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        closeSearch();
    }


    @OnClick({R.id.checkBoxSearchArchiveOnly, R.id.checkBoxSearchPrivateOnly})
    protected void searchArchiveOnly() {
        if(searchQuery != null && searchQuery.length() > 0)
            StartSearch(true);
    }
}