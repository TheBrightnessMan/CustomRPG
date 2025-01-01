package me.bright.enchant.enchants;

import me.bright.brightrpg.BrightStat;
import me.bright.brightrpg.BrightStatModifier;
import me.bright.enchant.BrightEnchant;

public class Sharpness extends BrightEnchant {

    public Sharpness() {
        super("SHARPNESS", "Sharpness");
    }

    @Override
    public BrightStatModifier getModifier(int level) {
        BrightStatModifier result = new BrightStatModifier(false);
        if (level <= 0 || level > 7) {
            return result;
        }
        result.setStatAddMod(BrightStat.DAMAGE, level * 5);
        return result;
    }

}
