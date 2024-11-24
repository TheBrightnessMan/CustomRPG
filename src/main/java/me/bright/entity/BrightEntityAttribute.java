package me.bright.entity;

public enum BrightEntityAttribute {
    MAX_HP("BASE_HP", "Max Health", 100),
    CURRENT_HP("CURRENT_HP", "Current Health", 100),
    STRENGTH("STRENGTH", "Strength", 100),
    INT("INT", "Intelligence", 100),
    CURRENT_MANA("CURRENT_MANA", "Current Mana", 100),
    ARMOR("ARMOR", "Armor", 100),
    MAGIC_RESIST("MAGIC_RESIST", "Magic Resistance", 100),
    CRIT_CHANCE("CRIT_CHANCE", "Critical Strike Chance", 5),
    CRIT_DMG("CRIT_DMG", "Critical Strike Damage", 75),
    SPELL_DMG("SPELL_DMG", "Spell Damage", 100),
    SPEED("SPEED", "Movement Speed", 0),
    LEVEL("LEVEL", "Level", 1),
    DMG_REDUCTION("DMG_REDUCTION", "Damage Reduction", 0);

    public final String key;
    public final String displayName;
    public final long defaultValue;

    BrightEntityAttribute(String key, String displayName, long defaultValue) {
        this.key = key;
        this.displayName = displayName;
        this.defaultValue = defaultValue;
    }
}
