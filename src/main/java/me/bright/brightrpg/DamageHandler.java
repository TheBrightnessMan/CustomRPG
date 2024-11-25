package me.bright.brightrpg;

import me.bright.entity.BrightEntity;
import me.bright.entity.BrightPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageHandler implements Listener {

    @EventHandler
    public void onEntityHit(EntityDamageEvent event) {
        Entity damagerEntity = event.getDamageSource().getCausingEntity();
        Entity targetEntity = event.getEntity();
        BrightEntity attacker = null;
        BrightEntity victim;
        event.setDamage(0.01);

        if (damagerEntity instanceof LivingEntity)
            attacker = BrightEntity.fromLivingEntity((LivingEntity) damagerEntity);
        if (damagerEntity instanceof Player)
            attacker = BrightPlayer.fromPlayer((Player) damagerEntity);
        if (!(targetEntity instanceof LivingEntity)) return;
        if (targetEntity.getType() == EntityType.ARMOR_STAND) return;

        victim = BrightEntity.fromLivingEntity((LivingEntity) targetEntity);

        switch (event.getCause()) {
            case ENTITY_ATTACK, ENTITY_SWEEP_ATTACK -> {
                if (attacker == null) return;
                attacker.physicalHit(victim);
            }
            case PROJECTILE -> {
                if (attacker == null) return;
                if (event.getDamageSource().getDirectEntity().getType() == EntityType.ARROW) {
                    attacker.physicalHit(victim);
                }
            }
        }
    }
}
