package com.coleman.http.json.connection;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import com.coleman.util.MyApp;

public class SLResponse<T> extends Observable
{
    private static final String TAG = "SLResponse";
    
    private boolean loaded = false;
    
    private T mResponseBean;
    
    @SuppressWarnings("unchecked")
    public SLResponse(Class<?> mClass)
    {
        try
        {
            mResponseBean = (T) mClass.newInstance();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (InstantiationException e)
        {
            e.printStackTrace();
        }
    }
    
    public T getResponse()
    {
        return mResponseBean;
    }
    
    public boolean isLoaded()
    {
        return loaded;
    }
    
    public void setResponse(T reponse)
    {
        this.mResponseBean = reponse;
    }
    
    public void notifyLoaded()
    {
        MyApp.hander.post(new Runnable()
        {
            @Override
            public void run()
            {
                List<Observer> list = getObservers();
                
                ActivityManager am = (ActivityManager) MyApp.context.getSystemService(Context.ACTIVITY_SERVICE);
                ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
                String cName = cn.getClassName();
                Log.v(TAG, "Top activity name: " + cName);
                
                int size = list.size();
                Observer[] arrays = new Observer[size];
                list.toArray(arrays);
                
                loaded = true;
                
                for (Observer observer : arrays)
                {
                    if (observer instanceof Activity)
                    {
                        observer.update(SLResponse.this, null);
                        Log.i(TAG, "===coleman-debug-notifyObserver: "
                                + observer.getClass().getName());
                    }
                    else
                    {
                        observer.update(SLResponse.this, null);
                        Log.i(TAG, "===coleman-debug-notifyObserver: "
                                + observer.getClass().getName());
                    }
                }
            }
        });
    }
    
    public void notifyError(final String cause)
    {
        MyApp.hander.post(new Runnable()
        {
            @Override
            public void run()
            {
                String cName = null;
                try
                {
                    List<Observer> list = getObservers();
                    
                    ActivityManager am = (ActivityManager) MyApp.context.getSystemService(Context.ACTIVITY_SERVICE);
                    ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
                    cName = cn.getClassName();
                    Log.v(TAG, "Top activity name: " + cName);
                    
                    int size = list.size();
                    Observer[] arrays = new Observer[size];
                    list.toArray(arrays);
                    for (Observer observer : arrays)
                    {
                        if (observer instanceof Activity)
                        {
                            observer.update(SLResponse.this, cause);
                            Log.i(TAG, "===coleman-debug-notifyError: "
                                    + observer.getClass().getName());
                        }
                        else
                        {
                            observer.update(SLResponse.this, cause);
                            Log.i(TAG, "===coleman-debug-notifyError: "
                                    + observer.getClass().getName());
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    private List<Observer> getObservers()
    {
        List<Observer> observer = null;
        try
        {
            Field field = Observable.class.getDeclaredField("observers");
            field.setAccessible(true);
            Object object = field.get(this);
            if (object instanceof List<?>)
            {
                observer = (List<Observer>) object;
            }
        }
        catch (SecurityException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return observer;
    }
}
