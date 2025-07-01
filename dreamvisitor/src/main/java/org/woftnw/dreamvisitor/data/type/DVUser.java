package org.woftnw.dreamvisitor.data.type;

import org.bukkit.inventory.ItemStack;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DVUser {
    // Entry info
    private String id = null;
    private String collectionId = null;
    private String collectionName = null;

    // Identification
    private UUID minecraftUuid = null;
    private String minecraftUsername = null;
    private String discordUsername = null;
    private Long discordId = null;
    private String discordAvatarUrl = null;

    private List<String> infractions = new ArrayList<>(); // This is irrelevant and can be removed
    private List<String> homes = new ArrayList<>();
    private List<String> inventory_items = new ArrayList<>(); // This is irrelevant and can be removed
    private List<String> claims = new ArrayList<>();
    private List<String> alts = new ArrayList<>(); // This is irrelevant and can be removed

    // In-game stats
    private Integer claimLimit = 0;
    private Integer playTime = 0;
    // These are irrelevant and can be removed
    private Double balance = 0d;
    private Integer daily_streak = 0;

    // These are irrelevant and can be removed
    private OffsetDateTime lastWork = null;
    private OffsetDateTime lastDaily = null;
    private OffsetDateTime lastPlayed = null;

    // Standing
    private Boolean isSuspended = false;
    private Boolean isBanned = false;

    // User options
    private Boolean showDiscordMessages = true;
    private Boolean flightDisabled = false;
    private Boolean vanished = false;
    private Boolean autoInvSwapEnabled = true;
    private Boolean autoRadioEnabled = false;

    // Sandbox Mode
    private Boolean inSandboxMode = false;

    // Inventory selection
    private Boolean usingCreativeInv = false;
    private ItemStack[] survivalInv = null;
    private ItemStack[] creativeInv = null;

    private OffsetDateTime created = null;
    private OffsetDateTime updated = null;

    // Existing getters and setters
    public UUID getMinecraftUuid() {
        return minecraftUuid;
    }

    public String getDiscordUsername() {
        return discordUsername;
    }

    public String getMinecraftUsername() {
        return minecraftUsername;
    }

    public void setDiscordUsername(String dcUsername) {
        this.discordUsername = dcUsername;
    }

    public void setMinecraftUsername(String minecraftUsername) {
        this.minecraftUsername = minecraftUsername;
    }

    public void setMinecraftUuid(UUID minecraftUuid) {
        this.minecraftUuid = minecraftUuid;
    }

    // New getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public Long getDiscordId() {
        return discordId;
    }

    public void setDiscordId(Long discordId) {
        this.discordId = discordId;
    }

    public String getDiscordAvatarUrl() {
        return discordAvatarUrl;
    }

    public void setDiscordAvatarUrl(String discordAvatarUrl) {
        this.discordAvatarUrl = discordAvatarUrl;
    }

    public List<String> getInfractions() {
        return infractions;
    }

    public void setInfractions(List<String> infractions) {
        this.infractions = infractions;
    }

    public List<String> getHomes() {
        return homes;
    }

    public void setHomes(List<String> homes) {
        this.homes = homes;
    }

    public List<String> getInventory_items() {
        return inventory_items;
    }

    public void setInventory_items(List<String> inventory_items) {
        this.inventory_items = inventory_items;
    }

    public List<String> getClaims() {
        return claims;
    }

    public void setClaims(List<String> claims) {
        this.claims = claims;
    }

    public List<String> getAlts() {
        return alts;
    }

    public void setAlts(List<String> alts) {
        this.alts = alts;
    }

    public Integer getClaimLimit() {
        return claimLimit;
    }

    public void setClaimLimit(Integer claimLimit) {
        this.claimLimit = claimLimit;
    }

    public Integer getPlayTime() {
        return playTime;
    }

    public void setPlayTime(Integer playTime) {
        this.playTime = playTime;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public Integer getDaily_streak() {
        return daily_streak;
    }

    public void setDaily_streak(Integer daily_streak) {
        this.daily_streak = daily_streak;
    }

    public OffsetDateTime getLastWork() {
        return lastWork;
    }

    public void setLastWork(OffsetDateTime lastWork) {
        this.lastWork = lastWork;
    }

    public OffsetDateTime getLastPlayed() {
        return lastPlayed;
    }

    public OffsetDateTime getLastDaily() {
        return lastDaily;
    }

    public void setLastDaily(OffsetDateTime lastDaily) {
        this.lastDaily = lastDaily;
    }

    public Boolean getIsSuspended() {
        return isSuspended;
    }

    public void setIsSuspended(Boolean isSuspended) {
        this.isSuspended = isSuspended;
    }

    public Boolean getIsBanned() {
        return isBanned;
    }

    public void setIsBanned(Boolean isBanned) {
        this.isBanned = isBanned;
    }

    public OffsetDateTime getCreated() {
        return created;
    }

    public void setCreated(OffsetDateTime created) {
        this.created = created;
    }

    public OffsetDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(OffsetDateTime updated) {
        this.updated = updated;
    }

    public void setLast_Played(OffsetDateTime last_played) {
        this.lastPlayed = last_played;
    }

    public Boolean isShowDiscordOn() {
        return showDiscordMessages;
    }

    public void setShowDiscordMessages(Boolean showDiscordMessages) {
        this.showDiscordMessages = showDiscordMessages;
    }

    public Boolean isFlightDisabled() {
        return flightDisabled;
    }

    public void setFlightDisabled(Boolean flightDisabled) {
        this.flightDisabled = flightDisabled;
    }

    public Boolean isVanished() {
        return vanished;
    }

    public void setVanished(Boolean vanished) {
        this.vanished = vanished;
    }

    public Boolean isAutoInvSwapEnabled() {
        return autoInvSwapEnabled;
    }

    public void setAutoInvSwapEnabled(Boolean autoInvSwapEnabled) {
        this.autoInvSwapEnabled = autoInvSwapEnabled;
    }

    public Boolean isAutoRadioEnabled() {
        return autoRadioEnabled;
    }

    public void setAutoRadioEnabled(Boolean autoRadioEnabled) {
        this.autoRadioEnabled = autoRadioEnabled;
    }

    public Boolean isInSandboxMode() {
        return inSandboxMode;
    }

    public void setInSandboxMode(Boolean inSandboxMode) {
        this.inSandboxMode = inSandboxMode;
    }

    public Boolean isUsingCreativeInv() {
        return usingCreativeInv;
    }

    public void setUsingCreativeInv(Boolean usingCreativeInv) {
        this.usingCreativeInv = usingCreativeInv;
    }

    public ItemStack[] getSurvivalInv() {
        return survivalInv;
    }

    public void setSurvivalInv(ItemStack[] survivalInv) {
        this.survivalInv = survivalInv;
    }

    public ItemStack[] getCreativeInv() {
        return creativeInv;
    }

    public void setCreativeInv(ItemStack[] creativeInv) {
        this.creativeInv = creativeInv;
    }
}
