package com.thunsaker.brevos.app;

import android.os.Bundle;
import android.view.Window;

import com.thunsaker.android.common.dagger.BaseActivity;

public class BaseBrevosActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);
    }

    @Override
    protected Object[] getActivityModules() {
        return new Object[] {
                new BrevosActivityModule(this)
        };
    }

    public void showProgress() {
        setProgressBarVisibility(true);
        setProgressBarIndeterminate(true);
    }

    public void hideProgress() {
        setProgressBarVisibility(false);
    }
}
