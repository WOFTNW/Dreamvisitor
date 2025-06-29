package io.github.stonley890.dreamvisitor.functions;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ItemBanList implements Listener {
    public static final Inventory inv = Bukkit.createInventory(null, 27, "Banned Items");
    public static List<ItemStack> badItems;

    static final File file = new File(Dreamvisitor.getPlugin().getDataFolder().getPath() + "/bannedItems.yml");
    static FileConfiguration config = YamlConfiguration.loadConfiguration(file);

    public static void init() throws IOException {
        // If the file does not exist, create one
        if (!file.exists()) {
            Messager.debug(file.getName() + " does not exist. Creating one now...");
            try {
                if (!file.createNewFile())
                    throw new IOException("The existence of " + file.getName() + " cannot be verified!", null);
            } catch (IOException e) {
                throw new IOException("Dreamvisitor tried to create " + file.getName() + ", but it cannot be read/written! Does the server have read/write access?", e);
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        try {
            badItems = (List<ItemStack>) config.getList("items", new ArrayList<>());
        } catch (Exception e) {
            Dreamvisitor.getPlugin().getLogger().warning("Unable to restore item ban list.");
            badItems = new ArrayList<>();
        }
    }

    public static void saveItems() {
        badItems = List.of(inv.getContents());
        config.set("items", badItems);
        try {
            config.save(file);
        } catch (IOException e) {
            Dreamvisitor.getPlugin().getLogger().warning("Unable to save to " + file.getName() + "!");
        }
    }

    @EventHandler
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        if (!player.isOp() && ItemBanList.badItems != null) {

            for (ItemStack item : ItemBanList.badItems) {
                if (item == null) continue;
                for (ItemStack content : player.getInventory().getContents()) {
                    if (content == null || !content.isSimilar(item)) continue;
                    player.getInventory().remove(item);
                    Dreamvisitor.getPlugin().getLogger().info("Removed " + item.getType().name() + " (" + Objects.requireNonNull(item.getItemMeta()).getDisplayName() + ") from " + player.getName());
                }
            }
        }

        if (event.getInventory().equals(ItemBanList.inv)) {
            ItemBanList.saveItems();
        }
    }

}
