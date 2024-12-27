package me.bright.listener.launchpad;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

public class GoldToCoalLaunchPad extends LaunchPad {

    public GoldToCoalLaunchPad() {
        super(new BoundingBox(-4, 78, -286, -6, 74, -285),
                new Location(HUB, -4.5, 75, -282.5),
                new Location(HUB, -6.75, 75, -255),
                new Location(HUB, -9.5, 65, -227.5));
    }
}
