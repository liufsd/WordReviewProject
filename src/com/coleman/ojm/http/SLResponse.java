
package com.coleman.ojm.http;

import com.coleman.ojm.bean.BasicResponse;
import com.coleman.ojm.core.Observable;

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

    /**
     * 如果一个Activity已经被Finish, 这个Activity在被通知时会被过滤掉
     */
    @Override
    public void notifyObservers(Object errorData) {
        loaded = true;
        setChanged();
        if (errorData != null) {
            mResponseBean.setResultCode(-1);
            mResponseBean.setDescription(String.valueOf(errorData));
        }
        SLResponse.super.notifyObservers(mResponseBean);
    }
}
