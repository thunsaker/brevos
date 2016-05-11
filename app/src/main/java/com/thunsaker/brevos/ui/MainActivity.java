package com.thunsaker.brevos.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.os.Vibrator;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.github.fabtransitionactivity.SheetLayout;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.squareup.picasso.Picasso;
import com.thunsaker.R;
import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.brevos.BrevosPrefsManager;
import com.thunsaker.brevos.adapters.LinkListAdapter;
import com.thunsaker.brevos.app.BaseBrevosActivity;
import com.thunsaker.brevos.app.BrevosApp;
import com.thunsaker.brevos.app.BrevosUtil;
import com.thunsaker.brevos.data.api.Bitmark;
import com.thunsaker.brevos.data.api.BitmarkInfo;
import com.thunsaker.brevos.data.api.LinkHistoryItem;
import com.thunsaker.brevos.data.events.BitlyAuthEvent;
import com.thunsaker.brevos.data.events.ExpandUrlEvent;
import com.thunsaker.brevos.data.events.GetUserHistoryEvent;
import com.thunsaker.brevos.data.events.ShortenedUrlEvent;
import com.thunsaker.brevos.services.BitlyClient;
import com.thunsaker.brevos.services.BitlyTasks;
import com.thunsaker.brevos.services.BitlyUtil;
import com.thunsaker.brevos.ui.custom.SwipeDismissTouchListener;

import java.util.List;
import java.util.TimeZone;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import de.greenrobot.event.EventBus;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends BaseBrevosActivity
        implements LinkFragment.OnFragmentInteractionListener,
        LinkFragment.OnFragmentListViewScrollListener,
        SheetLayout.OnFabAnimationEndListener {

    @Inject @ForApplication Context mContext;
    @Inject EventBus mBus;
    @Inject Picasso mPicasso;

    @Inject android.text.ClipboardManager mClipboardManagerLegacy;
    @Inject NotificationManager mNotificationManager;

    @Inject
    BrevosPrefsManager mPreferences;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.coordinatorLayoutMainContainer) CoordinatorLayout mMainLayoutContainer;

    @BindView(R.id.buttonBitlyAuth) Button mButtonAuth;
    @BindView(R.id.linearLayoutMainEmptyWrapper) LinearLayout mLinearLayoutEmpty;
    @BindView(R.id.imageViewEmptyIcon) ImageView mImageViewEmptyIcon;
    @BindView(R.id.textViewEmptyText) TextView mTextViewEmptyText;

    @BindView(R.id.linearLayoutMainWrapperOuter) LinearLayout mMainLayoutWrapperOuter;
    @BindView(R.id.relativeLayoutMainWrapperInner) RelativeLayout mMainLayoutWrapperInner;
    @BindView(R.id.linearLayoutMainWrapper) LinearLayout mMainLayoutWrapper;

    @BindView(R.id.relativeLayoutResultWrapper) RelativeLayout mResultLayoutWrapper;
    @BindView(R.id.textViewShortenResultUrl) TextView mTextViewResultUrl;
    @BindView(R.id.textViewOriginalUrl) TextView mTextViewOriginalUrl;
    @BindView(R.id.imageViewFavicon) ImageView mImageViewFavicon;
    @BindView(R.id.imageViewPrivaticon) ImageView mImageViewPrivaticon;

    @BindView(R.id.imageButtonResultActionCopyUrl) ImageButton mButtonCopy;
    @BindView(R.id.imageButtonResultActionInfo) ImageButton mButtonInfo;
    @BindView(R.id.imageButtonResultActionShare) ImageButton mButtonShare;

    @BindView(R.id.relativeLayoutExpandResultWrapper) RelativeLayout mExpandResultLayoutWrapper;
    @BindView(R.id.textViewExpandResultUrl) TextView mTextViewExpandResultUrl;
    @BindView(R.id.textViewExpandShortUrl) TextView mTextViewExpandShortUrl;

    @BindView(R.id.imageButtonExpandResultCopy) ImageButton mButtonExpandCopy;
    @BindView(R.id.imageButtonExpandResultBrowse) ImageButton mButtonExpandBrowse;

