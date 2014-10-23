package com.thunsaker.brevos.app;

import com.crashlytics.android.Crashlytics;
import com.thunsaker.android.common.dagger.DaggerApplication;

import io.fabric.sdk.android.Fabric;
import java.util.Collections;
import java.util.List;

public class BrevosApp extends DaggerApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());    }

    @Override
    protected List<Object> getAppModules() {
        return Collections.<Object>singletonList(new BrevosAppModule());
    }
}
