package com.ketch.sample.webview;

import android.content.Context;
import android.content.SharedPreferences;

public class KetchSharedPreferences {
    private static final String IAB_TCF_TC_STRING = "IABTCF_TCString";
    private static final String IAB_US_PRIVACY_STRING = "IABUSPrivacy_String";

    private final SharedPreferences sharedPreferences;

    public KetchSharedPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    }

    public String getUSPrivacyString() {
        return sharedPreferences.getString(IAB_US_PRIVACY_STRING, null);
    }

    public void saveUSPrivacyString(String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (value != null) {
            editor.putString(IAB_US_PRIVACY_STRING, value);
        } else {
            editor.remove(IAB_US_PRIVACY_STRING);
        }
        editor.apply();
    }

    public String getTCFTCString() {
        return sharedPreferences.getString(IAB_TCF_TC_STRING, null);
    }

    public void saveTCFTCString(String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (value != null) {
            editor.putString(IAB_TCF_TC_STRING, value);
        } else {
            editor.remove(IAB_TCF_TC_STRING);
        }
        editor.apply();
    }
}
