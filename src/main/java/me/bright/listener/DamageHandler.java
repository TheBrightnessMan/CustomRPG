package me.bright.listener;

import me.bright.damage.BrightDamage;
import me.bright.entity.BrightEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageHandler implements Listener {

    @EventHandler
    public void onEntityHit(EntityDamageEvent event) {
        event.setDamage(0.01);

        Entity damagerEntity = event.getDamageSource().getCausingEntity();
        Entity targetEntity = event.getEntity();

        if (!(damagerEntity instanceof LivingEntity)) return; // Shouldn't happen
        if (!(targetEntity instanceof LivingEntity)) return; // Shouldn't happen
        if (targetEntity.getType() == EntityType.ARMOR_STAND) return; // Ignore armor stands

        BrightEntity attacker = BrightEntity.fromLivingEntity((LivingEntity) damagerEntity);
        BrightEntity target = BrightEntity.fromLivingEntity((LivingEntity) targetEntity);
        BrightDamage[] damages = BrightDamage.emptyFinalised;


        switch (event.getCause()) {
            case ENTITY_ATTACK, ENTITY_SWEEP_ATTACK ->
                    damages = attacker.meleeHit(target);
            case PROJECTILE -> {
                if (event.getDamageSource().getDirectEntity().getType() == EntityType.ARROW) {
                    damages = attacker.meleeHit(target);
                }
            }
        }

        BrightDamage.showDamages(target.getLivingEntity(), damages);
    }
}
