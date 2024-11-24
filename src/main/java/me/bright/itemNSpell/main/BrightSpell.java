package me.bright.itemNSpell.main;

import me.bright.brightrpg.BrightRPG;
import me.bright.damage.Damage;
import me.bright.damage.DamageType;
import me.bright.entity.BrightEntity;
import me.bright.entity.BrightEntityAttribute;
import me.bright.entity.BrightPlayer;
import org.bukkit.*;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

public abstract class BrightSpell implements Listener {

    private final String displayName;
    private final Damage baseDamage;
    private final long id, castTime, manaCost, cooldown;
    private final double power, range, radius;
    private final Set<BrightPlayer> onCooldown = new CopyOnWriteArraySet<>();

    public BrightSpell(long id, String displayName, double power, Damage baseDamage,
                       long castTimeSeconds, long manaCost, long cooldownSeconds, double range, double radius) {
        this.id = id;
        this.displayName = displayName;
        this.power = power;
        this.baseDamage = baseDamage;
        this.castTime = castTimeSeconds;
        this.manaCost = manaCost;
        this.cooldown = cooldownSeconds;
        this.range = range;
        this.radius = radius;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        BrightPlayer player = new BrightPlayer(event.getPlayer());
        Action action = event.getAction();
        BrightItem mainHand = player.getItemInMainHand();
        if (mainHand == null) return;
        if (mainHand.getAttribute(BrightItemAttribute.SPELL) != this.id) return;
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
        if (onCooldown.contains(player)) {
            player.getPlayer().sendMessage(ChatColor.RED + "This spell is on cooldown!");
            return;
        }

        long currentMana = player.getEntityAttribute(BrightEntityAttribute.CURRENT_MANA);
        if (currentMana < manaCost) {
            player.getPlayer().sendMessage(ChatColor.RED + "Insufficient mana!");
            return;
        }
        player.setEntityAttribute(BrightEntityAttribute.CURRENT_MANA, currentMana - manaCost);

        if (castTime == 0) {
            onRightClick(event, player);
            applyCooldown(player);
            return;
        }
        onChannel(player);
        Bukkit.getScheduler().runTaskLater(BrightRPG.getPlugin(),
                () -> {
                    onChannelComplete(player);
                    applyCooldown(player);
                }, castTime * 20L);
    }

    public @NotNull String getHitMessage(int targets, Damage damage) {
        return ChatColor.GRAY + "Your " +
                ChatColor.RED + this.displayName +
                ChatColor.GRAY + " hit " + targets + " target(s) for " +
                damage.type().color + damage.amount() + damage.type().displayName + " Damage!";
    }

    public long getId() {
        return id;
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

    public @NotNull Damage getBaseDamage() {
        return baseDamage;
    }

    public @NotNull java.util.List<String> buildAttributes() {
        final java.util.List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Base Damage: " + ChatColor.RED + baseDamage);
        lore.add(ChatColor.GRAY + "Power: " + ChatColor.RED + power);
        lore.add(ChatColor.GRAY + "Mana Cost: " + ChatColor.AQUA + manaCost);
        lore.add(ChatColor.GRAY + "Range: " + ChatColor.GREEN + range + " block(s)");
        lore.add(ChatColor.GRAY + "Effect Radius: " + ChatColor.GREEN + radius + " block(s)");
        if (castTime > 0)
            lore.add(ChatColor.GRAY + "Cast Time: " + ChatColor.GREEN + castTime + "s");
        if (cooldown > 0)
            lore.add(ChatColor.GRAY + "Cooldown: " + ChatColor.GREEN + cooldown + "s");
        return lore;
    }

    private void applyCooldown(@NotNull BrightPlayer player) {
        onCooldown.add(player);
        Bukkit.getScheduler().runTaskLater(BrightRPG.getPlugin(),
                () -> onCooldown.remove(player), cooldown * 20L);
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

    public @NotNull Collection<BrightEntity> getTargets(@NotNull BrightEntity caster,
                                                        @NotNull Location targetLocation) {
        World world = targetLocation.getWorld();
        if (world == null) return new ArrayList<>();
        Collection<Entity> initialTargets = world.getNearbyEntities(
                targetLocation, radius, radius, radius,
                entity -> isValidTarget(caster.getLivingEntity(), entity));
        return initialTargets.stream()
                .map(entity -> new BrightEntity((LivingEntity) entity))
                .toList();
    }

    public long hitTargets(@NotNull BrightEntity caster,
                           @NotNull Location targetLocation,
                           @Nullable Consumer<BrightEntity> onHit) {
        Collection<BrightEntity> targets = getTargets(caster, targetLocation);
        if (targets.isEmpty()) return 0L;

        long damage = 0;
        DamageType type;
        if (onHit == null) {
            for (BrightEntity target : targets) {
                damage += caster.spellHit(target, this).amount();
            }
        } else {
            for (BrightEntity target : targets) {
                onHit.accept(target);
                damage += caster.spellHit(target, this).amount();
            }
        }

        caster.getLivingEntity().sendMessage(getHitMessage(
                targets.size(),
                new Damage(DamageType.compress(baseDamage.type()), damage, caster, false)
        ));
        return damage;
    }

    public static boolean isValidTarget(LivingEntity caster, Entity entity) {
        return entity instanceof LivingEntity &&
                entity.getType() != EntityType.ARMOR_STAND &&
                !entity.getUniqueId().equals(caster.getUniqueId());
    }

}
