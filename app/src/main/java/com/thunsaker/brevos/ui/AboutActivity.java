package com.thunsaker.brevos.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.mikepenz.aboutlibraries.LibsBuilder;
import com.thunsaker.R;
import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.brevos.app.BaseBrevosActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AboutActivity extends BaseBrevosActivity {

    @Inject @ForApplication
    Context mContext;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @OnClick(R.id.button_about_open_source)
    public void showOpenSourceLicences() {
        new LibsBuilder()
                .withLibraries("AboutLibraries", "gson", "jodatime", "dagger", "picasso", "okhttp",
                        "retrofit", "butterknife", "eventbus", "twitter-text", "swipetodismissnoa",
                        "PrettySharedPreferences")
                .withAutoDetect(true)
                .withLicenseShown(true)
                .withVersionShown(true)
                .withActivityTitle(getString(R.string.title_activity_about))
                .withActivityTheme(R.style.Theme_Brevos)
                .start(AboutActivity.this);
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