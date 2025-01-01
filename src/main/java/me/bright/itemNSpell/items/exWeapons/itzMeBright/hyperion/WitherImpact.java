package me.bright.itemNSpell.items.exWeapons.itzMeBright.hyperion;

import me.bright.conditions.BrightConditions;
import me.bright.entity.BrightPlayer;
import me.bright.itemNSpell.main.BrightSpell;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class WitherImpact extends BrightSpell {

    public WitherImpact() {
        super("WITHER_IMPACT", "Wither Impact",
                0, 150, 0, 5, 5);
        setBaseDamage(500);
    }

    @Override
    public void onChannel(@NotNull BrightPlayer player) {

    }

    @Override
    public void onChannelComplete(@NotNull BrightPlayer player) {

    }

    @Override
    public void onRightClick(@NotNull PlayerInteractEvent event, @NotNull BrightPlayer player) {
        Player bukkitPlayer = player.getPlayer();
        Location location = bukkitPlayer.getLocation();
        Vector direction = location.getDirection().normalize();
        World world = bukkitPlayer.getWorld();
        Location destination = location.clone();
        int distanceTraveled = 0;
        while (distanceTraveled < getRange()) {
            if (!world.getBlockAt(destination).isPassable()) break;
            destination.add(direction);
            distanceTraveled++;
        }
        bukkitPlayer.teleport(destination.add(0, 1, 0), PlayerTeleportEvent.TeleportCause.PLUGIN);
        bukkitPlayer.playSound(bukkitPlayer, Sound.ENTITY_GENERIC_EXPLODE, 4.0F, 0.6F);
        player.applyCondition(BrightConditions.WITHER_SHIELD, 5 * 20, false);
    }
}
