package me.bright.itemNSpell.main;

import me.bright.itemNSpell.items.exWeapons.itzMeBright.ZeusLightning;
import me.bright.itemNSpell.items.fireball.FireballSpell;
import me.bright.itemNSpell.items.firebolt.FireboltSpell;
import org.jetbrains.annotations.Nullable;

public class BrightSpellList {

    public static FireboltSpell FIREBOLT = new FireboltSpell(0);
    public static FireballSpell FIREBALL = new FireballSpell(1);
    public static ZeusLightning ZEUS_LIGHTNING = new ZeusLightning(2);

    public static BrightSpell[] values() {
        return new BrightSpell[]{
                FIREBOLT,
                FIREBALL,
                ZEUS_LIGHTNING
        };
    }

    public static @Nullable BrightSpell getSpell(long spellId) {
        for (BrightSpell spell : values()) {
            if (spell.getId() == spellId) return spell;
        }
        return null;
    }
}
