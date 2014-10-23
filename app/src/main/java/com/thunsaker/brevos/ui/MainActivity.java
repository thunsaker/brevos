package com.thunsaker.brevos.ui;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.squareup.picasso.Picasso;
import com.thunsaker.BuildConfig;
import com.thunsaker.R;
import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.android.common.util.Util;
import com.thunsaker.brevos.app.BaseBrevosActivity;
import com.thunsaker.brevos.app.BrevosApp;
import com.thunsaker.brevos.data.api.Bitmark;
import com.thunsaker.brevos.data.events.BitlyAuthEvent;
import com.thunsaker.brevos.data.events.ExpandUrlEvent;
import com.thunsaker.brevos.data.events.ShortenedUrlEvent;
import com.thunsaker.brevos.services.BitlyClient;
import com.thunsaker.brevos.services.BitlyTasks;
import com.thunsaker.brevos.services.BitlyUtil;
import com.thunsaker.brevos.ui.custom.SwipeDismissTouchListener;

import java.util.TimeZone;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends BaseBrevosActivity implements LinkFragment.OnFragmentInteractionListener, LinkFragment.OnFragmentListViewScrollListener {

    @Inject @ForApplication Context mContext;
    @Inject EventBus mBus;
    @Inject Picasso mPicasso;

    @Inject android.text.ClipboardManager mClipboardManagerLegacy;
    @Inject NotificationManager mNotificationManager;

    @InjectView(R.id.buttonShortenUrl) ImageButton mImageButtonShortenUrl;
    @InjectView(R.id.editTextUrl) EditText mEditTextUrl;
    @InjectView(R.id.toggleButtonOptionsPrivateUrl) ToggleButton mTogglePrivate;

    @InjectView(R.id.linearLayoutAuthButtonWrapper) LinearLayout mAuthButtonWrapper;

    @InjectView(R.id.linearLayoutMainWrapperOuter) LinearLayout mMainLayoutWrapperOuter;
    @InjectView(R.id.relativeLayoutMainWrapperInner) RelativeLayout mMainLayoutWrapperInner;
    @InjectView(R.id.linearLayoutMainWrapper) LinearLayout mMainLayoutWrapper;

    @InjectView(R.id.relativeLayoutResultWrapper) RelativeLayout mResultLayoutWrapper;
    @InjectView(R.id.textViewShortenResultUrl) TextView mTextViewResultUrl;
    @InjectView(R.id.textViewOriginalUrl) TextView mTextViewOriginalUrl;
    @InjectView(R.id.imageViewFavicon) ImageView mImageViewFavicon;
    @InjectView(R.id.imageViewPrivaticon) ImageView mImageViewPrivaticon;

    @InjectView(R.id.imageButtonResultActionCopyUrl) ImageButton mButtonCopy;
    @InjectView(R.id.imageButtonResultActionInfo) ImageButton mButtonInfo;
    @InjectView(R.id.imageButtonResultActionShare) ImageButton mButtonShare;

    @InjectView(R.id.relativeLayoutExpandResultWrapper) RelativeLayout mExpandResultLayoutWrapper;
    @InjectView(R.id.textViewExpandResultUrl) TextView mTextViewExpandResultUrl;
    @InjectView(R.id.textViewExpandShortUrl) TextView mTextViewExpandShortUrl;

    @InjectView(R.id.imageButtonExpandResultCopy) ImageButton mButtonExpandCopy;
    @InjectView(R.id.imageButtonExpandResultBrowse) ImageButton mButtonExpandBrowse;

    @InjectView(R.id.linearLayoutMainUrlInClipboardWrapper) LinearLayout mClipboardWrapper;
    @InjectView(R.id.textViewMainClipboardText) TextView mTextViewClipboard;

    @InjectView(R.id.linearLayoutMainLinkListWrapper) LinearLayout mLinkListWrapper;

//    @InjectView(R.id.relativeLayoutFabWrapper) RelativeLayout mFabWrapper;
//    @InjectView(R.id.fabCreate) ImageButton mFabCreate;

    public static String BREVOS_POP_OVER_BITMARK = "BREVOS_POP_OVER_BITMARK";
    private static String BREVOS_CURRENT_BITMARK = "BREVOS_CURRENT_BITMARK";

    public static final int NOTIFICATION_BREVOS_SHORTEN = 0;
    public static String EXTRA_LONG_URL = "EXTRA_LONG_URL";

    protected static final String SIGN_OUT_CONFIRMATION_DIALOG = "SIGN_OUT_CONFIRMATION_DIALOG";
    public static String CLEAR_BITLY_DATA = "CLEAR";

    public static Boolean isBitlyConnected;
    private boolean isPrivate = false;
    private String domain = BitlyClient.BITLY_DOMAIN_DEFAULT;
    private static boolean ignoreClipboard = false;
    private CountDownTimer clipboardPromptCountDown;
    public static int COMPACT_LINK_LIST_COUNT = 20;

    private AdView adView;
    private Bitmark mCurrentBitmark;
    private PendingIntent retryPendingIntent;
    private PendingIntent genericPendingIntent;
    private boolean createHidden = false;
//    public static boolean mFabStateUp = false;

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!BuildConfig.DEBUG)
            Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_main);

        ActionBar ab = getSupportActionBar();
        ab.setIcon(getResources().getDrawable(R.drawable.ic_launcher_flat_white));
        ab.setTitle(null);

        ButterKnife.inject(this);

        if(android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            mImageButtonShortenUrl.setEnabled(true);
            mImageButtonShortenUrl.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_right_gray));
        } else {
            mImageButtonShortenUrl.setEnabled(false);
            mImageButtonShortenUrl.setImageDrawable(getResources().getDrawable(R.drawable.btn_arrow_right));
        }

        mEditTextUrl.addTextChangedListener(mTextEditorWatcher);
        mEditTextUrl.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == R.id.action_send_url) {
                    shortenButtonClick();
                    return true;
                }
                return false;
            }
        });

