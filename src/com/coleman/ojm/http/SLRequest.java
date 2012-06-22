
package com.coleman.ojm.http;

import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.HashSet;

import android.util.Log;

import com.coleman.ojm.core.Observer;
import com.coleman.util.ThreadUtils;

public class SLRequest<T> {
    private static final String TAG = "SLRequest";

    private T mRequestBean;

    private boolean cancel;

    private HashSet<Observer> set = new HashSet<Observer>();

    private HttpURLConnection mConnection;

    public SLRequest(T request) {
        this.mRequestBean = request;
    }

    public T getRequest() {
        return mRequestBean;
    }

    public boolean isCancel() {
        return cancel;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }

    public boolean addObserver(Observer observer) {
        return set.add(observer);
    }

    public boolean removeObserver(Observer observer) {
        return set.remove(observer);
    }

    public Collection<Observer> getObservers() {
        return set;
    }

    void setConnection(HttpURLConnection con) {
        this.mConnection = con;
    }

    void startTimeOutCount() {
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(20 * 1000);
                    if (mConnection != null) {
                        mConnection.disconnect();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    void disconnect() {
        try {
            if (this.mConnection != null) {
                this.mConnection.disconnect();
            }
        } catch (Exception e) {
            Log.i(TAG, "" + e.getStackTrace());
        }
    }
}
