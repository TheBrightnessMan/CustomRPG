package me.bright.itemNSpell.items.exWeapons.itzMeBright;

import me.bright.itemNSpell.main.BrightItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class InfinityGauntlet extends BrightItem {

    public InfinityGauntlet() {
        super("INFINITY_GAUNTLET", Material.EMERALD,
                "" + ChatColor.GOLD + ChatColor.BOLD + "Infinity Gauntlet");
        setRarity(Rarity.DIVINE);
    }
}
