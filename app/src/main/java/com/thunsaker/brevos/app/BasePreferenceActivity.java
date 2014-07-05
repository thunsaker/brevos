package com.thunsaker.brevos.app;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.thunsaker.android.common.dagger.DaggerApplication;
import com.thunsaker.android.common.dagger.Injector;

import dagger.ObjectGraph;

public abstract class BasePreferenceActivity extends PreferenceActivity implements Injector {

    private ObjectGraph mActivityGraph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerApplication daggerApplication = (DaggerApplication) getApplication();
        mActivityGraph = daggerApplication.getObjectGraph().plus(getActivityModules());
        mActivityGraph.inject(this);
    }

    @Override
    protected void onDestroy() {
        mActivityGraph = null;
        super.onDestroy();
    }

    @Override
    public void inject(Object object) {
        mActivityGraph.inject(object);
    }

    @Override
    public ObjectGraph getObjectGraph() {
        return mActivityGraph;
    }

    protected abstract Object[] getActivityModules();
}
