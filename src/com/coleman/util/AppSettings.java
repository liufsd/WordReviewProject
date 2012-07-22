/*
 * Name        : Settings.java
 * Author      : Coleman
 * Copyright   : Copyright (c) 2009-2012 CIeNET Ltd. All rights reserved
 * Description : Settings.java
 * Review      : 
 */

package com.coleman.util;

import com.coleman.kingword.wordlist.IgnoreListVisitor;
import com.coleman.kingword.wordlist.NewListVisitor;
import com.coleman.kingword.wordlist.ReviewListVisitor;
import com.coleman.kingword.wordlist.SubListVisitor;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * 在res/xml中定义的参数必须也定义到AppSettings中
 * 
 * @author coleman
 * @version [version, Jun 21, 2012]
 * @see [relevant class/method]
 * @since [product/module version]
 */
public class AppSettings {
    public static final String TAG = "AppSettings";

    /* 升级数据库需要保存的参数 */
    /**
     * 默认值是6，其值对应软件的版本号。
     */
    public static final String SAVED_DB_VERSION_KEY = "saved_db_version_key";

    /* CoreActivity中需要保存的参数 */

    public static final String ANIM_TYPE_KEY = "anim_type";

    public static final String IS_NIGHT_MODE_KEY = "isNightMode";

    public static final String FIRST_STARTED_KEY = "first_started";

    public static final String SELECT_COLOR_MODE_KEY = "select_color_mode";

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

    public static final String SAVE_CACHE_KEY = "save_cache";

    /* WelcomeActivity中需要保存的参数 */
    public static final String LOG_TYPE_KEY = "log_type";

    // if less than cur level, upgrade and send a message to author.
    public static final String MARK_SEND_MSG_LEVEL_KEY = "mark_send_msg_level";

    /* settings.xml 中定义的KEY */

    public static final String SPLIT = "split";

    public static final String LEVEL = "level";

    public static final String VERSION_CHECK = "version_check";

    public static final String VIEW_METHOD = "view_method";

    public static final String IGNORE_VIEW_METHOD = "ignore_view_method";

    public static final String NEW_VIEW_METHOD = "new_view_method";

    public static final String SUB_VIEW_METHOD = "sub_view_method";

    public static final String REVIEW_VIEW_METHOD = "review_view_method";

    public static final String METHODS[] = new String[] {
            SUB_VIEW_METHOD, REVIEW_VIEW_METHOD, IGNORE_VIEW_METHOD, NEW_VIEW_METHOD
    };

    public static final String DEFAULT_METHOD_VALUE[] = new String[] {
            SubListVisitor.DEFAULT_VIEW_METHOD, ReviewListVisitor.DEFAULT_VIEW_METHOD,
            IgnoreListVisitor.DEFAULT_VIEW_METHOD, NewListVisitor.DEFAULT_VIEW_METHOD
    };

    public static final String CUT_LIB_KEY = "cut_lib";

    public static final String MORE_LIB_KEY = "more_lib";

    public static final String DATABASE_SET = "database_set";

    public static final String RESTORE = "restore";

    public static final String BACKUP = "backup";

    /* review_settings.xml 中定义的KEY */
    public static final String FIXED_TIME_REVIEW = "fixed_time_review";

    public static final String TIME3 = "time3";

    public static final String TIME2 = "time2";

    public static final String TIME1 = "time1";

    public static void saveBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean getBoolean(String key, boolean defvalue) {
        return getSharedPreferences().getBoolean(key, defvalue);
    }

    /**
     * Save a string value to the shared preference.
     * 
     * @param context to construct a preference.
     * @param key to mark the store value.
     * @param value to saved value.
     */
    public static void saveString(String key, String value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
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
    public static String getString(String key, String def) {
        return getSharedPreferences().getString(key, def);
    }

    /**
     * Save a integer value to the shared preference.
     * 
     * @param context to construct a preference.
     * @param key to mark the store value.
     * @param value to saved value.
     */
    public static void saveInt(String key, int value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
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
    public static int getInt(String key, int def) {
        return getSharedPreferences().getInt(key, def);
    }

    /**
     * Save a Long value to the shared preference.
     * 
     * @param context to construct a preference.
     * @param key to mark the store value.
     * @param value to saved value.
     */
    public static void saveLong(String key, long value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
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
    public static long getLong(String key, long def) {
        return getSharedPreferences().getLong(key, def);
    }

    /**
     * Retrieve the package shared preferences object.
     * 
     * @param context
     * @return
     */
    private static SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(MyApp.context);
    }

}
