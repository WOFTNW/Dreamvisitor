package io.github.stonley890.dreamvisitor.functions;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.InvTemplate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
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

}
