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
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Toast;

import com.thunsaker.R;
import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.brevos.app.BaseBrevosActivity;
import com.thunsaker.brevos.app.BrevosApp;
import com.thunsaker.brevos.data.api.Bitmark;
import com.thunsaker.brevos.services.BitlyClient;
import com.thunsaker.brevos.services.BitlyTasks;
import com.thunsaker.brevos.services.BitlyUtil;
import com.twitter.Extractor;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class ShortenUrlReceiver extends BaseBrevosActivity {
    @Inject
    @ForApplication
    Context mContext;

    @Inject
    NotificationManager mNotificationManager;

    NotificationCompat.Builder mNotificationBuilder;
    private PendingIntent genericPendingIntent;
    private String EXTRA_POP_OVER_LONG_URL = "EXTRA_POP_OVER_LONG_URL";
    private PendingIntent retryPendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupNotificationIntents(null);

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
                        showPopOver(new Bitmark(linksList.get(0)));
                    } else {
                        showPopOverMultiple(linksList);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.error_no_url), Toast.LENGTH_SHORT).show();
                }
            }

            finish();
        } else {
            Toast.makeText(mContext, String.format(mContext.getString(R.string.error_placeholder), mContext.getString(R.string.error_no_url)), Toast.LENGTH_SHORT).show();
        }
    }

    private void showPopOver(Bitmark bitmark) {
        Intent popOverService = new Intent(mContext, BrevosPopOverService.class);
        popOverService.putExtra(MainActivity.BREVOS_POP_OVER_BITMARK, bitmark.toString());
        mContext.startService(popOverService);
        mNotificationManager.cancel(MainActivity.NOTIFICATION_BREVOS_SHORTEN);
    }

    private void showPopOverMultiple(ArrayList<String> linksList) {
        Toast.makeText(mContext, "Multiple URLs found...", Toast.LENGTH_SHORT).show();

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
}