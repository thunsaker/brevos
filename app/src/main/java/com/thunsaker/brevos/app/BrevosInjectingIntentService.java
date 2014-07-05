package com.thunsaker.brevos.app;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.thunsaker.android.common.dagger.Injector;

import java.util.ArrayList;
import java.util.List;

import dagger.ObjectGraph;

import static com.google.common.base.Preconditions.checkState;

@Deprecated
public class BrevosInjectingIntentService extends IntentService implements Injector {
    private Context mContext;
    private ObjectGraph mObjectGraph;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public BrevosInjectingIntentService(String name) {
        super(name);
        mObjectGraph = ((Injector) getApplication()).getObjectGraph().plus(getModules().toArray());
        mObjectGraph.inject(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    @Override
    public void inject(Object target) {
        checkState(mObjectGraph != null, "object graph must be initialized prior to calling inject");
        mObjectGraph.inject(target);
    }

    protected List<Object> getModules() {
        List<Object> result = new ArrayList<Object>();
        return result;
    }

    @Override
    public ObjectGraph getObjectGraph() {
        return mObjectGraph;
    }
}
