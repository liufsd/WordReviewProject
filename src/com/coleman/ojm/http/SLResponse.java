
package com.coleman.ojm.http;

import com.coleman.ojm.bean.BasicResponse;
import com.coleman.ojm.core.Observable;
import com.coleman.util.MyApp;

public class SLResponse<T extends BasicResponse> extends Observable {
    private boolean loaded = false;

    private T mResponseBean;

    @SuppressWarnings("unchecked")
    public SLResponse(Class<?> mClass) {
        try {
            mResponseBean = (T) mClass.newInstance();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    public T getResponse() {
        return mResponseBean;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setResponse(T reponse) {
        this.mResponseBean = reponse;
    }

    public void notifyLoaded() {
        MyApp.handler.post(new Runnable() {
            @Override
            public void run() {
                loaded = true;
                setChanged();
                SLResponse.super.notifyObservers(mResponseBean);
            }
        });
    }

    public void notifyError(final String data) {
        MyApp.handler.post(new Runnable() {
            @Override
            public void run() {
                loaded = true;
                setChanged();
                mResponseBean.setResultCode(-1);
                mResponseBean.setDescription(data);
                SLResponse.super.notifyObservers(mResponseBean);
            }
        });
    }

}
