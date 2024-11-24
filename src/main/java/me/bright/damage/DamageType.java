package me.bright.damage;

import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

public enum DamageType {
    PHYSICAL(" Physical", ChatColor.GOLD),
    MAGIC(" Magical", ChatColor.AQUA),
    TRUE(" True", ChatColor.WHITE),

    CURRENT_HP_PHYSICAL("% Current Health Physical", ChatColor.GOLD),
    CURRENT_HP_MAGIC("% Current Health Magical", ChatColor.AQUA),
    CURRENT_HP_TRUE("% Current Health True", ChatColor.WHITE),

    MISSING_HP_PHYSICAL("% Missing Health Physical", ChatColor.GOLD),
    MISSING_HP_MAGIC("% Missing Health Magical", ChatColor.AQUA),
    MISSING_HP_TRUE("% Missing Health True", ChatColor.WHITE),

    MAX_HP_PHYSICAL("% Max Health Physical", ChatColor.GOLD),
    MAX_HP_MAGIC("% Max Health Magical", ChatColor.AQUA),
    MAX_HP_TRUE("% Max Health True", ChatColor.WHITE);

    public final String displayName;
    public final ChatColor color;

    DamageType(String displayName, ChatColor color) {
        this.displayName = displayName;
        this.color = color;
    }


    public static @NotNull DamageType compress(DamageType type) {
        DamageType ret = TRUE;
        switch (type) {
            case PHYSICAL, CURRENT_HP_PHYSICAL, MISSING_HP_PHYSICAL, MAX_HP_PHYSICAL -> ret = PHYSICAL;
            case MAGIC, CURRENT_HP_MAGIC, MISSING_HP_MAGIC, MAX_HP_MAGIC -> ret = MAGIC;
        }
        return ret;
    }

}
