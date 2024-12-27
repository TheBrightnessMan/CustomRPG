package me.bright.listener;

import me.bright.itemNSpell.main.BrightItem;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

public class ItemConverter implements Listener {

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        Item item = event.getItem();
        BrightItem brightItem = BrightItem.fromItemStack(item.getItemStack());
        if (brightItem == null) return;
        brightItem.getItemStack().setAmount(item.getItemStack().getAmount());
        event.getItem().setItemStack(brightItem.buildItem());
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent event) {
        CraftingInventory table = event.getInventory();
        ItemStack result = table.getResult();
        if (result == null) return;
        BrightItem brightItem = BrightItem.fromItemStack(result);
        if (brightItem == null) return;
        ItemStack finalItem = brightItem.buildItem();
        finalItem.setAmount(result.getAmount());
        table.setResult(finalItem);
    }
}
