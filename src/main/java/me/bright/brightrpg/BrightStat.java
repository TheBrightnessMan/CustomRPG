package me.bright.brightrpg;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum BrightStat {
    DAMAGE("DAMAGE", "ඞ", "Damage", null,
            5, ChatColor.DARK_RED),
    STRENGTH("STRENGTH", "⚔", "Strength", Material.NETHERITE_AXE,
            100, ChatColor.RED),
    CRIT_CHANCE("CRIT_CHANCE", "☣", "Critical Hit Chance", Material.WITHER_SKELETON_SKULL,
            5, ChatColor.BLUE),
    CRIT_DMG("CRIT_DMG", "☠", "Critical Hit Damage", Material.WITHER_SKELETON_SKULL,
            125, ChatColor.BLUE),

    INTELLIGENCE("INT", "✎", "Intelligence", Material.BOOK,
            100, ChatColor.AQUA),
    SPELL_DMG("SPELL_DMG", "꩜", "Spell Damage", Material.BOOKSHELF,
            100, ChatColor.AQUA),
    CURRENT_MANA("CURRENT_MANA", "ඞ", "Current Mana", null,
            100, ChatColor.AQUA),
    MANA_REGEN("MANA_REGEN", "ඞ", "Mana Regeneration", null,
            100, ChatColor.DARK_AQUA),

    MAX_HP("MAX_HP", "♥", "Max Health", Material.ENCHANTED_GOLDEN_APPLE,
            100, ChatColor.DARK_RED),
    CURRENT_HP("CURRENT_HP", "ඞ", "Current Health", null,
            100, ChatColor.RED),
    TEMP_HP("TEMP_HP", "ඞ", "Absorption Shield", null,
            0, ChatColor.GOLD),
    VITALITY("VITALITY", "☀", "Vitality", Material.GLISTERING_MELON_SLICE,
            100, ChatColor.RED),

    ARMOR("ARMOR", "⛨", "Armor", Material.GOLDEN_CHESTPLATE,
            100, ChatColor.GOLD),
    MAGIC_RESIST("MAGIC_RESIST", "☉", "Magic Resistance", Material.DIAMOND_CHESTPLATE,
            100, ChatColor.BLUE),
    DAMAGE_TAKEN("DAMAGE_TAKEN", "ඞ", "Damage Taken (%)", null,
            100, ChatColor.GREEN),

    FLAT_ARMOR_PEN("FLAT_ARMOR_PEN", "ඞ", "Armor Penetration (Flat)", null,
            0, ChatColor.GOLD),
    PERCENT_ARMOR_PEN("PERCENT_ARMOR_PEN", "ඞ", "Armor Penetration (%)", null,
            0, ChatColor.GOLD),

    FLAT_MAGIC_PEN("FLAT_MAGIC_PEN", "ඞ", "Magical Penetration (Flat)", null,
            0, ChatColor.BLUE),
    PERCENT_MAGIC_PEN("PERCENT_MAGIC_PEN", "ඞ", "Magical Penetration (%)", null,
            0, ChatColor.BLUE),

    SPEED("SPEED", "✦", "Movement Speed", Material.SUGAR,
            100, ChatColor.WHITE),

    LEVEL("LEVEL", "ඞ", "Level", null,
            1, ChatColor.WHITE);

    public final String key, displayIcon, displayName;
    public final Material displayItem;
    public final double entityBaseValue;
    public final ChatColor color;

    BrightStat(String key, String displayIcon, String displayName, Material displayItem, double entityBaseValue, ChatColor color) {
        this.key = key;
        this.displayIcon = displayIcon;
        this.displayName = displayName;
        this.displayItem = displayItem;
        this.entityBaseValue = entityBaseValue;
        this.color = color;
    }
}
