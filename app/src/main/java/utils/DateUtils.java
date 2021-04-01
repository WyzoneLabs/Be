package utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * A class with static util methods.
 */

public class DateUtils {

    // This class should not be initialized
    private DateUtils() {

    }

    /**
     * Gets timestamp in millis and converts it to HH:mm (e.g. 16:44).
     */
    public static String formatTime(long timeInMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return dateFormat.format(timeInMillis);
    }

    public static String formatTimeWithMarker(long timeInMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return dateFormat.format(timeInMillis);
    }

    public static int getHourOfDay(long timeInMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("H", Locale.getDefault());
        return Integer.valueOf(dateFormat.format(timeInMillis));
    }

    public static int getMinute(long timeInMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("m", Locale.getDefault());
        return Integer.valueOf(dateFormat.format(timeInMillis));
    }

    /**
     * If the given time is of a different date, display the date.
     * If it is of the same date, display the time.
     *
     * @param timeInMillis The time to convert, in milliseconds.
     * @return The time or date.
     */
    public static String formatDateTime(long timeInMillis) {
        if (isToday(timeInMillis)) {
            return formatTime(timeInMillis);
        } else {
            return formatDate(timeInMillis);
        }
    }

    /**
     * Formats timestamp to 'date month' format (e.g. 'February 3').
     */
    public static String formatDate(long timeInMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd", Locale.getDefault());
        return dateFormat.format(timeInMillis);
    }

    /**
     * Returns whether the given date is today, based on the user's current locale.
     */
    public static boolean isToday(long timeInMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String date = dateFormat.format(timeInMillis);
        return date.equals(dateFormat.format(System.currentTimeMillis()));
    }

    public static long getTimePassed(long timeMillis) {
        if (timeMillis == 0) return 0;

        long millis = System.currentTimeMillis() - timeMillis;

        return millis / 1000;
    }

    public static String getPassedTime(long timeInMillis) {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("H", Locale.getDefault());

        if (timeInMillis == 0) {
            return "";
        }

        long millis = System.currentTimeMillis() - timeInMillis;
        long sec = millis / 1000;
        long mins = sec / 60;
        long hours = mins / 60;
        long days = hours / 24;
        long weeks = days / 7;
        long months = weeks / 4;

        if (sec <= 30) {
            return "few secs ago";
        } else if (sec < 60) {
            return sec + "s";
        } else if (mins >= 1 && mins < 60) {
            return mins + "m ago";
        } else if (hours >= 1 && hours < 24) {
            return hours + "h ago";
        } else if (days >= 1 && days < 7) {
            return days + "d ago";
        } else if (weeks >= 1 && weeks < 4) {
            return weeks + "w ago";
        } else if (months >= 1) {
            return months + "mo ago";
        } else {
            return "";
        }
//        return Integer.valueOf(dateFormat.format(timeInMillis));
    }

    /**
     * Checks if two dates are of the same day.
     *
     * @param millisFirst  The time in milliseconds of the first date.
     * @param millisSecond The time in milliseconds of the second date.
     * @return Whether {@param millisFirst} and {@param millisSecond} are off the same day.
     */
    public static boolean hasSameDate(long millisFirst, long millisSecond) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return dateFormat.format(millisFirst).equals(dateFormat.format(millisSecond));
    }
}
