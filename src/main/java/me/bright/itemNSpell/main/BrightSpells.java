package me.bright.itemNSpell.main;

import me.bright.itemNSpell.items.exWeapons.itzMeBright.hyperion.WitherImpact;
import me.bright.itemNSpell.items.exWeapons.itzMeBright.infinityGauntlet.MassDisintegration;
import me.bright.itemNSpell.items.fireball.FireballSpell;
import me.bright.itemNSpell.items.firebolt.FireboltSpell;
import org.jetbrains.annotations.Nullable;

public class BrightSpells {

    public static FireboltSpell FIREBOLT = new FireboltSpell();
    public static FireballSpell FIREBALL = new FireballSpell();
    public static MassDisintegration MASS_DISINTEGRATION = new MassDisintegration();
    public static WitherImpact WITHER_IMPACT = new WitherImpact();

    public static BrightSpell[] values() {
        return new BrightSpell[]{
                FIREBOLT,
                FIREBALL,
                MASS_DISINTEGRATION,
                WITHER_IMPACT
        };
    }

    public static @Nullable BrightSpell getSpell(String key) {
        for (BrightSpell spell : values()) {
            if (spell.getKey().equals(key)) return spell;
        }
        return null;
    }
}
