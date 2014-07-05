package com.thunsaker.android.common.dagger;

import dagger.ObjectGraph;

public interface Injector {
    void inject(Object object);
    ObjectGraph getObjectGraph();
}