//        raiseFabCreate();

        isBitlyConnected = PreferencesHelper.getBitlyConnected(mContext);
        configureLayout();

        if(mBus != null && !mBus.isRegistered(this))
            mBus.register(this);

        if (getIntent().hasExtra(BREVOS_POP_OVER_BITMARK)) {
            String intentShortUrlRaw = getIntent().getStringExtra(BREVOS_POP_OVER_BITMARK);
            Bitmark bitmark = Bitmark.GetBitmarkFromJson(intentShortUrlRaw);
            mBus.post(new ShortenedUrlEvent(true, "", bitmark, BitlyClient.SHORTENED_ACTION_DEFAULT, bitmark.getLong_url()));
        } else {
            if(!PreferencesHelper.getBrevosWelcomeWizard(mContext)) {
                showWizardActivity();
                return;
            }

            if(getIntent().hasExtra(EXTRA_LONG_URL)) {
                mEditTextUrl.setText(getIntent().getStringExtra(EXTRA_LONG_URL));
            }

            if(savedInstanceState != null) {
                String currentBitmarkRaw = savedInstanceState.getString(MainActivity.BREVOS_CURRENT_BITMARK);
                if(currentBitmarkRaw != null) {
                    Bitmark currentBitmark = Bitmark.GetBitmarkFromJson(currentBitmarkRaw);
                    if (currentBitmark != null) {
                        mBus.post(new ShortenedUrlEvent(true, "", currentBitmark, BitlyClient.SHORTENED_ACTION_DEFAULT, currentBitmark.getLong_url()));
                    }
                }
            }

            // TODO: FAB for a future release
//            if(android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//                mFabWrapper.setVisibility(View.VISIBLE);
//                mFabCreate.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if (mFabStateUp) {
//                            shortenButtonClick();
//                        } else {
//                            showCreateLink();
//                            raiseFabCreate();
//                        }
//                    }
//                });
//            }
        }

        if(getIsPro())
            hideAds();
        else
            showAds();

        checkClipboardForUrl();
    }

    // TODO: FAB for a future release
