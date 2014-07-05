package com.thunsaker.brevos.data;

import android.app.Application;
import android.util.Log;

import com.squareup.okhttp.HttpResponseCache;
import com.squareup.okhttp.OkHttpClient;
import com.thunsaker.brevos.data.api.ApiModule;

import java.io.File;
import java.io.IOException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        includes = ApiModule.class,
        complete = false,
        library = true
)
public class DataModule {

    static final int DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50MB

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(Application app) {
        return createOkHttpClient(app);
    }

    static OkHttpClient createOkHttpClient(Application app) {
        OkHttpClient client = new OkHttpClient();

        try {
            File cacheDir = new File(app.getCacheDir(), "http");
            HttpResponseCache cache = new HttpResponseCache(cacheDir, DISK_CACHE_SIZE);
            client.setResponseCache(cache);
        } catch (IOException e) {
            Log.e("DataModule", "Unable to instal disk cache. Error: " + e.getMessage());
        }

        return client;
    }
}
