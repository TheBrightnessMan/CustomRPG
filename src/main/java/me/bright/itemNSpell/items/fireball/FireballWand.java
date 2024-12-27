package me.bright.itemNSpell.items.fireball;

import me.bright.brightrpg.BrightStats;
import me.bright.itemNSpell.main.BrightItem;
import me.bright.itemNSpell.main.BrightSpells;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class FireballWand extends BrightItem {

    public FireballWand() {
        super("FIREBALL_WAND", Material.BLAZE_ROD, ChatColor.GOLD + "Fireball Wand");
        setRarity(Rarity.UNCOMMON);
        setSpell(BrightSpells.FIREBALL);
        setStatFlatMod(BrightStats.INTELLIGENCE, 50);
    }
}
