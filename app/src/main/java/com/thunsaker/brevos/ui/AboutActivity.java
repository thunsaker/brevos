package com.thunsaker.brevos.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;

import com.thunsaker.R;
import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.brevos.app.BaseBrevosActivity;
import com.tundem.aboutlibraries.Libs;
import com.tundem.aboutlibraries.ui.LibsCompatActivity;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class AboutActivity extends BaseBrevosActivity {

    @Inject @ForApplication
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ButterKnife.inject(this);

        ActionBar ab = getSupportActionBar();
        ab.setIcon(getResources().getDrawable(R.drawable.ic_launcher_flat_white));
        ab.setTitle(R.string.action_about);
        ab.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.ic_up_affordance_white));
    }

    @OnClick(R.id.button_about_open_source)
    public void showOpenSourceLicences() {
        Intent openSourceIntent = new Intent(mContext, LibsCompatActivity.class);
        openSourceIntent.putExtra(Libs.BUNDLE_FIELDS, Libs.toStringArray(R.string.class.getFields()));
        openSourceIntent.putExtra(Libs.BUNDLE_LIBS, new String[]{"AboutLibraries", "gson", "jodatime", "dagger", "picasso", "okhttp", "retrofit", "butterknife", "eventbus", "twitter-text", "swipetodismissnoa"});

        openSourceIntent.putExtra(Libs.BUNDLE_VERSION, true);
        openSourceIntent.putExtra(Libs.BUNDLE_LICENSE, true);
        openSourceIntent.putExtra(Libs.BUNDLE_TITLE, getString(R.string.about_open_source_licences));
        openSourceIntent.putExtra(Libs.BUNDLE_THEME, R.style.Theme_Brevos);
        openSourceIntent.putExtra(Libs.BUNDLE_TRANSLUCENT_DECOR, false);
        openSourceIntent.putExtra(Libs.BUNDLE_ICON, R.drawable.ic_launcher_flat_white);
        openSourceIntent.putExtra(Libs.BUNDLE_ICON_UP_AFFORDANCE, R.drawable.ic_up_affordance_white);

        startActivity(openSourceIntent);
    }

    @OnClick(R.id.button_about_other_apps)
    public void otherAppsButton() {
        startActivity(new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://search?q=pub:Thomas+Hunsaker")));
    }

    @OnClick(R.id.button_about_welcome)
    public void showWelcomeWizard() {
        startActivity(new Intent(mContext, WizardActivity.class));
    }
}