package com.thunsaker.brevos;

import android.content.SharedPreferences;

import com.tale.prettysharedpreferences.BooleanEditor;
import com.tale.prettysharedpreferences.PrettySharedPreferences;
import com.tale.prettysharedpreferences.StringEditor;

public class BrevosPrefsManager extends PrettySharedPreferences<BrevosPrefsManager> {
    private SharedPreferences mPreferences;
    private boolean firstLaunch;

    public BrevosPrefsManager(SharedPreferences sharedPreferences) {
        super(sharedPreferences);
    }

    public void ClearAllPreferences() {
        mPreferences.getAll().clear();
    }

    public BooleanEditor<BrevosPrefsManager> bitlyEnabled() {
        return getBooleanEditor("brevos_bitly_connected");
    }

    public StringEditor<BrevosPrefsManager> bitlyUsername() {
        return getStringEditor("brevos_bitly_login");
    }

    public StringEditor<BrevosPrefsManager> bitlyToken() {
        return getStringEditor("brevos_bitly_token");
    }

    public StringEditor<BrevosPrefsManager> bitlyApiKey() {
        return getStringEditor("brevos_bitly_apikey");
    }

    public BooleanEditor<BrevosPrefsManager> isFirstLaunch() {
        return getBooleanEditor("brevos_first_launch");
    }
}
