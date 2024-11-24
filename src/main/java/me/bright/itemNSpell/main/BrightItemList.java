package me.bright.itemNSpell.main;

import me.bright.itemNSpell.items.exWeapons.itzMeBright.ZeusSceptre;
import me.bright.itemNSpell.items.fireball.FireballWand;
import me.bright.itemNSpell.items.firebolt.FireboltWand;
import org.jetbrains.annotations.Nullable;

public class BrightItemList {

    public static FireboltWand FIREBOLT_WAND = new FireboltWand();
    public static FireballWand FIREBALL_WAND = new FireballWand();
    public static ZeusSceptre ZEUS_SCEPTRE = new ZeusSceptre();

    public static BrightItem[] values() {
        return new BrightItem[] {
                FIREBOLT_WAND,
                FIREBALL_WAND,
                ZEUS_SCEPTRE
        };
    }

    public static @Nullable BrightItem getItem(String key) {
        for (BrightItem item : values()) {
            if (item.getKey().equals(key)) return item;
        }
        return null;
    }
}
