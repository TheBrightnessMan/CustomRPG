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

        BrightItem[] customItems = BrightItems.items;
        Inventory inventory = Bukkit.createInventory(null,
                roundUp(customItems.length, 9), "Custom Items");
        for (BrightItem brightItem : customItems) {
            inventory.addItem(brightItem.buildItem());
        }
        sender.openInventory(inventory);
        return true;
    }

    private int roundUp(double toRound, int multipleOf) {
        return (int) (Math.ceil(toRound / multipleOf) * multipleOf);
    }
}
