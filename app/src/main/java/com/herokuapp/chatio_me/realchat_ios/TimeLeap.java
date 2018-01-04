package com.herokuapp.chatio_me.realchat_ios;

import android.app.Application;
import android.content.Context;

/**
 * Created by Tandv on 12/14/2017.
 */

public class TimeLeap extends Application{
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;


    public static String getTimeAgo(long time, Context ctx) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "vừa offline";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "offline khoảng 1 phút trước";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return "offline " + diff / MINUTE_MILLIS + " phút trước";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "offline 1 giờ trước";
        } else if (diff < 24 * HOUR_MILLIS) {
            return "offline " + diff / HOUR_MILLIS + " giờ trước";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "offline hôm qua";
        } else {
            return "offline " + diff / DAY_MILLIS + " ngày trước";
        }
    }

    /**
     * Created by DucPV on 12/15/2017.
     */

    public static class Requests {
    }
}
