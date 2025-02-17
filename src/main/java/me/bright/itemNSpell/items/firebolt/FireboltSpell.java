package me.bright.itemNSpell.items.firebolt;

import me.bright.damage.BrightDamage;
import me.bright.entity.BrightEntity;
import me.bright.entity.BrightPlayer;
import me.bright.itemNSpell.main.BrightSpell;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class FireboltSpell extends BrightSpell {

    private final double projectileSpeed = 10;

    public FireboltSpell() {
        super("FIREBOLT", "Firebolt",
                0,
                3,
                1,
                10,
                0.5);
    }

    @Override
    public void onChannel(@NotNull BrightPlayer player) {

    }

    @Override
    public void onChannelComplete(@NotNull BrightPlayer player) {

    }

    @Override
    public void onRightClick(@NotNull PlayerInteractEvent event, @NotNull BrightPlayer player) {
        Location start = player.getPlayer().getEyeLocation().clone();
        Vector direction = player.getPlayer().getLocation().getDirection().clone().normalize();
        BrightSpell.shootProjectile(player,
                start, direction, getRadius(), getRange(), projectileSpeed,
                Particle.DUST,
                new Particle.DustOptions(Color.ORANGE, 3),
                rayTraceResult -> {
                    if (rayTraceResult.getHitEntity() == null) return;
                    if (!(rayTraceResult.getHitEntity() instanceof LivingEntity)) return;
                    if (rayTraceResult.getHitEntity().getType() == EntityType.ARMOR_STAND) return;
                    BrightEntity entity = BrightEntity.fromLivingEntity((LivingEntity) rayTraceResult.getHitEntity());
                    if (entity == null) return;
                    @NotNull BrightDamage[] brightDamage = player.spellHit(entity, this);
                    player.getPlayer().sendMessage(getHitMessage(1, brightDamage));
                });
    }
}
