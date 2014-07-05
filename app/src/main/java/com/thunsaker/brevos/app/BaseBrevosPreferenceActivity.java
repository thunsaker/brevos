package com.thunsaker.brevos.app;

public class BaseBrevosPreferenceActivity extends BasePreferenceActivity {
    @Override
    protected Object[] getActivityModules() {
        return new Object[] {
                new BrevosActivityModule(this)
        };
    }
}