//    @SuppressLint("InlinedApi")
//    private void raiseFabCreate() {
//        if(android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//            float newY = mMainLayoutWrapperInner.getY() + mMainLayoutWrapperInner.getMeasuredHeight() - (mFabWrapper.getMeasuredHeight() / 2) + 24;
//            mFabWrapper.animate().y(newY).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(200).rotation(360);
//            mFabCreate.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_right_white));
//            mFabStateUp = true;
//        }
//    }
//
//    @SuppressLint("InlinedApi")
//    private void dropFabCreate() {
//        if(android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//            float y = mFabWrapper.getY();
//            DisplayMetrics displayMetrics = new DisplayMetrics();
//            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//            float newY = displayMetrics.heightPixels - y - mFabCreate.getMeasuredHeight() - Util.convertDpToPixel(56f, mContext);
//            mFabWrapper.animate().y(newY).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(400).rotation(-360);
//            mFabCreate.setImageDrawable(getResources().getDrawable(R.drawable.ic_plus_white));
//            mFabStateUp = false;
//        }
//    }

    private void showWizardActivity() {
        startActivity(new Intent(mContext, WizardActivity.class));
    }

    private boolean getIsPro() {
        String appName = getPackageName();
        return appName.contains("Donate");
    }

    private void configureLayout() {
        if (!isBitlyConnected) {
            mAuthButtonWrapper.setVisibility(View.VISIBLE);
            mTogglePrivate.setVisibility(View.GONE);
            mLinkListWrapper.setVisibility(View.GONE);
        } else {
            mAuthButtonWrapper.setVisibility(View.GONE);
            if (PreferencesHelper.getBitlyIsPrivateAlways(mContext)) {
                isPrivate = true;
                mEditTextUrl.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.ic_lock_closed_tiny), null);
                mEditTextUrl.invalidate();
                mTogglePrivate.setVisibility(View.GONE);
            } else {
                mTogglePrivate.setVisibility(View.VISIBLE);
                mTogglePrivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        isPrivate = isChecked;
                    }
                });
            }

            showLinkFragment();
        }
    }

    private void showLinkFragment() {
        mLinkListWrapper.setVisibility(View.VISIBLE);
        FragmentManager fragmentManager = getSupportFragmentManager();
        LinkFragment linkFragmentHistory = LinkFragment.newInstance(getResources().getInteger(R.integer.recent_items_count), BitlyTasks.HISTORY_LIST_TYPE_COMPACT);
        fragmentManager
                .beginTransaction()
                .replace(R.id.frameListContentHistory, linkFragmentHistory)
                .commit();
    }

    private void removeLinkFragment() {
        // TODO: Removing the fragment is killing eventbus...figure out the reason, hiding it is just a temporary fix.
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        fragmentManager.beginTransaction().replace(R.id.frameListContentHistory, null).commit();
        findViewById(R.id.frameListContentHistory).setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkClipboardForUrl();

        if(mBus != null && !mBus.isRegistered(this))
            mBus.register(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.hasExtra(BREVOS_POP_OVER_BITMARK)) {
            String intentShortUrlRaw = intent.getStringExtra(BREVOS_POP_OVER_BITMARK);
            Bitmark bitmark = Bitmark.GetBitmarkFromJson(intentShortUrlRaw);
            openLinkInfoActivity(bitmark);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if(isBitlyConnected) {
            getMenuInflater().inflate(R.menu.main_authenticated, menu);

//            if(android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD && android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
//                getMenuInflater().inflate(R.menu.main_history, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.home:
                NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
                break;
            case R.id.action_settings:
                startActivity(new Intent(mContext, SettingsActivity.class));
                break;
            case R.id.action_about:
                startActivity(new Intent(mContext, AboutActivity.class));
                break;
            case R.id.action_sign_out:
                showConfirmationDialog();
                break;
            case R.id.action_bundle:
//                if(getIsPro()) {
//
//                } else {
//                    showUpsellDialog();
//                }
                Toast.makeText(mContext, "Bundles coming soon!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_search:
                startActivity(new Intent(mContext, LinkSearchActivity.class));
                break;

            case R.id.action_history:
                startActivity(new Intent(mContext, HistoryActivity.class));
                break;
        }
        return true;
    }

    private void showConfirmationDialog() {
        ConfirmSignOutDialogFragment confirmDialog = new ConfirmSignOutDialogFragment();
        confirmDialog.show(getSupportFragmentManager(), SIGN_OUT_CONFIRMATION_DIALOG);
    }

    private void clearBrevosSettings() {
        PreferencesHelper.setBitlyConnected(mContext, false);
        PreferencesHelper.setBitlyToken(mContext, null);
        PreferencesHelper.setBitlyApiKey(mContext, null);
        PreferencesHelper.setBitlyLogin(mContext, null);
        PreferencesHelper.setBrevosWelcomeWizard(mContext, false);
        Toast.makeText(mContext, "Clearing all Settings", Toast.LENGTH_SHORT).show();
        mBus.post(new BitlyAuthEvent(false, ""));
    }

    private final TextWatcher mTextEditorWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            hideClipboardPrompt();
            if (count > 0) {
                if(android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                    mImageButtonShortenUrl.setEnabled(true);
                    mImageButtonShortenUrl.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_right_green));
                } else {
                    mImageButtonShortenUrl.setEnabled(true);
                    mImageButtonShortenUrl.setImageDrawable(getResources().getDrawable(R.drawable.btn_arrow_right));
                }
            } else {
                if(android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                    mImageButtonShortenUrl.setEnabled(true);
                    mImageButtonShortenUrl.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_right_gray));
                } else {
                    mImageButtonShortenUrl.setEnabled(false);
                    mImageButtonShortenUrl.setImageDrawable(getResources().getDrawable(R.drawable.btn_arrow_right));
                }
            }
        }

        public void afterTextChanged(Editable s) {
            hideClipboardPrompt();
        }
    };

    @OnClick(R.id.buttonShortenUrl)
    public void shortenButtonClick() {
        shorten(mEditTextUrl.getText().toString());
    }

    public void shorten(String linkToShorten) {
        if (linkToShorten != null && BitlyUtil.isValidUrl(linkToShorten)) {
            BitlyTasks bitlyTasks = new BitlyTasks((BrevosApp) mContext);
            if (BitlyUtil.isBitlyUrl(linkToShorten)) {
                if (isBitlyConnected)
                    openLinkInfoActivity(new Bitmark(linkToShorten));
                else {
                    bitlyTasks.new ExpandBitmark(linkToShorten, BitlyClient.EXPAND_ACTION_DEFAULT).execute();
                }
            } else {
                showProgress();
                if (isPrivate)
                    bitlyTasks.new SaveBitmark(linkToShorten, BitlyClient.SHORTENED_ACTION_DEFAULT, isPrivate).execute();
                else {
                    bitlyTasks.new CreateBitmark(linkToShorten, BitlyClient.SHORTENED_ACTION_DEFAULT, domain, false).execute();
                }
            }
        } else {
            Toast.makeText(mContext, getString(R.string.error_invalid_url), Toast.LENGTH_SHORT).show();
        }
    }

    private void showPopOver(Bitmark bitmark) {
        Intent popOverServiceTest = new Intent(mContext, BrevosPopOverService.class);
        popOverServiceTest.putExtra(BREVOS_POP_OVER_BITMARK, bitmark.toString());
        startService(popOverServiceTest);
    }

    @OnClick(R.id.linearLayoutAuthButton)
    public void showAuth() {
        startActivity(new Intent(mContext, BitlyAuthActivity.class));
    }

    public void onEvent(BitlyAuthEvent event) {
        if (event.result) {
            showLinkFragment();
            mAuthButtonWrapper.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fall_down_out));
            mAuthButtonWrapper.setVisibility(View.GONE);
            mTogglePrivate.setVisibility(View.VISIBLE);
            mTogglePrivate.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in));

            isBitlyConnected = true;
