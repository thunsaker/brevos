package com.thunsaker.brevos.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.thunsaker.R;
import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.brevos.app.BaseBrevosActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WizardActivity extends BaseBrevosActivity {

    @Inject
    @ForApplication
    Context mContext;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.wizard_pager) ViewPager mPager;

    private PagerAdapter mPagerAdapter;

    private static final int NUM_PAGES = 3;
    private boolean show_previous = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wizard);

        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

//        ActionBar ab = getSupportActionBar();
//        ab.setIcon(getResources().getDrawable(R.drawable.ic_launcher_flat_white));
//        ab.setTitle(R.string.title_activity_wizard);

        mPagerAdapter = new WizardPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.wizard, menu);

//        menu.findItem(R.id.action_previous).setVisible(show_previous);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_next:
                return handleNextButton();
            default:
                return false;
        }
    }

    @Override
    public void onBackPressed() {
        handlePreviousButton();
    }

    private void handlePreviousButton() {
        if(mPager.getCurrentItem() == 0)
            super.onBackPressed();
        else
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
    }

    private boolean handleNextButton() {
        if(mPager.getCurrentItem() == 2) {
            PreferencesHelper.setBrevosWelcomeWizard(mContext, true);
            finish();
        } else {
            mPager.setCurrentItem(mPager.getCurrentItem() + 1);
            show_previous = true;
            supportInvalidateOptionsMenu();
        }

        return true;
    }

    private class WizardPagerAdapter extends FragmentStatePagerAdapter {
        public WizardPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return WizardFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    public static class WizardFragment extends Fragment {
        private int pos = 0;
        private static final String ARG_SECTION_NUMBER = "section_number";

        public static WizardFragment newInstance(int sectionNumber) {
            WizardFragment fragment = new WizardFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public WizardFragment() { }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            pos = getArguments().getInt(ARG_SECTION_NUMBER);

            View rootView = inflater.inflate(R.layout.fragment_wizard_page_brevos, container, false);

            switch (pos) {
                case 1:
                    rootView = inflater.inflate(R.layout.fragment_wizard_page_about, container, false);
                    break;
                case 2:
                    rootView = inflater.inflate(R.layout.fragment_wizard_page_features, container, false);
                    break;
                default:
                    break;
            }
            return rootView;
        }
    }
}