//    @BindView(R.id.linearLayoutMainLinkListWrapper) LinearLayout mLinkListWrapper;
    @BindView(R.id.recyclerLinks) RecyclerView mRecyclerLinks;

    @BindView(R.id.fabMainCreate) FloatingActionButton mFabCreate;
    @BindView(R.id.fabMainClipboard) FloatingActionButton mFabClipboard;
    @BindView(R.id.bottomSheet) SheetLayout mSheetLayout;

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
    public static int COMPACT_LINK_LIST_COUNT = 20;

    private static final int REQUEST_CODE_TRANSITION = 1;

    private AdView adView;
    private Bitmark mCurrentBitmark;
    private PendingIntent retryPendingIntent;
    private PendingIntent genericPendingIntent;
    private boolean createHidden = false;

    public List<LinkHistoryItem> mLinkHistoryItems;
    private LinkListAdapter mLinkListAdapter;
    private boolean mNewList = true;

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        mFabCreate.show();
        mSheetLayout.setFab(mFabCreate);
        mSheetLayout.setFabAnimationEndListener(this);

        isBitlyConnected = mPreferences.bitlyEnabled().getOr(false);
        configureLayout();

        if(mBus != null && !mBus.isRegistered(this))
            mBus.register(this);

        if (getIntent().hasExtra(BREVOS_POP_OVER_BITMARK)) {
            String intentShortUrlRaw = getIntent().getStringExtra(BREVOS_POP_OVER_BITMARK);
            Bitmark bitmark = Bitmark.GetBitmarkFromJson(intentShortUrlRaw);
            mBus.post(new ShortenedUrlEvent(true, "", bitmark, BitlyClient.SHORTENED_ACTION_DEFAULT, bitmark.getLong_url()));
        } else {
            if(mPreferences.showWizard().getOr(true)) {
                showWizardActivity();
                return;
            }

            // TODO: Send link to edit screen
//            if(getIntent().hasExtra(EXTRA_LONG_URL)) {
//                mEditTextUrl.setText(getIntent().getStringExtra(EXTRA_LONG_URL));
//            }

            if(savedInstanceState != null) {
                String currentBitmarkRaw = savedInstanceState.getString(MainActivity.BREVOS_CURRENT_BITMARK);
                if(currentBitmarkRaw != null) {
                    Bitmark currentBitmark = Bitmark.GetBitmarkFromJson(currentBitmarkRaw);
                    if (currentBitmark != null) {
                        mBus.post(new ShortenedUrlEvent(true, "", currentBitmark, BitlyClient.SHORTENED_ACTION_DEFAULT, currentBitmark.getLong_url()));
                    }
                }
            }
        }

//        if(getIsPro())
            hideAds();
//        else
//            showAds();

        checkClipboardForUrl();
    }

    private void showWizardActivity() {
        startActivity(new Intent(mContext, WizardActivity.class));
    }

    private boolean getIsPro() {
        String appName = getPackageName();
        return appName.contains("Donate");
    }

    private void configureLayout() {
        if (!isBitlyConnected) {
            mButtonAuth.setVisibility(View.VISIBLE);
//            mLinkListWrapper.setVisibility(View.GONE);
            mRecyclerLinks.setVisibility(View.GONE);
            mLinearLayoutEmpty.setVisibility(View.VISIBLE);
            // TODO: Change the empty layout for first time users not signed in
//            mImageViewEmptyIcon.setImageResource(R.drawable.ic_add);
//            mTextViewEmptyText.setText(R.string.create_short_url);
        } else {
            invalidateOptionsMenu();

            mButtonAuth.setVisibility(View.GONE);
            // TODO: Move to edit view
//            if (PreferencesHelper.getBitlyIsPrivateAlways(mContext)) {
//                isPrivate = true;
//                mTogglePrivate.setVisibility(View.GONE);
//            } else {
////                mTogglePrivate.setVisibility(View.VISIBLE);
//                mTogglePrivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                        isPrivate = isChecked;
//                    }
//                });
//            }

//            showLinkFragment();
            showLinkList();
        }
    }

    private void showLinkList() {
        mLinearLayoutEmpty.setVisibility(View.GONE);
        BitlyTasks mBitlyTasks = new BitlyTasks((BrevosApp) mContext);
        mBitlyTasks.new GetUserHistory(20, 0, "", BitlyTasks.HISTORY_LIST_TYPE_DEFAULT).execute();
        mNewList = true;
    }

