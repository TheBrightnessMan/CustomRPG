package me.bright.conditions.implementations;

import me.bright.brightrpg.BrightStat;
import me.bright.conditions.BrightCondition;
import me.bright.entity.BrightEntity;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class WitherShield extends BrightCondition {

    public WitherShield() {
        super("WITHER_SHIELD", "Wither Shield");
    }

    @Override
    public void onStart(BrightEntity affectedEntity) {
        double critDamage = affectedEntity.getStatFromCache(BrightStat.CRIT_DMG);
        affectedEntity.setTempHp(critDamage * 1.5);
        if (!(affectedEntity.getLivingEntity() instanceof Player player)) return;
        player.playSound(player, Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1F, 0.5F);
    }

    @Override
    public void onEnd(BrightEntity affectedEntity) {
        double remainingShield = affectedEntity.getTempHp();
        affectedEntity.setTempHp(0);
        affectedEntity.healSelf(remainingShield * 0.75);
        if (!(affectedEntity.getLivingEntity() instanceof Player player)) return;
        player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 0.75F, 1F);
    }
}
