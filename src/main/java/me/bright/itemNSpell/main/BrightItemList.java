package me.bright.itemNSpell.main;

import me.bright.damage.Damage;
import me.bright.damage.DamageType;
import me.bright.itemNSpell.items.exWeapons.itzMeBright.InfinityGauntlet;
import me.bright.itemNSpell.items.fireball.FireballWand;
import me.bright.itemNSpell.items.firebolt.FireboltWand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class BrightItemList {

    public static FireboltWand FIREBOLT_WAND = new FireboltWand();
    public static FireballWand FIREBALL_WAND = new FireballWand();
    public static InfinityGauntlet INFINITY_GAUNTLET = new InfinityGauntlet();

    public static BrightItem[] values() {
        return new BrightItem[]{
                FIREBOLT_WAND,
                FIREBALL_WAND,
                INFINITY_GAUNTLET
        };
    }

    public static @Nullable BrightItem getCustomItem(String key) {
        for (BrightItem item : values()) {
            if (item.getKey().equals(key)) return item;
        }
        return null;
    }

    @Contract("!null -> !null; null -> null")
    public static BrightItem getVanillaItem(ItemStack itemStack) {
        if (itemStack == null) return null;
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null)
            return new BrightItem(BrightItem.DEFAULT_KEY, itemStack.getType(), itemStack.getType().toString());
        String displayName = itemMeta.getDisplayName();
        BrightItem finalItem = new BrightItem(BrightItem.DEFAULT_KEY, itemStack.getType(), displayName);
        switch (itemStack.getType()) {
            case DIAMOND_ORE, DIAMOND, DIAMOND_BLOCK, DIAMOND_HOE, DIAMOND_PICKAXE, DIAMOND_SHOVEL,
                 DIAMOND_HORSE_ARMOR, NETHERITE_SCRAP -> finalItem.setRarity(BrightItem.Rarity.UNCOMMON);
            case NETHERITE_INGOT, NETHERITE_BLOCK, NETHERITE_HOE, NETHERITE_PICKAXE, NETHERITE_SHOVEL,
                 NETHERITE_UPGRADE_SMITHING_TEMPLATE -> finalItem.setRarity(BrightItem.Rarity.RARE);

            case LEATHER_HELMET, LEATHER_BOOTS, GOLDEN_BOOTS -> finalItem.setAttribute(BrightItemAttribute.ARMOR, 5);
            case LEATHER_CHESTPLATE, GOLDEN_LEGGINGS -> finalItem.setAttribute(BrightItemAttribute.ARMOR, 15);
            case LEATHER_LEGGINGS, GOLDEN_HELMET, IRON_BOOTS -> finalItem.setAttribute(BrightItemAttribute.ARMOR, 10);
            case GOLDEN_CHESTPLATE, IRON_LEGGINGS -> finalItem.setAttribute(BrightItemAttribute.ARMOR, 25);
            case CHAINMAIL_HELMET -> {
                finalItem.setAttribute(BrightItemAttribute.ARMOR, 12);
                finalItem.setRarity(BrightItem.Rarity.UNCOMMON);
            }
            case CHAINMAIL_CHESTPLATE -> {
                finalItem.setAttribute(BrightItemAttribute.ARMOR, 30);
                finalItem.setRarity(BrightItem.Rarity.UNCOMMON);
            }
            case CHAINMAIL_LEGGINGS -> {
                finalItem.setAttribute(BrightItemAttribute.ARMOR, 20);
                finalItem.setRarity(BrightItem.Rarity.UNCOMMON);
            }
            case CHAINMAIL_BOOTS -> {
                finalItem.setAttribute(BrightItemAttribute.ARMOR, 7);
                finalItem.setRarity(BrightItem.Rarity.UNCOMMON);
            }
            case IRON_HELMET -> finalItem.setAttribute(BrightItemAttribute.ARMOR, 12);
            case IRON_CHESTPLATE -> finalItem.setAttribute(BrightItemAttribute.ARMOR, 30);
            case DIAMOND_HELMET, DIAMOND_BOOTS -> {
                finalItem.setAttribute(BrightItemAttribute.ARMOR, 15);
                finalItem.setAttribute(BrightItemAttribute.MAGIC_RESIST, 15);
                finalItem.setRarity(BrightItem.Rarity.UNCOMMON);
            }
            case DIAMOND_CHESTPLATE -> {
                finalItem.setAttribute(BrightItemAttribute.ARMOR, 40);
                finalItem.setAttribute(BrightItemAttribute.MAGIC_RESIST, 40);
                finalItem.setRarity(BrightItem.Rarity.UNCOMMON);
            }
            case DIAMOND_LEGGINGS -> {
                finalItem.setAttribute(BrightItemAttribute.ARMOR, 20);
                finalItem.setAttribute(BrightItemAttribute.MAGIC_RESIST, 20);
                finalItem.setRarity(BrightItem.Rarity.UNCOMMON);
            }
            case NETHERITE_HELMET, NETHERITE_BOOTS -> {
                finalItem.setAttribute(BrightItemAttribute.ARMOR, 25);
                finalItem.setAttribute(BrightItemAttribute.MAGIC_RESIST, 25);
                finalItem.setRarity(BrightItem.Rarity.RARE);
            }
            case NETHERITE_CHESTPLATE -> {
                finalItem.setAttribute(BrightItemAttribute.ARMOR, 50);
                finalItem.setAttribute(BrightItemAttribute.MAGIC_RESIST, 50);
                finalItem.setRarity(BrightItem.Rarity.RARE);
            }
            case NETHERITE_LEGGINGS -> {
                finalItem.setAttribute(BrightItemAttribute.ARMOR, 30);
                finalItem.setAttribute(BrightItemAttribute.MAGIC_RESIST, 30);
                finalItem.setRarity(BrightItem.Rarity.RARE);
            }
            case WOODEN_SWORD, GOLDEN_SWORD ->
                    finalItem.setBaseDamage(new Damage(DamageType.PHYSICAL, 20, null, false));
            case STONE_SWORD, WOODEN_AXE, GOLDEN_AXE ->
                    finalItem.setBaseDamage(new Damage(DamageType.PHYSICAL, 25, null, false));
            case IRON_SWORD, STONE_AXE -> finalItem.setBaseDamage(new Damage(DamageType.PHYSICAL, 30, null, false));
            case IRON_AXE -> finalItem.setBaseDamage(new Damage(DamageType.PHYSICAL, 35, null, false));
            case DIAMOND_SWORD -> {
                finalItem.setBaseDamage(new Damage(DamageType.PHYSICAL, 35, null, false));
                finalItem.setRarity(BrightItem.Rarity.UNCOMMON);
            }
            case DIAMOND_AXE -> {
                finalItem.setBaseDamage(new Damage(DamageType.PHYSICAL, 40, null, false));
                finalItem.setRarity(BrightItem.Rarity.UNCOMMON);
            }
            case NETHERITE_SWORD -> {
                finalItem.setBaseDamage(new Damage(DamageType.PHYSICAL, 40, null, false));
                finalItem.setRarity(BrightItem.Rarity.RARE);
            }
            case NETHERITE_AXE -> {
                finalItem.setBaseDamage(new Damage(DamageType.PHYSICAL, 45, null, false));
                finalItem.setRarity(BrightItem.Rarity.RARE);
            }
        }
        return finalItem;

    }
}