//    private void showLinkFragment() {
//        mLinearLayoutEmpty.setVisibility(View.GONE);
//        mLinkListWrapper.setVisibility(View.VISIBLE);
//        mLinkListWrapper.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in));
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        LinkFragment linkFragmentHistory = LinkFragment.newInstance(getResources().getInteger(R.integer.recent_items_count), BitlyTasks.HISTORY_LIST_TYPE_COMPACT);
//        fragmentManager
//                .beginTransaction()
//                .replace(R.id.frameListContentHistory, linkFragmentHistory)
//                .commit();
//    }

//    private void removeLinkFragment() {
//        // TODO: Removing the fragment is killing eventbus...figure out the reason, hiding it is just a temporary fix.
////        FragmentManager fragmentManager = getSupportFragmentManager();
////        fragmentManager.beginTransaction().replace(R.id.frameListContentHistory, null).commit();
//
//        FrameLayout mFrameLayout =
//                (FrameLayout) findViewById(R.id.frameListContentHistory);
//        if(mFrameLayout != null)
//            mFrameLayout.setVisibility(View.GONE);
//    }

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
        if(isBitlyConnected)
            getMenuInflater().inflate(R.menu.main_authenticated, menu);
        else
            getMenuInflater().inflate(R.menu.main_unauthenticated, menu);

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
            case R.id.action_sign_in:
                showAuth();
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
                // TODO: BUNDLES?!
                Snackbar.make(mMainLayoutContainer, "Bundles coming soon!", Snackbar.LENGTH_SHORT).show();
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
        mPreferences
                .bitlyEnabled().put(false)
                .bitlyToken().put("")
                .bitlyUsername().put("")
                .bitlyApiKey().put("")
                .commit();
        Snackbar.make(mMainLayoutContainer, "Clearing all Settings", Snackbar.LENGTH_SHORT).show();
        mBus.post(new BitlyAuthEvent(false, ""));
    }

    // TODO: Move to edit section
//    private final TextWatcher mTextEditorWatcher = new TextWatcher() {
//        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//        }
//
//        public void onTextChanged(CharSequence s, int start, int before, int count) {
//            if (count > 0) {
//                if(android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
//                    mImageButtonShortenUrl.setEnabled(true);
//                    mImageButtonShortenUrl.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_right_green));
//                } else {
//                    mImageButtonShortenUrl.setEnabled(true);
//                    mImageButtonShortenUrl.setImageDrawable(getResources().getDrawable(R.drawable.btn_arrow_right));
//                }
//            } else {
//                if(android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
//                    mImageButtonShortenUrl.setEnabled(true);
//                    mImageButtonShortenUrl.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_right_gray));
//                } else {
//                    mImageButtonShortenUrl.setEnabled(false);
//                    mImageButtonShortenUrl.setImageDrawable(getResources().getDrawable(R.drawable.btn_arrow_right));
//                }
//            }
//        }
//
//        public void afterTextChanged(Editable s) { }
//    };
    // TODO: Move to edit section
