package me.bright.itemNSpell.items.exWeapons.itzMeBright;

import me.bright.damage.ConditionalDamage;
import me.bright.damage.Damage;
import me.bright.damage.DamageType;
import me.bright.itemNSpell.main.BrightItem;
import me.bright.itemNSpell.main.BrightItemAttribute;
import me.bright.itemNSpell.main.BrightSpellList;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.Arrays;

public class InfinityGauntlet extends BrightItem {

    public InfinityGauntlet() {
        super("INFINITY_GAUNTLET", Material.EMERALD,
                "" + ChatColor.GOLD + ChatColor.BOLD + "Infinity Gauntlet");
        setRarity(Rarity.DIVINE);
        setAttribute(BrightItemAttribute.DMG_REDUCTION, 100);
        Damage additionalDamage = new Damage(DamageType.CURRENT_HP_TRUE, 100, null, true);
        addConditionalDamage(
                new ConditionalDamage(
                        (caster, target) -> true,
                        additionalDamage
                ),
                Arrays.asList(
                        ChatColor.GRAY + "Attacks and Spells deal an additional",
                        additionalDamage.toString() + ChatColor.GRAY + " damage"
                )
        );
        setSpell(BrightSpellList.MASS_DISINTEGRATION);
    }
}
