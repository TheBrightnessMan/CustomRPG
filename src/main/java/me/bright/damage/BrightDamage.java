package me.bright.damage;

import me.bright.brightrpg.BrightRPG;
import me.bright.brightrpg.BrightStat;
import me.bright.entity.BrightEntity;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public record BrightDamage(@NotNull DamageType type, double amount, @Nullable BrightEntity dealer, boolean critical) {

    private static final long BALANCE_FACTOR = 100;
    public static final BrightDamage[] emptyFinalised = new BrightDamage[]{
            new BrightDamage(DamageType.PHYSICAL, 0, null, false),
            new BrightDamage(DamageType.MAGIC, 0, null, false),
            new BrightDamage(DamageType.TRUE, 0, null, false)
    };

    @Override
    public String toString() {
        return ChatColor.RED + String.valueOf(amount) + type.color + type.displayName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BrightDamage(
                DamageType otherType,
                double otherAmount,
                BrightEntity otherDealer,
                boolean otherCritical
        ))) return false;
        return this.type() == otherType &&
                this.amount() == otherAmount &&
                this.dealer() == otherDealer &&
                this.critical() == otherCritical;
    }

    public static boolean rollCrit(double critChance) {
        if (critChance <= 0) return false;
        if (critChance >= 100) return true;
        return new Random().nextInt(1, 101) <= critChance;
    }

    public static BrightDamage calculateFlatDamage(BrightDamage brightDamage, BrightEntity target) {
        double currentHp = target.getCurrentHp(),
                maxHp = target.getStatFromCache(BrightStat.MAX_HP),
                missingHp = maxHp - currentHp,
                actualDamage = 0;
        switch (brightDamage.type) {
            case PHYSICAL, MAGIC, TRUE -> actualDamage = brightDamage.amount();
            case CURRENT_HP_PHYSICAL, CURRENT_HP_MAGIC, CURRENT_HP_TRUE ->
                    actualDamage = brightDamage.amount() / 100 * currentHp;
            case MISSING_HP_PHYSICAL, MISSING_HP_MAGIC, MISSING_HP_TRUE ->
                    actualDamage = brightDamage.amount() / 100 * missingHp;
            case MAX_HP_PHYSICAL, MAX_HP_MAGIC, MAX_HP_TRUE -> actualDamage = brightDamage.amount() / 100 * maxHp;
        }
        return new BrightDamage(DamageType.compress(brightDamage.type), actualDamage, brightDamage.dealer, brightDamage.critical);
    }

    public static BrightDamage calculateResistedDamage(BrightDamage flatDamage, BrightEntity target) {
        if (flatDamage.dealer == null) return flatDamage;
        double flatPen = 0,
                percentPen = 0,
                resistance = 0;
        switch (DamageType.compress(flatDamage.type)) {
            case PHYSICAL -> {
                resistance = target.getStatFromCache(BrightStat.ARMOR);
                flatPen = Math.max(flatDamage.dealer.getStatFromCache(BrightStat.FLAT_ARMOR_PEN), 0);
                percentPen = Math.max(flatDamage.dealer.getStatFromCache(BrightStat.PERCENT_ARMOR_PEN), 0);
            }
            case MAGIC -> {
                resistance = target.getStatFromCache(BrightStat.MAGIC_RESIST);
                flatPen = Math.max(flatDamage.dealer.getStatFromCache(BrightStat.FLAT_MAGIC_PEN), 0);
                percentPen = Math.max(flatDamage.dealer.getStatFromCache(BrightStat.PERCENT_MAGIC_PEN), 0);
            }
        }
        double actualResistance = resistance * (1 - percentPen / 100) - flatPen;
        if (actualResistance < 0 && flatDamage.dealer.getLivingEntity() instanceof Player player) {
            LivingEntity livingEntity = target.getLivingEntity();
            player.playSound(livingEntity, Sound.BLOCK_GLASS_BREAK, 1.0F, 0.8F);
            livingEntity.getWorld().spawnParticle(Particle.BLOCK,
                    livingEntity.getLocation(), 5, 1, 0.1, 0.1, 0.1,
                    Material.GLASS.createBlockData());
        }
        double reductionFromResistance = actualResistance / (actualResistance + BALANCE_FACTOR);
        return new BrightDamage(flatDamage.type,
                flatDamage.amount * (1 - reductionFromResistance),
                flatDamage.dealer,
                flatDamage.critical);
    }

    public static @NotNull BrightDamage[] mergeDamages(@NotNull List<BrightDamage> brightDamages) {
        if (brightDamages.isEmpty())
            return new BrightDamage[]{
                    new BrightDamage(DamageType.PHYSICAL, 0, null, false),
                    new BrightDamage(DamageType.MAGIC, 0, null, false),
                    new BrightDamage(DamageType.TRUE, 0, null, false)
            };
        BrightEntity dealer = brightDamages.getFirst().dealer();
        double physicalDamage = 0L,
                magicDamage = 0L,
                trueDamage = 0L;
        boolean physicalCrit = false,
                magicCrit = false,
                trueCrit = false;
        for (BrightDamage brightDamage : brightDamages) {
            switch (brightDamage.type()) {
                case PHYSICAL -> {
                    physicalDamage += brightDamage.amount;
                    physicalCrit = physicalCrit || brightDamage.critical();
                }
                case MAGIC -> {
                    magicDamage += brightDamage.amount;
                    magicCrit = magicCrit || brightDamage.critical();
                }
                case TRUE -> {
                    trueDamage += brightDamage.amount;
                    trueCrit = trueCrit || brightDamage.critical();
                }
            }
        }

        return new BrightDamage[]{
                new BrightDamage(DamageType.PHYSICAL, physicalDamage, dealer, physicalCrit),
                new BrightDamage(DamageType.MAGIC, magicDamage, dealer, magicCrit),
                new BrightDamage(DamageType.TRUE, trueDamage, dealer, trueCrit)
        };
    }

    public static @NotNull String mergedDamageToString(@NotNull BrightDamage[] brightDamages) {
        StringBuilder hitMsg = new StringBuilder();
        for (BrightDamage brightDamage : brightDamages) {
            if (brightDamage.amount() <= 0) continue;
            hitMsg.append(brightDamage)
                    .append(",");
        }
        hitMsg.deleteCharAt(hitMsg.length() - 1);
        return hitMsg.toString();
    }

    public static void showDamages(@NotNull LivingEntity target,
                                   @NotNull BrightDamage[] brightDamages) {
        for (BrightDamage brightDamage : brightDamages) {
            if (brightDamage.amount() > 0) showDamage(target, brightDamage);
        }
    }

    public static void showDamage(@NotNull LivingEntity target,
                                  @NotNull BrightDamage brightDamage) {
        World world = target.getWorld();
        long damage = (long) brightDamage.amount();
        final String displayDamage = (brightDamage.critical()) ?
                "" + brightDamage.type().color + ChatColor.BOLD + "!  " + damage + "  !" :
                "" + brightDamage.type().color + damage;
        Location location = target.getLocation().clone()
                .add(new Random().nextDouble() * 2 - 1,
                        1,
                        new Random().nextDouble() * 2 - 1);
        world.spawn(location, ArmorStand.class, CreatureSpawnEvent.SpawnReason.CUSTOM, false,
                dmgIndicator -> {
                    dmgIndicator.setMarker(true);
                    dmgIndicator.setVisible(false);
                    dmgIndicator.setGravity(false);
                    dmgIndicator.setSmall(true);
                    dmgIndicator.setCustomNameVisible(true);
                    dmgIndicator.setCustomName(displayDamage);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            dmgIndicator.remove();
                        }
                    }.runTaskLater(BrightRPG.getPlugin(), 3 * 20L);
                });
    }
}
