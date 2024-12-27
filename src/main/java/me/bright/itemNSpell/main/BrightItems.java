package me.bright.itemNSpell.main;

import me.bright.brightrpg.BrightStats;
import me.bright.itemNSpell.items.exWeapons.itzMeBright.hyperion.Hyperion;
import me.bright.itemNSpell.items.exWeapons.itzMeBright.infinityGauntlet.InfinityGauntlet;
import me.bright.itemNSpell.items.fireball.FireballWand;
import me.bright.itemNSpell.items.firebolt.FireboltWand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BrightItems {

    public static final FireboltWand FIREBOLT_WAND = new FireboltWand();
    public static final FireballWand FIREBALL_WAND = new FireballWand();
    public static final InfinityGauntlet INFINITY_GAUNTLET = new InfinityGauntlet();
    public static final Hyperion HYPERION = new Hyperion();

    public static BrightItem[] items = {
            FIREBOLT_WAND,
            FIREBALL_WAND,
            INFINITY_GAUNTLET,
            HYPERION
    };

    public static final Map<String, BrightItem> keyItemEntries;

    static {
        Map<String, BrightItem> tempMap = new HashMap<>();
        for (BrightItem item : items) {
            tempMap.put(item.getKey(), item);
        }
        keyItemEntries = Collections.unmodifiableMap(tempMap); // Make map immutable
    }


    public static @Nullable BrightItem getCustomItem(String key) {
        return keyItemEntries.get(key);
    }

    @Contract("null -> null")
    public static BrightItem getVanillaItem(ItemStack itemStack) {
        if (itemStack == null) return null;

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return null;

        String displayName = itemMeta.getDisplayName();
        BrightItem finalItem = new GenericBrightItem(BrightItem.DEFAULT_KEY, itemStack.getType(), displayName);
        switch (itemStack.getType()) {
            case DIAMOND_ORE, DIAMOND, DIAMOND_BLOCK, DIAMOND_HOE, DIAMOND_PICKAXE, DIAMOND_SHOVEL,
                 DIAMOND_HORSE_ARMOR, NETHERITE_SCRAP -> finalItem.setRarity(BrightItem.Rarity.UNCOMMON);
            case NETHERITE_INGOT, NETHERITE_BLOCK, NETHERITE_HOE, NETHERITE_PICKAXE, NETHERITE_SHOVEL,
                 NETHERITE_UPGRADE_SMITHING_TEMPLATE -> finalItem.setRarity(BrightItem.Rarity.RARE);

            case LEATHER_HELMET, LEATHER_BOOTS, GOLDEN_BOOTS -> finalItem.setStatFlatMod(BrightStats.ARMOR, 5);
            case LEATHER_CHESTPLATE, GOLDEN_LEGGINGS -> finalItem.setStatFlatMod(BrightStats.ARMOR, 15);
            case LEATHER_LEGGINGS, GOLDEN_HELMET, IRON_BOOTS -> finalItem.setStatFlatMod(BrightStats.ARMOR, 10);
            case GOLDEN_CHESTPLATE, IRON_LEGGINGS -> finalItem.setStatFlatMod(BrightStats.ARMOR, 25);
            case CHAINMAIL_HELMET -> {
                finalItem.setStatFlatMod(BrightStats.ARMOR, 12);
                finalItem.setRarity(BrightItem.Rarity.UNCOMMON);
            }
            case CHAINMAIL_CHESTPLATE -> {
                finalItem.setStatFlatMod(BrightStats.ARMOR, 30);
                finalItem.setRarity(BrightItem.Rarity.UNCOMMON);
            }
            case CHAINMAIL_LEGGINGS -> {
                finalItem.setStatFlatMod(BrightStats.ARMOR, 20);
                finalItem.setRarity(BrightItem.Rarity.UNCOMMON);
            }
            case CHAINMAIL_BOOTS -> {
                finalItem.setStatFlatMod(BrightStats.ARMOR, 7);
                finalItem.setRarity(BrightItem.Rarity.UNCOMMON);
            }
            case IRON_HELMET -> finalItem.setStatFlatMod(BrightStats.ARMOR, 12);
            case IRON_CHESTPLATE -> finalItem.setStatFlatMod(BrightStats.ARMOR, 30);
            case DIAMOND_HELMET, DIAMOND_BOOTS -> {
                finalItem.setStatFlatMod(BrightStats.ARMOR, 15);
                finalItem.setStatFlatMod(BrightStats.MAGIC_RESIST, 15);
                finalItem.setRarity(BrightItem.Rarity.UNCOMMON);
            }
            case DIAMOND_CHESTPLATE -> {
                finalItem.setStatFlatMod(BrightStats.ARMOR, 40);
                finalItem.setStatFlatMod(BrightStats.MAGIC_RESIST, 40);
                finalItem.setRarity(BrightItem.Rarity.UNCOMMON);
            }
            case DIAMOND_LEGGINGS -> {
                finalItem.setStatFlatMod(BrightStats.ARMOR, 20);
                finalItem.setStatFlatMod(BrightStats.MAGIC_RESIST, 20);
                finalItem.setRarity(BrightItem.Rarity.UNCOMMON);
            }
            case NETHERITE_HELMET, NETHERITE_BOOTS -> {
                finalItem.setStatFlatMod(BrightStats.ARMOR, 25);
                finalItem.setStatFlatMod(BrightStats.MAGIC_RESIST, 25);
                finalItem.setRarity(BrightItem.Rarity.RARE);
            }
            case NETHERITE_CHESTPLATE -> {
                finalItem.setStatFlatMod(BrightStats.ARMOR, 50);
                finalItem.setStatFlatMod(BrightStats.MAGIC_RESIST, 50);
                finalItem.setRarity(BrightItem.Rarity.RARE);
            }
            case NETHERITE_LEGGINGS -> {
                finalItem.setStatFlatMod(BrightStats.ARMOR, 30);
                finalItem.setStatFlatMod(BrightStats.MAGIC_RESIST, 30);
                finalItem.setRarity(BrightItem.Rarity.RARE);
            }
            case WOODEN_SWORD, GOLDEN_SWORD -> finalItem.setBaseDamage(20);
            case STONE_SWORD, WOODEN_AXE, GOLDEN_AXE -> finalItem.setBaseDamage(25);
            case IRON_SWORD, STONE_AXE -> finalItem.setBaseDamage(30);
            case IRON_AXE -> finalItem.setBaseDamage(35);
            case DIAMOND_SWORD -> {
                finalItem.setBaseDamage(35);
                finalItem.setRarity(BrightItem.Rarity.UNCOMMON);
            }
            case DIAMOND_AXE -> {
                finalItem.setBaseDamage(40);
                finalItem.setRarity(BrightItem.Rarity.UNCOMMON);
            }
            case NETHERITE_SWORD -> {
                finalItem.setBaseDamage(40);
                finalItem.setRarity(BrightItem.Rarity.RARE);
            }
            case NETHERITE_AXE -> {
                finalItem.setBaseDamage(45);
                finalItem.setRarity(BrightItem.Rarity.RARE);
            }
        }
        return finalItem;

    }
}
