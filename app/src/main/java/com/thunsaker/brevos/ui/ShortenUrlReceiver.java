package com.thunsaker.brevos.ui;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.thunsaker.R;
import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.brevos.app.BaseBrevosActivity;
import com.thunsaker.brevos.app.BrevosApp;
import com.thunsaker.brevos.data.api.Bitmark;
import com.thunsaker.brevos.data.events.ShortenedUrlEvent;
import com.thunsaker.brevos.services.BitlyClient;
import com.thunsaker.brevos.services.BitlyTasks;
import com.thunsaker.brevos.services.BitlyUtil;
import com.thunsaker.brevos.ui.custom.PopOverView;
import com.twitter.Extractor;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class ShortenUrlReceiver extends BaseBrevosActivity {
    @Inject
    @ForApplication
    Context mContext;

    @Inject
    EventBus mBus;

    @Inject
    NotificationManager mNotificationManager;

    @BindView(R.id.pop_over_wrapper) PopOverView mPopOverWrapper;
    @BindView(R.id.linearLayoutShortenProgress) LinearLayout mProgress;
    @BindView(R.id.linearLayoutShortenCard) LinearLayout mPopOverCard;
    @BindView(R.id.linearLayoutShortenAction2) LinearLayout mButtonShare;
    @BindView(R.id.linearLayoutShortenAction1) LinearLayout mButtonCopy;
    @BindView(R.id.linearLayoutShortenLink) LinearLayout mButtonApp;
    @BindView(R.id.textViewShortenLongUrl) TextView mTextLongUrl;
    @BindView(R.id.textViewShortenShortUrl) TextView mTextShortUrl;

    NotificationCompat.Builder mNotificationBuilder;
    private PendingIntent genericPendingIntent;
    private String EXTRA_POP_OVER_LONG_URL = "EXTRA_POP_OVER_LONG_URL";
    private PendingIntent retryPendingIntent;

    Bitmark mCurrentBitmark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_pop_over);
        ButterKnife.bind(this);

        mPopOverWrapper.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in));

        if(mBus != null && !mBus.isRegistered(this))
            mBus.register(this);

        setupNotificationIntents(null);

        setTitle("");

        mPopOverWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        if(intent.getAction().equals(Intent.ACTION_SEND)) {
            handleIntent(intent);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if(intent.getAction().equals(Intent.ACTION_SEND)) {
            handleIntent(intent);
        }
    }

    private void handleIntent(Intent intent) {
        String receivedUrl;

        if(intent.hasExtra(Intent.EXTRA_TEXT)) {
            receivedUrl = intent.getStringExtra(Intent.EXTRA_TEXT);
            if(BitlyUtil.isValidUrl(receivedUrl)) {
                if (BitlyUtil.isBitlyUrl(receivedUrl)) {
                    expandUrl(receivedUrl);
                } else {
                    shortenUrl(receivedUrl);
                }
            } else {
                ArrayList<String> linksList = GetLinksInText(receivedUrl);
                if(linksList != null) {
                    if(linksList.size() == 1) {
                        shortenUrl(linksList.get(0));
                    } else {
                        showPopOverMultiple(linksList);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.error_no_url), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(mContext, String.format(mContext.getString(R.string.error_placeholder), mContext.getString(R.string.error_no_url)), Toast.LENGTH_SHORT).show();
        }
    }

    private void showPopOverMultiple(ArrayList<String> linksList) {
//        Toast.makeText(mContext, "Multiple URLs found...", Toast.LENGTH_SHORT).show();

        Toast.makeText(mContext, "Show list of links available to shorten in pasted text", Toast.LENGTH_SHORT).show();
        hideShortenNotification();
        finish();

//        if(linksList.size() > 1) {
//            Intent popOverServiceList = new Intent(mContext, BrevosPopOverList.class);
//
//            popOverServiceList.putExtra(BrevosPopOverList.EXTRA_POPOVER_LINK_LIST, linksList);
//            popOverServiceList.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            mContext.startActivity(popOverServiceList);
//        }
    }

    @SuppressLint("InlinedApi")
    public void copyToClipboard(String shortUrlString) {
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.HONEYCOMB) {
            ClipboardManager clipboardManager = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("Bit.ly Short Url", shortUrlString);
            clipboardManager.setPrimaryClip(clipData);
        } else {
            android.text.ClipboardManager clipboardManager = (android.text.ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setText(shortUrlString);
        }
        Toast.makeText(mContext, String.format(mContext.getString(R.string.link_copied), shortUrlString), Toast.LENGTH_SHORT).show();
    }

    private void shortenUrl(String receivedUrl) {
        setupNotificationIntents(receivedUrl);
        showShorteningNotification(receivedUrl, false);
        BitlyTasks bitlyTasks = new BitlyTasks((BrevosApp) mContext);
        bitlyTasks.new CreateBitmark(receivedUrl, BitlyClient.SHORTENED_ACTION_POPOVER).execute();
    }

    private void expandUrl(String receivedUrl) {
//        BitlyTasks bitlyTasks = new BitlyTasks((BrevosApp) mContext);
//        bitlyTasks.new ExpandShortUrl(receivedUrl, BitlyClient.EXPAND_ACTION_POPOVER).execute();
        Toast.makeText(mContext, mContext.getString(R.string.expand_pop_over), Toast.LENGTH_SHORT).show();
    }

    public static ArrayList<String> GetLinksInText(String textToCheck) {
        Extractor twitterExtractor = new Extractor();
        List<String> myLinks = twitterExtractor.extractURLs(textToCheck);

        if(myLinks != null && !myLinks.isEmpty()) {
            ArrayList<String> myArrayLinks = new ArrayList<String>();
            myArrayLinks.addAll(myLinks);
            return myArrayLinks;
        }

        return null;
    }

    private void setupNotificationIntents(String linkToShorten) {
        Intent genericIntent = new Intent(mContext, MainActivity.class);
        if(linkToShorten != null && linkToShorten.length() > 0) {
            genericIntent.putExtra(MainActivity.EXTRA_LONG_URL, linkToShorten);
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.from(mContext);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(genericIntent);
        genericPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_CANCEL_CURRENT
        );
    }

    public void showShorteningNotification(String urlToShorten, boolean showRetry) {
        NotificationCompat.Builder mNotificationShorten = createShorteningNotification(String.format(getString(R.string.notification_shortening_placeholder), urlToShorten), showRetry);
        mNotificationManager.notify(MainActivity.NOTIFICATION_BREVOS_SHORTEN, mNotificationShorten.getNotification());
    }

    private NotificationCompat.Builder createShorteningNotification(String notificationText, boolean showRetry) {
        return new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_stat_brevos_wing)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .setContentTitle(getString(R.string.app_name))
                .setContentText(notificationText)
                .setContentIntent(genericPendingIntent)
                .setAutoCancel(true);
    }

    private void setupPopOverCard(final Bitmark bitmark) {
        final Animation spinUpAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.spin_up);
        final Animation fadeInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        final Animation fadeOutAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
        final Animation slideDownAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);

        mProgress.startAnimation(fadeOutAnimation);
        mProgress.setVisibility(View.GONE);

        mTextShortUrl.setText(bitmark.getUrl());
        if(bitmark.getLong_url() != null) {
            mTextLongUrl.setText(bitmark.getLong_url());
            mTextLongUrl.setVisibility(View.VISIBLE);
        } else {
            mTextLongUrl.setVisibility(View.GONE);
        }

        mButtonApp.startAnimation(slideDownAnimation);
        mButtonApp.setVisibility(View.VISIBLE);
        mButtonApp.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mPopOverCard.startAnimation(fadeInAnimation);
                Toast.makeText(mContext, R.string.pop_over_open_app, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        mButtonApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrevos(bitmark);
                finish();
            }
        });

        mButtonShare.startAnimation(fadeInAnimation);
        mButtonShare.setVisibility(View.VISIBLE);
        mButtonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openShareDialog(bitmark.getUrl());
                finish();
            }
        });

        mButtonCopy.startAnimation(fadeInAnimation);
        mButtonCopy.setVisibility(View.VISIBLE);
        mButtonCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard(bitmark.getUrl());
                finish();
            }
        });
    }

    private void openBrevos(Bitmark bitmark) {
        buzz(300);
        Intent shortenedUrlIntent = new Intent(getApplicationContext(), MainActivity.class);
        shortenedUrlIntent.putExtra(MainActivity.BREVOS_POP_OVER_BITMARK, bitmark.toString());
        shortenedUrlIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(shortenedUrlIntent);
    }

    private void openShareDialog(String shortUrlString) {
        buzz(200);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shortUrlString);

        Intent intentChooser = Intent.createChooser(intent, getText(R.string.action_share));
        intentChooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentChooser);
    }

    private void buzz(int msDuration) {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(msDuration);
    }

    public void onEvent(ShortenedUrlEvent event) {
        hideProgress();

        assert event != null;
        if(event.result) {
            mCurrentBitmark = event.bitmark;
            if(event.action == 0) {
                setupPopOverCard(event.bitmark);
                hideShortenNotification();
            }
        } else {
            if(event.action.equals(BitlyClient.SHORTENED_ACTION_POPOVER)) {
                setupNotificationIntents(event.longUrl);
                showShorteningFailedNotification(event.longUrl);
            } else
                Toast.makeText(mContext, "Error: " + event.resultMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private void hideShortenNotification() {
        mNotificationManager.cancel(MainActivity.NOTIFICATION_BREVOS_SHORTEN);
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
}