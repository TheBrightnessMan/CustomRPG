package me.bright.entity;

import me.bright.brightrpg.BrightRPG;
import me.bright.brightrpg.BrightStats;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BrightPlayer extends BrightEntity {

    private final @NotNull Player player;

    public BrightPlayer(@NotNull Player player) {
        super(player);
        this.player = player;
    }

    public static BrightPlayer fromBukkitPlayer(Player player) {
        for (BrightPlayer brightPlayer : BrightRPG.getPlayers()) {
            if (brightPlayer.getPlayer().getUniqueId().equals(player.getUniqueId())) return brightPlayer;
        }
        return null;
    }

    public void naturalHpRegenTick() {
        double currentHp = getCurrentHp(),
                maxHp = getStatFromCache(BrightStats.MAX_HP);
        if (currentHp == maxHp) return;
        healSelf(maxHp / 100);
    }

    public void naturalManaRegenTick() {
        double currentMana = getCurrentMana(),
                maxMana = getStatFromCache(BrightStats.INTELLIGENCE);
        if (currentMana == maxMana) return;
        setCurrentMana(currentMana + maxMana * 2 / 100);
    }

    public void sendActionBar() {
        long tempHp = (long) getTempHp(),
                currentHp = (long) getCurrentHp(),
                maxHp = (long) getStatFromCache(BrightStats.MAX_HP),
                armor = (long) getStatFromCache(BrightStats.ARMOR),
                magicResist = (long) getStatFromCache(BrightStats.MAGIC_RESIST),
                currentMana = (long) getCurrentMana(),
                maxMana = (long) getStatFromCache(BrightStats.INTELLIGENCE);
        StringBuilder hpDisplay = new StringBuilder();
        if (tempHp > 0)
            hpDisplay.append(BrightStats.TEMP_HP.color).append(tempHp + currentHp);
        else
            hpDisplay.append(BrightStats.CURRENT_HP.color).append(currentHp);
        hpDisplay.append(ChatColor.GRAY).append("/")
                .append(BrightStats.MAX_HP.color).append(maxHp).
                append(ChatColor.RED).append("♥");
        BaseComponent message = TextComponent.fromLegacy(hpDisplay.toString() +
                ChatColor.RESET + "       " +
                BrightStats.ARMOR.color + armor +
                ChatColor.GRAY + "/" +
                BrightStats.MAGIC_RESIST.color + magicResist +
                ChatColor.RESET + "       " +
                BrightStats.CURRENT_MANA.color + currentMana +
                ChatColor.GRAY + "/" +
                BrightStats.INTELLIGENCE.color + maxMana + "✎");
        player.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, message);
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BrightPlayer brightPlayer)) return false;
        return getPlayer().getUniqueId().equals(brightPlayer.getPlayer().getUniqueId());
    }
}
