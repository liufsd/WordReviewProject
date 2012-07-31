
package com.coleman.log;

import java.io.Serializable;

public abstract class LogImpl implements Log, Serializable {
    private static final long serialVersionUID = -3275114910556411467L;

    protected Level level;

    @Override
    public void v(String TAG, Object msg) {
        log(TAG, Level.verbose, msg);
    }

    @Override
    public void d(String TAG, Object msg) {
        log(TAG, Level.debug, msg);
    }

    @Override
    public void i(String TAG, Object msg) {
        log(TAG, Level.info, msg);
    }

    @Override
    public void w(String TAG, Object msg) {
        log(TAG, Level.warning, msg);
    }

    @Override
    public void e(String TAG, Object msg) {
        log(TAG, Level.error, msg);
    }

    @Override
    public void setLevel(Level level) {
        this.level = level;
    }

    protected abstract void log(String TAG, Level verbose, Object msg);

}
