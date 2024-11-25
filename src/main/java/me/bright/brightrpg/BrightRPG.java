package me.bright.brightrpg;

import me.bright.damage.DamageType;
import me.bright.entity.BrightEntity;
import me.bright.entity.BrightPlayer;
import me.bright.itemNSpell.main.BrightItemList;
import me.bright.itemNSpell.main.BrightSpell;
import me.bright.itemNSpell.main.BrightSpellList;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class BrightRPG extends JavaPlugin {

    private static BrightRPG plugin;
    private static final List<BrightEntity> entities = new CopyOnWriteArrayList<>();
    private BukkitTask updaterTask;
    private final long regenPeriod = 50L;

    @Override
    public void onEnable() {
        plugin = this;
        registerSpells();
        registerRecipes();
        registerListeners(new EntityRegistrator(), new ItemConverter(), new DamageHandler());

        long period = 10L;
        updaterTask = new BukkitRunnable() {
            long triggerRegen = 0L;

            @Override
            public void run() {
                updateEntities(triggerRegen);
                triggerRegen = (triggerRegen + period) % regenPeriod;
            }
        }.runTaskTimer(this, 0L, period);
    }

    private void registerSpells() {
        for (BrightSpell spell : BrightSpellList.values()) {
            registerListeners(spell);
        }
    }

    @Override
    public void onDisable() {
        if (updaterTask != null) {
            updaterTask.cancel();
        }
        Iterator<BrightEntity> iterator = entities.iterator();
        iterator.forEachRemaining(entity -> {
            if (entity.getLivingEntity().getType() == EntityType.PLAYER) return;
            entity.getLivingEntity().setHealth(0);
            entity.getLivingEntity().remove();
        });
        entities.clear();
    }

    private void updateEntities(long triggerRegen) {
        Iterator<BrightEntity> iterator = entities.iterator();
        iterator.forEachRemaining(entity -> {
            if (entity instanceof BrightPlayer) {
                updatePlayer(triggerRegen, (BrightPlayer) entity);
                return;
            }
            if (!entity.update()) {
                entity.getLivingEntity().remove();
                entities.remove(entity);
            }
        });
    }

    private void registerRecipes() {
        registerListeners(
                new CustomRecipeListener(
                        new ItemStack[]{
                                null, new ItemStack(Material.COAL), null,
                                null, new ItemStack(Material.TORCH), null,
                                null, new ItemStack(Material.STICK), null,
                        },
                        BrightItemList.FIREBOLT_WAND.buildItem()),
                new CustomRecipeListener(
                        new ItemStack[]{
                                null, new ItemStack(Material.BLAZE_POWDER), null,
                                null, new ItemStack(Material.FIRE_CHARGE), null,
                                null, new ItemStack(Material.BLAZE_ROD), null,
                        },
                        BrightItemList.FIREBALL_WAND.buildItem())
        );
    }

    private void updatePlayer(long triggerRegen, BrightPlayer player) {
        if (!player.update()) return;
        BaseComponent message = TextComponent.fromLegacy("" +
                ChatColor.RED + player.getCurrentHp() +
                ChatColor.GRAY + "/" +
                ChatColor.DARK_RED + player.getMaxHp() + "♥" +
                ChatColor.RESET + "       " +
                DamageType.PHYSICAL.color + player.getArmor() +
                ChatColor.GRAY + "/" +
                DamageType.MAGIC.color + player.getMagicResist() +
                ChatColor.RESET + "       " +
                ChatColor.AQUA + player.getCurrentMana() +
                ChatColor.GRAY + "/" +
                ChatColor.DARK_BLUE + player.getMaxMana() + "✎");
        player.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, message);
        player.getPlayer().setFoodLevel(20);
        if (triggerRegen == 0) player.regenResources();
    }

    private void registerListeners(Listener... listeners) {
        for (Listener listener : listeners) {
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }

    public static BrightRPG getPlugin() {
        return plugin;
    }

    public static void addEntity(BrightEntity entity) {
        entities.add(entity);
    }

    public static void removeEntity(LivingEntity entity) {
        entities.removeIf(brightEntity ->
                brightEntity.getLivingEntity().getUniqueId() == entity.getUniqueId());
    }
}