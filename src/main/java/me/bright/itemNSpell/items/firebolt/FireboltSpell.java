package me.bright.itemNSpell.items.firebolt;

import me.bright.damage.Damage;
import me.bright.damage.DamageType;
import me.bright.entity.BrightEntity;
import me.bright.entity.BrightPlayer;
import me.bright.itemNSpell.main.BrightSpell;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FireboltSpell extends BrightSpell {

    private final double projectileSpeed = 10;

    public FireboltSpell() {
        super("FIREBOLT", "Firebolt",
                10,
                new Damage(DamageType.MAGIC, 10, null, false),
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
                Particle.FLAME,
                null,
                rayTraceResult -> {
                    if (rayTraceResult.getHitEntity() == null) return;
                    if (!(rayTraceResult.getHitEntity() instanceof LivingEntity)) return;
                    if (rayTraceResult.getHitEntity().getType() == EntityType.ARMOR_STAND) return;
                    BrightEntity entity = new BrightEntity((LivingEntity) rayTraceResult.getHitEntity());
                    List<Damage> damage = player.spellHit(entity, this);
                    player.getPlayer().sendMessage(getHitMessage(1, damage));
                });
    }
}
