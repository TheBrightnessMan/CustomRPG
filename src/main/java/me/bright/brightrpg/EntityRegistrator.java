package me.bright.brightrpg;

import me.bright.entity.BrightEntity;
import me.bright.entity.BrightEntityAttribute;
import me.bright.entity.BrightPlayer;
import me.bright.itemNSpell.main.BrightItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class EntityRegistrator implements Listener {

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) return;
        BrightEntity entity = BrightEntity.fromLivingEntity(event.getEntity());
        BrightRPG.addEntity(entity);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player bukkitPlayer = event.getPlayer();
        BrightRPG.addEntity(new BrightPlayer(bukkitPlayer));
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
