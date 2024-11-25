package me.bright.itemNSpell.main;

import me.bright.brightrpg.BrightRPG;
import me.bright.damage.Damage;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

public abstract class BrightSpell implements Listener {

    private static final Logger log = LoggerFactory.getLogger(BrightSpell.class);
    private final String displayName, key;
    private final Damage baseDamage;
    private final long castTime, manaCost, cooldown;
    private final double power, range, radius;
    private final Set<BrightPlayer> onCooldown = new CopyOnWriteArraySet<>();
    private List<String> description = new ArrayList<>();

    public BrightSpell(String key, String displayName, double power, Damage baseDamage,
                       long castTimeSeconds, long manaCost, long cooldownSeconds, double range, double radius) {
        this.key = key;
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
        if (mainHand.getSpell() == null) return;
        if (!mainHand.getSpell().getKey().equals(this.getKey())) return;
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

    public @NotNull String getHitMessage(int targets, List<Damage> damages) {
        StringBuilder hitMsg = new StringBuilder(ChatColor.GRAY + "Your " +
                ChatColor.RED + this.displayName +
                ChatColor.GRAY + " hit " + targets + " target(s) for ");
        for (Damage damage : damages) {
            if (damage.amount() <= 0) continue;
            ;
            hitMsg.append(damage)
                    .append(",");
        }
        hitMsg.deleteCharAt(hitMsg.length() - 1);
        hitMsg.append(ChatColor.GRAY).append(" Damage!");
        return hitMsg.toString();
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

    public @NotNull Damage getBaseDamage() {
        return baseDamage;
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
                .map(entity -> BrightEntity.fromLivingEntity((LivingEntity) entity))
                .toList();
    }

    public List<Damage> hitTargets(@NotNull BrightEntity caster,
                                   @NotNull Location targetLocation,
                                   @NotNull Consumer<BrightEntity> onHit) {
        Collection<BrightEntity> targets = getTargets(caster, targetLocation);
        if (targets.isEmpty()) return new ArrayList<>();

        List<Damage> damages = new ArrayList<>();
        for (BrightEntity target : targets) {
            onHit.accept(target);
            damages.addAll(caster.spellHit(target, this));
        }
        List<Damage> merged = Damage.mergeDamages(damages);
        caster.getLivingEntity().sendMessage(getHitMessage(targets.size(), merged));
        return merged;
    }

    public List<Damage> hitTargets(@NotNull BrightEntity caster,
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
}
