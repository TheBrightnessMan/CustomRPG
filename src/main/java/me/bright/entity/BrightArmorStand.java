package me.bright.entity;

import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R1.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BrightArmorStand extends ArmorStand {


    public BrightArmorStand(@NotNull Location location) {
        super(((CraftWorld) Objects.requireNonNull(location.getWorld())).getHandle(),
                location.getX(), location.getY(), location.getZ());
        setInvisible(true);
        setInvisible(true);
        setSmall(true);
        setNoGravity(true);
        level().addFreshEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    public org.bukkit.entity.ArmorStand getBukkitArmorStand() {
        return (org.bukkit.entity.ArmorStand) super.getBukkitEntity();
    }

    @Override
    public void travel(Vec3 vec3) {
        if (!isNoGravity()) {
            super.travel(vec3);
        } else {
            move(MoverType.SELF, vec3);
        }
    }


}
