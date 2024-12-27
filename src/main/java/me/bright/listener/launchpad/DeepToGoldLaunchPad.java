package me.bright.listener.launchpad;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

public class DeepToGoldLaunchPad extends LaunchPad {

    public DeepToGoldLaunchPad() {
        super(new BoundingBox(-14, 161, -520, -11, 157, -519),
                new Location(HUB, -12, 159, -516.5),
                new Location(HUB, -9.5, 100, -462),
                new Location(HUB, -7, 69, -408.5, 50, 0),
                2.45);
    }
}
