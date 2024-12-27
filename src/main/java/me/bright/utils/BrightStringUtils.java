package me.bright.utils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class BrightStringUtils {

    public static String toPrettyString(String key) {
        return Arrays.stream(key.toLowerCase().split("_"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}
