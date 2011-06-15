
package com.coleman.kingword.countdown;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.coleman.kingword.R;
import com.coleman.kingword.activity.CoreActivity;

/**
 * As there are many ways to view word list, so the countdown manager should not
 * be designed as singleton.
 * 
 * @author coleman
 */
public class CountdownManager {
    private final long TOTAL_TIME;

    private static final String TAG = CountdownManager.class.getName();

    private long startTime, pauseTime;

    private boolean running = false;

    private Handler handler;

    public CountdownManager(Handler handler, int wordsNum) {
        this.handler = handler;
        this.TOTAL_TIME = 12 * wordsNum * 1000;
        start();
    }

    private void start() {
        running = true;
        update();
        startTime = System.currentTimeMillis();
    }

    public void pause() {
        running = false;
        pauseTime = System.currentTimeMillis();
    }

    public void resume() {
        running = true;
        update();
        long curTime = System.currentTimeMillis();
        startTime = startTime + (curTime - pauseTime);
    }

    public long getSpentTime() {
        return System.currentTimeMillis() - startTime;
    }

    public String getRemainderTimeFormatted(Context context) {
        long remainder = TOTAL_TIME - getSpentTime();
        int m, s, ms;
        boolean over = false;
        if (remainder < 0) {
            over = true;
            remainder = -remainder;
        }
        m = (int) (remainder / (60 * 1000));
        s = (int) (remainder / 1000 % 60);
        ms = (int) (remainder % 1000);
        String reStr = !over ? context.getString(R.string.remainder_time) + "\n" + getMString(m)
                + ":" + getSString(s) + ":" + getMSString(ms) : context
                .getString(R.string.over_time)
                + "\n"
                + getMString(m)
                + ":"
                + getSString(s)
                + ":"
                + getMSString(ms);
        Log.d(TAG, "********remainder time>>>" + reStr);
        return reStr;
    }

    public String getRemainderTimeShortFormatted(Context context) {
        long remainder = TOTAL_TIME - getSpentTime();
        int m, s;
        boolean over = false;
        if (remainder < 0) {
            over = true;
            remainder = -remainder;
        }
        m = (int) (remainder / (60 * 1000));
        s = (int) (remainder / 1000 % 60);
        String reStr = !over ? context.getString(R.string.remainder_time) + "\n" + getMString(m)
                + ":" + getSString(s) : context.getString(R.string.over_time) + "\n"
                + getMString(m) + ":" + getSString(s);
        Log.d(TAG, "********remainder time>>>" + reStr);
        return reStr;
    }

    private String getMString(int m) {
        return m < 10 ? "0" + m : "" + m;
    }

    private String getSString(int s) {
        return s < 10 ? "0" + s : "" + s;
    }

    private String getMSString(int ms) {
        if (ms < 10) {
            return "00" + ms;
        } else if (ms < 100) {
            return "0" + ms;
        } else {
            return "" + ms;
        }
    }

    public void update() {
        if (running) {
            handler.sendEmptyMessageDelayed(CoreActivity.UPDATE_REMAINDER_TIME, 1000);
        }
    }
}
