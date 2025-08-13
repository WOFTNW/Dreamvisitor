package org.woftnw.dreamvisitor.functions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.woftnw.dreamvisitor.data.PlayerUtility;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Container;
import org.bukkit.block.DecoratedPot;
import org.bukkit.block.EnderChest;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.jetbrains.annotations.NotNull;
import org.woftnw.dreamvisitor.data.type.DVUser;

import java.util.Objects;

public class Sandbox implements Listener {

    public static final String[] DISALLOWED_COMMANDS = {"tpaccept", "tpa", "hub", "etpa", "etpaccept", "home", "ehome"};

    /**
     * Enable sandbox mode for the given {@link Player}. If they are not already in sandbox mode, they will be put into creative mode and their inventory will be swapped.
     *
     * @param player the player to enable sandbox mode for.
     */
    public static void enableSandbox(@NotNull Player player) {
        DVUser user = PlayerUtility.getUser(player);

        if (user.isInSandboxMode()) return;

        user.setInSandboxMode(true);
        InvSwap.swapInventories(player);
        player.setGameMode(GameMode.CREATIVE);
        player.setGlowing(true);

        Component message = Component.text("You are now in sandbox mode.\n").decorate(TextDecoration.BOLD)
                .append(Component.text("""
                        A staff member put you into sandbox mode. You are now in creative mode. \
                        Your inventory has been cleared and stored for later restore. \
                        In sandbox mode, the following limitations are imposed:
                        - You cannot access containers.
                        - You cannot drop items.
                        - You cannot use spawn eggs.
                        - You cannot teleport.
                        Please notify a staff member if you require assistance with any of these rules."""));
        player.sendMessage(message);

    }

    /**
     * Disable sandbox mode for the given {@link Player}. If they are still in sandbox mode, they will be put into survival mode and their inventory will be swapped.
     *
     * @param player the player to disable sandbox mode for.
     */
    public static void disableSandbox(@NotNull Player player) {
        DVUser user = PlayerUtility.getUser(player);

        if (!user.isInSandboxMode()) return;

        user.setInSandboxMode(false);
        InvSwap.swapInventories(player);
        player.setGameMode(GameMode.SURVIVAL);
        player.setGlowing(false);

        player.sendMessage(Component.text("You are no longer in sandbox mode.").decorate(TextDecoration.BOLD));
    }


}
