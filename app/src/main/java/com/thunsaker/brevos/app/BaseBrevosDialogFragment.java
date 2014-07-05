package com.thunsaker.brevos.app;

public class BaseBrevosDialogFragment extends BaseDialogFragment {
    @Override
    protected Object[] getActivityModules() {
        return new Object[] {
                new BrevosActivityModule(this.getActivity())
        };
    }
}
