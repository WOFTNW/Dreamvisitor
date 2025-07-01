package org.woftnw.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import org.woftnw.dreamvisitor.functions.ItemBanList;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CmdItemBanList implements DVCommand {

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("itembanlist")
                .withPermission(CommandPermission.fromString("dreamvisitor.itembanlist"))
                .withHelp("Manage the item ban list.", "Open the item ban list inventory GUI.")
                .executesNative(((sender, args) -> {
                    final CommandSender callee = sender.getCallee();
                    if (callee instanceof Player player) {
                        if (ItemBanList.badItems != null) {
                            ItemBanList.inv.setContents(ItemBanList.badItems.toArray(new ItemStack[0]));
                        }
                        player.openInventory(ItemBanList.inv);
                    } else throw CommandAPI.failWithString("This command must be executed as a player!");

                }));
    }
}
