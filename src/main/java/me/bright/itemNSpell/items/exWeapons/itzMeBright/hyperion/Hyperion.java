package me.bright.itemNSpell.items.exWeapons.itzMeBright.hyperion;

import me.bright.brightrpg.BrightStat;
import me.bright.enchant.BrightEnchants;
import me.bright.itemNSpell.main.BrightItem;
import me.bright.itemNSpell.main.BrightSpells;
import org.bukkit.Material;

public class Hyperion extends BrightItem {

    public Hyperion() {
        super("HYPERION", Material.NETHERITE_SWORD, "Hyperion");
        setRarity(Rarity.MYTHIC);
        setStatFlatMod(BrightStat.DAMAGE, 300);
        setStatFlatMod(BrightStat.STRENGTH, 150);
        setStatFlatMod(BrightStat.INTELLIGENCE, 500);
        addEnchant(BrightEnchants.SHARPNESS, 7);
        setSpell(BrightSpells.WITHER_IMPACT);
    }
}
