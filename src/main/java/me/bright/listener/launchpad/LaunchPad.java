package me.bright.listener.launchpad;

import me.bright.brightrpg.BrightRPG;
import org.apache.commons.math3.linear.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class LaunchPad implements Listener {

    private final BrightRPG plugin;
    private final BoundingBox activeRegion;
    private final Location from, mid, to;
    protected static final World HUB = Bukkit.getWorld("hypixel_skyblock");
    protected static final int travelSeconds = 3;
    private final long updateRate = 2L;
    private final double multiplier;

    private final double xa, xb, ya, yb, yc, za, zb;

    public LaunchPad(BoundingBox activeRegion, Location from, Location mid, Location to, double multiplier) {
        this.activeRegion = activeRegion;
        this.from = from;
        this.mid = mid;
        this.to = to;
        this.multiplier = multiplier;
        this.plugin = BrightRPG.getPlugin();

        // Line going through (0, x1), (T, x2)
        // 0a + b = x1  =>  b = x1
        // Ta + b = x2  =>  Ta + x1 = x2  =>  a = (x2 - x1) / T
        double x1 = from.getX();
        double x2 = to.getX();
        this.xb = x1;
        this.xa = (x2 - x1) / travelSeconds;

        // Quadratic going through (0, y1), (T/2, y2), (T, y3)
        RealVector solution = getYCoeff();
        this.ya = solution.getEntry(0);
        this.yb = solution.getEntry(1);
        this.yc = solution.getEntry(2);

        // Line going through (0, z1), (T, z2)
        // 0a + b = z1  =>  b = z1
        // Ta + b = z2  =>  Ta + z1 = z2  =>  a = (z2 - z1) / T
        double z1 = from.getZ();
        double z2 = to.getZ();
        this.zb = z1;
        this.za = (z2 - z1) / travelSeconds;
    }

    public LaunchPad(BoundingBox activeRegion, Location from, Location mid, Location to) {
        this.activeRegion = activeRegion;
        this.from = from;
        this.mid = mid;
        this.to = to;
        this.multiplier = 1.1;
        this.plugin = BrightRPG.getPlugin();

        // Line going through (0, x1), (T, x2)
        // 0a + b = x1  =>  b = x1
        // Ta + b = x2  =>  Ta + x1 = x2  =>  a = (x2 - x1) / T
        double x1 = from.getX();
        double x2 = to.getX();
        this.xb = x1;
        this.xa = (x2 - x1) / travelSeconds;

        // Quadratic going through (0, y1), (T/2, y2), (T, y3)
        RealVector solution = getYCoeff();
        this.ya = solution.getEntry(0);
        this.yb = solution.getEntry(1);
        this.yc = solution.getEntry(2);

        // Line going through (0, z1), (T, z2)
        // 0a + b = z1  =>  b = z1
        // Ta + b = z2  =>  Ta + z1 = z2  =>  a = (z2 - z1) / T
        double z1 = from.getZ();
        double z2 = to.getZ();
        this.zb = z1;
        this.za = (z2 - z1) / travelSeconds;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final World world = player.getWorld();
        if (!world.getName().equals("hypixel_skyblock")) return;
        if (!activeRegion.overlaps(player.getBoundingBox())) return;
        if (player.getVehicle() != null) return;

        world.spawn(from, ArmorStand.class, CreatureSpawnEvent.SpawnReason.CUSTOM, false, armorStand -> {
            armorStand.setVisible(false);
            armorStand.setSmall(true);
            armorStand.addPassenger(player);
            player.setInvulnerable(true);
            new BukkitRunnable() {
                double t = 0;

                @Override
                public void run() {
                    if (t > travelSeconds) {
                        cancel();
                        armorStand.remove();
                        player.setInvulnerable(false);
                        player.teleport(to);
                        return;
                    }
                    Vector velocity = new Vector(vx(t), vy(t), vz(t)).normalize().multiply(multiplier);
                    armorStand.setVelocity(velocity);
                    t += (double) updateRate / 20;
                }
            }.runTaskTimer(plugin, 0L, updateRate);
        });
    }

    private double x(double t) {
        return xa * t + xb;
    }

    private double y(double t) {
        return ya * t * t + yb * t + yc;
    }

    private double z(double t) {
        return za * t + zb;
    }

    private double vx(double t) {
        return xa;
    }

    private double vy(double t) {
        return 2 * ya * t + yb;
    }

    private double vz(double t) {
        return za;
    }

    private RealVector getYCoeff() {
        double[][] matrixT = {
                {0, 0, 1},
                {(double) (travelSeconds * travelSeconds) / 4, (double) travelSeconds / 2, 1},
                {travelSeconds * travelSeconds, travelSeconds, 1}
        };
        double[] vectorY = {from.getY(), mid.getY(), to.getY()};
        RealMatrix A = new Array2DRowRealMatrix(matrixT);
        RealVector b = new ArrayRealVector(vectorY);
        DecompositionSolver solver = new LUDecomposition(A).getSolver();
        return solver.solve(b);
    }
}