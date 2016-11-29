package com.tribalscale.gamify.utils;


public final class StringUtils {

    private StringUtils() {
        // no-op
    }

    /**
     * Returns {@code true} if the String passed in is Empty or {@code null}.
     *
     * @param string The {@link String} to check.
     * @return {@code true} if the String passed in is Empty or {@code null}.
     */
    public static boolean isEmptyOrNull(String string) {
        return string == null || string.length() <= 0;
    }
}
