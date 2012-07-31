
package com.coleman.kingword.skin;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;

import com.coleman.util.AppSettings;

public class ColorManager {
    private static ColorManager mgr;

    private int selectMode;// 0 day 1 night 2 custom

    private int textColor, bgColor, selectColor;

    private ColorManager() {
        init();
    }

    public static ColorManager getInstance() {
        if (mgr == null) {
            mgr = new ColorManager();
        }
        return mgr;
    }

    public int getTextColor() {
        return textColor;
    }

    public int getBgColor() {
        return bgColor;
    }

    public int selectColor() {
        return selectColor;
    }

    public int getSelectMode() {
        return selectMode;
    }

    public StateListDrawable getSelector() {
        return new BGDrawable();
    }

    public void init() {
        selectMode = AppSettings.getInt(AppSettings.SELECT_COLOR_MODE_KEY, 0);
        textColor = AppSettings.getInt(AppSettings.COLOR_MODE[selectMode][0], Color.BLACK);
        bgColor = AppSettings.getInt(AppSettings.COLOR_MODE[selectMode][1], Color.WHITE);
        selectColor = AppSettings.getInt(AppSettings.COLOR_MODE[selectMode][2], Color.GRAY);
    }

    private class BGDrawable extends StateListDrawable {
        public BGDrawable() {
            addState(new int[] {
                android.R.attr.state_pressed
            }, new ColorDrawable(selectColor));
            addState(new int[] {
                android.R.attr.state_selected
            }, new ColorDrawable(selectColor));
            addState(new int[] {
                -android.R.attr.state_selected
            }, new ColorDrawable(bgColor));
        }
    }
}
