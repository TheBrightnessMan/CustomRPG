package me.bright.itemNSpell.items.exWeapons.itzMeBright;

import me.bright.damage.Damage;
import me.bright.damage.DamageType;
import me.bright.entity.BrightPlayer;
import me.bright.itemNSpell.main.BrightSpell;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public class ZeusLightning extends BrightSpell {

    public ZeusLightning() {
        super("ZEUS_LIGHTNING", "Zeus' Smite",
                100,
                new Damage(DamageType.MAX_HP_TRUE, 100, null, false),
                0,
                0,
                0,
                -1,
                100);
    }

    @Override
    public void onChannel(@NotNull BrightPlayer player) {

    }

    @Override
    public void onChannelComplete(@NotNull BrightPlayer player) {

    }

    @Override
    public void onRightClick(@NotNull PlayerInteractEvent event, @NotNull BrightPlayer player) {
        Location location = player.getPlayer().getLocation();
        hitTargets(player, location,
                entity -> {
                    LivingEntity bukkitEntity = entity.getLivingEntity();
                    Location targetLocation = bukkitEntity.getLocation();
                    World world = targetLocation.getWorld();
                    if (world == null) return;
                    world.strikeLightning(targetLocation);
                }
        );
    }
}
