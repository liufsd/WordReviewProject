/*
 * Name        : Settings.java
 * Author      : Coleman
 * Copyright   : Copyright (c) 2009-2012 CIeNET Ltd. All rights reserved
 * Description : Settings.java
 * Review      : 
 */

package com.coleman.util;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSettings {
    public static final String TAG = "AppSettings";

    public static void saveBoolean(Context context, String key, boolean value) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean getBoolean(Context context, String key, boolean defvalue) {
        return getSharedPreferences(context).getBoolean(key, defvalue);
    }

    /**
     * Save a string value to the shared preference.
     * 
     * @param context to construct a preference.
     * @param key to mark the store value.
     * @param value to saved value.
     */
    public static void saveString(Context context, String key, String value) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * Get the specified value through the key value.
     * 
     * @param context to get a shared preference.
     * @param key to retrieve the value.
     * @return the string value returned.
     */
    public static String getString(Context context, String key, String def) {
        return getSharedPreferences(context).getString(key, def);
    }

    /**
     * Save a integer value to the shared preference.
     * 
     * @param context to construct a preference.
     * @param key to mark the store value.
     * @param value to saved value.
     */
    public static void saveInt(Context context, String key, int value) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(key, value);
        editor.commit();

    }

    /**
     * Get the specified value through the key value.
     * 
     * @param context to get a shared preference.
     * @param key to retrieve the value.
     * @return the integer value returned.
     */
    public static int getInt(Context context, String key, int def) {
        return getSharedPreferences(context).getInt(key, def);
    }

    /**
     * Save a Long value to the shared preference.
     * 
     * @param context to construct a preference.
     * @param key to mark the store value.
     * @param value to saved value.
     */
    public static void saveLong(Context context, String key, long value) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putLong(key, value);
        editor.commit();

    }

    /**
     * Get the specified value through the key value.
     * 
     * @param context to get a shared preference.
     * @param key to retrieve the value.
     * @return the integer value returned.
     */
    public static long getLong(Context context, String key, long def) {
        return getSharedPreferences(context).getLong(key, def);
    }

    /**
     * Retrieve the package shared preferences object.
     * 
     * @param context
     * @return
     */
    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }
}