//    @OnClick(R.id.buttonShortenUrl)
//    public void shortenButtonClick() {
//        shorten(mEditTextUrl.getText().toString());
//    }

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
                if (isPrivate) {
                    bitlyTasks.new SaveBitmark(linkToShorten, BitlyClient.SHORTENED_ACTION_DEFAULT, isPrivate).execute();
                } else {
                    bitlyTasks.new CreateBitmark(linkToShorten, BitlyClient.SHORTENED_ACTION_DEFAULT, domain, false).execute();
                }
            }
        } else {
            Snackbar.make(mMainLayoutContainer, R.string.error_invalid_url, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void showPopOver(Bitmark bitmark) {
        Intent popOverServiceTest = new Intent(mContext, BrevosPopOverService.class);
        popOverServiceTest.putExtra(BREVOS_POP_OVER_BITMARK, bitmark.toString());
        startService(popOverServiceTest);
    }

    @OnClick(R.id.buttonBitlyAuth)
    public void showAuth() {
        startActivityForResult(new Intent(mContext, BitlyAuthActivity.class), BitlyAuthActivity.REQUEST_CODE_BITLY_SIGN_IN);
    }

    public void onEvent(BitlyAuthEvent event) {
        if (event.result) {
//            showLinkFragment();
            mButtonAuth.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fall_down_out));
            mButtonAuth.setVisibility(View.GONE);
//            mTogglePrivate.setVisibility(View.VISIBLE);
//            mTogglePrivate.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in));

            isBitlyConnected = mPreferences.bitlyEnabled().getOr(false);
        } else {
//            removeLinkFragment();
            if (event.resultMessage.length() > 0) {
                if (event.resultMessage.equals(CLEAR_BITLY_DATA)) {
                    clearBitlyData();
                } else if (event.resultMessage.length() > 0) {
                    Snackbar.make(mMainLayoutContainer, String.format(getString(R.string.error_placeholder), event.resultMessage), Snackbar.LENGTH_SHORT).show();
                }
            } else {
                Snackbar.make(mMainLayoutContainer, R.string.bitly_connection_error, Snackbar.LENGTH_SHORT).show();
            }

            mButtonAuth.setVisibility(View.VISIBLE);
//            mTogglePrivate.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_out));
//            mTogglePrivate.setVisibility(View.GONE);
        }

        supportInvalidateOptionsMenu();
    }

    public void onEvent(ShortenedUrlEvent event) {
        hideProgress();

        assert event != null;
        if(event.result) {
            mCurrentBitmark = event.bitmark;
            switch (event.action) {
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
            } else {
                Snackbar.make(mMainLayoutContainer, String.format(getString(R.string.error_placeholder), event.resultMessage), Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void showLinkResult(final Bitmark bitmark) {
        copyToClipboard(bitmark.getUrl());

        mExpandResultLayoutWrapper.setVisibility(View.GONE);
//        mEditTextUrl.setText(null);

        final Animation slideDownFromTopAnimation = AnimationUtils.loadAnimation(mContext, R.anim.slide_down_from_top);
        final String shortUrl = bitmark.getUrl();
        final String longUrl = bitmark.getLong_url();

        mResultLayoutWrapper.setVisibility(View.VISIBLE);
        mTextViewResultUrl.setText(shortUrl);
        mTextViewOriginalUrl.setText(longUrl);

        // Use favicon fetcher url
        mPicasso.load(String.format(BrevosUtil.faviconFetcherUrl, longUrl)).into(mImageViewFavicon);
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
            Snackbar.make(mMainLayoutContainer, String.format(getString(R.string.error_placeholder), event.resultMessage), Snackbar.LENGTH_SHORT).show();
        }
    }

    private void showExpandLinkResult(final Bitmark bitmark) {
        mResultLayoutWrapper.setVisibility(View.GONE);
//        mEditTextUrl.setText(null);

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
//        mEditTextUrl.setText(null);

        if(isBitlyConnected) {
            Intent linkInfo = new Intent(mContext, android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH ? LinkInfoActivityNfc.class : LinkInfoActivity.class);
            linkInfo.putExtra(LinkInfoActivity.EXTRA_LINK, myBitmark.toString());
            startActivity(linkInfo);
        } else {
            Snackbar.make(mMainLayoutContainer, R.string.bitly_auth_prompt_info, Snackbar.LENGTH_SHORT).show();
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

    @OnClick(R.id.fabMainClipboard)
    public void shortenFromClipboard(){
        shorten(getClipboardText());
        mFabClipboard.hide();
    }

    @OnLongClick(R.id.fabMainClipboard)
    public boolean shortenFromClipboardHelp() {
        Snackbar.make(
                mMainLayoutContainer,
                R.string.clipboard_action_paste_info,
                Snackbar.LENGTH_SHORT).show();
        return false;
    }

    private void checkClipboardForUrl() {
        if(!ignoreClipboard) {
            String clipText = getClipboardText();

            assert clipText != null;
            if (clipText.length() > 0 && BitlyUtil.isValidUrl(clipText) && !BitlyUtil.isBitlyUrl(clipText))
                mFabClipboard.show();
//                mFabClipboard.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.spin_up));
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
            Snackbar.make(mMainLayoutContainer, String.format(getString(R.string.link_copied), shortUrlString), Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(mMainLayoutContainer, R.string.error_copy_nothing, Snackbar.LENGTH_SHORT).show();
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
                    .addTestDevice("D50AF454D0BF794B6A38811EEA1F21EE") // GS3 - 4.3
                    .addTestDevice("6287716BED76BCE3BB981DD19AA858E1") // GS3 - 4.1
                    .addTestDevice("EF2AC044641A83A5F1084ADC3828467B") // GS6 - 6.0.1
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

        mPreferences
                .bitlyEnabled().put(false)
                .bitlyToken().put("")
                .bitlyUsername().put("")
                .bitlyApiKey().put("")
                .commit();

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

    public NotificationCompat.Builder createShorteningFailedNotification(String notificationText) {
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
        if(view != null) {
            View first = view.getChildAt(0);
            if(first != null) {
                int distance = first.getTop() + view.getFirstVisiblePosition() * first.getHeight();

                if (distance > first.getHeight() * 3) {
                    mMainLayoutWrapperInner.setVisibility(View.GONE);
                    return;
                }

                if (distance < first.getHeight() * 2) {
                    showCreateLink();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BitlyAuthActivity.REQUEST_CODE_BITLY_SIGN_IN:
                if(resultCode == Activity.RESULT_OK) {
                    Snackbar.make(mMainLayoutContainer, R.string.bitly_auth_success, Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(mMainLayoutContainer, R.string.bitly_auth_error, Snackbar.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_CODE_TRANSITION:
                mSheetLayout.contractFab();
                break;
        }
    }

    @OnClick(R.id.fabMainCreate)
    public void showCreateScreen() {
        mFabClipboard.hide();
        mSheetLayout.expandFab();
    }

    @Override
    public void onFabAnimationEnd() {
        Intent editLinkIntent = new Intent(this, EditLinkActivity.class);
        startActivityForResult(editLinkIntent, REQUEST_CODE_TRANSITION);
    }


    public void onEvent(GetUserHistoryEvent event) {
        if (event != null) {
            if (event.listType != BitlyTasks.HISTORY_LIST_TYPE_SEARCH) {
                if(event.userHistoryList != null && event.userHistoryList.size() > 0) {
                    mLinkHistoryItems = event.userHistoryList;

                    if(mNewList) {
                        mLinkListAdapter = new LinkListAdapter(mLinkHistoryItems);
                        mLinkListAdapter.notifyDataSetChanged();
                        mLinkListAdapter.setOnItemClickListener(new LinkListAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(View itemView, int position) {
                                LinkHistoryItem link = mLinkHistoryItems.get(position);
                                if(link != null)
                                    openLinkInfoActivity(link, itemView);
                            }
                        });
                        mRecyclerLinks.setAdapter(mLinkListAdapter);
                        mRecyclerLinks.setLayoutManager(new LinearLayoutManager(this));
                        mRecyclerLinks.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    private void openLinkInfoActivity(LinkHistoryItem link, View view) {
        Intent linkInfoIntent =
                new Intent(mContext,
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
                                ? LinkInfoActivityNfc.class : LinkInfoActivity.class);
        linkInfoIntent.putExtra(
                LinkInfoActivity.EXTRA_LINK,
                new Bitmark(link.link, link.long_url, link.aggregate_link).toString());
        linkInfoIntent.putExtra(
                LinkInfoActivity.EXTRA_LINK_INFO,
                new BitmarkInfo(link.link, link.long_url, link.title).toString());
        linkInfoIntent.putExtra(
                LinkInfoActivity.EXTRA_LINK_INFO_SOURCE,
                LinkInfoActivity.EXTRA_SOURCE_MAIN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                Pair<View, String> pairWrapper = Pair.create(view, this.getString(R.string.transition_link_item));
                Pair<View, String> pairClicks = Pair.create(view.findViewById(R.id.textViewHistoryClicks), this.getString(R.string.transition_text_clicks));
                Pair<View, String> pairTitle = Pair.create(view.findViewById(R.id.textViewHistoryTitle), this.getString(R.string.transition_text_title));
                ActivityOptionsCompat options =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(this, pairClicks);

                startActivity(linkInfoIntent, options.toBundle());
            } catch (Exception ex) {
                startActivity(linkInfoIntent);
            }
        } else {
            startActivity(linkInfoIntent);
        }
    }
}