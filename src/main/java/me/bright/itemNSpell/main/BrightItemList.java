package me.bright.itemNSpell.main;

import me.bright.itemNSpell.items.exWeapons.itzMeBright.ZeusSceptre;
import me.bright.itemNSpell.items.fireball.FireballWand;
import me.bright.itemNSpell.items.firebolt.FireboltWand;
import org.jetbrains.annotations.Nullable;

public class BrightItemList {

    public static FireboltWand FIREBOLT_WAND = new FireboltWand(0);
    public static FireballWand FIREBALL_WAND = new FireballWand(1);
    public static ZeusSceptre ZEUS_SCEPTRE = new ZeusSceptre(2);

    public static BrightItem[] values() {
        return new BrightItem[] {
                FIREBOLT_WAND,
                FIREBALL_WAND,
                ZEUS_SCEPTRE
        };
    }

    public static @Nullable BrightItem getItem(long itemId) {
        for (BrightItem item : values()) {
            if (item.getId() == itemId) return item;
        }
        return null;
    }
}
