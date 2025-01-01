package me.bright.enchant;

import me.bright.brightrpg.BrightStatModifier;
import me.bright.damage.BrightDamage;
import me.bright.entity.BrightEntity;

import java.util.List;

public abstract class BrightEnchant extends BrightStatModifier {

    private final String key, displayName;

    public BrightEnchant(String key, String displayName) {
        super(false);
        this.key = key;
        this.displayName = displayName;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<BrightDamage> onMeleeHit(BrightEntity self, BrightEntity target, int level, List<BrightDamage> damages) {
        // No op
        return damages;
    }

    public List<BrightDamage> onArrowHit(BrightEntity self, BrightEntity target, int level, List<BrightDamage> damages) {
        // No op
        return damages;
    }

    public List<BrightDamage> onSpellHit(BrightEntity self, BrightEntity target, int level, List<BrightDamage> damages) {
        // No op
        return damages;
    }

    public List<BrightDamage> onHurt(BrightEntity attacker, BrightEntity self, int level, List<BrightDamage> damages) {
        // No op
        return damages;
    }

    public BrightStatModifier getModifier(int level) {
        return new BrightStatModifier(false);
    }
}
