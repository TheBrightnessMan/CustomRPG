package me.bright.itemNSpell.items.exWeapons.itzMeBright;

import me.bright.damage.ConditionalDamage;
import me.bright.damage.Damage;
import me.bright.damage.DamageType;
import me.bright.itemNSpell.main.BrightItem;
import me.bright.itemNSpell.main.BrightItemAttribute;
import me.bright.itemNSpell.main.BrightSpellList;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.Collections;

public class ZeusSceptre extends BrightItem {

    public ZeusSceptre() {
        super("ZEUS_SCEPTRE", Material.END_ROD, "" + ChatColor.RED + ChatColor.BOLD + "Zeus' Sceptre");
        setRarity(Rarity.DIVINE);
        setAttribute(BrightItemAttribute.DMG_REDUCTION, 100);
        setSpell(BrightSpellList.ZEUS_LIGHTNING);
    }
}
