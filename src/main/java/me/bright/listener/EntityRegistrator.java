package me.bright.listener;

import me.bright.brightrpg.BrightRPG;
import me.bright.brightrpg.BrightStat;
import me.bright.entity.BrightEntity;
import me.bright.entity.BrightPlayer;
import me.bright.itemNSpell.main.BrightItem;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class EntityRegistrator implements Listener {

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {
//        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) {
//            event.setCancelled(true);
//            return;
//        }
        if (event.getEntityType() == EntityType.ARMOR_STAND) return;
        BrightEntity entity = new BrightEntity(event.getEntity());
        BrightRPG.addEntity(entity);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player bukkitPlayer = event.getPlayer();
        BrightPlayer brightPlayer = new BrightPlayer(bukkitPlayer);
        BrightRPG.addEntity(brightPlayer);
        BrightRPG.addPlayer(brightPlayer);
        PlayerInventory playerInventory = bukkitPlayer.getInventory();
        ItemStack[] contents = playerInventory.getContents();
        for (int i = 0; i <= 40; i++) {
            ItemStack itemStack = contents[i];
            if (itemStack == null) continue;
            BrightItem brightItem = BrightItem.fromItemStack(itemStack);
            if (brightItem == null) continue;
            playerInventory.setItem(i, brightItem.buildItem());
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        BrightPlayer player = BrightPlayer.fromBukkitPlayer(event.getPlayer());
        if (player == null) return;
        player.setCurrentHp(player.getStatFromCache(BrightStat.MAX_HP));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        BrightRPG.removeEntity(event.getPlayer());
    }
}
