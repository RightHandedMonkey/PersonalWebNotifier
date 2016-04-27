package com.rhm.pwn.models;

import junit.framework.Assert;

/**
 * Created by sambo on 4/25/2016.
 */
public class URLPeriods {
    public final static int MANUAL = 0;
    public final static int DAY_1 = 86400000;
    public final static int HOURS_6 = 21600000;
    public final static int HOURS_1 = 3600000;
    public final static int MINUTES_15 = 900000;
    public final static int MINUTES_5 = 300000;
    public final static int MINUTES_1 = 60000;

    // Array of choices
    public final static String names[] = {"Manual", "1 Day", "6 Hours", "1 Hour", "15 Minutes", "5 Minutes", "1 Minute"};
    // Array of values in milliseconds
    public final static int values[] = {MANUAL, DAY_1, HOURS_6, HOURS_1, MINUTES_15, MINUTES_5, MINUTES_1};

    public static String getName(int position) {
        Assert.assertTrue(position <= URLPeriods.names.length);
        return URLPeriods.names[position];
    }

    public static int getValue(int position) {
        Assert.assertTrue(position <= URLPeriods.values.length);
        return URLPeriods.values[position];
    }

}
