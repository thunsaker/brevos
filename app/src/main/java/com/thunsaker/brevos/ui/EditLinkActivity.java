package com.thunsaker.brevos.ui;

import android.content.Context;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.thunsaker.R;
import com.thunsaker.android.common.annotations.ForActivity;
import com.thunsaker.brevos.app.BaseBrevosActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditLinkActivity extends BaseBrevosActivity {

    @Inject
    @ForActivity
    Context mContext;

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.appbarCreate) AppBarLayout mAppBarLayout;
    @BindView(R.id.editTextCreateUrl) EditText mEditTextUrl;
    @BindView(R.id.spinnerCreateDomain) Spinner mSpinnerDomain;
    @BindView(R.id.switchPrivate) MaterialAnimatedSwitch mSwitchPrivate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.slide_up, R.anim.no_animation);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_link);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupSpinner();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create, menu);

        return super.onCreateOptionsMenu(menu);
    }

    private void setupSpinner() {
        // TODO: IF they have custom domains, add them to the list, maybe auto-select their custom domain?
        ArrayAdapter<CharSequence> domainAdapter =
                ArrayAdapter.createFromResource(mContext,
                        R.array.bitly_domain_options,
                        android.R.layout.simple_spinner_item);
        domainAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerDomain.setAdapter(domainAdapter);
    }

    @OnClick(R.id.switchPrivate)
    public void onClickCheckboxPrivate() {
        Window window = this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        if(mSwitchPrivate.isChecked()) {
            TransitionDrawable transitionDrawable =
                    (TransitionDrawable) mAppBarLayout.getBackground();
            transitionDrawable.startTransition(500);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.setStatusBarColor(getResources().getColor(R.color.gray_darkish));
            }
        } else {
            TransitionDrawable transitionDrawable =
                    (TransitionDrawable) mAppBarLayout.getBackground();
            transitionDrawable.reverseTransition(500);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.setStatusBarColor(getResources().getColor(R.color.brevos_pink_dark));
            }
        }
    }
}
