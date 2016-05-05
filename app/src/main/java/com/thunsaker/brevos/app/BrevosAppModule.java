package com.thunsaker.brevos.app;

import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.Context;
import android.support.v7.appcompat.BuildConfig;

import com.squareup.picasso.Picasso;
import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.android.common.dagger.AndroidApplicationModule;
import com.thunsaker.brevos.BrevosPrefsManager;
import com.thunsaker.brevos.services.BitlyClient;
import com.thunsaker.brevos.services.BitlyService;
import com.thunsaker.brevos.services.BitlyTasks;
import com.thunsaker.brevos.ui.MainActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

@Module(
        complete = true,
        library = true,
        addsTo = AndroidApplicationModule.class,
        injects = {
                BrevosApp.class,
                MainActivity.class,
                BitlyTasks.class
        }
)
public class BrevosAppModule {
    public BrevosAppModule() {}

    @Provides
    @Singleton
    BrevosPrefsManager providesBrevosPrefsManager(@ForApplication Context mContext) {
        return new BrevosPrefsManager(mContext.getSharedPreferences("bitdroid_prefs", Context.MODE_PRIVATE));
    }

    @Provides
    @Singleton
    BitlyService providesBitlyService() {
        final RequestInterceptor requestInterceptor = new RequestInterceptor() {

            @Override
            public void intercept(RequestFacade request) {
                request.addQueryParam("format", "json");
            }
        };

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(BitlyClient.BITLY_BASE_URL)
                .setRequestInterceptor(requestInterceptor)
                .setLogLevel(RestAdapter.LogLevel.BASIC)
                .build();

        return restAdapter.create(BitlyService.class);
    }

    @Provides
    Picasso providesPicasso(@ForApplication Context context) {
        Picasso picasso = Picasso.with(context);

        picasso.setDebugging(BuildConfig.DEBUG);
        return picasso;
    }

    @Provides
    @Singleton
    android.text.ClipboardManager providesLegacyClipboardManager(@ForApplication Context context) {
        return (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @Provides
    @Singleton
    NotificationManager providesNotificationManager(@ForApplication Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Provides
    @Singleton
    SearchManager providesSearchManager(@ForApplication Context context) {
        return (SearchManager) context.getSystemService(Context.SEARCH_SERVICE);
    }
}
