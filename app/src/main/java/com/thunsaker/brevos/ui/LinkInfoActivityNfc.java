package com.thunsaker.brevos.ui;

import android.annotation.TargetApi;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.thunsaker.R;
import com.thunsaker.android.common.util.NfcUtils;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class LinkInfoActivityNfc extends LinkInfoActivity
        implements NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback {
    NfcAdapter mNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SetupNfc();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void SetupNfc() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(mNfcAdapter != null) {
            mNfcAdapter.setNdefPushMessageCallback(this, this);
            mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
        } else {
            Log.i("LinkInfoActivity", getString(R.string.notice_nfc_unavailable));
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        String urlToPass = mCurrentBitmark.getUrl();
        return new NdefMessage(new NdefRecord[] {
                NdefRecord.createUri(urlToPass)
        });
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onNdefPushComplete(NfcEvent event) {
        mHandler.obtainMessage(NfcUtils.MESSAGE_SENT).sendToTarget();
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NfcUtils.MESSAGE_SENT:
                    Toast.makeText(mContext, getString(R.string.nfc_link_sent), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}