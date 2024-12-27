package me.bright.itemNSpell.items.fireball;

import me.bright.entity.BrightPlayer;
import me.bright.itemNSpell.main.BrightSpell;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class FireballSpell extends BrightSpell {

    public FireballSpell() {
        super("FIREBALL", "Fireball",
                0,
                10,
                5,
                -1,
                3);
    }

    @Override
    public void onChannel(@NotNull BrightPlayer player) {

    }

    @Override
    public void onChannelComplete(@NotNull BrightPlayer player) {

    }

    @Override
    public void onRightClick(@NotNull PlayerInteractEvent event, @NotNull BrightPlayer player) {
        World world = player.getPlayer().getWorld();
        Location fireballSpawn = player.getPlayer().getEyeLocation().clone();
        Vector direction = fireballSpawn.getDirection().normalize();
        fireballSpawn = fireballSpawn.add(direction);
        world.spawn(fireballSpawn, Fireball.class, fireball -> {
            fireball.setVelocity(direction.multiply(2));
            fireball.setVisualFire(false);
            fireball.setYield(0);
            fireball.setShooter(player.getPlayer());
        });
    }

    @EventHandler
    public void onFireballHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Fireball fireball)) return;
        if (!((fireball.getShooter()) instanceof Player)) return;

        BrightPlayer player = BrightPlayer.fromBukkitPlayer((Player) fireball.getShooter());
        if (player == null) return;
        Entity hitEntity = event.getHitEntity();
        Block hitBlock = event.getHitBlock();
        if (hitEntity == null && hitBlock == null) return;

        Location hitLocation = (hitEntity == null) ?
                hitBlock.getLocation() :
                hitEntity.getLocation();

        hitTargets(player, hitLocation);
    }
}