//            isBitlyConnected = PreferencesHelper.getBitlyConnected(mContext);
        } else {
            removeLinkFragment();
            if (event.resultMessage.length() > 0) {
                if (event.resultMessage.equals(CLEAR_BITLY_DATA)) {
                    clearBitlyData();
                } else if (event.resultMessage.length() > 0) {
                    Toast.makeText(mContext, String.format(getString(R.string.error_placeholder), event.resultMessage), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(mContext, getString(R.string.bitly_connection_error), Toast.LENGTH_SHORT).show();
            }

            mAuthButtonWrapper.setVisibility(View.VISIBLE);
            mTogglePrivate.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_out));
            mTogglePrivate.setVisibility(View.GONE);
        }

        supportInvalidateOptionsMenu();
    }

    public void onEvent(ShortenedUrlEvent event) {
        hideProgress();

        assert event != null;
        if(event.result) {
            mCurrentBitmark = event.bitmark;
            switch (event.action) {
                case 0: // SHORTENED_ACTION_POPOVER
                    showPopOver(event.bitmark);
                    hideShortenNotification();
                    break;
                case 1: // SHORTENED_ACTION_DEFAULT
                    showLinkResult(event.bitmark);
                    break;
                case 2: // SHORTENED_ACTION_COPY
                    copyToClipboard(event.bitmark.getUrl());
                    break;
            }
        } else {
            if(event.action.equals(BitlyClient.SHORTENED_ACTION_POPOVER)) {
                setupNotificationIntents(event.longUrl);
                showShorteningFailedNotification(event.longUrl);
            } else
                Toast.makeText(mContext, "Error: " + event.resultMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private void showLinkResult(final Bitmark bitmark) {
        mClipboardWrapper.setVisibility(View.GONE);
        mExpandResultLayoutWrapper.setVisibility(View.GONE);
        mEditTextUrl.setText(null);

        final Animation slideDownFromTopAnimation = AnimationUtils.loadAnimation(mContext, R.anim.slide_down_from_top);
        final String shortUrl = bitmark.getUrl();
        final String longUrl = bitmark.getLong_url();

        mResultLayoutWrapper.setVisibility(View.VISIBLE);
        mTextViewResultUrl.setText(shortUrl);
        mTextViewOriginalUrl.setText(longUrl);

        // Use favicon fetcher url
        mPicasso.load(String.format(Util.faviconFetcherUrl, longUrl)).into(mImageViewFavicon);
        // .placeholder(getResources().getColor(android.R.color.transparent))

        mButtonCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {copyToClipboard(shortUrl);}
        });

        mButtonInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLinkInfoActivity(bitmark);
            }
        });

        mButtonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openShareDialog(shortUrl);
            }
        });

        mImageViewPrivaticon.setVisibility(bitmark.getIs_private() ? View.VISIBLE : View.GONE);

        hideExpandLinkResult();
        hideClipboardPrompt();

        mResultLayoutWrapper.startAnimation(slideDownFromTopAnimation);
        mResultLayoutWrapper.setOnTouchListener(new SwipeDismissTouchListener(
                mResultLayoutWrapper,
                null,
                new SwipeDismissTouchListener.OnDismissCallback() {
                    @Override
                    public void onDismiss(View view, Object token) {
                        mMainLayoutWrapperOuter.removeView(mResultLayoutWrapper);
                    }
                }
        ));

        mClipboardWrapper.setOnTouchListener(new SwipeDismissTouchListener(
                mClipboardWrapper,
                null,
                new SwipeDismissTouchListener.OnDismissCallback() {
                    @Override
                    public void onDismiss(View view, Object token) {
                        mMainLayoutWrapperOuter.removeView(mClipboardWrapper);
                    }
                }
        ));
    }

    private void hideLinkResult() {
        if(mResultLayoutWrapper.getVisibility() == View.VISIBLE) {
            mResultLayoutWrapper.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_out));
            mResultLayoutWrapper.setVisibility(View.GONE);
        }
    }

    public void onEvent(ExpandUrlEvent event) {
        hideProgress();

        assert event != null;
        if(event.result) {
            switch (event.action) {
                case 0: // SHORTENED_ACTION_DEFAULT
                    showExpandLinkResult(event.data);
                    break;
                case 1: // SHORTENED_ACTION_COPY
                    copyToClipboard(event.data.getUrl());
                    break;
            }
        } else {
            Toast.makeText(mContext, "Error: " + event.resultMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private void showExpandLinkResult(final Bitmark bitmark) {
        mClipboardWrapper.setVisibility(View.GONE);
        mResultLayoutWrapper.setVisibility(View.GONE);
        mEditTextUrl.setText(null);

        final Animation slideDownFromTopAnimation = AnimationUtils.loadAnimation(mContext, R.anim.slide_down_from_top);
        final String shortUrl = bitmark.getUrl();
        final String longUrl = bitmark.getLong_url();

        mExpandResultLayoutWrapper.setVisibility(View.VISIBLE);
        mTextViewExpandResultUrl.setText(shortUrl);
        mTextViewExpandShortUrl.setText(longUrl);

        mButtonExpandCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {copyToClipboard(shortUrl);}
        });

        mButtonExpandBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser(bitmark.getUrl());
            }
        });

        hideLinkResult();
        hideClipboardPrompt();

        mExpandResultLayoutWrapper.startAnimation(slideDownFromTopAnimation);
    }

    private void openBrowser(String short_url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(short_url)));
    }

    private void hideExpandLinkResult() {
        if(mExpandResultLayoutWrapper.getVisibility() == View.VISIBLE) {
            mExpandResultLayoutWrapper.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_out));
            mExpandResultLayoutWrapper.setVisibility(View.GONE);
        }
    }

    private void openShareDialog(String shortUrlString) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shortUrlString);

        Intent intentChooser = Intent.createChooser(intent, getText(R.string.action_share));
        intentChooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentChooser);
    }

    public void openLinkInfoActivity(Bitmark myBitmark) {
        mEditTextUrl.setText(null);

        if(isBitlyConnected) {
            Intent linkInfo = new Intent(mContext, android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH ? LinkInfoActivityNfc.class : LinkInfoActivity.class);
            linkInfo.putExtra(LinkInfoActivity.EXTRA_LINK, myBitmark.toString());
            startActivity(linkInfo);
        } else {
            Toast.makeText(mContext, "Sign in to bit.ly to see link info.", Toast.LENGTH_SHORT).show();
        }
    }

    private void buzz(int msDuration) {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(msDuration);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (adView != null)
            adView.destroy();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (adView != null)
            adView.destroy();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick(R.id.buttonPasteFromClipboard)
    public void pasteFromClipboard() {
        mEditTextUrl.setText(getClipboardText());
        hideClipboardPrompt();
    }

    @OnClick(R.id.buttonShortenUrlFromClipboard)
    public void shortenFromClipboard(){
        shorten(getClipboardText());
        hideClipboardPrompt();
    }

    @OnClick(R.id.linearLayoutMainUrlInClipboardWrapper)
    public void dismissClipboardPrompt() {
        hideClipboardPrompt();
    }

    public void hideClipboardPrompt() {
        if(clipboardPromptCountDown != null)
            clipboardPromptCountDown.cancel();

        if(mClipboardWrapper.getVisibility() == View.VISIBLE) {
            Animation slideUpAnimation = AnimationUtils.loadAnimation(mContext, R.anim.slide_up);
            slideUpAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) { }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mClipboardWrapper.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) { }
            });
            mClipboardWrapper.startAnimation(slideUpAnimation);
            mClipboardWrapper.setVisibility(View.GONE);
        }
    }

    private void showClipboardPrompt(String clipText) {
        if(mClipboardWrapper.getVisibility() == View.GONE) {
            mClipboardWrapper.setVisibility(View.VISIBLE);
            mTextViewClipboard.setText(String.format(getString(R.string.clipboard_url_text), clipText));
            final Animation slideDown = AnimationUtils.loadAnimation(mContext, R.anim.slide_down);
            mClipboardWrapper.startAnimation(slideDown);

            clipboardPromptCountDown = new CountDownTimer(20000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) { }

                @Override
                public void onFinish() {
                    hideClipboardPrompt();
                }
            }.start();
        }
    }

    private void checkClipboardForUrl() {
        if(!ignoreClipboard) {
            String clipText = getClipboardText();

            if (clipText.length() > 0 && BitlyUtil.isValidUrl(clipText) && !BitlyUtil.isBitlyUrl(clipText))
                showClipboardPrompt(clipText);
        }
    }

    @SuppressLint("InlinedApi")
    private String getClipboardText() {
        try {
            String clipText = "";
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.HONEYCOMB) {
                ClipboardManager mClipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                if (mClipboardManager.hasPrimaryClip() && mClipboardManager.getPrimaryClip() != null) {
                    if(mClipboardManager.getPrimaryClip().getItemCount() > 0) {
                        ClipData.Item mClipDataItem = mClipboardManager.getPrimaryClip().getItemAt(0);
                        if(mClipDataItem.getText() != null)
                            clipText = mClipDataItem.getText().toString();
                    }
                }
            } else {
                if (mClipboardManagerLegacy.hasText())
                    clipText = mClipboardManagerLegacy.getText().toString();
            }
            return clipText;
        } catch(Exception ex) {
            Log.d("MainActivity", "There was a problem getting the clipboard contents...");
            return null;
        }
    }

    @SuppressLint("InlinedApi")
    public void copyToClipboard(String shortUrlString) {
        if(shortUrlString.length() > 0) {
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.HONEYCOMB) {
                ClipData clipData = ClipData.newPlainText("Bit.ly Short Url", shortUrlString);
                ClipboardManager mClipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                mClipboardManager.setPrimaryClip(clipData);
            } else {
                mClipboardManagerLegacy.setText(shortUrlString);
            }
            ignoreClipboard = true;
            Toast.makeText(this, String.format(getString(R.string.link_copied), shortUrlString), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.error_copy_nothing), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAds() {
        adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(getString(R.string.admob_id));
        LinearLayout adLayout = (LinearLayout)findViewById(R.id.adViewLayoutWrapper);
        if(adLayout != null) {
            adLayout.addView(adView);

            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice("2B0F45ECB7E319BC2500CD6AFF1353CC") // N7 - 4.4.4
                    .addTestDevice("D50AF454D0BF794B6A38811EEA1F21EE") // GS3 - 4.3
                    .addTestDevice("6287716BED76BCE3BB981DD19AA858E1") // GS3 - 4.1
                    .addTestDevice("FDC26B2E6C049E2E9ECE7C97D42A4726") // G2 - 2.3.4
                    .addTestDevice("1BF36BBC3C197AFF96AF3F9F305CAD48") // N5 - L
                    .build();

            if (adView != null)
                adView.loadAd(adRequest);
        }
    }

    private void hideAds() {
        LinearLayout adLayout = (LinearLayout) findViewById(R.id.adViewLayoutWrapper);
        if(adLayout != null) {
            adLayout.setVisibility(View.GONE);

            if (adView != null)
                adView.destroy();
        }
    }

    private void clearBitlyData() {
        isBitlyConnected = false;
        PreferencesHelper.setBitlyConnected(mContext, false);
        PreferencesHelper.setBitlyToken(mContext, "");
        PreferencesHelper.setBitlyLogin(mContext, "");
        PreferencesHelper.setBitlyApiKey(mContext, "");

        PreferencesHelper.setBitlyIsPrivateAlways(mContext, false);
        PreferencesHelper.setBitlyDomain(mContext, BitlyClient.BITLY_DOMAIN_DEFAULT);
    }


    @Override
    public void onFragmentInteraction(String shortUrl) {
        openLinkInfoActivity(new Bitmark(shortUrl));
    }

    public static int getTimeZoneOffset() {
        return TimeZone.getDefault().getRawOffset() / 3600000;
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
//            if(mResultLayoutWrapper.getVisibility() == View.VISIBLE) {
//                Bundle outState = new Bundle();
//                outState.putString(MainActivity.BREVOS_CURRENT_BITMARK, mCurrentBitmark.toString());
//                super.onSaveInstanceState(outState);
//            }
//        }
//    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mResultLayoutWrapper.getVisibility() == View.VISIBLE) {
            outState.putString(MainActivity.BREVOS_CURRENT_BITMARK, mCurrentBitmark.toString());
        }
    }

    public void hideShortenNotification() {
        mNotificationManager.cancel(NOTIFICATION_BREVOS_SHORTEN);
    }

    private void setupNotificationIntents(String linkToShorten) {
        Intent genericIntent = new Intent(mContext, MainActivity.class);
        Intent retryIntent = new Intent(mContext, ShortenUrlReceiver.class);
        if(linkToShorten != null && linkToShorten.length() > 0) {
            genericIntent.putExtra(MainActivity.EXTRA_LONG_URL, linkToShorten);

            retryIntent.setAction(Intent.ACTION_SEND);
            retryIntent.putExtra(Intent.EXTRA_TEXT, linkToShorten);
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.from(mContext);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(genericIntent);
        genericPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_CANCEL_CURRENT
        );

        TaskStackBuilder retryStackBuilder = TaskStackBuilder.from(mContext);
        retryStackBuilder.addParentStack(MainActivity.class);
        retryStackBuilder.addNextIntent(retryIntent);
        retryPendingIntent = retryStackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_CANCEL_CURRENT
        );
    }

    public void showShorteningFailedNotification(String urlToShorten) {
        NotificationCompat.Builder mNotificationShorten = createShorteningFailedNotification(String.format(getString(R.string.notification_shortening_placeholder), urlToShorten));
        mNotificationManager.notify(MainActivity.NOTIFICATION_BREVOS_SHORTEN, mNotificationShorten.getNotification());
    }

    private NotificationCompat.Builder createShorteningFailedNotification(String notificationText) {
        return new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_stat_brevos_wing)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .setContentTitle(getString(R.string.app_name))
                .setContentText(notificationText)
                .setContentIntent(genericPendingIntent)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_action_refresh_white, getString(R.string.notification_retry), retryPendingIntent);
    }

    @SuppressLint("InlinedApi")
    @Override
    public void onFragmentListViewScrollListener(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        hideClipboardPrompt();

        if(view != null) {
            View first = view.getChildAt(0);
            if(first != null) {
                int distance = first.getTop() + view.getFirstVisiblePosition() * first.getHeight();

//                Log.i("MainActivity", "Distance = " + distance);
                if (distance > first.getHeight() * 3) {
                    hideCreateLink();
                    mMainLayoutWrapperInner.setVisibility(View.GONE);
//                    if(mFabStateUp)
//                        dropFabCreate();
                    return;
                }

                if (distance < first.getHeight() * 2) {
                    showCreateLink();
//                    if(!mFabStateUp)
//                        raiseFabCreate();
                }
            }
        }
    }

    private void showCreateLink() {
        if(createHidden) {
            mMainLayoutWrapperInner.setVisibility(View.VISIBLE);
            mMainLayoutWrapperInner.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_down));
            createHidden = false;
        }
    }

    private void hideCreateLink() {
        if(!createHidden) {
            Animation slideUpAnimation = AnimationUtils.loadAnimation(mContext, R.anim.slide_up);
//            slideUpAnimation.setAnimationListener(new Animation.AnimationListener() {
//                @Override
//                public void onAnimationStart(Animation animation) { }
//
//                @Override
//                public void onAnimationEnd(Animation animation) {
//                    mMainLayoutWrapperInner.setVisibility(View.GONE);
//                }
//
//                @Override
//                public void onAnimationRepeat(Animation animation) { }
//            });
            mMainLayoutWrapperInner.startAnimation(slideUpAnimation);
            createHidden = true;
        }
    }
}