package me.bright.itemNSpell.items.exWeapons.itzMeBright.infinityGauntlet;

import me.bright.brightrpg.BrightStat;
import me.bright.damage.BrightDamage;
import me.bright.damage.DamageType;
import me.bright.entity.BrightEntity;
import me.bright.itemNSpell.main.BrightItem;
import me.bright.itemNSpell.main.BrightSpells;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InfinityGauntlet extends BrightItem {

    private final BrightDamage additionalDamage = new BrightDamage(DamageType.MAX_HP_TRUE, 100, null, true);

    public InfinityGauntlet() {
        super("INFINITY_GAUNTLET", Material.EMERALD,
                "" + ChatColor.GOLD + ChatColor.BOLD + "Infinity Gauntlet");
        setRarity(Rarity.DIVINE);
        setStatFlatMod(BrightStat.DAMAGE_TAKEN, -100);
        setAdditionalModifierDescription(
                Arrays.asList(
                        ChatColor.GOLD + "Passive: " +
                                ChatColor.DARK_PURPLE + "Power Stone",
                        ChatColor.GRAY + "Attacks deal an additional",
                        additionalDamage.toString() + ChatColor.GRAY + " damage"
                )
        );
        getItemMeta().setFireResistant(true);
        setSpell(BrightSpells.MASS_DISINTEGRATION);
        setCustomModelData(getKey().hashCode()); // = 873639121
    }

    @Override
    public List<BrightDamage> onMeleeHit(BrightEntity self, BrightEntity target, List<BrightDamage> damages) {
        List<BrightDamage> finalDamage = new ArrayList<>();
        finalDamage.add(new BrightDamage(additionalDamage.type(), additionalDamage.amount(), self, additionalDamage.critical()));
        finalDamage.addAll(damages);
        return finalDamage;
    }

    @Override
    public List<BrightDamage> onArrowHit(BrightEntity self, BrightEntity target, List<BrightDamage> damages) {
        return onMeleeHit(self, target, damages);
    }

    @Override
    public List<BrightDamage> onSpellHit(BrightEntity self, BrightEntity target, List<BrightDamage> damages) {
        return onMeleeHit(self, target, damages);
    }

}
