package me.bright.damage;

import me.bright.entity.BrightEntity;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Damage(@NotNull DamageType type,
                     long amount,
                     @Nullable BrightEntity dealer,
                     boolean critical) {

    private static final long balanceFactor = 100;

    public static long getBalanceFactor() {
        return balanceFactor;
    }

    public static Damage noDamage(@NotNull BrightEntity dealer) {
        return new Damage(DamageType.TRUE, 0, dealer, false);
    }

    public static Damage noDamage = new Damage(DamageType.TRUE, 0, null, false);


    @Override
    public String toString() {
        return ChatColor.RED + String.valueOf(amount) + type.color + type.displayName + " Damage";
    }
}
