package me.bright.entity;

import me.bright.brightrpg.BrightStats;
import org.bukkit.entity.LivingEntity;

public class BrightEntityList {

    public static BrightEntity getCustomEntity(String key) {
        return null;
    }

    public static BrightEntity getVanillaEntity(LivingEntity livingEntity) {
        BrightEntity finalEntity = new BrightEntity(livingEntity);
        switch (livingEntity.getType()) {
            case BAT, CHICKEN, COD, FROG, GLOW_SQUID, PARROT, PUFFERFISH, RABBIT,
                 SALMON, TADPOLE, BEE -> {
                finalEntity.setStatToNbt(BrightStats.MAX_HP, 10);
                finalEntity.setStatToNbt(BrightStats.ARMOR, 0);
                finalEntity.setStatToNbt(BrightStats.MAGIC_RESIST, 0);
            }
            case ALLAY, AXOLOTL, CAT, OCELOT, PIG, SHEEP, SNOW_GOLEM, VILLAGER,
                 WANDERING_TRADER, CAVE_SPIDER, DOLPHIN, FOX, ENDERMITE, SILVERFISH -> {
                finalEntity.setStatToNbt(BrightStats.MAX_HP, 20);
                finalEntity.setStatToNbt(BrightStats.ARMOR, 0);
                finalEntity.setStatToNbt(BrightStats.MAGIC_RESIST, 0);
            }
            case COW, DONKEY, HORSE, MOOSHROOM, MULE, GOAT, WOLF, ZOMBIFIED_PIGLIN, VEX -> {
                finalEntity.setStatToNbt(BrightStats.MAX_HP, 50);
                finalEntity.setStatToNbt(BrightStats.ARMOR, 0);
                finalEntity.setStatToNbt(BrightStats.MAGIC_RESIST, 0);
            }
            case ARMADILLO, CAMEL, SKELETON_HORSE, SNIFFER, STRIDER, TURTLE, LLAMA,
                 PANDA, SPIDER, TRADER_LLAMA, PHANTOM -> {
                finalEntity.setStatToNbt(BrightStats.MAX_HP, 50);
                finalEntity.setStatToNbt(BrightStats.ARMOR, 20);
                finalEntity.setStatToNbt(BrightStats.MAGIC_RESIST, 10);
            }
            case DROWNED, BOGGED, CREEPER, HUSK, SKELETON, SLIME, STRAY, WITCH, ZOMBIE,
                 ZOMBIE_VILLAGER, ZOMBIE_HORSE -> {
                finalEntity.setStatToNbt(BrightStats.MAX_HP, 100);
                finalEntity.setStatToNbt(BrightStats.ARMOR, 0);
                finalEntity.setStatToNbt(BrightStats.MAGIC_RESIST, 0);
            }
            case BLAZE, BREEZE, EVOKER, GHAST, GUARDIAN, MAGMA_CUBE, PIGLIN_BRUTE,
                 PILLAGER, SHULKER, VINDICATOR, WITHER_SKELETON, ZOGLIN -> {
                finalEntity.setStatToNbt(BrightStats.MAX_HP, 100);
                finalEntity.setStatToNbt(BrightStats.ARMOR, 30);
                finalEntity.setStatToNbt(BrightStats.MAGIC_RESIST, 50);
            }
            case POLAR_BEAR, HOGLIN -> {
                finalEntity.setStatToNbt(BrightStats.MAX_HP, 100);
                finalEntity.setStatToNbt(BrightStats.ARMOR, 50);
                finalEntity.setStatToNbt(BrightStats.MAGIC_RESIST, 30);
            }
            case ENDERMAN, IRON_GOLEM, ELDER_GUARDIAN, RAVAGER -> {
                finalEntity.setStatToNbt(BrightStats.MAX_HP, 150);
                finalEntity.setStatToNbt(BrightStats.ARMOR, 100);
                finalEntity.setStatToNbt(BrightStats.MAGIC_RESIST, 100);
            }
            case WARDEN, WITHER, ENDER_DRAGON -> {
                finalEntity.setStatToNbt(BrightStats.MAX_HP, 3000);
                finalEntity.setStatToNbt(BrightStats.ARMOR, 500);
                finalEntity.setStatToNbt(BrightStats.MAGIC_RESIST, 500);
            }
        }
        return finalEntity;
    }

}
