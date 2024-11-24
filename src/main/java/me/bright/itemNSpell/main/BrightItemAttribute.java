package me.bright.itemNSpell.main;

public enum BrightItemAttribute {
    MAX_HP("MAX_HP", "Max Health", 0),
    ARMOR("ARMOR", "Armor", 0),
    MAGIC_RESIST("MAGIC_RESIST", "Magic Resistance", 0),
    DMG_REDUCTION("DMG_REDUCTION", "Bonus Damage Reduction (%)", 0),

    STRENGTH("STRENGTH", "Strength", 0),
    CRIT_CHANCE("CRIT_CHANCE", "Critical Strike Chance (%)", 0),
    CRIT_DMG("CRIT_DMG", "Critical Strike Damage (%)", 0),
    FLAT_ARMOR_PEN("FLAT_ARMOR_PEN", "Armor Penetration (Flat)", 0),
    PERCENT_ARMOR_PEN("PERCENT_ARMOR_PEN", "Armor Penetration (%)", 0),

    INT("INT", "Intelligence", 0),
    SPELL_DMG("SPELL_DMG", "Bonus Spell Damage (%)", 0),
    FLAT_MAGIC_PEN("FLAT_MAGIC_PEN", "Magical Penetration (Flat)", 0),
    PERCENT_MAGIC_PEN("PERCENT_MAGIC_PEN", "Magical Penetration (%)", 0);

    public final String key;
    public final String displayName;
    public final long defaultValue;


    BrightItemAttribute(String key, String displayName, long defaultValue) {
        this.key = key;
        this.displayName = displayName;
        this.defaultValue = defaultValue;
    }
}
