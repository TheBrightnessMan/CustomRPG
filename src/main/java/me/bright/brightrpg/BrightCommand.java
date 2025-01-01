package me.bright.brightrpg;

import me.bright.itemNSpell.main.BrightItem;
import me.bright.itemNSpell.main.BrightItems;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class BrightCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player sender)) return true;
        if (!command.getName().equalsIgnoreCase("brightRPG")) return true;
        if (!sender.hasPermission("brightrpg.all")) return true;

        var customItems = BrightItems.keyItemEntries;
        Inventory inventory = Bukkit.createInventory(null,
                roundUp(customItems.size(), 9), "Custom Items");
        for (var key : customItems.keySet()) {
            BrightItem item = BrightItems.getCustomItem(key);
            if (item == null) {
                continue;
            }
            inventory.addItem(item.buildItem());
        }
        sender.openInventory(inventory);
        return true;
    }

    private int roundUp(double toRound, int multipleOf) {
        return (int) (Math.ceil(toRound / multipleOf) * multipleOf);
    }
}
