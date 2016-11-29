package com.tribalhacks.gamify.utils;

import java.util.concurrent.atomic.AtomicInteger;

public final class IntegerUtils {

    private static final AtomicInteger seed = new AtomicInteger();

    private IntegerUtils() {
        // no-op
    }

    /**
     * Gets the next integer in {@link #seed} and then increments so that the next time you use this
     * method, it is guaranteed to return a different integer. Use this places where you would need
     * a unique integer.
     * <p>
     * http://stackoverflow.com/a/13179983
     *
     * @return A fresh integer.
     */
    public static int getFreshInt() {
        return seed.incrementAndGet();
    }
}
