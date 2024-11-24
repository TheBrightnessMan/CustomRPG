package me.bright.brightrpg;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

public class CustomRecipeListener implements Listener {

    private final ItemStack[] matrix;
    private final ItemStack result;

    CustomRecipeListener(ItemStack[] matrix, ItemStack result) {
        this.matrix = matrix;
        this.result = result;
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent event) {
        CraftingInventory table = event.getInventory();
        ItemStack[] tableMatrix = table.getMatrix();
        if (matrix.length != 9) {
            return;
        }
        for (int i = 0; i < 9; i++) {
            ItemStack tableItem = tableMatrix[i];
            ItemStack expectedItem = matrix[i];
            if (tableItem == null && expectedItem == null) continue;
            if (tableItem == null ^ expectedItem == null) return;
            if (!tableItem.equals(expectedItem)) return;
        }
        table.setResult(result);
    }
}
