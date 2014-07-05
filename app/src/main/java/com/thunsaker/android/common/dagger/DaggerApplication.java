package com.thunsaker.android.common.dagger;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

import dagger.ObjectGraph;

public abstract class DaggerApplication extends Application implements Injector {
    private ObjectGraph mObjectGraph;

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidApplicationModule baseApplicationModule = new AndroidApplicationModule();
        baseApplicationModule.xApplicationContext = this.getApplicationContext();

        List<Object> modules = new ArrayList<Object>();
        modules.add(baseApplicationModule);
        modules.addAll(getAppModules());

        mObjectGraph = ObjectGraph.create(modules.toArray());
        mObjectGraph.inject(this);
    }

    protected abstract List<Object> getAppModules();

    @Override
    public void inject(Object object) {
        mObjectGraph.inject(object);
    }

    @Override
    public ObjectGraph getObjectGraph() {
        return mObjectGraph;
    }
}
