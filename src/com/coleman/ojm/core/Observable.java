
package com.coleman.ojm.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.app.Activity;

import com.coleman.util.Log;

public class Observable {
    private static final String TAG = Observable.class.getName();

    List<Observer> observers = new ArrayList<Observer>();

    boolean changed = false;

    /**
     * Constructs a new {@code Observable} object.
     */
    public Observable() {
        super();
    }

    /**
     * Adds the specified observer to the list of observers. If it is already
     * registered, it is not added a second time. *
     * <p>
     * <b> Notice:</b> If the observer is an Activity, and it is finished, it
     * will not be notified, it is automatic. That means you don't need to
     * remove the observer if it is an Activity.
     * </p>
     * 
     * @param observer the SLObserver to add.
     */
    public void addObserver(Observer observer) {
        if (observer == null) {
            throw new NullPointerException();
        }
        synchronized (this) {
            if (!observers.contains(observer))
                observers.add(observer);
        }
    }

    /**
     * Adds the specified observers to the list of observers. If it is already
     * registered, it is not added a second time.
     * <p>
     * <b> Notice:</b> If the observer is an Activity, and it is finished, it
     * will not be notified, it is automatic. That means you don't need to
     * remove the observer if it is an Activity.
     * </p>
     * 
     * @param obs the observers to be added.
     */
    public void addObservers(Collection<Observer> obs) {
        if (obs == null) {
            throw new NullPointerException();
        }
        synchronized (this) {
            for (Observer observer : obs) {
                if (!observers.contains(observer))
                    observers.add(observer);
            }
        }
    }

    /**
     * Get all the observers added before.
     * 
     * @return the list observers
     */
    public List<Observer> getObservers() {
        return observers;
    }

    /**
     * Clears the changed flag for this {@code Observable}. After calling
     * {@code clearChanged()}, {@code hasChanged()} will return {@code false}.
     */
    protected void clearChanged() {
        changed = false;
    }

    /**
     * Returns the number of observers registered to this {@code Observable}.
     * 
     * @return the number of observers.
     */
    public int countObservers() {
        return observers.size();
    }

    /**
     * Removes the specified observer from the list of observers. Passing null
     * won't do anything.
     * 
     * @param observer the observer to remove.
     */
    public synchronized void deleteObserver(Observer observer) {
        observers.remove(observer);
    }

    /**
     * Removes all observers from the list of observers.
     */
    public synchronized void deleteObservers() {
        observers.clear();
    }

    /**
     * Returns the changed flag for this {@code Observable}.
     * 
     * @return {@code true} when the changed flag for this {@code Observable} is
     *         set, {@code false} otherwise.
     */
    public boolean hasChanged() {
        return changed;
    }

    /**
     * If {@code hasChanged()} returns {@code true}, calls the {@code update()}
     * method for every Observer in the list of observers using the specified
     * argument. Afterwards calls {@code clearChanged()}.
     * 
     * @param data the argument passed to {@code update()}.
     */
    protected void notifyObservers(Object data) {
        int size = 0;
        Observer[] arrays = null;
        synchronized (this) {
            if (hasChanged()) {
                clearChanged();
                size = observers.size();
                arrays = new Observer[size];
                observers.toArray(arrays);
            }
        }
        if (arrays != null) {
            for (Observer observer : arrays) {
                Log.i(TAG, "===coleman-debug-notify observer: " + observer.getClass().getName());
                if (observer instanceof Activity) {
                    Activity tempActivity = (Activity) observer;
                    if (!tempActivity.isFinishing()) {
                        observer.update(data);
                    } else {
                        Log.w(TAG, "===coleman-debug-Notified failed, "
                                + observer.getClass().getName() + " is finished!");
                    }
                } else {
                    observer.update(data);
                }
            }
        }
    }

    /**
     * Sets the changed flag for this {@code Observable}. After calling
     * {@code setChanged()}, {@code hasChanged()} will return {@code true}.
     */
    protected void setChanged() {
        changed = true;
    }
}
