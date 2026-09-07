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

    public static boolean isItemBanned(@NotNull ItemStack item, @NotNull ItemStack[] banList, boolean ignoreData) {
        for(ItemStack bannedItem : banList) {
            if(ignoreData) {
                if(item.getType() != bannedItem.getType()) continue;
            } else if(!item.isSimilar(bannedItem)) continue;
            return true;
        }
        return false;
    }

    private static void removeItems(Player player, @NotNull ItemStack[] items, boolean ignoreData) {
        for (ItemStack content : player.getInventory().getContents()) {
            if (content == null) continue;
            Dreamvisitor.debug("Checking player item " + content.getType());
            Dreamvisitor.debug("Ignore data? " + ignoreData);
            if(isItemBanned(content, items, ignoreData)) {
                player.getInventory().remove(content);
                Bot.sendLog("Removed " + content.getType().name() + " (" + Objects.requireNonNull(content.getItemMeta()).getDisplayName() + ") from " + player.getName());
            }
        }
    }



}
