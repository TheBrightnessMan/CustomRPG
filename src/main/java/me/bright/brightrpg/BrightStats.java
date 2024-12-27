package me.bright.brightrpg;

import org.bukkit.ChatColor;

public enum BrightStats {
    MAX_HP("MAX_HP", "Max Health", 100, 0, ChatColor.DARK_RED),
    CURRENT_HP("CURRENT_HP", "Current Health", 100, 0, ChatColor.RED),
    TEMP_HP("TEMP_HP", "Absorption Shield", 0, 0, ChatColor.GOLD),
    VITALITY("VITALITY", "Vitality", 100, 0, ChatColor.RED),

    STRENGTH("STRENGTH", "Strength", 100, 0, ChatColor.RED),
    CRIT_CHANCE("CRIT_CHANCE", "Critical Hit Chance", 5, 0, ChatColor.BLUE),
    CRIT_DMG("CRIT_DMG", "Critical Hit Damage", 25, 0, ChatColor.BLUE),

    INTELLIGENCE("INT", "Intelligence", 100, 0, ChatColor.AQUA),
    SPELL_DMG("SPELL_DMG", "Spell Damage", 100, 0, ChatColor.AQUA),
    CURRENT_MANA("CURRENT_MANA", "Current Mana", 100, 0, ChatColor.AQUA),
    MANA_REGEN("MANA_REGEN", "Mana Regeneration", 100, 0, ChatColor.DARK_AQUA),

    ARMOR("ARMOR", "Armor", 100, 0, ChatColor.GOLD),
    MAGIC_RESIST("MAGIC_RESIST", "Magic Resistance", 100, 0, ChatColor.BLUE),
    DAMAGE_TAKEN("DAMAGE_TAKEN", "Damage Taken (%)", 100, 0, ChatColor.GREEN),

    FLAT_ARMOR_PEN("FLAT_ARMOR_PEN", "Armor Penetration (Flat)", 0, 0, ChatColor.GOLD),
    PERCENT_ARMOR_PEN("PERCENT_ARMOR_PEN", "Armor Penetration (%)", 0, 0, ChatColor.GOLD),

    FLAT_MAGIC_PEN("FLAT_MAGIC_PEN", "Magical Penetration (Flat)", 0, 0, ChatColor.BLUE),
    PERCENT_MAGIC_PEN("PERCENT_MAGIC_PEN", "Magical Penetration (%)", 0, 0, ChatColor.BLUE),

    SPEED("SPEED", "Movement Speed", 100, 0, ChatColor.WHITE),

    LEVEL("LEVEL", "Level", 1, 0, ChatColor.WHITE);

    public final String key;
    public final String displayName;
    public final double entityBaseValue, otherBaseValue;
    public final ChatColor color;

    BrightStats(String key, String displayName, double entityBaseValue, double otherBaseValue, ChatColor color) {
        this.key = key;
        this.displayName = displayName;
        this.entityBaseValue = entityBaseValue;
        this.otherBaseValue = otherBaseValue;
        this.color = color;
    }
}
