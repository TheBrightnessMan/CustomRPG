package me.bright.itemNSpell.items.exWeapons.itzMeBright.infinityGauntlet;

import me.bright.entity.BrightPlayer;
import me.bright.itemNSpell.main.BrightSpell;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class MassDisintegration extends BrightSpell {

    public MassDisintegration() {
        super("MASS_DISINTEGRATION", "Universal Disintegration",
                3,
                0,
                0,
                -1,
                20);
        setDescription(Collections.singletonList(ChatColor.GRAY + "Disintegrate all living entities in range"));
    }

    @Override
    public void onChannel(@NotNull BrightPlayer player) {
        showRangeIndicator(player);
    }

    @Override
    public void onChannelComplete(@NotNull BrightPlayer player) {
        Location location = player.getPlayer().getLocation();
        hitTargets(player, location,
                entity -> {
                    LivingEntity bukkitEntity = entity.getLivingEntity();
                    Location targetLocation = bukkitEntity.getLocation();
                    World world = targetLocation.getWorld();
                    if (world == null) return;
                    world.strikeLightningEffect(targetLocation);
                }
        );
    }

    @Override
    public void onRightClick(@NotNull PlayerInteractEvent event, @NotNull BrightPlayer player) {

    }
}
