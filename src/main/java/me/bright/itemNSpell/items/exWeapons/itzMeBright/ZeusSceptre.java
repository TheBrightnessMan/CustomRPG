package me.bright.itemNSpell.items.exWeapons.itzMeBright;

import me.bright.itemNSpell.main.BrightItem;
import me.bright.itemNSpell.main.BrightItemAttribute;
import me.bright.itemNSpell.main.BrightSpellList;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class ZeusSceptre extends BrightItem {

    public ZeusSceptre(long id) {
        super(id, Material.END_ROD, "" + ChatColor.RED + ChatColor.BOLD + "Zeus' Sceptre");
        setRarity(Rarity.DIVINE);
        setAttribute(BrightItemAttribute.DMG_REDUCTION, 100);
        setSpell(BrightSpellList.ZEUS_LIGHTNING);

//        Damage additionalDamage = new Damage(DamageType.MAX_HP_TRUE, 100, null, true);
//
//        setAdditionalDamage(entity -> true, additionalDamage);
//        setAdditionalModifierDescription(Collections.singletonList(
//                ChatColor.GRAY + "Deals an additional " + additionalDamage)
//        );
    }
}
