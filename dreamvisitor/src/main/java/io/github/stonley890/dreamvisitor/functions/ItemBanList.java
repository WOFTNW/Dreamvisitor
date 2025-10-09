package io.github.stonley890.dreamvisitor.functions;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ItemBanList implements Listener {
    public static final Inventory componentsInv = Bukkit.createInventory(null, 27, "Banned Items (Including data)");
    public static final Inventory componentlessInv = Bukkit.createInventory(null, 27, "Banned Items (Not including data)");
    public static ItemStack[] badItemsComponents;
    public static ItemStack[] badItemsComponentless;

    public static void saveItems() {
        Dreamvisitor plugin = Dreamvisitor.getPlugin();
        badItemsComponents = componentsInv.getContents();
        badItemsComponentless = componentlessInv.getContents();
        plugin.getConfig().set("itemBlacklist", badItemsComponents);
        plugin.getConfig().set("datalessItemBlacklist", badItemsComponentless);
        plugin.saveConfig();
    }

    @EventHandler
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        checkBannedItems(player);

        if (event.getInventory().equals(ItemBanList.componentsInv) || event.getInventory().equals(ItemBanList.componentlessInv)) {
            ItemBanList.saveItems();
        }
    }

    public static void checkBannedItems(@NotNull Player player) {
        if (!player.hasPermission("dreamvisitor.itembanlist.bypass")) {
            if (ItemBanList.badItemsComponents != null) removeItems(player, badItemsComponents, false);
            if (ItemBanList.badItemsComponentless != null) removeItems(player, badItemsComponentless, true);
        }
    }

    private static void removeItems(Player player, @NotNull ItemStack[] items, boolean ignoreData) {
        for (ItemStack item : items) {
            if (item == null) continue;
            Dreamvisitor.debug("Checking against item " + item.getType());
            for (ItemStack content : player.getInventory().getContents()) {
                if (content == null) continue;
                Dreamvisitor.debug("Checking player item " + content.getType());
                Dreamvisitor.debug("Ignore data? " + ignoreData);
                if (ignoreData) {
                    Dreamvisitor.debug("Types:" + content.getType() + " and " + item.getType());
                    if (content.getType() != item.getType()) continue;
                } else if (!content.isSimilar(item)) continue;
                player.getInventory().remove(content);
                Bot.sendLog("Removed " + item.getType().name() + " (" + Objects.requireNonNull(item.getItemMeta()).getDisplayName() + ") from " + player.getName());
            }
        }
    }



}
