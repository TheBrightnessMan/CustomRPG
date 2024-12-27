package me.bright.conditions.implementations;

import me.bright.brightrpg.BrightStats;
import me.bright.conditions.BrightCondition;
import me.bright.entity.BrightEntity;

public class WitherShield extends BrightCondition {

    public WitherShield() {
        super("WITHER_SHIELD", "Wither Shield");
    }

    @Override
    public void onStart(BrightEntity affectedEntity) {
        double critDamage = affectedEntity.getStatFromCache(BrightStats.CRIT_DMG);
        affectedEntity.setTempHp(critDamage * 1.5);
    }

    @Override
    public void onEnd(BrightEntity affectedEntity) {
        double remainingShield = affectedEntity.getTempHp();
        affectedEntity.setTempHp(0);
        affectedEntity.healSelf(remainingShield * 0.75);
    }
}
