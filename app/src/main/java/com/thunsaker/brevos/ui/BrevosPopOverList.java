package com.thunsaker.brevos.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.thunsaker.brevos.app.BaseBrevosActivity;

import java.util.ArrayList;

public class BrevosPopOverList extends BaseBrevosActivity {

    public static final String EXTRA_POPOVER_LINK_LIST = "POPOVER_LINK_LIST";

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if(intent.hasExtra(EXTRA_POPOVER_LINK_LIST)) {
            ArrayList<String> urls = intent.getStringArrayListExtra(EXTRA_POPOVER_LINK_LIST);

            DialogFragment urlSelectDialog = new PopOverListDialogFragment();
            Bundle args = new Bundle();
            args.putStringArrayList(PopOverListDialogFragment.ARG_LINK_LIST, urls);
            urlSelectDialog.setArguments(args);
            urlSelectDialog.show(getSupportFragmentManager(), "url_list");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getIntent().hasExtra(EXTRA_POPOVER_LINK_LIST)) {
            handleIntent(getIntent());
        }
//        setContentView(R.layout.activity_brevos_pop_over_list);
//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.container, new PlaceholderFragment())
//                    .commit();
//        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
//    public static class PlaceholderFragment extends Fragment {
//
//        public PlaceholderFragment() {
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_brevos_pop_over_list, container, false);
//            return rootView;
//        }
//    }
}