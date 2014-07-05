package com.thunsaker.android.common.dagger;

import android.content.Context;

import com.thunsaker.android.common.annotations.ForApplication;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.greenrobot.event.EventBus;

@Module(
        complete = false,
        library = true,
        injects = {
        }
)
public class AndroidApplicationModule {
    static Context xApplicationContext = null;

    @Provides
    @Singleton
    @ForApplication
    Context provideApplicationContext() {
        return xApplicationContext;
    }

    @Provides
    @Singleton
    EventBus provideEventBus() {
        return new EventBus();
    }
}