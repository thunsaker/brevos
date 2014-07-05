package com.thunsaker.brevos.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.thunsaker.R;

public class PreferencesHelper {
    public final static String PREFS_NAME = "bitdroid_prefs";

    // Bitly Prefs
    public static boolean getBitlyConnected(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean(
                context.getString(R.string.prefs_bitly_connected),
                false);
    }

    public static void setBitlyConnected(Context context, boolean newValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Editor prefsEditor = prefs.edit();
        prefsEditor.putBoolean(
                context.getString(R.string.prefs_bitly_connected),
                newValue);
        prefsEditor.commit();
    }

    public static String getBitlyToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(
                context.getString(R.string.prefs_bitly_token),
                null);
    }

    public static void setBitlyToken(Context context, String newValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Editor prefsEditor = prefs.edit();
        prefsEditor.putString(
                context.getString(R.string.prefs_bitly_token),
                newValue);
        prefsEditor.commit();
    }

    public static String getBitlyApiKey(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(
                context.getString(R.string.prefs_bitly_apikey),
                null);
    }

    public static void setBitlyApiKey(Context context, String newValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Editor prefsEditor = prefs.edit();
        prefsEditor.putString(
                context.getString(R.string.prefs_bitly_apikey),
                newValue);
        prefsEditor.commit();
    }

    public static String getBitlyLogin(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(
                context.getString(R.string.prefs_bitly_login),
                null);
    }

    public static void setBitlyLogin(Context context, String newValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Editor prefsEditor = prefs.edit();
        prefsEditor.putString(
                context.getString(R.string.prefs_bitly_login),
                newValue);
        prefsEditor.commit();
    }

    public static String getBitlyDomain(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(
                context.getString(R.string.prefs_bitly_shorten_domain),
                "0");
    }

    /**
     *
     * @param context
     * @param newValue Either The domain to use. Either {@link com.thunsaker.brevos.services.BitlyClient#BITLY_DOMAIN_DEFAULT} or
     *                      {@link com.thunsaker.brevos.services.BitlyClient#BITLY_DOMAIN_BITLY} or
     *                      {@link com.thunsaker.brevos.services.BitlyClient#BITLY_DOMAIN_JMP} or
     *                      {@link com.thunsaker.brevos.services.BitlyClient#BITLY_DOMAIN_BITLYCOM} or
     */
    public static void setBitlyDomain(Context context, String newValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Editor prefsEditor = prefs.edit();
        prefsEditor.putString(
                context.getString(R.string.prefs_bitly_shorten_domain),
                newValue);
        prefsEditor.commit();
    }

    public static Boolean getBitlyDomainPro(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean(
                context.getString(R.string.prefs_bitly_shorten_domain_pro),
                false);
    }

    public static void setBitlyDomainPro(Context context, Boolean newValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Editor prefsEditor = prefs.edit();
        prefsEditor.putBoolean(
                context.getString(R.string.prefs_bitly_shorten_domain_pro),
                newValue);
        prefsEditor.commit();
    }

    public static Boolean getBitlyAutoCopyLinks(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean(
                context.getString(R.string.prefs_bitly_auto_copy_links),
                true);
    }

    public static void setBitlyAutoCopyLinks(Context context, Boolean newValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Editor prefsEditor = prefs.edit();
        prefsEditor.putBoolean(
                context.getString(R.string.prefs_bitly_auto_copy_links),
                newValue);
        prefsEditor.commit();
    }

    public static Boolean getBitlyShowInShareMenu(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean(
                context.getString(R.string.prefs_bitly_share_menu),
                true);
    }

    public static void setBitlyShowInShareMenu(Context context, Boolean newValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Editor prefsEditor = prefs.edit();
        prefsEditor.putBoolean(
                context.getString(R.string.prefs_bitly_share_menu),
                newValue);
        prefsEditor.commit();
    }

    public static boolean getBitlyFeelings(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean(
                context.getString(R.string.prefs_bitly_beta_shorten_feelings),
                false);
    }

    public static void setBitlyFeelings(Context context, boolean newValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Editor prefsEditor = prefs.edit();
        prefsEditor.putBoolean(
                context.getString(R.string.prefs_bitly_beta_shorten_feelings),
                newValue);
        prefsEditor.commit();
    }

    public static boolean getBitlyIsPrivateAlways(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean(
                context.getString(R.string.prefs_bitly_private_always),
                false);
    }

    public static void setBitlyIsPrivateAlways(Context context, boolean newValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Editor prefsEditor = prefs.edit();
        prefsEditor.putBoolean(
                context.getString(R.string.prefs_bitly_private_always),
                newValue);
        prefsEditor.commit();
    }

    public static boolean getBitlyHideAdvancedOptions(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean(
                context.getString(R.string.prefs_bitly_options_hide),
                false);
    }

    public static void setBitlyHideAdvancedOptions(Context context, boolean newValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Editor prefsEditor = prefs.edit();
        prefsEditor.putBoolean(
                context.getString(R.string.prefs_bitly_options_hide),
                newValue);
        prefsEditor.commit();
    }

    public static boolean getBrevosWelcomeWizard(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean(
                context.getString(R.string.prefs_brevos_welcome_wizard),
                false);
    }

    public static void setBrevosWelcomeWizard(Context context, boolean newValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Editor prefsEditor = prefs.edit();
        prefsEditor.putBoolean(
                context.getString(R.string.prefs_brevos_welcome_wizard),
                newValue);
        prefsEditor.commit();
    }
}
