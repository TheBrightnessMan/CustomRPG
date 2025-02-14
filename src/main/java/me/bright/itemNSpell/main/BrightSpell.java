package me.bright.itemNSpell.main;

import me.bright.brightrpg.BrightRPG;
import me.bright.damage.BrightDamage;
import me.bright.entity.BrightEntity;
import me.bright.entity.BrightPlayer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

public abstract class BrightSpell implements Listener {

    private static final Logger log = LoggerFactory.getLogger(BrightSpell.class);
    private final String displayName, key;
    private double baseDamage = 0, power = 100;
    private final double castTime, manaCost, cooldown, range, radius;
    private final Set<BrightPlayer> onCooldown = new CopyOnWriteArraySet<>();
    private List<String> description = new ArrayList<>();

    public BrightSpell(String key, String displayName, double castTimeSeconds,
                       double manaCost, double cooldownSeconds, double range, double radius) {
        this.key = key;
        this.displayName = displayName;
        this.castTime = castTimeSeconds;
        this.manaCost = manaCost;
        this.cooldown = cooldownSeconds;
        this.range = range;
        this.radius = radius;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        BrightPlayer player = BrightPlayer.fromBukkitPlayer(event.getPlayer());
        if (player == null) return;
        Action action = event.getAction();
        BrightItem mainHand = player.getItemInMainHand();
        if (mainHand == null) return;
        if (mainHand.getSpell() == null) return;
        if (!mainHand.getSpell().getKey().equals(this.getKey())) return;
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
        if (onCooldown.contains(player)) {
            player.getPlayer().sendMessage(ChatColor.RED + "This spell is on cooldown!");
            return;
        }
        if (player.magicDisabled()) {
            player.getPlayer().sendMessage(ChatColor.RED + "Something is disrupting your magic!");
            return;
        }

        double currentMana = player.getCurrentMana();
        if (currentMana < manaCost) {
            player.getPlayer().sendMessage(ChatColor.RED + "Insufficient mana!");
            return;
        }
        player.setCurrentMana(currentMana - manaCost);
        applyCooldown(player);

        if (castTime == 0) {
            onRightClick(event, player);
            return;
        }
        onChannel(player);
        Bukkit.getScheduler().runTaskLater(BrightRPG.getPlugin(),
                () -> onChannelComplete(player), (long) (castTime * 20L));
    }

    public @NotNull String getHitMessage(int targets, BrightDamage[] brightDamages) {
        return ChatColor.GRAY + "Your " +
                ChatColor.RED + displayName +
                ChatColor.GRAY + " hit " + targets +
                " target(s) for " +
                BrightDamage.mergedDamageToString(brightDamages) +
                ChatColor.GRAY + " Damage!";
    }

    public @NotNull String getKey() {
        return key;
    }

    public double getRange() {
        return range;
    }

    public double getRadius() {
        return radius;
    }

    public @NotNull String getDisplayName() {
        return displayName;
    }

    public double getPower() {
        return power;
    }

    public double getBaseDamage() {
        return baseDamage;
    }

    protected void setBaseDamage(double baseDamage) {
        this.baseDamage = baseDamage;
    }

    public @NotNull List<String> buildAttributes() {
        final List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Base Damage: " + ChatColor.RED + baseDamage);
        lore.add(ChatColor.GRAY + "Mana Cost: " + ChatColor.AQUA + manaCost);
        if (cooldown > 0)
            lore.add(ChatColor.GRAY + "Cooldown: " + ChatColor.GREEN + cooldown + "s");
        return lore;
    }

    private void applyCooldown(@NotNull BrightPlayer player) {
        double totalCD = cooldown + castTime;
        if (totalCD == 0) return;
        onCooldown.add(player);
        Bukkit.getScheduler().runTaskLater(BrightRPG.getPlugin(),
                () -> onCooldown.remove(player), (long) (totalCD * 20));
    }

    public abstract void onChannel(@NotNull BrightPlayer player);

    public abstract void onChannelComplete(@NotNull BrightPlayer player);

    public abstract void onRightClick(@NotNull PlayerInteractEvent event, @NotNull BrightPlayer player);

    public static void shootProjectile(@NotNull BrightEntity shooter,
                                       @NotNull Location start,
                                       @NotNull Vector direction,
                                       double projectileSize,
                                       double projectileRange,
                                       double blockPerSecond,
                                       @NotNull Particle particle,
                                       @Nullable Particle.DustOptions dustOptions,
                                       @NotNull Consumer<RayTraceResult> onHit) {
        final World world = start.getWorld();
        if (world == null) return;

        final long updateRate = 2L;
        final double stepSize = blockPerSecond / 20 * updateRate;
        final Vector nextStep = direction.normalize().multiply(stepSize);

        new BukkitRunnable() {
            double distanceTraveled = 0;
            final Location current = start.clone();

            @Override
            public void run() {
                if (distanceTraveled >= projectileRange) {
                    cancel();
                    return;
                }
                world.spawnParticle(particle, current, 1, dustOptions);
                RayTraceResult result = world.rayTrace(current,
                        direction, stepSize, FluidCollisionMode.NEVER, true, projectileSize,
                        entity -> !entity.getUniqueId().equals(shooter.getLivingEntity().getUniqueId()));
                if (result == null) {
                    // Hit nothing, step forward
                    current.add(nextStep);
                    distanceTraveled += stepSize;
                    return;
                }

                // Hit something, terminate loop
                onHit.accept(result);
                cancel();
            }
        }.runTaskTimer(BrightRPG.getPlugin(), 0L, updateRate);
    }

