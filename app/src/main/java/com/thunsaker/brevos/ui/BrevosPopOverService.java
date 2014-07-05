package com.thunsaker.brevos.ui;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.thunsaker.R;
import com.thunsaker.brevos.data.api.Bitmark;
import com.thunsaker.brevos.ui.custom.PopOverView;

public class BrevosPopOverService extends Service {
    private WindowManager mWindowManager;

    private View mView;

    public BrevosPopOverService() { }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mWindowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        mView = mLayoutInflater.inflate(R.layout.fragment_pop_over, null);

        PopOverView popOverView = (PopOverView) mView.findViewById(R.id.pop_over_wrapper);
        popOverView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSelf();
            }
        });

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.BOTTOM;
        params.x = 0;
        params.y = 100;

        mWindowManager.addView(mView, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mView != null)
            mWindowManager.removeView(mView);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent.hasExtra(MainActivity.BREVOS_POP_OVER_BITMARK) && mView != null) {
            String shortUrlStringRaw = intent.getStringExtra(MainActivity.BREVOS_POP_OVER_BITMARK);
            final Bitmark bitmark = Bitmark.GetBitmarkFromJson(shortUrlStringRaw);

            setupPopOverButtons(bitmark);
        }

        return flags;
    }

    private void setupPopOverButtons(final Bitmark bitmark) {
        final Animation spinUpAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.spin_up);
        final Animation fadeInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);

        final LinearLayout popOverButtons = (LinearLayout) mView.findViewById(R.id.pop_over_buttons);
        popOverButtons.startAnimation(fadeInAnimation);

        ImageButton btnMain = (ImageButton) mView.findViewById(R.id.pop_over_btn_main);
        btnMain.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                popOverButtons.startAnimation(fadeInAnimation);
                Toast.makeText(BrevosPopOverService.this, R.string.pop_over_open_app, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        btnMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrevos(bitmark);
                stopSelf();

            }
        });

        final ImageButton btnAction1 = (ImageButton) mView.findViewById(R.id.pop_over_btn_action_primary);
        btnAction1.startAnimation(spinUpAnimation);
        btnAction1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openShareDialog(bitmark.getUrl());
                stopSelf();
            }
        });

        final ImageButton btnAction2 = (ImageButton) mView.findViewById(R.id.pop_over_btn_action_secondary);
        btnAction2.startAnimation(spinUpAnimation);
        btnAction2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard(bitmark.getUrl());
                stopSelf();
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

    @SuppressLint("InlinedApi")
    private void copyToClipboard(String shortUrlString) {
        buzz(400);

        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.HONEYCOMB) {
            ClipboardManager clipboardManager = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("Bit.ly Short Url", shortUrlString);
            clipboardManager.setPrimaryClip(clipData);
        } else {
            android.text.ClipboardManager clipboardManager = (android.text.ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setText(shortUrlString);
        }

        Toast.makeText(this, String.format(getString(R.string.link_copied), shortUrlString), Toast.LENGTH_SHORT).show();
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
}