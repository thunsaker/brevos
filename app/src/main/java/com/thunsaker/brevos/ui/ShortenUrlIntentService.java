package com.thunsaker.brevos.ui;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.thunsaker.R;
import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.brevos.app.BrevosApp;
import com.thunsaker.brevos.app.BrevosInjectingIntentService;
import com.thunsaker.brevos.data.api.Bitmark;
import com.thunsaker.brevos.services.BitlyClient;
import com.thunsaker.brevos.services.BitlyTasks;
import com.thunsaker.brevos.services.BitlyUtil;
import com.twitter.Extractor;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class ShortenUrlIntentService extends BrevosInjectingIntentService {
    @Inject
    @ForApplication
    Context mContext;

    public ShortenUrlIntentService() {
        this("ShortenUrlIntentService");
    }

    public ShortenUrlIntentService(String name) {
        super(name);
    }

    private void showPopOver(Bitmark bitmark) {
        Intent popOverService = new Intent(mContext, BrevosPopOverService.class);
        popOverService.putExtra(MainActivity.BREVOS_POP_OVER_BITMARK, bitmark.toString());
        mContext.startService(popOverService);
    }

    private void showPopOverMultiple(ArrayList<String> linksList) {
        if(linksList.size() > 1) {
            Intent popOverServiceList = new Intent(mContext, BrevosPopOverList.class);

            popOverServiceList.putExtra(BrevosPopOverList.EXTRA_POPOVER_LINK_LIST, linksList);
            mContext.startActivity(popOverServiceList);
        }
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

    @Override
    protected void onHandleIntent(Intent intent) {
        String receivedUrl;
        if(intent.hasExtra(Intent.EXTRA_TEXT)) {
            receivedUrl = intent.getStringExtra(Intent.EXTRA_TEXT);

            if(BitlyUtil.isValidUrl(receivedUrl)) {
                shortenUrl(receivedUrl);
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
        } else {
            Toast.makeText(mContext, String.format(mContext.getString(R.string.error_placeholder), mContext.getString(R.string.error_no_url)), Toast.LENGTH_SHORT).show();
        }
    }

    private void shortenUrl(String receivedUrl) {
        BitlyTasks bitlyTasks = new BitlyTasks((BrevosApp) mContext);
        bitlyTasks.new CreateBitmark(receivedUrl, BitlyClient.SHORTENED_ACTION_POPOVER).execute();
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
}