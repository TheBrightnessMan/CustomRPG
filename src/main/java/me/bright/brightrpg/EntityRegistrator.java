package me.bright.brightrpg;

import me.bright.entity.BrightEntity;
import me.bright.entity.BrightEntityAttribute;
import me.bright.entity.BrightPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class EntityRegistrator implements Listener {

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) return;
        BrightRPG.addEntity(new BrightEntity(event.getEntity()));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        BrightRPG.addEntity(new BrightPlayer(event.getPlayer()));
//        PlayerInventory inventory = event.getPlayer().getInventory();
//        inventory.addItem(new FireballWand().buildItem());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        BrightRPG.removeEntity(event.getEntity());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        BrightPlayer player = new BrightPlayer(event.getPlayer());
        BrightRPG.addEntity(player);
        player.setEntityAttribute(BrightEntityAttribute.CURRENT_HP, player.getMaxHp());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        BrightRPG.removeEntity(event.getPlayer());
    }
}
