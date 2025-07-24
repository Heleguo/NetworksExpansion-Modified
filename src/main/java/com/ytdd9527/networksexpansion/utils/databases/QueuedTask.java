package com.ytdd9527.networksexpansion.utils.databases;

public interface QueuedTask {

    /**
     * The code will be invoked
     *
     * @return true if it does callback, else false.
     */
    boolean execute();

    /**
     * Invoke after execute()
     *
     * @return true if abort the processor thread, else false.
     */
    default boolean callback() {
        return false;
    }
}
