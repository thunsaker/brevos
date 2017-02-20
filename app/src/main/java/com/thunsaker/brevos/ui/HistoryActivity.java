package com.thunsaker.brevos.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.FrameLayout;

import com.thunsaker.R;
import com.thunsaker.brevos.app.BaseBrevosActivity;
import com.thunsaker.brevos.services.BitlyTasks;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class HistoryActivity extends BaseBrevosActivity implements LinkFragment.OnFragmentInteractionListener, LinkFragment.OnFragmentListViewScrollListener {
    @Inject EventBus mBus;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.container)
    FrameLayout mContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

//        ActionBar ab = getSupportActionBar();
//        ab.setIcon(getResources().getDrawable(R.drawable.ic_launcher_flat_white));
//        ab.setTitle(getString(R.string.title_activity_history));
//        ab.setDisplayUseLogoEnabled(true);
//        ab.setDisplayShowHomeEnabled(true);
//        ab.setDisplayHomeAsUpEnabled(true);
//        ab.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.ic_up_affordance_white));

        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        setTitle(R.string.title_activity_history);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (savedInstanceState == null) {
            LinkFragment linkFragmentHistory = LinkFragment.newInstance(20, BitlyTasks.HISTORY_LIST_TYPE_DEFAULT);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, linkFragmentHistory).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
    }

    @Override
    public void onFragmentInteraction(String id) { }

    public void ShowProgress() {
        showProgress();
    }

    public void HideProgress() {
        hideProgress();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if(mBus.isRegistered(LinkFragment.class))
//            mBus.unregister(LinkFragment.class);
    }

    @Override
    public void onFragmentListViewScrollListener(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) { }
}