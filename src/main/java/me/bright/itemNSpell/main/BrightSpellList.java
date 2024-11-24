package me.bright.itemNSpell.main;

import me.bright.itemNSpell.items.exWeapons.itzMeBright.ZeusLightning;
import me.bright.itemNSpell.items.fireball.FireballSpell;
import me.bright.itemNSpell.items.firebolt.FireboltSpell;
import org.jetbrains.annotations.Nullable;

public class BrightSpellList {

    public static FireboltSpell FIREBOLT = new FireboltSpell();
    public static FireballSpell FIREBALL = new FireballSpell();
    public static ZeusLightning ZEUS_LIGHTNING = new ZeusLightning();

    public static BrightSpell[] values() {
        return new BrightSpell[]{
                FIREBOLT,
                FIREBALL,
                ZEUS_LIGHTNING
        };
    }

    public static @Nullable BrightSpell getSpell(String key) {
        for (BrightSpell spell : values()) {
            if (spell.getKey().equals(key)) return spell;
        }
        return null;
    }
}
