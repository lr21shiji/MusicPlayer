package com.attackt.music;

import java.util.Locale;

/**
 * Created by mh on 2017/5/14.
 */
public class MediaUtils {

    public static String formatTime(long time) {
        String finalTimerString = "";
        int hours, minutes, seconds;

        hours = (int) (time / (1000 * 60 * 60));
        minutes = (int) (time % (1000 * 60 * 60)) / (1000 * 60);
        seconds = (int) ((time % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        if (hours > 0) {
            finalTimerString = String.format(Locale.getDefault(),
                    "%02d%02d:%02d", hours, minutes, seconds);
        } else {
            finalTimerString = String.format(Locale.getDefault(), "%02d:%02d",
                    minutes, seconds);
        }
        return finalTimerString;
    }

}

