package me.bright.entity;

import org.bukkit.entity.Player;

public class BrightPlayer extends BrightEntity {

    private final Player player;

    public BrightPlayer(Player player) {
        super(player);
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BrightPlayer)) return false;
        return player.getUniqueId().equals(
                ((BrightPlayer) obj).getLivingEntity().getUniqueId()
        );
    }

}
