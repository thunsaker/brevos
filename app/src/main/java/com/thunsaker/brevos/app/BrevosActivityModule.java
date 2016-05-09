package com.thunsaker.brevos.app;

import android.app.Activity;
import android.content.Context;

import com.thunsaker.android.common.annotations.ForActivity;
import com.thunsaker.brevos.adapters.LinkListAdapter;
import com.thunsaker.brevos.ui.AboutActivity;
import com.thunsaker.brevos.ui.BitlyAuthActivity;
import com.thunsaker.brevos.ui.BrevosPopOverList;
import com.thunsaker.brevos.ui.ConfirmSignOutDialogFragment;
import com.thunsaker.brevos.ui.HistoryActivity;
import com.thunsaker.brevos.ui.LinkFragment;
import com.thunsaker.brevos.ui.LinkInfoActivity;
import com.thunsaker.brevos.ui.LinkInfoActivityNfc;
import com.thunsaker.brevos.ui.LinkSearchActivity;
import com.thunsaker.brevos.ui.MainActivity;
import com.thunsaker.brevos.ui.SettingsActivity;
import com.thunsaker.brevos.ui.ShortenUrlReceiver;
import com.thunsaker.brevos.ui.WizardActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        complete = true,
        library = true,
        addsTo = BrevosAppModule.class,
        injects = {
                MainActivity.class,
                BitlyAuthActivity.class,
                ShortenUrlReceiver.class,
                BrevosPopOverList.class,
                LinkInfoActivity.class,
                LinkInfoActivityNfc.class,
                SettingsActivity.class,
                ConfirmSignOutDialogFragment.class,
                LinkFragment.class,
                HistoryActivity.class,
                LinkSearchActivity.class,
                AboutActivity.class,
                WizardActivity.class,
                LinkListAdapter.class
        }
)
public class BrevosActivityModule {
    private final Activity mActivity;

    public BrevosActivityModule(Activity activity) {
        mActivity = activity;
    }

    @Provides
    @Singleton
    @ForActivity
    Context providesActivityContext() {
        return mActivity;
    }

    @Provides
    @Singleton
    Activity providesActivity() {
        return mActivity;
    }
}
