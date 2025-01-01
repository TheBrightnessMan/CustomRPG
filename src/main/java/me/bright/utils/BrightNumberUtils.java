package me.bright.utils;

public class BrightNumberUtils {

    public static double clamp(double min, double value, double max) {
        return Math.max(min, Math.min(value, max));
    }
}
