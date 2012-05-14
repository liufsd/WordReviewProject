package com.coleman.http.json.connection;

import java.net.HttpURLConnection;

import com.coleman.util.ThreadUtils;


import android.util.Log;

public class SLRequest<T>
{
    private static final String TAG = "SLRequest";
    
    private T mRequestBean;
    
    private boolean cancel;
    
    private HttpURLConnection mConnection;
    
    public SLRequest(T request)
    {
        this.mRequestBean = request;
    }
    
    public T getRequest()
    {
        return mRequestBean;
    }
    
    public boolean isCancel()
    {
        return cancel;
    }
    
    public void setCancel(boolean cancel)
    {
        this.cancel = cancel;
    }
    
    void setConnection(HttpURLConnection con)
    {
        this.mConnection = con;
    }
    
    void startTimeOutCount()
    {
        ThreadUtils.execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Thread.sleep(20 * 1000);
                    if (mConnection != null)
                    {
                        mConnection.disconnect();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }
    
    void disconnect()
    {
        try
        {
            if (this.mConnection != null)
            {
                this.mConnection.disconnect();
            }
        }
        catch (Exception e)
        {
            Log.i(TAG, "" + e.getStackTrace());
        }
    }
}
