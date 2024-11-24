package me.bright.itemNSpell.items.fireball;

import me.bright.itemNSpell.main.BrightItem;
import me.bright.itemNSpell.main.BrightItemAttribute;
import me.bright.itemNSpell.main.BrightSpellList;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class FireballWand extends BrightItem {

    public FireballWand(long id) {
        super(id, Material.BLAZE_ROD, ChatColor.GOLD + "Fireball Wand");
        setRarity(Rarity.UNCOMMON);
        setSpell(BrightSpellList.FIREBALL);
        setAttribute(BrightItemAttribute.INT, 50);
    }
}
