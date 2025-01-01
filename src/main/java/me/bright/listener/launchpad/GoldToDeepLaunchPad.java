package me.bright.listener.launchpad;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

public class GoldToDeepLaunchPad extends LaunchPad {

    public GoldToDeepLaunchPad() {
        super(new BoundingBox(-6, 72, -412, -9, 68, -413),
                new Location(HUB, -7, 70, -414.5),
                new Location(HUB, -9.5, 170, -469),
                new Location(HUB, -12, 158, -523.5, 180, 0));
    }
}
