package com.thunsaker.brevos.app;

import com.thunsaker.android.common.dagger.BaseFragment;

public class BaseBrevosFragment extends BaseFragment {

    @Override
    protected Object[] getActivityModules() {
        return new Object[] {
                new BrevosActivityModule(this.getActivity())
        };
    }
}
