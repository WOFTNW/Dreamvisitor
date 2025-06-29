package org.woftnw.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import org.woftnw.dreamvisitor.functions.Messager;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CmdUnwax implements DVCommand {

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("unwax")
                .withPermission(CommandPermission.fromString("dreamvisitor.unwax"))
                .withHelp("Unwax a sign.", "Unwax the sign you are looking at")
                .executesNative((sender, args) -> {
                    if (sender.getCallee() instanceof Player player) {
                        Block targetBlock = player.getTargetBlockExact(10, FluidCollisionMode.NEVER);
                        if (targetBlock == null) throw CommandAPI.failWithString("No nearby sign in line of sight!");

                        if (targetBlock.getState() instanceof Sign sign) {
                            sign.setWaxed(false);
                            sign.update(false);
                            sign.getWorld().spawnParticle(Particle.WAX_OFF, sign.getLocation().add(0.5, 0.5, 0.5), 5, 0.2, 0.2, 0.2);
                            Messager.send(sender, "Wax, be gone!");
                        } else throw CommandAPI.failWithString("That is not a sign.");
                    } else throw CommandAPI.failWithString("This command must be executed as a player!");

                });
    }
}
