package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.InvTemplate;
import io.github.stonley890.dreamvisitor.functions.InvTemplates;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class CmdInvTemplate implements DVCommand {

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("invtemplate")
                .withHelp("Manage inventory templates.", "Manage inventory templates.")
                .withPermission(CommandPermission.fromString("dreamvisitor.invtemplate"))
                .withSubcommands(
                        new CommandAPICommand("new")
                                .withArguments(new StringArgument("name"))
                                .executesPlayer((sender, args) -> {
                                    // Get name arg
                                    String name = (String) args.get("name");
                                    assert name != null;

                                    // Fail if a template by that name exists already.
                                    InvTemplate existingTemplate = InvTemplates.getInvTemplateByName(name);
                                    if (existingTemplate != null) throw CommandAPI.failWithString("A template by that name already exists. Try /invtemplate overwrite.");

                                    // Save the player's inventory as a template with that name.
                                    InvTemplates.saveInvTemplate(new InvTemplate(sender.getInventory().getContents(), name));
                                    sender.sendMessage(Dreamvisitor.PREFIX + "Saved your current inventory as template " + name + ".");
                                })
                                .executes((sender, args) -> {
                                    sender.sendMessage(Dreamvisitor.PREFIX + "You must execute this as a player.");
                                }),
                        new CommandAPICommand("overwrite")
                                .withArguments(new StringArgument("name").includeSuggestions(getTemplateSuggestions()))
                                .executesPlayer((sender, args) -> {
                                    // Get name arg
                                    String name = (String) args.get("name");
                                    assert name != null;

                                    // Fail if a template by that name does not exist.
                                    InvTemplate existingTemplate = InvTemplates.getInvTemplateByName(name);
                                    if (existingTemplate == null) throw CommandAPI.failWithString("No template by that name exists. Try /invtemplate new.");

                                    // Save the player's inventory to the template with that name.
                                    existingTemplate.setContents(sender.getInventory().getContents());
                                    InvTemplates.saveInvTemplate(existingTemplate);
                                    sender.sendMessage(Dreamvisitor.PREFIX + "Saved your current inventory to template " + name + ".");
                                })
                                .executes((sender, args) -> {
                                    sender.sendMessage(Dreamvisitor.PREFIX + "You must execute this as a player.");
                                }),
                        new CommandAPICommand("remove")
                                .withArguments(new StringArgument("name").includeSuggestions(getTemplateSuggestions()))
                                .executesPlayer((sender, args) -> {
                                    // Get name arg
                                    String name = (String) args.get("name");
                                    assert name != null;

                                    // Fail if template by that name does not exist.
                                    InvTemplate existingTemplate = InvTemplates.getInvTemplateByName(name);
                                    if (existingTemplate == null) {
                                        throw CommandAPI.failWithString("No template by that name exists.");
                                    }

                                    // Remove template
                                    InvTemplates.removeInvTemplate(existingTemplate);
                                    sender.sendMessage(Dreamvisitor.PREFIX + "Removed template " + name + ".");
                                })
                                .executes((sender, args) -> {
                                    sender.sendMessage(Dreamvisitor.PREFIX + "You must execute this as a player.");
                                }),
                        new CommandAPICommand("list")
                                .executes((sender, args) -> {
                                    List<InvTemplate> templates = InvTemplates.getTemplates();
                                    if (templates.isEmpty()) {
                                        sender.sendMessage(Dreamvisitor.PREFIX + "There are no inventory templates.");
                                        return;
                                    }
                                    ComponentBuilder message = new ComponentBuilder(Dreamvisitor.PREFIX).append("There are ").append(String.valueOf(templates.size())).append(" templates:\n");
                                    for (InvTemplate template : templates) {
                                        ComponentBuilder hoverText = new ComponentBuilder();
                                        int index = -1;
                                        for (int i = 0; i < 10; i++) {
                                            index++;
                                            ItemStack item;
                                            try {
                                                item = template.getContents()[index];
                                            } catch (IndexOutOfBoundsException ignored) {
                                                break;
                                            }
                                            if (item == null) {
                                                i--;
                                                continue;
                                            }
                                            hoverText.append(String.valueOf(item.getAmount())).append(" ").append(new TranslatableComponent(item.getType().getItemTranslationKey()));
                                            hoverText.append(", ");
                                        }
                                        hoverText.removeComponent(hoverText.getCursor());
                                        BaseComponent[] component = {hoverText.build()};
                                        message.append(template.getName()).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, component)).append(", ");
                                    }
                                    message.removeComponent(message.getCursor());
                                    sender.spigot().sendMessage(message.build());
                                }),
                        new CommandAPICommand("apply")
                                .withArguments(
                                        new EntitySelectorArgument.ManyPlayers("players"),
                                        new StringArgument("template").includeSuggestions(getTemplateSuggestions())
                                )
                                .withOptionalArguments(
                                        new BooleanArgument("overwrite")
                                )
                                .executesNative((sender, args) -> {
                                    // Get args
                                    String templateName = (String) args.get("template");
                                    assert templateName != null;
                                    Collection<Player> players = (Collection<Player>) args.get("players");
                                    assert players != null;
                                    Boolean overwrite = (Boolean) args.get("overwrite");
                                    if (overwrite == null) overwrite = false;

                                    // Fail if no players
                                    if (players.isEmpty()) throw CommandAPI.failWithString("No players selected.");

                                    // Fail if template by that name does not exist
                                    InvTemplate template = InvTemplates.getInvTemplateByName(templateName);
                                    if (template == null) throw CommandAPI.failWithString("No template by that name exists.");

                                    // Run this for each player
                                    InvTemplates.applyToPlayers(players, template, overwrite);

                                    sender.sendMessage(Dreamvisitor.PREFIX + "Applied template to " + players.size() + " player(s).");
                                }),
                        new CommandAPICommand("unapply")
                                .withArguments(new EntitySelectorArgument.ManyPlayers("players"))
                                .executesNative((sender, args) -> {
                                    // Get players arg
                                    Collection<Player> players = (Collection<Player>) args.get("players");
                                    assert players != null;

                                    // Fail if no players
                                    if (players.isEmpty()) throw CommandAPI.failWithString("No players selected.");

                                    int playersUnapplied = InvTemplates.unapplyPlayers(players);

                                    sender.sendMessage(Dreamvisitor.PREFIX + "Unapplied template to " + playersUnapplied + " player(s).");
                                })
                );
    }

    @NotNull
    private static ArgumentSuggestions<CommandSender> getTemplateSuggestions() {
        return ArgumentSuggestions.strings(InvTemplates.getTemplates().stream().map(InvTemplate::getName).toList());
    }

}
