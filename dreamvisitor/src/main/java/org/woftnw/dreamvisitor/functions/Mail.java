package org.woftnw.dreamvisitor.functions;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import org.woftnw.dreamvisitor.Dreamvisitor;
import org.woftnw.dreamvisitor.data.Tribe;
import org.woftnw.dreamvisitor.data.TribeUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class Mail {

    static final File file = new File(Dreamvisitor.getPlugin().getDataFolder().getPath() + "/mail.yml");
    static List<Deliverer> activeDeliverers = new ArrayList<>();
    static double maxDistance;

    public static void init() throws IOException {
        // If the file does not exist, create one
        if (!file.exists()) {
            Messager.debug(file.getName() + " does not exist. Creating one now...");
            try {
                if (!file.createNewFile())
                    throw new IOException("The existence of " + file.getName() + " cannot be verified!", null);
            } catch (IOException e) {
                throw new IOException("Dreamvisitor tried to create " + file.getName() + ", but it cannot be read/written! Does the server have read/write access?", e);
            }
        }
        calculateMaxDistance();

        Bukkit.getScheduler().runTaskTimer(Dreamvisitor.getPlugin(), () -> {
            if (activeDeliverers.isEmpty()) return;

            for (Deliverer activeDeliverer : activeDeliverers) {
                ComponentBuilder builder = new ComponentBuilder("You are currently delivering a parcel to ").color(ChatColor.WHITE);
                String distance = "unknown";
                try {
                    distance = String.valueOf(Math.round(calculateDistanceWithPantalaOffset(activeDeliverer.player.getLocation(), activeDeliverer.endLoc.location)));
                } catch (IllegalArgumentException ignored) {}
                builder.append(activeDeliverer.endLoc.name.replace("_", " ")).color(ChatColor.AQUA).append(", ").color(ChatColor.WHITE)
                        .append(distance).color(ChatColor.AQUA).append(" meters away.").color(ChatColor.WHITE);
                activeDeliverer.player.spigot().sendMessage(ChatMessageType.ACTION_BAR, builder.create());
            }
        }, 20, 20);
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

    public static List<Deliverer> getDeliverers() {
        return activeDeliverers;
    }

    @NotNull
    public static List<MailLocation> getLocations() {
        List<Map<?, ?>> locations = getConfig().getMapList("locations");
        List<MailLocation> parsedLocations = new ArrayList<>();
        for (Map<?, ?> location : locations) {
            parsedLocations.add(MailLocation.deserialize((Map<String, Object>) location));
        }
        return parsedLocations;
    }

    public static void saveLocations(@NotNull List<MailLocation> mailLocations) {
        YamlConfiguration config = getConfig();
        List<Map<String, Object>> serializedMailLocations = new ArrayList<>();
        for (MailLocation mailLocation : mailLocations) {
            serializedMailLocations.add(mailLocation.serialize());
        }
        config.set("locations", serializedMailLocations);
        saveConfig(config);
    }

    /**
     * Get a {@link MailLocation} by its name.
     * @param name The name to search for.
     * @return The first {@link MailLocation} found with that name, or null if none are found.
     */
    @Nullable
    public static MailLocation getLocationByName(String name) {
        for (MailLocation location : getLocations()) {
            if (location.getName().equals(name)) return location;
        }
        return null;
    }

    /**
     * Get the {@link MailLocation} nearest to a given {@link Location} (within 50000 blocks).
     * @param queryLocation Any {@link Location} to query.
     * @return The closest {@link MailLocation} in the same dimension, or null if none are found.
     */
    @Nullable
    public static MailLocation getNearestLocation(@NotNull Location queryLocation) {
        double shortestDistance = 50000.0;
        MailLocation nearest = null;
        Messager.debug("Getting nearest MailLocation to " + queryLocation);
        for (MailLocation location : getLocations()) {
            Messager.debug("Checking " + location.getName());
            if (!Objects.equals(location.getLocation().getWorld(), queryLocation.getWorld())) continue;
            Messager.debug("Same world.");
            double distance = location.getLocation().distance(queryLocation);
            Messager.debug("Distance: " + distance);
            if (distance < shortestDistance) {
                Messager.debug("New nearest!");
                nearest = location;
                shortestDistance = distance;
            }
        }
        return nearest;
    }

    public static double getMaxDistance() {
        return maxDistance;
    }

    @NotNull
    @Contract("_ -> param1")
    private static Location pantalaTransform(@NotNull Location location) {
        location.setWorld(Bukkit.getWorlds().get(0));
        location.add(-6015, 0, 0);
        return location;
    }

    public static double calculateDistanceWithPantalaOffset(@NotNull Location location1, @NotNull Location location2) {
        if (Objects.requireNonNull(location1.getWorld()).getName().contains("pantala")) {
            location1 = pantalaTransform(location1.clone());
        }
        if (Objects.requireNonNull(location2.getWorld()).getName().contains("pantala")) {
            location2 = pantalaTransform(location2.clone());
        }
        return location1.distance(location2);
    }

    public static void calculateMaxDistance() {
        List<MailLocation> locations = getLocations();
        double maxDistance = 0;
        for (MailLocation location : locations) {
            for (MailLocation mailLocation : locations) {
                Location location1 = location.location;
                Location location2 = mailLocation.location;
                double distance = calculateDistanceWithPantalaOffset(location1, location2);
                if (distance > maxDistance) maxDistance = distance;
            }
        }
        Mail.maxDistance = maxDistance;
    }

    public static double getDistanceWeightMultiplier() {
        return Dreamvisitor.getPlugin().getConfig().getDouble("mailDeliveryLocationSelectionDistanceWeightMultiplier");
    }

    public static double getDistanceRewardMultiplier() {
        return Dreamvisitor.getPlugin().getConfig().getDouble("mailDistanceToRewardMultiplier");
    }

    /**
     * Complete a mail delivery.
     * A player must be within 10 blocks of the nearest {@link MailLocation} to complete a delivery.
     * They also must have the mail parcel.
     * If they are, the reward amount will be given to them, and they will be removed from the active deliverers list.
     * @param player the player to complete this as.
     * @return the amount rewarded.
     * @throws Exception check message.
     */
    public static double complete(Player player) throws Exception {
        Deliverer deliverer = null;
        for (Deliverer activeDeliverer : activeDeliverers) {
            if (activeDeliverer.player.equals(player)) {
                deliverer = activeDeliverer;
                break;
            }
        }
        if (deliverer == null) throw new Exception("Player is not an active deliverer!");
        MailLocation nearestLocation = Mail.getNearestLocation(player.getLocation());
        if (nearestLocation == null) throw new Exception("No nearest location found!");
        if (!player.getWorld().equals(nearestLocation.location.getWorld())) throw new Exception("Not in same world!");
        double distance = player.getLocation().distance(nearestLocation.location);
        Messager.debug("Distance to nearest: " + distance);
        if (distance > 10) throw new Exception("Not close enough to MailLocation!");

        PlayerInventory inventory = player.getInventory();
        if (!inventory.contains(deliverer.parcel)) throw new Exception("Player does not have parcel!");

        if (!nearestLocation.equals(deliverer.endLoc)) throw new Exception("Not at the destination location!");

        double reward = deliverer.getReward();
        Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        if (ess == null) throw new Exception("EssentialsX is not currently active!");

        User user = ess.getUser(player);
        user.giveMoney(BigDecimal.valueOf(reward));

        player.getInventory().remove(deliverer.parcel);

        Messager.debug("Removed from activeDeliverers: " + activeDeliverers.remove(deliverer));

        return reward;
    }

    public static void cancel(@NotNull Player player) {
        activeDeliverers.removeIf(deliverer -> deliverer.player.equals(player));
        Messager.send(player, "You canceled your delivery.");
    }

    public static boolean isPLayerDeliverer(@NotNull Player player) {
        return activeDeliverers.stream().map(Deliverer::getPlayer).toList().contains(player);
    }

    @NotNull
    public static MailLocation chooseDeliveryLocation(@NotNull MailLocation startPos) throws InvalidConfigurationException {
        Messager.debug("Max distance:" + maxDistance);
        List<MailLocation> locations = getLocations();
        Messager.debug("Removed start pos:" + locations.remove(startPos));
        if (locations.isEmpty()) throw new InvalidConfigurationException("There aren't enough mail locations!");

        // Compute the total weight of all items together.
        // This can be skipped of course if sum is already 1.
        double totalWeight = 0.0;
        for (MailLocation location : locations) {
            Messager.debug("\nEvaluating " + location.getName());
            double weight = calculateWeight(startPos, location);
            totalWeight += weight;
            Messager.debug("Weight: " + weight);
        }
        Messager.debug("Total weight: " + totalWeight);

        // Now choose a random item.
        double countWeight = 0.0;
        double r = Math.random() * totalWeight;
        Messager.debug("random value: " + r);
        for (int index = 0; index < locations.size() - 1; ++index) {
            MailLocation evalLocation = locations.get(index);
            Messager.debug("\nLocation at index " + index + ": " + evalLocation.getName());
            double weight = calculateWeight(startPos, evalLocation);
            countWeight += weight;
            Messager.debug("New countWeight: " + countWeight);
            if (countWeight >= r) {
                Messager.debug(countWeight + " >= " + r + ", returning.");
                return evalLocation;
            }
            Messager.debug("countWeight < random");

        }
        Messager.debug("Selecting last, " + locations.get(locations.size() -1).getName());
        return locations.get(locations.size() -1);
    }

    private static double calculateWeight(@NotNull MailLocation startLoc, @NotNull MailLocation location) {
        Messager.debug("Base weight: " + location.getWeight());
        Messager.debug("Distance weight multiplier: " + getDistanceWeightMultiplier());
        Messager.debug("Maxdist: " + maxDistance);
        double distance = MailLocation.getDistance(startLoc, location);
        Messager.debug("distance: " + distance);
        Messager.debug("Maxdist - distance: " + (maxDistance - distance));
        Messager.debug("that divided by maxdist: " + ((maxDistance - distance) / maxDistance));
        return location.getWeight() + getDistanceWeightMultiplier() * ((maxDistance - distance) / maxDistance);
    }

    @NotNull
    public static String chooseName(@NotNull MailLocation startLoc, @NotNull MailLocation endLoc) {

        String placeholder = "Dreamvisitor";

        InputStream namesStream = Dreamvisitor.getPlugin().getResource("names.yml");
        if (namesStream == null) return placeholder;

        Random random = new Random();
        List<String> nameList;
        YamlConfiguration nameConfig = new YamlConfiguration();
        InputStreamReader inputStreamReader = new InputStreamReader(namesStream);
        try {
            nameConfig.load(inputStreamReader);
        } catch (IOException | InvalidConfigurationException e) {
            Bukkit.getLogger().warning("There was a problem accessing embedded names.yml: " + e.getMessage());
            return placeholder;
        }
        int roll = random.nextInt(100);
        if (roll < 10) {

            int tribeIndex = random.nextInt(10);
            Tribe tribe = TribeUtil.tribes[tribeIndex];

            nameList = nameConfig.getStringList(tribe.getTeamName().toLowerCase());

        } else if (roll < 55) {
            nameList = nameConfig.getStringList(startLoc.homeTribe.getTeamName().toLowerCase());
        } else {
            nameList = nameConfig.getStringList(endLoc.homeTribe.getTeamName().toLowerCase());
        }

        int name = random.nextInt(nameList.size());
        return nameList.get(name);

    }

    public static void saveLocation(MailLocation mailLocation) {
        List<MailLocation> locations = getLocations();
        for (MailLocation location : locations) {
            if (location.name.equals(mailLocation.name)) {
                locations.remove(location);
                break;
            }
        }
        locations.add(mailLocation);
        saveLocations(locations);
    }

    public static void removeLocation(MailLocation location) {
        List<MailLocation> locations = getLocations();
        for (MailLocation mailLocation : locations) {
            if (mailLocation.name.equals(location.name)) {
                locations.remove(mailLocation);
                break;
            }
        }
        saveLocations(locations);
    }

    public static void setDeliverers(List<Deliverer> deliverers) {
        activeDeliverers = deliverers;
    }

    public static class MailLocation implements ConfigurationSerializable {

        /**
         * The in-game location.
         */
        @NotNull private Location location;
        /**
         * The display name/title of this location that is displayed to players. This should be unique.
         */
        @NotNull private String name;
        /**
         * The weight/importance of this location. Central locations should be weighted more heavily.
         */
        private int weight;
        /**
         * The home tribe of this location. Mail will more like be addressed to/from this tribe.
         */
        private Tribe homeTribe;

        public MailLocation(@NotNull Location location, @NotNull String name, int weight, @NotNull Tribe homeTribe) {
            this.location = location;
            this.name = name;
            this.weight = weight;
            this.homeTribe = homeTribe;
        }

        public static double getDistance(@NotNull MailLocation location1, @NotNull MailLocation location2) {
            return calculateDistanceWithPantalaOffset(location1.location, location2.location);
        }

        @NotNull
        public Location getLocation() {
            return location;
        }

        public int getWeight() {
            return weight;
        }

        @NotNull
        public String getName() {
            return name;
        }

        public Tribe getHomeTribe() {
            return homeTribe;
        }

        public void setName(@NotNull String name) {
            this.name = name;
        }

        public void setLocation(@NotNull Location location) {
            this.location = location;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        public void setHomeTribe(@NotNull Tribe homeTribe) {
            this.homeTribe = homeTribe;
        }

        @NotNull
        @Contract("_ -> new")
        public static MailLocation deserialize(@NotNull Map<String, Object> map) {
            return new MailLocation((Location) map.get("location"), (String) map.get("name"), (Integer) map.get("weight"), Tribe.valueOf((String) map.get("home")));
        }

        @NotNull
        @Override
        public Map<String, Object> serialize() {
            return Map.of("location", location, "name", name, "weight", weight, "home", homeTribe.toString());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MailLocation that = (MailLocation) o;
            return weight == that.weight && Objects.equals(location, that.location) && Objects.equals(name, that.name) && homeTribe == that.homeTribe;
        }

        @Override
        public int hashCode() {
            return Objects.hash(location, name, weight, homeTribe);
        }
    }

    public static class Deliverer {

        @NotNull private final Player player;
        @NotNull private final MailLocation startLoc;
        @NotNull private final MailLocation endLoc;
        @Nullable private LocalDateTime startTime;
        @Nullable private ItemStack parcel;

        public Deliverer(@NotNull Player player, @NotNull MailLocation startLoc, @NotNull MailLocation endLoc) {
            this.player = player;
            this.startLoc = startLoc;
            this.endLoc = endLoc;
        }

        static final String[] lore = {
                "It's a letter with a heart on it.",
                "It's a very official-looking letter.",
                "It's a small package that has been hand-wrapped.",
                "It's a surprisingly heavy package.",
                "It's just a plain, folded letter.",
                "It's a letter with color drawings\nscribbled on the outside.",
                "It seems to be some kind of government letter.",
                "It's a letter with a wax seal.",
                "It's a package covered in festive wrapping paper.",
                "It's a thick envelope with multiple stamps.",
                "It's a letter with a return address\nin an unfamiliar language.",
                "It's a small box with fragile stickers on it.",
                "It's a postcard with a scenic picture.",
                "It's a letter with a faint scent\nthat reminds you of candy.",
                "It's a package with a handwritten label.",
                "It's a letter with glitter on the envelope.",
                "It's an envelope with a window showing\npart of a neatly-printed document inside.",
                "It's a padded envelope with a soft bulge inside.",
                "It's a letter with a gold emblem\non the top left corner.",
                "It's a package tied with a string.",
                "It's a letter with a metallic sheen to the paper.",
                "It's an envelope that has been hastily taped shut.",
                "It's a letter with colorful stamps.",
                "It's a letter with an urgent red stamp on it.",
                "It's a small box that makes a\nrattling sound when shaken."
        };

        @NotNull
        private ItemStack createParcel() {

            if (!started()) throw new NullPointerException("This delivery has not been started!");
            assert startTime != null;

            String name = Mail.chooseName(startLoc, endLoc);

            ItemStack parcel = new ItemStack(Material.ENCHANTED_BOOK);
            ItemMeta itemMeta = Bukkit.getItemFactory().getItemMeta(parcel.getType());
            assert itemMeta != null;

            itemMeta.setDisplayName(ChatColor.RESET + "Parcel for " + name);
            itemMeta.setLore(Arrays.stream(lore[new Random().nextInt(lore.length)].split("\n")).toList());
            PersistentDataContainer data = itemMeta.getPersistentDataContainer();
            data.set(new NamespacedKey(Dreamvisitor.getPlugin(), "mail_deliverer"), PersistentDataType.STRING, player.getUniqueId().toString());
            data.set(new NamespacedKey(Dreamvisitor.getPlugin(), "mail_deliver_start"), PersistentDataType.STRING, startTime.toString());

            parcel.setItemMeta(itemMeta);

            return parcel;
        }

        @Nullable
        public ItemStack getParcel() {
            return parcel;
        }

        public String getParcelName() {
            return Objects.requireNonNull(createParcel().getItemMeta()).getDisplayName().split(" ")[2];
        }

        public boolean started() {
            return startTime != null;
        }

        @NotNull
        public Player getPlayer() {
            return player;
        }

        @NotNull
        public MailLocation getStartLoc() {
            return startLoc;
        }

        @NotNull
        public MailLocation getEndLoc() {
            return endLoc;
        }

        /**
         * Starts the time for this Deliverer.
         * This also gives them their parcel.
         * This will throw {@link UnsupportedOperationException} if already started.
         */
        public void start() {
            if (startTime != null) throw new UnsupportedOperationException("Already started time!");
            startTime = LocalDateTime.now();

            ItemStack parcel = createParcel();
            this.parcel = parcel;
            player.getInventory().addItem(parcel);
        }

        /**
         * The time this deliverer has spent delivering their parcel.
         * @return The time spent or zero if not started.
         */
        public Duration timeSpent() {
            if (startTime == null) return Duration.ZERO;
            return Duration.between(startTime, LocalDateTime.now());
        }

        /**
         * Get the expected for the completion of this delivery.
         * @return a double representing the reward amount.
         */
        public double getReward() {
            return Math.round((MailLocation.getDistance(startLoc, endLoc) * getDistanceRewardMultiplier())/10.0) * 10;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Deliverer deliverer = (Deliverer) o;
            return Objects.equals(player, deliverer.player) && Objects.equals(startLoc, deliverer.startLoc) && Objects.equals(endLoc, deliverer.endLoc) && Objects.equals(startTime, deliverer.startTime);
        }

        @Override
        public int hashCode() {
            return Objects.hash(player, startLoc, endLoc, startTime);
        }
    }

}
