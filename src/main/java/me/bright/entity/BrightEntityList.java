package me.bright.entity;

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
                finalEntity.setEntityAttribute(BrightEntityAttribute.MAX_HP, 10);
                finalEntity.setEntityAttribute(BrightEntityAttribute.ARMOR, 0);
                finalEntity.setEntityAttribute(BrightEntityAttribute.MAGIC_RESIST, 0);
            }
            case ALLAY, AXOLOTL, CAT, OCELOT, PIG, SHEEP, SNOW_GOLEM, VILLAGER,
                 WANDERING_TRADER, CAVE_SPIDER, DOLPHIN, FOX, ENDERMITE, SILVERFISH -> {
                finalEntity.setEntityAttribute(BrightEntityAttribute.MAX_HP, 20);
                finalEntity.setEntityAttribute(BrightEntityAttribute.ARMOR, 0);
                finalEntity.setEntityAttribute(BrightEntityAttribute.MAGIC_RESIST, 0);
            }
            case COW, DONKEY, HORSE, MOOSHROOM, MULE, GOAT, WOLF, ZOMBIFIED_PIGLIN, VEX -> {
                finalEntity.setEntityAttribute(BrightEntityAttribute.MAX_HP, 50);
                finalEntity.setEntityAttribute(BrightEntityAttribute.ARMOR, 0);
                finalEntity.setEntityAttribute(BrightEntityAttribute.MAGIC_RESIST, 0);
            }
            case ARMADILLO, CAMEL, SKELETON_HORSE, SNIFFER, STRIDER, TURTLE, LLAMA,
                 PANDA, SPIDER, TRADER_LLAMA, PHANTOM -> {
                finalEntity.setEntityAttribute(BrightEntityAttribute.MAX_HP, 50);
                finalEntity.setEntityAttribute(BrightEntityAttribute.ARMOR, 20);
                finalEntity.setEntityAttribute(BrightEntityAttribute.MAGIC_RESIST, 10);
            }
            case DROWNED, BOGGED, CREEPER, HUSK, SKELETON, SLIME, STRAY, WITCH, ZOMBIE,
                 ZOMBIE_VILLAGER, ZOMBIE_HORSE -> {
                finalEntity.setEntityAttribute(BrightEntityAttribute.MAX_HP, 100);
                finalEntity.setEntityAttribute(BrightEntityAttribute.ARMOR, 0);
                finalEntity.setEntityAttribute(BrightEntityAttribute.MAGIC_RESIST, 0);
            }
            case BLAZE, BREEZE, EVOKER, GHAST, GUARDIAN, MAGMA_CUBE, PIGLIN_BRUTE,
                 PILLAGER, SHULKER, VINDICATOR, WITHER_SKELETON, ZOGLIN -> {
                finalEntity.setEntityAttribute(BrightEntityAttribute.MAX_HP, 100);
                finalEntity.setEntityAttribute(BrightEntityAttribute.ARMOR, 30);
                finalEntity.setEntityAttribute(BrightEntityAttribute.MAGIC_RESIST, 50);
            }
            case POLAR_BEAR, HOGLIN -> {
                finalEntity.setEntityAttribute(BrightEntityAttribute.MAX_HP, 100);
                finalEntity.setEntityAttribute(BrightEntityAttribute.ARMOR, 50);
                finalEntity.setEntityAttribute(BrightEntityAttribute.MAGIC_RESIST, 30);
            }
            case ENDERMAN, IRON_GOLEM, ELDER_GUARDIAN, RAVAGER -> {
                finalEntity.setEntityAttribute(BrightEntityAttribute.MAX_HP, 150);
                finalEntity.setEntityAttribute(BrightEntityAttribute.ARMOR, 100);
                finalEntity.setEntityAttribute(BrightEntityAttribute.MAGIC_RESIST, 100);
            }
            case WARDEN, WITHER, ENDER_DRAGON -> {
                finalEntity.setEntityAttribute(BrightEntityAttribute.MAX_HP, 3000);
                finalEntity.setEntityAttribute(BrightEntityAttribute.ARMOR, 500);
                finalEntity.setEntityAttribute(BrightEntityAttribute.MAGIC_RESIST, 500);
            }
        }
        return finalEntity;
    }

}
