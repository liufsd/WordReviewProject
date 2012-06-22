
package com.coleman.ojm.core;

/**
 * Provide config parameter to setup the observer.
 * <p>
 * if config.discard() is called, the observer will not be notified.
 * 
 * @author coleman
 * @version [version, May 30, 2012]
 * @see [relevant class/method]
 * @since [product/module version]
 */
public interface Observer {
    /**
     * This method is called if the specified {@code Observable} object's
     * {@code notifyObservers} method is called (because the {@code Observable}
     * object has been updated.
     * 
     * @param data the data passed to {@link Observable#notifyObserver(Object)}
     *            .
     */
    void update(Object data);

}
