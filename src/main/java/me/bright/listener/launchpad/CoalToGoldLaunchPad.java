package me.bright.listener.launchpad;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

public class CoalToGoldLaunchPad extends LaunchPad {

    public CoalToGoldLaunchPad() {
        super(new BoundingBox(-9, 68, -231, -11, 64, -232),
                new Location(HUB, -9.5, 65, -233.5),
                new Location(HUB, -6.75, 80, 259.5),
                new Location(HUB, -4.5, 75, -288.5, 180, 0));
    }
}
