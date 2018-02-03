package com.rhm.pwn.model;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

/**
 * Created by sambo on 9/2/17.
 */

public enum URLCheckInterval {
    INT_1_MIN(1 * 60, "1 Minute"),
    INT_15_MIN(15 * 60, "15 Minutes"),
    INT_1_HOUR(60 * 60, "1 Hour"),
    INT_6_HOURS(6 * 60 * 60, "6 Hours"),
    INT_1_DAY(24 * 60 * 60, "1 Day");

    public static final int DEFAULT_VAL = 3;

    private final long interval;
    private final String description;

    URLCheckInterval(long intv, String desc) {
        this.interval = intv;
        this.description = desc;
    }

    public long getInterval() {
        return interval;
    }

    public String getDescription() {
        return description;
    }

    public static String getDescriptionFromInterval(long intv) {
        String desc = "{Unknown Interval}";
        for (URLCheckInterval u : values()) {
            if (u.interval == intv) {
                desc = u.description;
                break;
            }
        }
        return desc;
    }

    public static int getIndexFromInterval(long intv) {
        int index = 0;
        for (URLCheckInterval u : values()) {
            if (u.interval == intv) {
                break;
            }
            index++;
        }
        return index;
    }

    public static long getIntervalFromDescription(String desc) {
        long intv = INT_1_DAY.getInterval();
        for (URLCheckInterval u : values()) {
            if (u.description.equals(desc)) {
                intv = u.interval;
                break;
            }
        }
        return intv;
    }

    public static List<String> getIntervalStrings() {
        return StreamSupport.stream(Arrays.asList(URLCheckInterval.values()))
                .map(urlCheckInterval -> urlCheckInterval.description)
                .collect(Collectors.toCollection(ArrayList::new));
    }

}