    public @NotNull List<BrightEntity> getTargets(@NotNull BrightEntity caster,
                                                  @NotNull Location targetLocation) {
        World world = targetLocation.getWorld();
        if (world == null) return new ArrayList<>();
        Collection<Entity> initialTargets = world.getNearbyEntities(
                targetLocation, radius, radius, radius,
                entity -> isValidTarget(caster.getLivingEntity(), entity));
        return initialTargets.stream()
                .map(entity -> BrightEntity.fromLivingEntity((LivingEntity) entity))
                .toList();
    }

    public BrightDamage[] hitTargets(@NotNull BrightEntity caster,
                                     @NotNull Location targetLocation,
                                     @NotNull Consumer<BrightEntity> onHit) {
        Collection<BrightEntity> targets = getTargets(caster, targetLocation);
        if (targets.isEmpty()) return BrightDamage.emptyFinalised;

        List<BrightDamage> brightDamages = new ArrayList<>();
        for (BrightEntity target : targets) {
            onHit.accept(target);
            BrightDamage[] brightDamage = caster.spellHit(target, this);
            String messageToTarget = ChatColor.GRAY + caster.getName() + " hit you with " +
                    ChatColor.RED + displayName +
                    ChatColor.GRAY + " for " +
                    BrightDamage.mergedDamageToString(brightDamage) + ChatColor.GRAY + " Damage!";
            target.getLivingEntity().sendMessage(messageToTarget);
            brightDamages.addAll(List.of(brightDamage));
        }
        BrightDamage[] merged = BrightDamage.mergeDamages(brightDamages);
        caster.getLivingEntity().sendMessage(getHitMessage(targets.size(), merged));
        return merged;
    }

    public BrightDamage[] hitTargets(@NotNull BrightEntity caster,
                                     @NotNull Location targetLocation) {
        return hitTargets(caster, targetLocation, entity -> {
        });
    }

    public static boolean isValidTarget(LivingEntity caster, Entity entity) {
        return entity instanceof LivingEntity &&
                entity.getType() != EntityType.ARMOR_STAND &&
                !entity.getUniqueId().equals(caster.getUniqueId());
    }

    protected void setDescription(@NotNull List<String> description) {
        this.description = description;
    }

    public @NotNull List<String> getDescription() {
        return description;
    }

    protected void showRangeIndicator(@NotNull BrightPlayer player) {
        Location center = player.getPlayer().getLocation().clone().add(0, 1, 0);
        final long tickRate = (long) (castTime * 20 / 6);
        if (tickRate < 1) return;

        new BukkitRunnable() {
            final Object lock = new Object();
            final Map<Location, BlockData> originalBlocks = new HashMap<>();
            long tick = 0;
            boolean blocksInitialized = false;
            Material indicator;

            @Override
            public void run() {
                if (tick <= 1) indicator = Material.YELLOW_CONCRETE;
                else if (tick <= 3) indicator = Material.RED_CONCRETE;
                else if (tick <= 5) indicator = Material.BLACK_CONCRETE;
                else {
                    Bukkit.getScheduler().runTaskLater(BrightRPG.getPlugin(),
                            () -> {
                                for (var entry : originalBlocks.entrySet()) {
                                    entry.getKey().getBlock().setBlockData(entry.getValue());
                                }
                            }, tickRate);
                    cancel();
                    return;
                }

                for (int x = (int) -radius; x <= radius; x++) {
                    for (int z = (int) -radius; z <= radius; z++) {
                        // Start with a square
                        Location target = center.clone().add(x, 0, z);

                        // Want a circle
                        if (center.distanceSquared(target) > radius * radius) continue;

                        target = getSolidBlockBelow(target);
                        if (target == null) continue;
                        if (tick % 2 == 0) {
                            if (!blocksInitialized) {
                                originalBlocks.put(target, target.getBlock().getBlockData());
                            }
                        } else {
                            target.getBlock().setType(indicator);
                        }
                    }
                }
                tick++;
                blocksInitialized = true;
            }
        }.runTaskTimer(BrightRPG.getPlugin(), 0L, tickRate);
    }

    private @Nullable Location getSolidBlockBelow(@NotNull Location location) {
        Location clone = location.clone();
        Block block = clone.getBlock();
        int depth = 0;
        while (depth < 10) {
            switch (block.getType()) {
                case CREEPER_HEAD, CREEPER_WALL_HEAD, DRAGON_HEAD, DRAGON_WALL_HEAD,
                     PIGLIN_HEAD, PIGLIN_WALL_HEAD, PLAYER_HEAD, PLAYER_WALL_HEAD,
                     ZOMBIE_HEAD, ZOMBIE_WALL_HEAD, SKELETON_SKULL, SKELETON_WALL_SKULL,
                     WITHER_SKELETON_SKULL, WITHER_SKELETON_WALL_SKULL,
                     AIR, CAVE_AIR, VOID_AIR,
                     WATER, LAVA -> {
                    depth++;
                    clone.subtract(0, 1, 0);
                    block = clone.getBlock();
                    continue;
                }
            }
            break;
        }

        if (block.isPassable()) return null;
        return clone;
    }
}
