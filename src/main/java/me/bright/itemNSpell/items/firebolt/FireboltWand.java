package me.bright.itemNSpell.items.firebolt;

import me.bright.itemNSpell.main.BrightItem;
import me.bright.itemNSpell.main.BrightSpellList;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class FireboltWand extends BrightItem {

    public FireboltWand(long id) {
        super(id, Material.STICK, ChatColor.GOLD + "Firebolt Wand");
        setSpell(BrightSpellList.FIREBOLT);
    }
}
