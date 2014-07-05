package com.thunsaker.brevos.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.thunsaker.R;
import com.thunsaker.brevos.app.BaseBrevosDialogFragment;
import com.thunsaker.brevos.data.events.BitlyAuthEvent;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

public class ConfirmSignOutDialogFragment extends BaseBrevosDialogFragment {
    @Inject
    EventBus mBus;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder confirmDialogBuilder = new AlertDialog.Builder(getActivity());
        return confirmDialogBuilder.setMessage(getString(R.string.bitly_disconnect))
                .setTitle(getString(R.string.confirm))
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mBus.post(new BitlyAuthEvent(false, MainActivity.CLEAR_BITLY_DATA));
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing...
                    }
                })
                .create();
    }
}