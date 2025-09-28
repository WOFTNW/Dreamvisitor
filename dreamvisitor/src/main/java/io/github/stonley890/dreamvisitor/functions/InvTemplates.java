package io.github.stonley890.dreamvisitor.functions;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.InvTemplate;
import io.github.stonley890.dreamvisitor.data.PlayerMemory;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class InvTemplates {

    static final File file = new File(Dreamvisitor.getPlugin().getDataFolder().getPath() + "/invTemplates.yml");

    public static void init() throws IOException {
        // If the file does not exist, create one
        if (!file.exists()) {
            Dreamvisitor.debug(file.getName() + " does not exist. Creating one now...");
            try {
                if (!file.createNewFile())
                    throw new IOException("The existence of " + file.getName() + " cannot be verified!", null);
            } catch (IOException e) {
                throw new IOException("Dreamvisitor tried to create " + file.getName() + ", but it cannot be read/written! Does the server have read/write access?", e);
            }
        }
    }

    @NotNull
    private static YamlConfiguration getConfig() {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (IOException e) {
            Bukkit.getLogger().severe(file.getName() + " cannot be read! Does the server have read/write access? " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(Dreamvisitor.getPlugin());
        } catch (InvalidConfigurationException e) {
            Bukkit.getLogger().severe(file.getName() + " is not a valid configuration! Is it formatted correctly? " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(Dreamvisitor.getPlugin());
        }
        return config;
    }

    private static void saveConfig(@NotNull YamlConfiguration config) {
        try {
            config.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().severe( file.getName() + " cannot be written! Does the server have read/write access? " + e.getMessage() + "\nHere is the data that was not saved:\n" + config.saveToString());
            Bukkit.getPluginManager().disablePlugin(Dreamvisitor.getPlugin());
        }
    }

    @NotNull
    public static List<InvTemplate> getTemplates() {
        List<Map<?, ?>> templates = getConfig().getMapList("templates");
        List<InvTemplate> parsedTemplates = new ArrayList<>();
        for (Map<?, ?> template : templates) {
            parsedTemplates.add(InvTemplate.deserialize((Map<String, Object>) template));
        }
        return parsedTemplates;
    }

    public static void saveLocations(@NotNull List<InvTemplate> invTemplates) {
        YamlConfiguration config = getConfig();
        List<Map<String, Object>> serializedInvTemplates = new ArrayList<>();
        for (InvTemplate invTemplate : invTemplates) {
            serializedInvTemplates.add(invTemplate.serialize());
        }
        config.set("templates", serializedInvTemplates);
        saveConfig(config);
    }

    /**
     * Get a {@link InvTemplate} by its name.
     * @param name The name to search for.
     * @return The first {@link InvTemplate} found with that name, or null if none are found.
     */
    @Nullable
    public static InvTemplate getInvTemplateByName(String name) {
        for (InvTemplate invTemplate : getTemplates()) {
            if (invTemplate.getName().equals(name)) return invTemplate;
        }
        return null;
    }

    public static void saveInvTemplate(InvTemplate template) {
        List<InvTemplate> templates = getTemplates();
        for (InvTemplate invTemplate : templates) {
            if (invTemplate.getName().equals(template.getName())) {
                templates.remove(invTemplate);
                break;
            }
        }
        templates.add(template);
        saveLocations(templates);
    }

    public static void removeInvTemplate(InvTemplate template) {
        List<InvTemplate> templates = getTemplates();
        for (InvTemplate invTemplate : templates) {
            if (invTemplate.getName().equals(template.getName())) {
                templates.remove(invTemplate);
                break;
            }
        }
        saveLocations(templates);
    }

    private static void applyTemplateToInventory(@NotNull PlayerInventory inventory, @NotNull InvTemplate template) {
        inventory.setContents(template.getContents());
    }

    /**
     * Apply an inventory template to a player.
     * @param player the player to apply the template to.
     * @param template the template to apply.
     * @param overwrite whether to overwrite if the same template is already applied
     */
    public static void applyToPlayer(@NotNull Player player, @NotNull InvTemplate template, boolean overwrite) {
        // Get player memory
        PlayerMemory memory = PlayerUtility.getPlayerMemory(player.getUniqueId());
        // Determine if the player is already using a template
        boolean isAlreadyUsingTemplate = memory.currentInventoryTemplate != null;
        if (!isAlreadyUsingTemplate) {
            // If the player is not using a template, save their inventory to PlayerMemory
            if (memory.creative) memory.creativeInv = player.getInventory().getContents();
            else memory.survivalInv = player.getInventory().getContents();
        } else if (Objects.equals(memory.currentInventoryTemplate, template.getName()) && !overwrite) {
            // If the template is already applied and overwrite is false, don't do anything.
            return;
        }
        // Overwrite the player's inventory
        applyTemplateToInventory(player.getInventory(), template);
        // Set value in PlayerMemory
        memory.currentInventoryTemplate = template.getName();
        // Save changes to PlayerMemory
        PlayerUtility.setPlayerMemory(player.getUniqueId(), memory);
    }

    /**
     * Apply an inventory template by name to a player.
     * @param player the player to apply the template to.
     * @param invTemplateName the template to apply.
     * @param overwrite whether to overwrite if the same template is already applied
     * @throws InvTemplate.MissingInventoryTemplateException if the template does not exist.
     */
    public static void applyToPlayer(@NotNull Player player, @NotNull String invTemplateName, boolean overwrite) throws InvTemplate.MissingInventoryTemplateException {
        InvTemplate template = getInvTemplateByName(invTemplateName);
        if (template == null) throw new InvTemplate.MissingInventoryTemplateException();
        applyToPlayer(player, template, overwrite);
    }

    /**
     * Apply an inventory template to a collection of players.
     * @param players the players to apply the template to.
     * @param template the template to apply.
     */
    public static void applyToPlayers(@NotNull Collection<Player> players, @NotNull InvTemplate template, boolean overwrite) {
        for (Player player : players) {
            applyToPlayer(player, template, overwrite);
        }
    }

    /**
     * Apply an inventory template to a collection of players.
     * @param players the players to apply the template to.
     * @param templateName the name of the template to apply.
     * @throws InvTemplate.MissingInventoryTemplateException if the template does not exist.
     */
    public static void applyToPlayers(@NotNull Collection<Player> players, @NotNull String templateName, boolean overwrite) throws InvTemplate.MissingInventoryTemplateException {
        InvTemplate template = getInvTemplateByName(templateName);
        if (template == null) throw new InvTemplate.MissingInventoryTemplateException();
        for (Player player : players) {
            applyToPlayer(player, template, overwrite);
        }
    }

    /**
     * Reset a player's inventory back to their saved inventory.
     * @param player the player to reset the inventory of.
     * @return true if the player was using a template, false otherwise.
     */
    public static boolean unapplyPlayer(@NotNull Player player) {
        // Get player memory
        PlayerMemory memory = PlayerUtility.getPlayerMemory(player.getUniqueId());
        // Determine if the player is already using a template
        boolean isUsingTemplate = memory.currentInventoryTemplate != null;
        // Only do this if the player is using a template
        if (isUsingTemplate) {
            if (memory.creative) player.getInventory().setContents(memory.creativeInv);
            else player.getInventory().setContents(memory.survivalInv);
            // Set value in PlayerMemory
            memory.currentInventoryTemplate = null;
            // Save changes to PlayerMemory
            PlayerUtility.setPlayerMemory(player.getUniqueId(), memory);
        }
        return isUsingTemplate;
    }

    /**
     * Reset a player's inventory back to their saved inventory.
     * @param players the players to reset the inventory of.
     * @return the number of players who were using a template.
     */
    public static int unapplyPlayers(@NotNull Collection<Player> players) {
        int playersUnapplied = 0;

        // Run this for each player
        for (Player player : players) if (unapplyPlayer(player)) playersUnapplied++;

        return playersUnapplied;
    }
}
