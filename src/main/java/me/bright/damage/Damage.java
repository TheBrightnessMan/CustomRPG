package me.bright.damage;

import me.bright.entity.BrightEntity;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public record Damage(@NotNull DamageType type, long amount, @Nullable BrightEntity dealer, boolean critical) {

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
        return ChatColor.RED + String.valueOf(amount) + type.color + type.displayName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Damage(
                DamageType otherType,
                long otherAmount,
                BrightEntity otherDealer,
                boolean otherCritical
        ))) return false;
        return this.type() == otherType &&
                this.amount() == otherAmount &&
                this.dealer() == otherDealer &&
                this.critical() == otherCritical;
    }

    public static @NotNull List<Damage> mergeDamages(@NotNull List<Damage> damages) {
        if (damages.isEmpty())
            return Arrays.asList(
                    new Damage(DamageType.PHYSICAL, 0, null, false),
                    new Damage(DamageType.MAGIC, 0, null, false),
                    new Damage(DamageType.TRUE, 0, null, false)
            );
        BrightEntity dealer = damages.getFirst().dealer();
        long physicalDamage = 0L,
                magicDamage = 0L,
                trueDamage = 0L;
        boolean physicalCrit = false,
                magicCrit = false,
                trueCrit = false;
        for (Damage damage : damages) {
            switch (damage.type()) {
                case PHYSICAL -> {
                    physicalDamage += damage.amount;
                    physicalCrit = physicalCrit || damage.critical();
                }
                case MAGIC -> {
                    magicDamage += damage.amount;
                    magicCrit = magicCrit || damage.critical();
                }
                case TRUE -> {
                    trueDamage += damage.amount;
                    trueCrit = trueCrit || damage.critical();
                }
            }
        }

        return Arrays.asList(
                new Damage(DamageType.PHYSICAL, physicalDamage, dealer, physicalCrit),
                new Damage(DamageType.MAGIC, magicDamage, dealer, magicCrit),
                new Damage(DamageType.TRUE, trueDamage, dealer, trueCrit)
        );
    }

    public static @NotNull String mergedDamageToString(@NotNull List<Damage> damages) {
        StringBuilder hitMsg = new StringBuilder();
        for (Damage damage : damages) {
            if (damage.amount() <= 0) continue;
            hitMsg.append(damage)
                    .append(",");
        }
        hitMsg.deleteCharAt(hitMsg.length() - 1);
        return hitMsg.toString();
    }
}
