package com.dank.util;

/**
 * Project: RS3Injector
 * Time: 23:58
 * Date: 03-02-2015
 * Created by Dogerina.
 */
public final class Time {

    private Time() {}

    public static String seconds(final long startNano, final long endNano) {
        return String.format("%.2f", (endNano - startNano) / 1e9);
    }
}
