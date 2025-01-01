package me.bright.brightrpg;

import me.bright.entity.BrightEntity;
import me.bright.entity.BrightPlayer;
import me.bright.itemNSpell.main.BrightItems;
import me.bright.itemNSpell.main.BrightSpell;
import me.bright.itemNSpell.main.BrightSpells;
import me.bright.listener.CustomRecipeListener;
import me.bright.listener.DamageHandler;
import me.bright.listener.EntityRegistrator;
import me.bright.listener.ItemConverter;
import me.bright.listener.launchpad.CoalToGoldLaunchPad;
import me.bright.listener.launchpad.DeepToGoldLaunchPad;
import me.bright.listener.launchpad.GoldToCoalLaunchPad;
import me.bright.listener.launchpad.GoldToDeepLaunchPad;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class BrightRPG extends JavaPlugin {

    private static BrightRPG plugin;
    private static final List<BrightEntity> entities = new CopyOnWriteArrayList<>();
    private static final List<BrightPlayer> players = new CopyOnWriteArrayList<>();
    private BukkitTask updaterTask;
    public final long updateTickRate = 2L;

    @Override
    public void onEnable() {
        plugin = this;
        registerSpells();
        registerRecipes();
        registerLaunchPads();
        registerCommand("brightrpg", new BrightCommand());
        registerListeners(new EntityRegistrator(), new ItemConverter(), new DamageHandler());

        long period = 1L;
        updaterTask = new BukkitRunnable() {
            long tick = 0L;

            @Override
            public void run() {
                if (tick % updateTickRate == 0) {
                    updateEntities(tick);
                }
                tick = (tick + period) % 1200L;
            }
        }.runTaskTimer(this, 0L, period);
    }

    private void registerSpells() {
        for (BrightSpell spell : BrightSpells.values()) {
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
        players.clear();
    }

    private void updateEntities(long tick) {
        Iterator<BrightEntity> iterator = entities.iterator();
        iterator.forEachRemaining(entity -> {
            entity.tickConditions();
            if (entity instanceof BrightPlayer player) {
                updatePlayer(tick, player);
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
                        Objects.requireNonNull(BrightItems.getCustomItem("FIREBOLT_WAND")).buildItem()),
                new CustomRecipeListener(
                        new ItemStack[]{
                                null, new ItemStack(Material.BLAZE_POWDER), null,
                                null, new ItemStack(Material.FIRE_CHARGE), null,
                                null, new ItemStack(Material.BLAZE_ROD), null,
                        },
                        Objects.requireNonNull(BrightItems.getCustomItem("FIREBALL_WAND")).buildItem())
        );
    }

    private void registerCommand(String command, CommandExecutor commandExecutor) {
        Logger logger = Bukkit.getLogger();
        this.getCommand(command).setExecutor(commandExecutor);
        logger.log(Level.INFO, command + " command Registered");
    }

    private void registerLaunchPads() {
        registerListeners(new CoalToGoldLaunchPad(), new GoldToCoalLaunchPad(),
                new GoldToDeepLaunchPad(), new DeepToGoldLaunchPad());
    }

    private void updatePlayer(long tick, BrightPlayer player) {
        if (!player.update()) return;
        player.sendActionBar();

        player.getPlayer().setFoodLevel(20);
        if (tick % 20 == 0) {
            player.naturalHpRegenTick();
            player.naturalManaRegenTick();
        }
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
                brightEntity.equals(entity));
    }

    public static List<BrightPlayer> getPlayers() {
        return players;
    }

    public static void addPlayer(BrightPlayer player) {
        players.add(player);
    }

    public static void removeEntity(BrightPlayer player) {
        players.removeIf(brightPlayer ->
                brightPlayer.equals(player));
    }

    public static List<BrightEntity> getEntities() {
        return entities;
    }
}