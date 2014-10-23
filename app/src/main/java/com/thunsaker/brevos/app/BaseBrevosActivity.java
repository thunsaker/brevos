package com.thunsaker.brevos.app;

import android.os.Bundle;

import com.thunsaker.android.common.dagger.BaseActivity;

public class BaseBrevosActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_PROGRESS);
//        supportRequestWindowFeature(Window.FEATURE_PROGRESS);
    }

    @Override
    protected Object[] getActivityModules() {
        return new Object[] {
                new BrevosActivityModule(this)
        };
    }

    public void showProgress() {
//        setProgressBarVisibility(true);
//        setSupportProgressBarVisibility(true);
//        setProgressBarIndeterminate(true);
//        setSupportProgressBarIndeterminate(true);
    }

    public void hideProgress() {
//        setProgressBarVisibility(false);
//        setSupportProgressBarIndeterminate(false);
    }
}
