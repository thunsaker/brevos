package com.thunsaker.brevos.ui.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.thunsaker.R;

public class BitlyAuthButton extends FrameLayout {
    public BitlyAuthButton(Context context) {
        this(context, null);
    }

    public BitlyAuthButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.style.Theme_Brevos);
    }

    public BitlyAuthButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}