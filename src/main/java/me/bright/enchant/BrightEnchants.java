package me.bright.enchant;

import me.bright.enchant.enchants.Sharpness;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class BrightEnchants {

    public static BrightEnchant SHARPNESS = new Sharpness();

    public static Map<String, BrightEnchant> enchants = Map.of(
            SHARPNESS.getKey(), SHARPNESS
    );

    public static @Nullable BrightEnchant fromKey(@NotNull String key) {
        return enchants.get(key);
    }
}
