package me.bright.itemNSpell.main;

import me.bright.damage.BrightDamage;
import me.bright.entity.BrightEntity;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GenericBrightItem extends BrightItem {

    protected GenericBrightItem(@NotNull String key, @NotNull Material material, @NotNull String name) {
        super(key, material, name);
    }

    @Override
    public List<BrightDamage> onMeleeHit(BrightEntity self, BrightEntity target, List<BrightDamage> damages) {
        return damages;
    }

    @Override
    public List<BrightDamage> onArrowHit(BrightEntity self, BrightEntity target, List<BrightDamage> damages) {
        return damages;
    }

    @Override
    public List<BrightDamage> onSpellHit(BrightEntity self, BrightEntity target, List<BrightDamage> damages) {
        return damages;
    }

    @Override
    public List<BrightDamage> onHurt(BrightEntity attacker, BrightEntity self, List<BrightDamage> damages) {
        return damages;
    }
}
