package org.woftnw.dreamvisitor.functions;

import org.woftnw.dreamvisitor.Dreamvisitor;
import org.woftnw.dreamvisitor.data.PlayerUtility;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.profile.PlayerProfile;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import spark.Spark;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Whitelist extends ListenerAdapter {

    private static @NotNull JSONArray get() throws IOException {

        // Access whitelist.json file
        Messager.debug("Trying to access whitelist file");
        String whitelistPath = Bukkit.getServer().getWorldContainer().getPath() + "/whitelist.json";
        // Parse whitelist.json to a string list
        List<String> lines = Files.readAllLines(new File(whitelistPath).toPath());
        Messager.debug("Success");

        // Format the string list to StringBuilder
        StringBuilder fileString = new StringBuilder();
        for (String line : lines) {
            fileString.append(line);
        }
        Messager.debug("Strings joined to StringBuilder");

        // Format string to JSONArray
        JSONArray whitelist = new JSONArray(fileString.toString());
        Messager.debug("String Builder parsed as JSON");
        return whitelist;
    }

    public static boolean isUserWhitelisted(@NotNull UUID uuid) throws IOException {
        JSONArray whitelist = get();
        for (Object entry : whitelist) {
            JSONObject object = (JSONObject) entry;
            if (object.get("uuid").equals(uuid.toString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds the specified details to the whitelist file and reloads the whitelist.
     * @param username The username to add.
     * @param uuid The {@link UUID} to add.
     * @throws IOException If there is an issue accessing the whitelist file.
     */
    public static void add(@NotNull String username, @NotNull UUID uuid) throws IOException {

        Messager.debug("Adding " + username + " to the whitelist.");

        JSONArray whitelist = get();

        // Create entry
        Messager.debug("Creating entry...");
        JSONObject whitelistEntry = new JSONObject();
        whitelistEntry.put("uuid", uuid.toString());
        whitelistEntry.put("name", username);

        // Add to whitelist.json
        Messager.debug("Adding to JSON...");
        whitelist.put(whitelistEntry);

        // Write to whitelist.json file
        Messager.debug("Attempting to write to file...");

        Files.writeString(new File(Bukkit.getServer().getWorldContainer().getPath() + "/whitelist.json").toPath(), whitelist.toString(4));
        Messager.debug("Success.");

        // reload whitelist
        Messager.debug("Reloading whitelist");
        Bukkit.reloadWhitelist();
        Messager.debug("Whitelist reloaded");
    }

    /**
     * Removes the specified details from the whitelist file and reloads the whitelist.
     * @param username The username to remove.
     * @param uuid The {@link UUID} to remove.
     * @throws IOException If there is an issue accessing the whitelist file.
     */
    public static void remove(@NotNull String username, @NotNull UUID uuid) throws IOException {

        Messager.debug("Removing " + username + " to the whitelist.");

        JSONArray whitelist = get();

        // Search for and remove entry
        for (int i = 0; i < whitelist.length(); i++) {
            JSONObject object = (JSONObject) whitelist.get(i);

            Messager.debug("Checking " + object.get("uuid") + " with " + uuid);

            if (object.get("uuid").equals(uuid.toString())) {

                Messager.debug("Found match! " + whitelist.remove(i));
            }
        }

        // Write to whitelist.json file
        Messager.debug("Attempting to write to file...");

        Files.writeString(new File(Bukkit.getServer().getWorldContainer().getPath() + "/whitelist.json").toPath(), whitelist.toString(4));
        Messager.debug("Success.");

        // reload whitelist
        Messager.debug("Reloading whitelist");
        Bukkit.reloadWhitelist();
        Messager.debug("Whitelist reloaded");
    }

    public static void startWeb(int port) {

        // Web whitelist server
        String websiteUrl = Dreamvisitor.getPlugin().getConfig().getString("website-url");

        Spark.port(port); // Choose a port for your API
        Spark.before((request, response) -> {
            response.header("Access-Control-Allow-Origin", websiteUrl);
            response.header("Access-Control-Request-Method", "*");
            response.header("Access-Control-Allow-Headers", "*");
        });
        Spark.post("/process-username", (request, response) -> {
            String username = request.queryParams("username");

            Messager.debug("Username from web form: " + username);

            // Process the username
            boolean success = processUsername(username);

            Messager.debug("Processed. Success: " + success);

            // Send a response back to the web page
            Messager.debug("response.header");
            response.type("application/json");
            Messager.debug("response.type");
            return "{\"success\": " + success + "}";
        });
    }

    public static void stopWeb() {
        Spark.stop();
    }

    private static boolean processUsername(@NotNull String username) throws IOException {

        // Check for valid UUID
        Messager.debug("Checking for valid UUID");
        UUID uuid = PlayerUtility.getUUIDOfUsername(username);
        if (uuid == null) {
            // username does not exist alert
            Messager.debug("Username does not exist.");
            Messager.debug("Failed whitelist.");
        } else {

            Messager.debug("Got UUID");

            // No account to link

            // Check if already whitelisted
            Messager.debug("Is user already whitelisted?");

            if (isUserWhitelisted(uuid)) {
                Messager.debug("Already whitelisted.");
                Messager.debug("Resolved.");

            } else {
                Messager.debug("Player is not whitelisted.");

                add(username, uuid);

                // success message
                Messager.debug("Success.");

            }
            return true;
        }
        return false;
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (Objects.requireNonNull(event.getButton().getId()).startsWith("unwhitelist")) {
            String uuid = Objects.requireNonNull(event.getButton().getId()).substring("unwhitelist-".length());
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
            event.editButton(event.getButton().asDisabled()).queue();
        } else if (event.getButton().getId().startsWith("ban")) {
            String uuid = event.getButton().getId().substring("ban-".length());
            String username = PlayerUtility.getUsernameOfUuid(uuid);

            try {

                if (Whitelist.isUserWhitelisted(UUID.fromString(uuid))) {
                    assert username != null;
                    Whitelist.remove(username, UUID.fromString(uuid));
                }
                BanList<PlayerProfile> banList = Bukkit.getBanList(BanList.Type.PROFILE);
                assert username != null;
                banList.addBan(Bukkit.getServer().createPlayerProfile(username), "Banned by Dreamvistitor.", (Date) null, null);
                event.reply("Banned `" + username + "`.").queue();

            } catch (IOException e) {
                event.reply("Unable to read or write the whitelist file: " + e.getMessage()).queue();
            }

            // Disable button after use
            event.editButton(event.getButton().asDisabled()).queue();
        }

    }

}