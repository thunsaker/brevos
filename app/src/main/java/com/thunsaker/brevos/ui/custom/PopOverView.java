package com.thunsaker.brevos.ui.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

public class PopOverView extends RelativeLayout implements View.OnClickListener {
    public PopOverView(Context context) {
        this(context, null);
    }

    public PopOverView(Context context, AttributeSet attrs) {
//        this(context, attrs, R.style.Theme_Brevos_PopOver);
        super(context, attrs);
    }
//
//    public PopOverView(Context context, AttributeSet attrs, int defStyle) {
//        super(context, attrs, defStyle);
//    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    // TODO: Add Button Actions for Main/Left/Right

    @Override
    public void onClick(View v) {
        Log.d("PopOverView", "Clicked the popover.");
    }
}