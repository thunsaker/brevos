package com.thunsaker.brevos.app;

public abstract class BaseBrevosListFragment extends BaseListFragment {
    @Override
    protected Object[] getActivityModules() {
        return new Object[] {
                new BrevosActivityModule(this.getActivity())
        };
    }
}