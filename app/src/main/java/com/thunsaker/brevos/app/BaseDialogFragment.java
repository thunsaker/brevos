package com.thunsaker.brevos.app;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.thunsaker.android.common.dagger.DaggerApplication;
import com.thunsaker.android.common.dagger.Injector;

import dagger.ObjectGraph;

public abstract class BaseDialogFragment extends DialogFragment implements Injector {

    private ObjectGraph mActivityGraph;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerApplication daggerApplication = (DaggerApplication) getActivity().getApplication();
        mActivityGraph = daggerApplication.getObjectGraph().plus(getActivityModules());
        mActivityGraph.inject(this);
    }

    @Override
    public void onDestroy() {
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