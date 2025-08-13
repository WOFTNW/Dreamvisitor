package org.woftnw.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.*;
import org.woftnw.dreamvisitor.functions.Messager;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class CmdVelocity implements DVCommand {

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("velocity")
                .withPermission(CommandPermission.fromString("dreamvisitor.velocity"))
                .withHelp("Apply velocity to entities.", "Apply velocity to entities.")
                .withArguments(
                        new EntitySelectorArgument.ManyEntities("entities"),
                        new LocationArgument("force", LocationType.PRECISE_POSITION, false)
                )
                .withOptionalArguments(
                        new BooleanArgument("replace")
                )
                .executesNative((sender, args) -> {

                    Collection<Entity> entities = (Collection<Entity>) args.get("entities");
                    Location force = (Location) args.get("force");
                    @Nullable Boolean replace = (Boolean) args.get("replace");
                    if (replace == null) replace = false;

                    if (entities == null || entities.isEmpty()) {
                        throw CommandAPI.failWithString("No entities were selected.");
                    }
                    if (force == null) {
                        throw CommandAPI.failWithString("No force was provided.");
                    }

                    for (Entity entity : entities) {
                        if (replace) {
                            entity.setVelocity(force.toVector());
                        } else {
                            entity.setVelocity(entity.getVelocity().add(force.toVector()));
                        }
                    }
                    Messager.send(sender, "Applied velocity to " + Messager.nameOrCountEntity(entities) + ".");

                });
    }
}
