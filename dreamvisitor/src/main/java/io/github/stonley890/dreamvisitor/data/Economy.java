package io.github.stonley890.dreamvisitor.data;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.functions.Messager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class Economy {

    static final File file = new File(Dreamvisitor.getPlugin().getDataFolder().getPath() + "/economy.yml");

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
    }

    public static String formatDouble(double number) {
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
        return decimalFormat.format(number);
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
            Bukkit.getLogger().severe(file.getName() + " cannot be written! Does the server have read/write access? " + e.getMessage() + "\nHere is the data that was not saved:\n" + config.saveToString());
            Bukkit.getPluginManager().disablePlugin(Dreamvisitor.getPlugin());
        }
    }

    public static String getCurrencySymbol() {
        return Dreamvisitor.getPlugin().getConfig().getString("currencyIcon");
    }

    public static void setCurrencySymbol(String symbol) {
        Dreamvisitor.getPlugin().getConfig().set("currencyIcon", symbol);
        Dreamvisitor.getPlugin().saveConfig();
    }

    @NotNull
    public static List<ShopItem> getItems() {
        List<Map<?, ?>> items = getConfig().getMapList("items");
        List<ShopItem> shopItems = new ArrayList<>();
        items.forEach(item -> shopItems.add(ShopItem.deserialize((Map<String, Object>) item)));
        return shopItems;
    }

    public static void setItems(@NotNull List<ShopItem> itemsToSet) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (ShopItem shopItem : itemsToSet) {
            mapList.add(shopItem.serialize());
        }
        YamlConfiguration config = getConfig();
        config.set("items", mapList);
        saveConfig(config);
    }

    @Nullable
    public static ShopItem getItem(int id) {
        for (ShopItem item : getItems()) {
            if (item.getId() == id) return item;
        }
        return null;
    }

    public static void saveItems(@NotNull List<ShopItem> itemsToSave) {
        if (itemsToSave.isEmpty()) return;
        List<ShopItem> items = getItems();
        if (items.isEmpty()) {
            items.addAll(itemsToSave);
        } else {
            for (ShopItem newItem : itemsToSave) {
                boolean saved = false;
                for (int i = 0; i < items.size(); i++) {
                    ShopItem item = items.get(i);
                    if (newItem.id != item.id) continue;
                    items.set(i, newItem);
                    saved = true;
                }
                if (!saved) items.add(newItem);
            }
        }

        setItems(items);
    }

    public static void saveItem(@NotNull ShopItem itemToSave) {
        List<ShopItem> items = getItems();
        if (items.isEmpty()) items.add(itemToSave);
        else {
            boolean saved = false;
            for (int i = 0; i < items.size(); i++) {
                ShopItem item = items.get(i);
                if (itemToSave.id != item.id) continue;
                items.set(i, itemToSave);
                saved = true;
            }
            if (!saved) items.add(itemToSave);
        }

        setItems(items);
    }

    public static void removeItem(ShopItem item) {
        List<ShopItem> items = getItems();
        int itemToRemove = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() == item.getId()) {
                itemToRemove = i;
                break;
            }
        }
        if (itemToRemove == -1) return;
        items.remove(itemToRemove);

        setItems(items);
    }

    @NotNull
    public static List<Consumer> getConsumers() {
        List<Map<?, ?>> users = getConfig().getMapList("users");
        List<Consumer> consumers = new ArrayList<>();
        for (Map<?, ?> user : users) {
            Consumer consumer = Consumer.deserialize((Map<String, Object>) user);
            consumers.add(consumer);
        }
        return consumers;
    }

    @NotNull
    public static Consumer getConsumer(long id) {
        for (Consumer consumer : getConsumers()) {
            if (consumer.id == id) return consumer;
        }
        return new Consumer(id);
    }

    public static void saveConsumers(@NotNull List<Consumer> consumerList) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (Consumer consumer : consumerList) {
            mapList.add(consumer.serialize());
        }
        YamlConfiguration config = getConfig();
        config.set("users", mapList);
        saveConfig(config);
    }

    public static void saveConsumer(Consumer newConsumer) {
        List<Consumer> consumers = getConsumers();
        consumers.removeIf(consumer -> consumer.id == newConsumer.id);
        consumers.add(newConsumer);
        saveConsumers(consumers);
    }

    public static String getShopName() {
        return Dreamvisitor.getPlugin().getConfig().getString("shopName");
    }

    public static void setShopName(String name) {
        Dreamvisitor.getPlugin().getConfig().set("shopName", name);
        Dreamvisitor.getPlugin().saveConfig();
    }

    private static double getDailyBaseAmount() {
        return Dreamvisitor.getPlugin().getConfig().getDouble("dailyBaseAmount");
    }

    public static double getDailyStreakMultiplier() {
        return Dreamvisitor.getPlugin().getConfig().getDouble("dailyStreakMultiplier");
    }

    public static double getWorkReward() {
        return Dreamvisitor.getPlugin().getConfig().getDouble("workReward");
    }

    public static class ShopItem implements ConfigurationSerializable {

        private int id;
        @NotNull
        private String name;
        @NotNull
        private String description;
        private double price = 0;
        private double salePercent = 0;
        private int quantity = -1;
        private int maxAllowed = -1;
        private boolean enabled = true;
        private boolean giftingEnabled = true;
        private boolean useDisabled = false;
        private boolean useOnPurchase = false;
        @Nullable
        private List<Long> onUseRolesAdd = null;
        @Nullable
        private List<Long> onUseRolesRemove = null;
        @Nullable
        private List<String> onUseConsoleCommands = null;
        @Nullable
        private List<String> onUseGroupsAdd = null;
        @Nullable
        private List<String> onUseGroupsRemove = null;

        public void ensureUniqueId() {
            boolean unique = false;
            List<ShopItem> items = Economy.getItems();
            if (items.isEmpty()) return;
            while (!unique) {
                for (ShopItem item : items) {
                    if (item.id == this.id) {
                        this.id = new Random().nextInt(0, 99999999);
                        break;
                    }
                    unique = true;
                }
            }
        }

        public ShopItem(@NotNull String name, @NotNull String description) {
            this.id = new Random().nextInt(0, 99999999);
            this.name = name;
            this.description = description;
        }

        @NotNull
        public static ShopItem deserialize(@NotNull Map<String, Object> map) {
            ShopItem shopItem = new ShopItem((String) map.get("name"), (String) map.get("description"));
            shopItem.id = (int) map.get("id");
            shopItem.setPrice((Double) map.get("price"));
            shopItem.setSalePercent((Double) map.get("salePercent"));
            shopItem.setQuantity((Integer) map.get("quantity"));
            shopItem.setMaxAllowed((Integer) map.get("maxAllowed"));
            shopItem.setEnabled((Boolean) map.get("enabled"));
            shopItem.setGiftingEnabled((Boolean) map.get("giftingEnabled"));
            shopItem.setUseDisabled((Boolean) map.get("useDisabled"));
            shopItem.setUseOnPurchase((Boolean) map.get("useOnPurchase"));
            shopItem.setOnUseRolesAdd((List<Long>) map.get("onUseRolesAdd"));
            shopItem.setOnUseRolesRemove((List<Long>) map.get("onUseRolesRemove"));
            shopItem.setOnUseConsoleCommands((List<String>) map.get("onUseConsoleCommands"));
            shopItem.setOnUseGroupsAdd((List<String>) map.get("onUseGroupsAdd"));
            shopItem.setOnUseGroupsRemove((List<String>) map.get("onUseGroupsRemove"));

            return shopItem;
        }

        public void use(@NotNull Member member) throws UnsupportedOperationException {
            if (useDisabled) throw new UnsupportedOperationException("This item does not allow use.");
            UUID uuid = AccountLink.getUuid(member.getIdLong());
            if (uuid == null) throw new UnsupportedOperationException("This account is not linked to a UUID.");

            Guild guild = member.getGuild();

            LuckPerms luckPerms = Dreamvisitor.getLuckPerms();

            if (onUseRolesAdd != null && !onUseRolesAdd.isEmpty()) {
                for (Long roleId : onUseRolesAdd) {
                    Role role = guild.getRoleById(roleId);
                    if (role == null) continue;
                    guild.addRoleToMember(member, role).queue();
                }
            }
            if (onUseRolesRemove != null && !onUseRolesRemove.isEmpty()) {
                for (Long roleId : onUseRolesRemove) {
                    Role role = guild.getRoleById(roleId);
                    if (role == null) continue;
                    guild.removeRoleFromMember(member, role).queue();
                }
            }
            if ((onUseGroupsAdd != null && !onUseGroupsAdd.isEmpty()) || onUseGroupsRemove != null && !onUseGroupsRemove.isEmpty()) {
                luckPerms.getUserManager().loadUser(uuid).thenAcceptAsync(user -> {
                    if (onUseGroupsAdd != null && !onUseGroupsAdd.isEmpty()) {
                        for (String groupName : onUseGroupsAdd) {
                            Group group = luckPerms.getGroupManager().getGroup(groupName);
                            if (group == null) return;
                            user.data().add(InheritanceNode.builder(group).build());
                        }
                    }
                    if (onUseGroupsRemove != null && !onUseGroupsRemove.isEmpty()) {
                        for (String groupName : onUseGroupsRemove) {
                            Group group = luckPerms.getGroupManager().getGroup(groupName);
                            if (group == null) return;
                            user.data().remove(InheritanceNode.builder(group).build());
                        }
                    }
                    luckPerms.getUserManager().saveUser(user);
                });
            }
            if (onUseConsoleCommands != null && !onUseConsoleCommands.isEmpty()) {
                for (String command : onUseConsoleCommands) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                }
            }
        }

        public boolean isInfinite() {
            return quantity == -1;
        }

        public double getTruePrice() {
            return price - price * salePercent * 0.01;
        }

        public int getId() {
            return id;
        }

        @NotNull
        public String getName() {
            return name;
        }

        public void setName(@NotNull String name) {
            this.name = name;
        }

        @NotNull
        public String getDescription() {
            return description;
        }

        public void setDescription(@NotNull String description) {
            this.description = description;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public double getSalePercent() {
            return salePercent;
        }

        public void setSalePercent(double salePercent) {
            this.salePercent = salePercent;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public int getMaxAllowed() {
            return maxAllowed;
        }

        public void setMaxAllowed(int maxAllowed) {
            this.maxAllowed = maxAllowed;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isGiftingEnabled() {
            return giftingEnabled;
        }

        public void setGiftingEnabled(boolean giftingEnabled) {
            this.giftingEnabled = giftingEnabled;
        }

        public boolean isUseDisabled() {
            return useDisabled;
        }

        public void setUseDisabled(boolean useDisabled) {
            this.useDisabled = useDisabled;
        }

        public boolean isUseOnPurchase() {
            return useOnPurchase;
        }

        public void setUseOnPurchase(boolean useOnPurchase) {
            this.useOnPurchase = useOnPurchase;
        }

        @Nullable
        public List<Long> getOnUseRolesAdd() {
            return onUseRolesAdd;
        }

        public void setOnUseRolesAdd(@Nullable List<Long> onUseRolesAdd) {
            this.onUseRolesAdd = onUseRolesAdd;
        }

        @Nullable
        public List<Long> getOnUseRolesRemove() {
            return onUseRolesRemove;
        }

        public void setOnUseRolesRemove(@Nullable List<Long> onUseRolesRemove) {
            this.onUseRolesRemove = onUseRolesRemove;
        }

        @Nullable
        public List<String> getOnUseConsoleCommands() {
            return onUseConsoleCommands;
        }

        public void setOnUseConsoleCommands(@Nullable List<String> onUseConsoleCommands) {
            this.onUseConsoleCommands = onUseConsoleCommands;
        }

        @Nullable
        public List<String> getOnUseGroupsAdd() {
            return onUseGroupsAdd;
        }

        public void setOnUseGroupsAdd(@Nullable List<String> onUseGroupsAdd) {
            this.onUseGroupsAdd = onUseGroupsAdd;
        }

        @Nullable
        public List<String> getOnUseGroupsRemove() {
            return onUseGroupsRemove;
        }

        public void setOnUseGroupsRemove(@Nullable List<String> onUseGroupsRemove) {
            this.onUseGroupsRemove = onUseGroupsRemove;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj instanceof ShopItem item) {
                return Objects.equals(name, item.name) &&
                        Objects.equals(description, item.description) &&
                        Objects.equals(price, item.price) &&
                        Objects.equals(salePercent, item.salePercent) &&
                        Objects.equals(quantity, item.quantity) &&
                        Objects.equals(maxAllowed, item.maxAllowed) &&
                        Objects.equals(enabled, item.enabled) &&
                        Objects.equals(giftingEnabled, item.giftingEnabled) &&
                        Objects.equals(useDisabled, item.useDisabled) &&
                        Objects.equals(useOnPurchase, item.useOnPurchase) &&
                        Objects.equals(onUseRolesAdd, item.onUseRolesAdd) &&
                        Objects.equals(onUseRolesRemove, item.onUseRolesRemove) &&
                        Objects.equals(onUseConsoleCommands, item.onUseConsoleCommands) &&
                        Objects.equals(onUseGroupsAdd, item.onUseGroupsAdd) &&
                        Objects.equals(onUseGroupsRemove, item.onUseGroupsRemove);
            }
            return false;
        }

        @NotNull
        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("name", name);
            map.put("description", description);
            map.put("price", price);
            map.put("salePercent", salePercent);
            map.put("quantity", quantity);
            map.put("maxAllowed", maxAllowed);
            map.put("enabled", enabled);
            map.put("giftingEnabled", giftingEnabled);
            map.put("useDisabled", useDisabled);
            map.put("useOnPurchase", useOnPurchase);
            map.put("onUseRolesAdd", onUseRolesAdd);
            map.put("onUseRolesRemove", onUseRolesRemove);
            map.put("onUseConsoleCommands", onUseConsoleCommands);
            map.put("onUseGroupsAdd", onUseGroupsAdd);
            map.put("onUseGroupsRemove", onUseGroupsRemove);

            return map;
        }
    }

    public static class Consumer implements ConfigurationSerializable {

        private long id;
        private double balance = 0;
        @NotNull
        private Map<Integer, Integer> items = new HashMap<>();

        @NotNull
        private GameData gameData = new GameData();

        private Consumer(long id) {
            this.id = id;
        }

        @NotNull
        public static Consumer deserialize(@NotNull Map<String, Object> map) {
            Consumer consumer = new Consumer((Long) map.get("id"));
            consumer.setBalance((Double) map.get("balance"));
            consumer.setItems((Map<Integer, Integer>) map.get("inventory"));
            consumer.setGameData(GameData.deserialize((Map<String, Object>) map.get("gameData")));
            return consumer;
        }

        @NotNull
        public GameData getGameData() {
            return gameData;
        }

        public void setGameData(@NotNull GameData gameData) {
            this.gameData = gameData;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public double getBalance() {
            return balance;
        }

        public void setBalance(double newBalance) {
            balance = newBalance;
        }

        @NotNull
        public Map<Integer, Integer> getItems() {
            return items;
        }

        public void setItems(@NotNull Map<Integer, Integer> itemMap) {
            items = itemMap;
        }

        public int getItemQuantity(int itemId) {
            try {
                return items.get(itemId);
            } catch (NullPointerException e) {
                return 0;
            }
        }

        public void setItemQuantity(int itemId, int quantity) {
            items.put(itemId, quantity);
        }

        public int getQuantityOfItem(int itemId) {
            int amount = 0;
            if (items.get(itemId) != null) amount = items.get(itemId);
            return amount;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Consumer consumer = (Consumer) o;
            return id == consumer.id && Double.compare(balance, consumer.balance) == 0 && Objects.equals(items, consumer.items) && Objects.equals(gameData, consumer.gameData);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, balance, items, gameData);
        }

        @NotNull
        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("balance", balance);
            map.put("inventory", items);
            map.put("gameData", gameData.serialize());
            return map;
        }

        /**
         * Make this {@link Consumer} purchase an item.
         *
         * @param itemId the ID of the item to purchase.
         * @throws NullPointerException       if the item does not exist.
         * @throws ItemNotEnabledException    if the item is not enabled.
         * @throws ItemOutOfStockException    if the item is out of stock.
         * @throws InsufficientFundsException if the {@link Consumer} does not have sufficient funds to purchase the item.
         * @throws MaxItemQualityException    if the {@link Consumer} already has the maximum allowed of this item.
         */
        public void purchaseItem(int itemId) throws NullPointerException, ItemNotEnabledException, ItemOutOfStockException, InsufficientFundsException, MaxItemQualityException {

            ShopItem desiredItem = getItem(itemId);
            if (desiredItem == null) throw new NullPointerException("Item does not exist.");
            if (!desiredItem.enabled) throw new ItemNotEnabledException("Item is not enabled.");
            if (desiredItem.quantity == 0) throw new ItemOutOfStockException("Item is out of stock.");
            if (balance < desiredItem.getTruePrice())
                throw new InsufficientFundsException("Consumer does not have sufficient funds for item.");
            if (desiredItem.maxAllowed != -1 && (getItemQuantity(itemId) + 1 > desiredItem.maxAllowed))
                throw new MaxItemQualityException("Consumer already has max amount of this item.");

            if (desiredItem.useOnPurchase) {
                try {
                    useItem(itemId);
                } catch (ItemUseNotEnabledException e) {
                    setItemQuantity(itemId, getQuantityOfItem(itemId) + 1);
                }
            } else setItemQuantity(itemId, getQuantityOfItem(itemId) + 1);
            if (!desiredItem.isInfinite()) desiredItem.setQuantity(desiredItem.quantity - 1);
            setBalance(balance - desiredItem.getTruePrice());
            Economy.saveItem(desiredItem);

        }

        public void useItem(int itemId) throws NullPointerException, ItemNotEnabledException, ItemUseNotEnabledException {

            ShopItem item = getItem(itemId);
            if (item == null) throw new NullPointerException("Item does not exist.");
            if (!item.enabled) throw new ItemNotEnabledException("Item is not enabled.");
            if (item.useDisabled) throw new ItemUseNotEnabledException("Item cannot be used.");

            Guild guild = Bot.getGameLogChannel().getGuild();
            Member member = guild.retrieveMemberById(id).complete();

            UUID uuid = AccountLink.getUuid(id);
            if (uuid == null) throw new NullPointerException("No linked Minecraft account exists.");

            String username = PlayerUtility.getUsernameOfUuid(uuid);
            if (username == null) throw new NullPointerException("Linked Minecraft account could not be found.");

            LuckPerms luckPerms = Dreamvisitor.getLuckPerms();

            List<Long> rolesToAdd = item.onUseRolesAdd;
            if (rolesToAdd != null && !rolesToAdd.isEmpty())
                for (Long roleId : rolesToAdd) {
                    Role role = Bot.getJda().getRoleById(roleId);
                    if (role == null) throw new NullPointerException("Role to add " + roleId + " does not exist!");
                    guild.addRoleToMember(member, role).queue();
                }
            List<Long> rolesToRemove = item.onUseRolesRemove;
            if (rolesToRemove != null && !rolesToRemove.isEmpty())
                for (Long roleId : rolesToRemove) {
                    Role role = Bot.getJda().getRoleById(roleId);
                    if (role == null) throw new NullPointerException("Role to remove " + roleId + " does not exist!");
                    guild.removeRoleFromMember(member, role).queue();
                }

            if (item.onUseGroupsAdd != null && !item.onUseGroupsAdd.isEmpty()) {
                luckPerms.getUserManager().loadUser(uuid).thenAcceptAsync(user -> {
                    for (String groupName : item.onUseGroupsAdd) {
                        Group group = luckPerms.getGroupManager().getGroup(groupName);
                        if (group == null)
                            throw new NullPointerException("Group to add " + groupName + " does not exist.");
                        user.getNodes().add(InheritanceNode.builder(group).build());
                    }
                    luckPerms.getUserManager().saveUser(user);
                });
            }

            if (item.onUseGroupsRemove != null && !item.onUseGroupsRemove.isEmpty()) {
                luckPerms.getUserManager().loadUser(uuid).thenAcceptAsync(user -> {
                    for (String groupName : item.onUseGroupsRemove) {
                        Group group = luckPerms.getGroupManager().getGroup(groupName);
                        if (group == null)
                            throw new NullPointerException("Group to remove " + groupName + " does not exist.");
                        user.getNodes().remove(InheritanceNode.builder(group).build());
                    }
                    luckPerms.getUserManager().saveUser(user);
                });
            }

            if (item.onUseConsoleCommands != null && !item.onUseConsoleCommands.isEmpty()) {
                for (String command : item.onUseConsoleCommands) {
                    Bukkit.getScheduler().runTask(Dreamvisitor.getPlugin(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("$PLAYER$", username)));
                }
            }

            setItemQuantity(itemId, getItemQuantity(itemId - 1));

        }

        /**
         * Claim the daily reward. This method will refresh and update the streak, then add the reward to the balance.
         *
         * @return the amount that was rewarded.
         * @throws CoolDownException if this consumer cannot yet claim their
         */
        public double claimDaily() throws CoolDownException {
            gameData.updateStreak();
            if (gameData.lastDaily != null && Objects.equals(gameData.lastDaily.toLocalDate(), LocalDate.now())) throw new CoolDownException();
            double dailyBaseAmount = Economy.getDailyBaseAmount();
            double reward = dailyBaseAmount + (gameData.getDailyStreak() * getDailyStreakMultiplier());
            setBalance(balance + reward);
            gameData.dailyStreak++;
            gameData.lastDaily = LocalDateTime.now();
            return reward;
        }

        public double claimWork() throws CoolDownException {
            if (!gameData.timeUntilNextWork().isZero()) throw new CoolDownException();
            double workReward = Economy.getWorkReward();
            setBalance(balance + workReward);
            gameData.lastWork = LocalDateTime.now();
            return workReward;
        }

        public static class ItemNotEnabledException extends Exception {
            public ItemNotEnabledException(String message) {
                super(message);
            }
        }

        public static class ItemOutOfStockException extends Exception {
            public ItemOutOfStockException(String message) {
                super(message);
            }
        }

        public static class InsufficientFundsException extends Exception {
            public InsufficientFundsException(String message) {
                super(message);
            }
        }

        public static class ItemUseNotEnabledException extends Exception {
            public ItemUseNotEnabledException(String message) {
                super(message);
            }
        }

        public static class MaxItemQualityException extends Exception {
            public MaxItemQualityException(String message) {
                super(message);
            }
        }

        public static class CoolDownException extends Exception {
            public CoolDownException() {
                super();
            }

        }
    }

    public static class GameData implements ConfigurationSerializable {

        private int dailyStreak = 0;
        @Nullable
        private LocalDateTime lastDaily = null;
        @Nullable
        private LocalDateTime lastWork = null;

        public GameData() {
        }

        public static double getDailyBaseAmount() {
            return Dreamvisitor.getPlugin().getConfig().getDouble("dailyBaseAmount");
        }

        @NotNull
        public static GameData deserialize(@NotNull Map<String, Object> map) {
            GameData gameData = new GameData();
            gameData.dailyStreak = (int) map.get("dailyStreak");
            Object lastDaily = map.get("lastDaily");
            if (lastDaily != null) lastDaily = LocalDateTime.parse((CharSequence) lastDaily);
            gameData.lastDaily = (LocalDateTime) lastDaily;
            Object lastWork = map.get("lastWork");
            if (lastWork != null) lastWork = LocalDateTime.parse((CharSequence) lastWork);
            gameData.lastWork = (LocalDateTime) lastWork;
            return gameData;
        }

        public int getDailyStreak() {
            return dailyStreak;
        }

        public void setDailyStreak(int dailyStreak) {
            this.dailyStreak = dailyStreak;
        }

        public Duration timeUntilNextWork() {
            if (lastWork == null || lastWork.plusHours(1).isBefore(LocalDateTime.now())) return Duration.ZERO;
            return Duration.between(lastWork.plusHours(1), LocalDateTime.now());
        }

        public void updateStreak() {
            if (lastDaily == null || LocalDate.from(lastDaily).plusDays(1).isBefore(LocalDate.now())) setDailyStreak(0);
        }

        @Nullable
        public LocalDateTime getLastDaily() {
            return lastDaily;
        }

        public void setLastDaily(@Nullable LocalDateTime lastDaily) {
            this.lastDaily = lastDaily;
        }

        @Nullable
        public LocalDateTime getLastWork() {
            return lastWork;
        }

        public void setLastWork(@Nullable LocalDateTime lastWork) {
            this.lastWork = lastWork;
        }

        @NotNull
        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> map = new HashMap<>();
            map.put("dailyStreak", dailyStreak);
            map.put("lastDaily", lastDaily != null ? lastDaily.toString() : null);
            map.put("lastWork", lastWork != null ? lastWork.toString() : null);
            return map;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GameData gameData = (GameData) o;
            return dailyStreak == gameData.dailyStreak && Objects.equals(lastDaily, gameData.lastDaily) && Objects.equals(lastWork, gameData.lastWork);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dailyStreak, lastDaily, lastWork);
        }
    }
}
