package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import io.github.stonley890.dreamvisitor.functions.ItemBanList;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CmdItemBanList implements DVCommand {

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("itembanlist")
                .withPermission(CommandPermission.fromString("dreamvisitor.itembanlist.managelp "))
                .withHelp("Manage the item ban list.", "Open the item ban list inventory GUI.")
                .withSubcommands(
                        new CommandAPICommand("data").executesNative(((sender, args) -> {
                            CommandSender callee = sender.getCallee();
                            if (callee instanceof Player player) {
                                if (ItemBanList.badItemsComponents != null) {
                                    ItemBanList.componentsInv.setContents(ItemBanList.badItemsComponents);
                                }
                                player.openInventory(ItemBanList.componentsInv);
                            } else throw CommandAPI.failWithString("This command must be executed as a player!");

                        })),
                        new CommandAPICommand("dataless").executesNative(((sender, args) -> {
                            CommandSender callee = sender.getCallee();
                            if (callee instanceof Player player) {
                                if (ItemBanList.badItemsComponentless != null) {
                                    ItemBanList.componentlessInv.setContents(ItemBanList.badItemsComponentless);
                                }
                                player.openInventory(ItemBanList.componentlessInv);
                            } else throw CommandAPI.failWithString("This command must be executed as a player!");
                        }))
                );

    }
}
