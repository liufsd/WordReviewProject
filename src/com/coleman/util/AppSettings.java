/*
 * Name        : Settings.java
 * Author      : Coleman
 * Copyright   : Copyright (c) 2009-2012 CIeNET Ltd. All rights reserved
 * Description : Settings.java
 * Review      : 
 */

package com.coleman.util;

import com.coleman.kingword.dict.DictManager;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSettings {
    public static final String TAG = "AppSettings";

    // key fields
    public static final String SPLIT_NUM_KEY = "split_num";

    public static final String ANIM_TYPE = "anim_type";

    public static final String IS_NIGHT_MODE_KEY = "isNightMode";

    public static final String FIRST_STARTED_KEY = "first_started";

    public static final String SELECT_COLOR_MODE_KEY = "select_color_mode";

    public static final String LEVEL_TYPE_KEY = "level_type";

    public static final String FIRST_STARTED_TIME_KEY = "first_started_time";

    public static final String STARTED_TOTAL_TIMES_KEY = "started_total_times";

    public static final String COLOR_MODE[][] = new String[][] {
            {
                    "day_font_color", "day_bg_color", "day_select_color"
            }, {
                    "night_font_color", "night_bg_color", "night_select_color"
            }, {
                    "custom_font_color", "custom_bg_color", "custom_select_color"
            }
    };

    public static final String REVIEW_TIME_KEY[] = new String[] {
            "review_time_1", "review_time_2", "review_time_3"
    };

    public static final String LAST_SEND_GET_PW_REQUEST_TIME_KEY = "last_send_get_pw_request_time";

    public static final String SAVED_PW_KEY = "saved_pw_key";

    public static final String LOG_TYPE_KEY = "log_type";

    /**
     * if less than cur lev, upgrade and send a msg to author.
     */
    public static final String MARK_SEND_MSG_LEVEL_KEY = "mark_send_msg_level";

    public static final String VIEW_METHOD_KEY = "view_method";

    public static final String LANGUAGE_TYPE = "explain_language_type";

    public static final String DB_VERSION_KEY = "db_version";

    public static final String DICTS_KEY = "dicts";

    public static final String VIEW_SUMMARY_INFO_KEY = "view_summary_info";

    public static final String VIEW_DETAILED_INFO_KEY = "view_detailed_info";

    public static final String SAVE_CACHE_KEY = "save_cache";

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

    public static String getCurLibraryString(Context context) {
        String str = getString(context, DICTS_KEY, "");
        String temps[] = str.split(";");
        for (String string : temps) {
            String subs[] = string.split(",");
            int type = Integer.parseInt(subs[4]);
            if (type == 1 || type == 3) {
                return subs[0];
            }
        }
        return DictManager.DEFAULT_CUR_LIB;
    }

    public static String getMoreLibraryString(Context context) {
        String str = getString(context, DICTS_KEY, "");
        String temps[] = str.split(";");
        for (String string : temps) {
            String subs[] = string.split(",");
            int type = Integer.parseInt(subs[4]);
            if (type == 2 || type == 3) {
                return subs[0];
            }
        }
        return DictManager.DEFAULT_MORE_LIB;
    }
}
