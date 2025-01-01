package me.bright.entity;

import me.bright.brightrpg.BrightRPG;
import me.bright.brightrpg.BrightStat;
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
                maxHp = getStatFromCache(BrightStat.MAX_HP);
        if (currentHp == maxHp) return;
        healSelf(maxHp / 100);
    }

    public void naturalManaRegenTick() {
        double currentMana = getCurrentMana(),
                maxMana = getStatFromCache(BrightStat.INTELLIGENCE);
        if (currentMana == maxMana) return;
        setCurrentMana(currentMana + maxMana * 2 / 100);
    }

    public void sendActionBar() {
        long tempHp = (long) getTempHp(),
                currentHp = (long) getCurrentHp(),
                maxHp = (long) getStatFromCache(BrightStat.MAX_HP),
                armor = (long) getStatFromCache(BrightStat.ARMOR),
                magicResist = (long) getStatFromCache(BrightStat.MAGIC_RESIST),
                currentMana = (long) getCurrentMana(),
                maxMana = (long) getStatFromCache(BrightStat.INTELLIGENCE);
        StringBuilder actionBar = new StringBuilder();
        if (tempHp > 0) {
            actionBar.append(BrightStat.TEMP_HP.color).append(tempHp + currentHp);
        } else {
            actionBar.append(BrightStat.CURRENT_HP.color).append(currentHp);
        }
        actionBar.append(ChatColor.GRAY).append("/")
                .append(BrightStat.MAX_HP.color).append(maxHp)
                .append(ChatColor.RED).append(BrightStat.MAX_HP.displayIcon)
                .append(ChatColor.RESET).append("       ")
                .append(BrightStat.ARMOR.color).append(armor)
                .append(ChatColor.GRAY).append("/")
                .append(BrightStat.MAGIC_RESIST.color).append(magicResist)
                .append(ChatColor.RESET).append("       ")
                .append(BrightStat.CURRENT_MANA.color).append(currentMana)
                .append(ChatColor.GRAY).append("/")
                .append(BrightStat.INTELLIGENCE.color).append(maxMana).append("✎");
        BaseComponent message = TextComponent.fromLegacy(actionBar.toString());
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
