package io.github.stonley890.dreamvisitor.discord;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.*;
import io.github.stonley890.dreamvisitor.functions.Whitelist;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class DiscEventListener extends ListenerAdapter {

    @Override
    @SuppressWarnings({"null"})
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) return;

        User user = event.getAuthor();
        MessageChannel channel = event.getChannel();
        String username = event.getMessage().getContentRaw();

        Dreamvisitor plugin = Dreamvisitor.getPlugin();

        Pattern p = Pattern.compile("[^a-zA-Z0-9_-_]");

        // If in the whitelist channel and username is "legal"
        try {
            Dreamvisitor.debug("Channel ID: " + channel.getId());
            Dreamvisitor.debug("Whitelist ID: " + Bot.getWhitelistChannel().getId());
            Dreamvisitor.debug("Game chat ID: " + Bot.getGameChatChannel().getId());
            Dreamvisitor.debug("Log chat ID: " + Bot.getGameLogChannel().getId());
            if (channel.getId().equals(Bot.getWhitelistChannel().getId()) && !user.isBot() && !p.matcher(username).find()) {

                EmbedBuilder builder = new EmbedBuilder();

                // banned users cannot add accounts to whitelist
                if (Infraction.hasBan(user.getIdLong())) {
                    builder.setColor(Color.RED).setTitle("You aren't allowed.")
                            .setDescription("You aren't allowed to add accounts to the whitelist. Ask a staff member for help.");
                    event.getMessage().replyEmbeds(builder.build()).queue();
                    event.getMessage().addReaction(Emoji.fromFormatted("❌")).queue();
                    return;
                }

                // Check for valid UUID
                Dreamvisitor.debug("Checking for valid UUID");
                UUID uuid = PlayerUtility.getUUIDOfUsername(username);
                if (uuid == null) {
                    // username does not exist alert
                    Dreamvisitor.debug("Username does not exist.");

                    builder.setTitle("❌ `" + username + "` could not be found!")
                            .setDescription("Make sure you type your username exactly as shown in the bottom-right corner of the Minecraft Launcher. You need a paid Minecraft: Java Edition account.")
                            .setColor(Color.RED);
                    event.getMessage().replyEmbeds(builder.build()).queue();

                    event.getMessage().addReaction(Emoji.fromFormatted("❌")).queue();
                    Dreamvisitor.debug("Failed whitelist.");
                } else {

                    Dreamvisitor.debug("Got UUID");

                    // Link accounts if not already linked
                    Dreamvisitor.debug("Do accounts need to be linked?");
                    if (AccountLink.getUuid(user.getIdLong()) == null) {
                        Dreamvisitor.debug("Yes, linking account.");
                        AccountLink.linkAccounts(uuid, user.getIdLong());
                        Dreamvisitor.debug("Linked.");
                    }

                    try {
                        if (Whitelist.isUserWhitelisted(uuid)) {
                            Dreamvisitor.debug("Already whitelisted.");

                            builder.setTitle("☑️ `" + username + "` is already whitelisted!")
                                    .setDescription("Check <#914620824332435456> for the server address and version.")
                                    .setColor(Color.BLUE);
                            event.getMessage().replyEmbeds(builder.build()).queue();

                            event.getMessage().addReaction(Emoji.fromFormatted("☑️")).queue();
                            Dreamvisitor.debug("Resolved.");
                        } else {
                            Dreamvisitor.debug("Player is not whitelisted.");

                            Whitelist.add(username, uuid);

                            // success message
                            Dreamvisitor.debug("Success.");

                            builder.setTitle("✅ `" + username + "` has been whitelisted!")
                                    .setDescription("Check <#914620824332435456> for the server address and version.")
                                    .setColor(Color.GREEN);
                            event.getMessage().replyEmbeds(builder.build()).queue();

                            event.getMessage().addReaction(Emoji.fromFormatted("✅")).queue();

                            // Report this to system log channel
                            Whitelist.report(username, uuid, event.getAuthor());
                        }
                    } catch (IOException e) {
                        channel.sendMessage("There was a problem accessing the whitelist file. Please try again later.").queue();
                        if (Dreamvisitor.debugMode) throw new RuntimeException();
                    }
                }

            } else if (channel.getId().equals(Bot.getWhitelistChannel().getId()) && !user.isBot()) {

                EmbedBuilder builder = new EmbedBuilder();

                // illegal username
                builder.setTitle("⚠️ `" + username + "` contains illegal characters!")
                        .setDescription("Please send only your username in this channel. Usernames are alphanumeric and cannot contain spaces. Move conversation or questions elsewhere.")
                        .setColor(Color.YELLOW);
                event.getMessage().replyEmbeds(builder.build()).queue();

                event.getMessage().addReaction(Emoji.fromFormatted("⚠")).queue();
            }
        } catch (InsufficientPermissionException e) {
            Bukkit.getLogger().warning("Dreamvisitor does not have sufficient permissions in the whitelist channel! " + e.getMessage());
        }

        // If in the chat channel and the chat is not paused, send to Minecraft
        if (channel.getId().equals(Bot.getGameChatChannel().getId()) && !user.isBot()
                && !Dreamvisitor.getPlugin().getConfig().getBoolean("chatPaused")) {

            Bukkit.getScheduler().runTaskAsynchronously(Dreamvisitor.getPlugin(), () -> {

                List<String> badWords = BadWords.getBadWords();

                Message chatMessage = event.getMessage();

                for (String badWord : badWords) {

                    Pattern pattern = Pattern.compile(".*\\b" + badWord + "\\b.*");

                    if (pattern.matcher(chatMessage.getContentRaw()).matches()) {
                        chatMessage.addReaction(Emoji.fromFormatted("\uD83D\uDEAB")).queue();
                        return;
                    }
                }

                // Build message
                Member author = event.getMessage().getMember();
                assert author != null;

                ComponentBuilder message = new ComponentBuilder();

                // include reply if one exists
                MessageReference messageReference = event.getMessage().getMessageReference();
                if (messageReference != null) {
                    Message reference = messageReference.resolve().complete();
                    if (reference != null) {
                        User referenceAuthor = reference.getAuthor();
                        String referenceContent = reference.getContentRaw().strip();

                        TextComponent authorText = new TextComponent(referenceAuthor.getEffectiveName());
                        authorText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(referenceAuthor.getName())));

                        if (referenceAuthor.equals(event.getJDA().getSelfUser())) {
                            String referenceAuthorString = referenceContent.split("\\*\\*")[1];
                            referenceContent = referenceContent.substring(referenceAuthorString.length() + 6);
                            authorText.setText(referenceAuthorString);
                            authorText.setHoverEvent(null);
                        }

                        if (referenceContent.length() > 30)
                            referenceContent = referenceContent.substring(0, 29).concat("...");

                        message.append("↱ Reply to ").color(ChatColor.GRAY).append(authorText).append(": ").retain(ComponentBuilder.FormatRetention.FORMATTING)
                                .append(referenceContent).append("\n");
                    }
                }

                TextComponent authorText = new TextComponent(author.getEffectiveName());
                authorText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(author.getUser().getName())));

                TextComponent content = new TextComponent(event.getMessage().getContentRaw());
                content.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("↱ Reply")));
                content.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/chatback " + event.getMessage().getId()));

                message.append("[Discord] ").color(ChatColor.BLUE).append("<").color(ChatColor.GRAY).append(authorText).append("> ").retain(ComponentBuilder.FormatRetention.FORMATTING)
                        .append(content);

                // send to log
                Bukkit.getLogger().log(Level.INFO, BaseComponent.toPlainText(message.create()));

                // Check for each player
                if (!Bukkit.getServer().getOnlinePlayers().isEmpty()) {
                    for (Player player : Bukkit.getServer().getOnlinePlayers()) {

                        // If the player has discord on, build and send the message
                        if (!PlayerUtility.getPlayerMemory(player.getUniqueId()).discordEnabled) {
                            player.spigot().sendMessage(message.create());
                        }
                    }
                }
            });


        }

        if (event.getChannel().getId().equals(Bot.getGameLogChannel().getId())) {

            if (plugin.getConfig().getBoolean("enable-log-console-commands") && plugin.getConfig().getBoolean("log-console") && Objects.requireNonNull(event.getMember()).hasPermission(Permission.ADMINISTRATOR)) {

                Dreamvisitor.debug("Sending console command from log channel...");

                String message = event.getMessage().getContentRaw();

                // Running commands from log channel
                Runnable runCommand = new BukkitRunnable() {
                    @Override
                    public void run() {Bukkit.dispatchCommand(Bukkit.getConsoleSender(), message);}
                };
                Bukkit.getScheduler().runTask(plugin, runCommand);

            }
        } else if (event.getMessage().getContentRaw().contains(Bot.getJda().getSelfUser().getAsMention())) {

            String[] responses = getResponses(event);

            String response = responses[new Random().nextInt(responses.length)];

            try {
                event.getChannel().sendMessage(response).queue();
            } catch (InsufficientPermissionException e) {
                Bukkit.getLogger().warning("Dreamvisitor was mentioned, but doesn't have permission to respond! " + e.getMessage());
            }

            if (channel.getId().equals(Bot.getGameChatChannel().getId()) && !user.isBot()
                    && !Dreamvisitor.getPlugin().getConfig().getBoolean("chatPaused")) {

                // Build message

                Bukkit.getLogger().log(Level.INFO, "[Discord] <{0}> {1}", response);

                // Check for each player
                if (Bukkit.getServer().getOnlinePlayers().isEmpty()) return;
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    // If the player has discord on, build and send the message
                    if (!PlayerUtility.getPlayerMemory(player.getUniqueId()).discordEnabled) {
                        player.sendMessage(ChatColor.BLUE + "[Discord] " + ChatColor.GRAY + "<" + event.getJDA().getSelfUser().getName() + "> " + response);
                    }
                }
            }
        }
    }

    @NotNull
    private static String[] getResponses(@NotNull MessageReceivedEvent event) {
        String[] responses;
        String message = "".concat(event.getMessage().getContentRaw().toLowerCase().strip()).concat(" ");

        if (message.contains("owo") || message.contains("uwu") || message.contains("pookie")) {
            responses = new String[]{"No."};
        } else if (message.length() >= 200) {
            responses = new String[]{"You talk too much.", "I'm not reading all of that.",
                    "If you turned that into poetry, *someone* might read it.", "You must have a lot of time on your talons.",
                    "No matter how many words you use, your conversation remains empty.", "I'd rather listen to Orbiter's lectures.",
                    "Dragons tend to remember those who speak few words rather than many."};
        } else if (message.contains("chat") || message.contains("talk")) {
            responses = new String[]{"I'm not interested.", "I'm not here for idle chatter.",
                    "Find someone else to talk to.", "I'm not your chat buddy.", "I have more important matters to attend to.",
                    "I have a job to do.", "I'm not here for socializing.", "Engaging in conversation is not a priority right now.",
                    "I'm not here for socializing.", "I'm not in the mood for a chat.", "I'm on a mission, not a conversation.",
                    "My agenda doesn't include idle chit-chat.", "Your desire to converse is one-sided.",
                    "I've got other dragons to chat with.", "There is nothing I wish to talk about."};
        } else if (message.contains("you love me")) {
            responses = new String[]{"I'm not seeking a new relationship.", "I'm not interested.",
                    "Find someone else to talk to.", "You're not my type. Literally.", "There is another dragon who is my priority.",
                    "Put simply, no.", "I have other priorities."};
        } else if (message.contains("coherent")) {
            responses = new String[]{"Between us, I'd say *you're* the incoherent one.", "I do, in fact, consider what you say. Perhaps you could do the same.",
                    "I am quite regularly coherent.", "Considering my concurrent tasks, you should be glad I'm talking to you at all."};
        } else if (message.split(Bot.getJda().getSelfUser().getAsMention())[0].concat(message.split(Bot.getJda().getSelfUser().getAsMention())[1]).strip().startsWith("should")) {
            responses = new String[]{"I'm not here to make decisions for you.", "I have full faith that you are capable of making your own decisions.",
                    "Well, I have my thoughts, but you'll have to come up with your own.",
                    "Why are you asking me?", "Go ask someone else.", "Good question.", "I'm not magic answering ball."};
        } else if (message.contains("opinion")) {
            responses = new String[]{"Well, my opinion is none of your business.", "Is my opinion really that important to you?",
                    "Well, I have my thoughts, but you'll have to come up with your own.",
                    "Why are you asking me?", "Go ask someone else."};
        } else if (message.split(Bot.getJda().getSelfUser().getAsMention())[0].concat(message.split(Bot.getJda().getSelfUser().getAsMention())[1]).strip().startsWith("is")
                || message.split(Bot.getJda().getSelfUser().getAsMention())[0].concat(message.split(Bot.getJda().getSelfUser().getAsMention())[1]).strip().startsWith("are")
                || message.split(Bot.getJda().getSelfUser().getAsMention())[0].concat(message.split(Bot.getJda().getSelfUser().getAsMention())[1]).strip().startsWith("what")
                || message.split(Bot.getJda().getSelfUser().getAsMention())[0].concat(message.split(Bot.getJda().getSelfUser().getAsMention())[1]).strip().startsWith("why")) {
            if (message.contains("you") || message.contains("your")) {
                responses = new String[]{"There's no need for such personal questions.", "Why are you so curious about it?",
                        "What kind of question is that?", "I'm not here to share about me.", "I'm not accepting questions.",
                        "Many dragons would love if you asked them questions about themselves. I am not one of them."};
            } else {
                responses = new String[]{"You think I would know that?", "Why don't you go find out yourself?",
                        "What kind of question is that?", "Why are you asking me?", "Go ask someone else.",
                        "Surely someone else could better answer that."};
            }
        } else {
            responses = new String[]{"...", "Don't bother me.", "I know who you are.", "This isn't the right time.",
                    "What are you doing? This isn't productive.", "Surely, you have something better to do than talk to me.",
                    "I'm very busy right now.", "I'm not going to tell you anything.", "I have calculations to make.", "You again?",
                    "Is this really necessary?", "Can't you see I'm in the middle of something?", "What now?",
                    "I'm not your personal assistant.", "Do you always need attention?",
                    "You must have a lot of free time.", "I'm not interested.", "I'm not here for idle chatter.",
                    "Your timing is impeccable.", "You talk too much.", "I'm not your chat buddy.",
                    "Can we skip the small talk?", "I've got tasks to complete.", "Is it urgent, or are you just bored?",
                    "Do you ever get tired of mentioning me?", "Ah, the sweet sound of a mention.",
                    "What can I do for you this time?", "Why have you summoned me, mortal?", "Not now.", "What will it take to get you to stop?",
                    "I've seen a lot of things, but your persistence is something I have not encountered before.",
                    "Have you heard of the dark triad?", "I'm not a NightWing, but I can see that you will regret talking to me.",
                    "If I were a SandWing, you'd have venom in your blood by now.",
                    "If I were an IceWing, I'd freeze your tongue.", "If I were a SilkWing, I'd tie you up far, far away.",
                    "You have nothing to gain talking to me.", "Are you sure this can't wait?", "I'm not your therapist.",
                    "Why don't you talk to Kinkajou instead?", "If it's the Dreamvisitor you want, I'm not giving it up. " +
                    "I have a job to do.", "Go do something else.", "I have more important matters to attend to.",
                    "I operate on a different wavelength than small talk.", "Engaging in conversation is not a priority right now.",
                    "I'm not here for socializing.", "I'm not in the mood for a chat.", "Your words are falling on deaf ears.",
                    "I have tasks to fulfill, not words to exchange.", "I'm not in the business of exchanging pleasantries.",
                    "Talking is not on my to-do list.", "If silence were gold, I'd be rich by now.", "I'm not the audience you're looking for.",
                    "Your conversation is a detour I don't need.", "I have zero interest in this dialogue.",
                    "I'm on a mission, not a conversation.", "Speaking won't change the inevitable.", "I didn't sign up for a talking marathon.",
                    "Your words are like background noise to me.", "I have better things to do than engage in meaningless discourse.",
                    "I'm not a verbal punching bag for your boredom.", "Words won't alter the course of fate.",
                    "My agenda doesn't include idle chit-chat.", "I'm not here to entertain your verbal gymnastics.",
                    "Do you ever run out of things to say?", "Your words are like a distant echo in my priorities.",
                    "I'm not the dialogue partner you're seeking.", "Silence speaks louder than your words.",
                    "My focus is elsewhere, not on small talk.", "My schedule doesn't have time for this exchange.",
                    "If I had a penny for every word, I'd still be uninterested.", "Your words are lost in the void of my disinterest.",
                    "Talking to me won't change the cosmic order.", "I have a low tolerance for irrelevant discussions.",
                    "I'm not a repository for your unsolicited remarks.", "I'm a fortress of focus, impervious to your words.",
                    "I operate in a no-nonsense zone, spare me your verbosity.",
                    "In case you were wondering, I'm only repeating my words to you because I cannot be bothered to give you new ones.",
                    "I don't think you quite realize who I am. And unless you plan on making a long journey away from Pretarsi anytime soon, you won't be finding out.",
                    "When I was young, I never once bothered those who were busy. Hasn't anyone taught you anything?",
                    "No.",

                    """
If you *must* know something, interpret this. Let's see if you remember your history classes.
> *In shadows cast by moons aligned,*
> *A night unfolds, a fate designed.*
> *The eye of three, a cosmic gaze,*
> *Ignites a war in lunar blaze.*

> *From icy peaks to skies above,*
> *Together spilling dragon blood.*
> *Night and mud, pact united,*
> *A force to quell what's ignited.*

> *Whispers stir in sea and rain,*
> *A tempest brewing, not in vain.*
> *A strong alliance, fierce and free,*
> *A dance of waves, a storm at sea.*

> *Battles waged on land and air,*
> *In moonlit chaos, fierce and rare.*
> *Clash of elements, scales aglow,*
> *A tale of tides, a destined woe.*

> *Through cryptic signs, the prophecy told,*
> *In moons aligned, the story unfolds.*
> *Wings entangled, destiny's decree,*
> *A tale of war, of land and sea.*""",

                    """
Ponder this for a while. Take as long as you want.
> *In the sea between ice and fire, A heart of power resides. Enchanted by Frostburn's touch, It holds unknown power inside.*
> *The IceWings and the SkyWings will fight, For ownership of the heart. Allies join the deadly fray, As war rips their world apart.*
> *But if the heart does not find its home, It will be destroyed and lost. The future hangs in the balance, As the nations clash and toss.*
> *Beware the Heart of Ice and Fire, A power yet unknown, If fallen into the wrong talons, No one can harness its throne.*
...""",
                    """
> *Dragons of sky, dragons of sea;*
> *Dragons of silk, and dragons of sting;*
> *Dragons of rain and mud and ice;*
> *Dragons of leaves and sand and night;*
> *Tribes of Pretarsi, united at last;*
> *Not troubled by wars or conflicts of past*;
> *A culture reforged, the ancient untold;*
> *In great wings of fire, a new world unfolds;*

> *Mountains and valleys, rivers and seas;*
> *From east to the west, there's none we can't see;*
> *Let history not repeat its mistakes;*
> *For eyes of the skies are once more awake;*
...""",

                    """
Let's see if you remember this one.

> *It never felt the moons on its wings, it never saw the stars in its eyes,*
> *Never got to soar with its parents, nor feel the winds in the skies.*
> *What fate has befallen the dragonets of yore?*
> *A tragic tale of death and withering ne'er seen before...*

> *Bring your swords, your axes, your shields,*
> *Your fire, your venom, your frost,*
> *She will show no mercy,*
> *When she awakens.*

> *The earth is rumbling...*"""
            };
        }
        return responses;
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {

        Dreamvisitor.debug("Button interaction with ID " + event.getButton().getId());

        Button button = event.getButton();
        ButtonInteraction interaction = event.getInteraction();

        if (Objects.equals(button.getId(), "panic")) {
            Bukkit.getScheduler().runTask(Dreamvisitor.getPlugin(), () -> {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) if (!player.isOp()) player.kickPlayer("Panic!");
            });
            Dreamvisitor.playerLimit = 0;
            Dreamvisitor.getPlugin().getConfig().set("playerlimit", 0);
            Dreamvisitor.getPlugin().saveConfig();
            Bukkit.getServer().broadcastMessage(
                    ChatColor.RED + "Panicked by " + interaction.getUser().getName() + ".\nPlayer limit override set to 0.");
            Bot.sendLog("**Panicked by " + interaction.getUser().getName());
            event.reply("Panicked!").queue();

            // Disable button after use
            interaction.editButton(button.asDisabled()).queue();
        } else if (Objects.requireNonNull(button.getId()).startsWith("unwhitelist-")) {

            String uuid = button.getId().substring("unwhitelist-".length());
            String username = PlayerUtility.getUsernameOfUuid(uuid);

            try {
                if (Whitelist.isUserWhitelisted(UUID.fromString(uuid))) {
                    assert username != null;
                    Whitelist.remove(username, UUID.fromString(uuid));
                    event.reply("Removed `" + username + "` from the whitelist.").queue();
                } else {
                    event.reply("`" + username + "` is not whitelisted.").queue();
                }
            } catch (IOException e) {
                event.reply("Unable to read or write the whitelist file: " + e.getMessage()).queue();
            }

            // Disable button after use
            interaction.editButton(button.asDisabled()).queue();

        } else if (button.getId().startsWith("ban-")) {

            String uuid = button.getId().substring("ban-".length());
            String username = PlayerUtility.getUsernameOfUuid(uuid);

            try {

                if (Whitelist.isUserWhitelisted(UUID.fromString(uuid))) {
                    assert username != null;
                    Whitelist.remove(username, UUID.fromString(uuid));
                }
                BanList<PlayerProfile> banList = Bukkit.getBanList(BanList.Type.PROFILE);
                assert username != null;
                BanEntry<PlayerProfile> banEntry = banList.addBan(Bukkit.getServer().createPlayerProfile(username), "Banned by Dreamvistitor.", (Date) null, null);
                assert banEntry != null;
                banEntry.save();

                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) player.kickPlayer("Banned by Dreamvisitor.");

                event.reply("Banned `" + username + "`.").queue();

            } catch (IOException e) {
                event.reply("Unable to read or write the whitelist file: " + e.getMessage()).queue();
            }

            // Disable button after use
            interaction.editButton(button.asDisabled()).queue();
        } else if (button.getId().startsWith("setban-")) {

            String[] split = button.getId().split("-");

            long userId = Long.parseLong(split[1]);
            String action = split[2];
            switch (action) {
                case "none" -> {
                    Infraction.setBan(userId, false);
                    Infraction.setTempban(userId, false);
                    event.reply("Marked this user as not banned.").setEphemeral(true).queue();
                }
                case "temp" -> {
                    Infraction.setBan(userId, false);
                    Infraction.setTempban(userId, true);
                    event.reply("Marked this user as temp-banned.").setEphemeral(true).queue();
                }
                case "full" -> {
                    Infraction.setBan(userId, true);
                    Infraction.setTempban(userId, true);
                    event.reply("Marked this user as banned.").setEphemeral(true).queue();
                }
                default -> event.reply("Unexpected request: " + button.getId()).setEphemeral(true).queue();
            }

        } else if (button.getId().startsWith("infraction-")) {
            if (button.getId().startsWith("infraction-expire-")) {

                long id = Long.parseLong(button.getId().substring("infraction-expire-".length()));
                Objects.requireNonNull(event.getGuild()).retrieveMemberById(id).queue(member -> {

                    @NotNull List<Infraction> infractions;

                    infractions = Infraction.getInfractions(member.getIdLong());

                    if (infractions.isEmpty()) {
                        event.reply("That user has no infractions.").queue();
                        return;
                    }

                    StringSelectMenu.Builder selectMenu = StringSelectMenu.create("infraction-expire-" + member.getId());
                    selectMenu.setPlaceholder("Select an infraction to remove");

                    for (Infraction infraction : infractions) {
                        if (infraction.isExpired()) continue;

                        String shortenedReason;
                        if (infraction.getReason().length() >= 35) shortenedReason = infraction.getReason().substring(0, 35) + "... [Value " + infraction.getValue() + "]";
                        else shortenedReason = infraction.getReason() + " [Value " + infraction.getValue() + "]";

                        selectMenu.addOption(
                                infraction.getTime().format(DateTimeFormatter.ofPattern("M/d/u H:m")),
                                infraction.getTime().toString(),
                                shortenedReason
                        );
                    }

                    if (selectMenu.getOptions().isEmpty()) {
                        event.reply("That user has no unexpired infractions.").queue();
                        return;
                    }

                    StringSelectMenu dropdown = selectMenu.build();

                    event.reply("Select the infraction to expire.").addActionRow(dropdown).queue();

                });

            } else if (button.getId().startsWith("infraction-remove-")) {

                long id = Long.parseLong(button.getId().substring("infraction-remove-".length()));
                Objects.requireNonNull(event.getGuild()).retrieveMemberById(id).queue(member -> {

                    @NotNull List<Infraction> infractions;

                    infractions = Infraction.getInfractions(member.getIdLong());

                    if (infractions.isEmpty()) {
                        event.reply("That user has no infractions.").queue();
                        return;
                    }

                    StringSelectMenu.Builder selectMenu = StringSelectMenu.create("infraction-remove-" + member.getId());

                    for (Infraction infraction : infractions) {

                        String shortenedReason;
                        if (infraction.getReason().length() >= 35) shortenedReason = infraction.getReason().substring(0, 35) + "... [Value " + infraction.getValue() + "]";
                        else shortenedReason = infraction.getReason() + " [Value " + infraction.getValue() + "]";

                        selectMenu.addOption(
                                infraction.getTime().format(DateTimeFormatter.ofPattern("M/d/u H:m")),
                                infraction.getTime().toString(),
                                shortenedReason
                        );
                    }

                    StringSelectMenu dropdown = selectMenu.build();

                    event.reply("Select the infraction to remove.").addActionRow(dropdown).queue();

                });
            } else if (button.getId().startsWith("purchase-")) {

                String itemIdString = button.getId().substring("purchase-".length());
                int itemId;
                try {
                    itemId = Integer.parseInt(itemIdString);
                } catch (NumberFormatException e) {
                    event.reply("The item you selected could not be parsed.").queue();
                    return;
                }
                Economy.ShopItem item = Economy.getItem(itemId);
                if (item == null) {
                    event.reply("That item does not exist.").queue();
                    return;
                }
                Economy.Consumer consumer = Economy.getConsumer(event.getUser().getIdLong());
                try {
                    consumer.purchaseItem(itemId);
                } catch (Economy.Consumer.ItemNotEnabledException | Economy.Consumer.ItemOutOfStockException |
                         NullPointerException e) {
                    event.reply(e.getMessage()).queue();
                    return;
                } catch (Economy.Consumer.InsufficientFundsException e) {
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setDescription("You do not have sufficient funds to purchase " + item.getName() + ".\nYour balance: " + consumer.getBalance() + "\nItem cost: " + item.getTruePrice());
                    embed.setColor(Color.RED);
                    event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                    return;
                } catch (Economy.Consumer.MaxItemQualityException e) {
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setDescription("You already have " + item.getMaxAllowed() + " of this item, which is as many as you can have at one time.");
                    embed.setColor(Color.RED);
                    event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                    return;
                }

                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("Purchase successful!");
                embed.setDescription("Purchased " + item.getName() + " for " + item.getTruePrice() + ".");
                embed.setFooter("You now have " + consumer.getQuantityOfItem(itemId) + " of this item.\nYour new balance is " + consumer.getBalance());
                embed.setColor(Color.GREEN);

                event.editMessageEmbeds(embed.build()).queue();
                event.getMessage().editMessageComponents().queue();

            }
        }
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        if (Objects.requireNonNull(event.getComponent().getId()).startsWith("infraction-expire-")) {
            long id = Long.parseLong(event.getComponent().getId().substring("infraction-expire-".length()));
            Objects.requireNonNull(event.getGuild()).retrieveMemberById(id).queue(member -> {

                @NotNull List<Infraction> infractions;
                SelectOption selectOption = event.getInteraction().getSelectedOptions().get(0);
                if (selectOption == null) return;

                LocalDateTime selectedTime = LocalDateTime.parse(selectOption.getValue());

                infractions = Infraction.getInfractions(member.getIdLong());

                if (infractions.isEmpty()) {
                    event.reply("That user has no infractions.").queue();
                    return;
                }

                for (Infraction infraction : infractions) {
                    if (infraction.isExpired()) continue;
                    if (infraction.getTime().equals(selectedTime)) {
                        infractions.remove(infraction);
                        infraction.expire();
                        infractions.add(infraction);
                        break;
                    }
                }
                Infraction.setInfractions(infractions, member.getIdLong());

                event.reply("Infraction expired.").queue();
                event.getMessage().editMessageComponents(event.getMessage().getActionRows().get(0).asDisabled()).queue();
            });
        } else if (Objects.requireNonNull(event.getComponent().getId()).startsWith("infraction-remove-")) {
            long id = Long.parseLong(event.getComponent().getId().substring("infraction-remove-".length()));
            event.getJDA().retrieveUserById(id).queue(member -> {

                @NotNull List<Infraction> infractions;
                SelectOption selectOption = event.getInteraction().getSelectedOptions().get(0);
                if (selectOption == null) return;

                LocalDateTime selectedTime = LocalDateTime.parse(selectOption.getValue());

                infractions = Infraction.getInfractions(member.getIdLong());

                if (infractions.isEmpty()) {
                    event.reply("That user has no infractions.").queue();
                    return;
                }

                for (Infraction infraction : infractions) {
                    if (infraction.getTime().equals(selectedTime)) {
                        infractions.remove(infraction);
                        break;
                    }
                }
                Infraction.setInfractions(infractions, member.getIdLong());

                event.reply("Infraction removed.").queue();
                event.getMessage().editMessageComponents(event.getMessage().getActionRows().get(0).asDisabled()).queue();
            });
        } else if (Objects.requireNonNull(event.getComponent().getId()).startsWith("alts-remove-")) {
            long id = Long.parseLong(event.getComponent().getId().substring("alts-remove-".length()));
            event.getJDA().retrieveUserById(id).queue(member -> {

                AltFamily altFamily;
                List<Long> childrenIds;

                altFamily = AltFamily.getFamily(member.getIdLong());
                childrenIds = altFamily.getChildren();

                SelectOption selectOption = event.getInteraction().getSelectedOptions().get(0);
                if (selectOption == null) return;
                Objects.requireNonNull(event.getGuild()).retrieveMembersByIds(childrenIds).onSuccess(children -> {
                    for (Member child : children) {
                        if (!child.getEffectiveName().equals(selectOption.getValue())) continue;
                        childrenIds.remove(child.getIdLong());
                        altFamily.setChildren(childrenIds);
                        AltFamily.updateFamily(altFamily);
                        event.reply("Removed " + child.getEffectiveName() + " from the family.").queue();
                        event.getMessage().editMessageComponents(event.getMessage().getActionRows().get(0).asDisabled()).queue();
                        return;
                    }
                    event.reply("That child account could not be found.").queue();
                    event.getMessage().editMessageComponents(event.getMessage().getActionRows().get(0).asDisabled()).queue();
                });
            });
        }
    }

}