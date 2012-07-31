
package com.coleman.log.jdk;

import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Logger;

import com.coleman.log.LogImpl;

class JavaLogImpl extends LogImpl {

    private static final long serialVersionUID = -9155633081308008268L;

    private String key;

    private boolean printed = true;

    public JavaLogImpl(String key) {
        this.key = key;
        Logger.getLogger(key);
    }

    public JavaLogImpl(String key, String fileName, int limitSize, int fileCount) {
        this.key = key;
        Logger log = Logger.getLogger(key);
        log.setUseParentHandlers(false);
        try {
            FileHandler handler = new FileHandler(fileName, limitSize, fileCount);
            Formatter formatter = new JavaLogFormatter();
            handler.setFormatter(formatter);
            log.addHandler(handler);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void log(String TAG, Level level, Object msg) {
        Logger log = Logger.getLogger(key);
        if (printed) {
            android.util.Log.println(level.value, TAG, String.valueOf(msg));
        }
        java.util.logging.Level lev = getJavaLevel(level);
        if (msg instanceof Throwable) {
            log.logp(lev, TAG, "", "", (Throwable) msg);
        } else {
            log.logp(lev, TAG, "", String.valueOf(msg));
        }
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        Logger log = Logger.getLogger(key);
        log.setLevel(getJavaLevel(level));
    }

    @Override
    public void setPrintable(boolean printed) {
        this.printed = printed;
    }

    private java.util.logging.Level getJavaLevel(Level level) {
        java.util.logging.Level lev;
        switch (level) {
            case verbose:
            default:
                lev = java.util.logging.Level.FINE;
                break;
            case debug:
                lev = java.util.logging.Level.CONFIG;
                break;
            case info:
                lev = java.util.logging.Level.INFO;
                break;
            case warning:
                lev = java.util.logging.Level.WARNING;
                break;
            case error:
                lev = java.util.logging.Level.SEVERE;
                break;
            case off:
                lev = java.util.logging.Level.OFF;
                break;
        }
        return lev;
    }

}
