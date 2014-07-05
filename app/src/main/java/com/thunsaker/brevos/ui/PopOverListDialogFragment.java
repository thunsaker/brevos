package com.thunsaker.brevos.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import com.thunsaker.R;
import com.thunsaker.brevos.app.BrevosApp;
import com.thunsaker.brevos.services.BitlyClient;
import com.thunsaker.brevos.services.BitlyTasks;

import java.util.ArrayList;

public class PopOverListDialogFragment extends DialogFragment {
    public static final String ARG_LINK_LIST = "link_list";
    ListAdapter linkListAdapter;
    public static ArrayList<String> linkList;

//    public static PopOverListDialogFragment newInstance(ArrayList<String> linkList) {
//        PopOverListDialogFragment fragment = new PopOverListDialogFragment();
//        Bundle args = new Bundle();
//        args.putStringArrayList(ARG_LINK_LIST, linkList);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        linkList = savedInstanceState.getStringArrayList(ARG_LINK_LIST);
        linkListAdapter = new ArrayAdapter<String>(
                getActivity().getApplicationContext(),
                android.R.layout.simple_list_item_single_choice,
                linkList);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity().getApplicationContext());
        dialogBuilder.setTitle(R.string.pop_over_select_title)
                .setAdapter(linkListAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BitlyTasks bitlyTasks = new BitlyTasks((BrevosApp) getActivity().getApplicationContext());
                        bitlyTasks.new CreateBitmark(
                                linkListAdapter.getItem(which).toString(),
                                BitlyClient.SHORTENED_ACTION_POPOVER).execute();
                    }
                });

        return dialogBuilder.create();
    }
}