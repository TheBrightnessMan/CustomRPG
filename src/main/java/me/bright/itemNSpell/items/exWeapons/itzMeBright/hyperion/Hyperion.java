package me.bright.itemNSpell.items.exWeapons.itzMeBright.hyperion;

import me.bright.brightrpg.BrightStats;
import me.bright.itemNSpell.main.BrightItem;
import me.bright.itemNSpell.main.BrightSpells;
import org.bukkit.Material;

public class Hyperion extends BrightItem {

    public Hyperion() {
        super("HYPERION", Material.NETHERITE_SWORD, "Hyperion");
        setRarity(Rarity.MYTHIC);
        setBaseDamage(300);
        setStatFlatMod(BrightStats.STRENGTH, 150);
        setStatFlatMod(BrightStats.INTELLIGENCE, 500);
        setSpell(BrightSpells.WITHER_IMPACT);
    }
}
