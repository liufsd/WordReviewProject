
package com.coleman.kingword.inspirit.countdown;

import java.io.Serializable;

import android.content.Context;
import android.os.Handler;

import com.coleman.kingword.CoreActivity;
import com.coleman.kingword.R;
import com.coleman.log.Log;
import com.coleman.util.Config;

/**
 * As there are many ways to view word list, so the countdown manager should not
 * be designed as singleton.
 * 
 * @author coleman
 */
public class CountdownManager implements Serializable {
    private static final long serialVersionUID = 7969022016568854596L;

    private static final String TAG = CountdownManager.class.getName();

    private static Log Log = Config.getLog();

    private int TOTAL_TIME;

    private int costTime = 0;

    private transient Handler handler;


    public void setHandler(Handler handler) {
        this.handler = handler;
        start();
    }

    public CountdownManager(Handler handler, int wordsNum, int count_down) {
        this.handler = handler;
        this.TOTAL_TIME = 12 * wordsNum;
        costTime = count_down;
        start();
    }

    public void start() {
        handler.sendEmptyMessage(CoreActivity.UPDATE_REMAINDER_TIME);
    }

    public void pause() {
        handler.removeMessages(CoreActivity.UPDATE_REMAINDER_TIME);
    }

    public String getRemainderTimeShortFormatted(Context context) {
        int remainder = TOTAL_TIME - costTime;
        int m, s;
        boolean over = false;
        if (remainder < 0) {
            over = true;
            remainder = -remainder;
        }
        m = (int) (remainder / 60);
        s = (int) (remainder % 60);
        String reStr = !over ? context.getString(R.string.remainder_time) + "\n" + getMString(m)
                + ":" + getSString(s) : context.getString(R.string.over_time) + "\n"
                + getMString(m) + ":" + getSString(s);
        // Log.d(TAG, "********remainder time>>>" + reStr);
        return reStr;
    }

    private String getMString(int m) {
        return m < 10 ? "0" + m : "" + m;
    }

    private String getSString(int s) {
        return s < 10 ? "0" + s : "" + s;
    }

    public void update() {
        costTime++;
        handler.sendEmptyMessageDelayed(CoreActivity.UPDATE_REMAINDER_TIME, 1000);
    }

    public int getCountDown() {
        return costTime;
    }
}
