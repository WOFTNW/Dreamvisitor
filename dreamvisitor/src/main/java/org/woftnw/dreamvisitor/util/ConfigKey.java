package org.woftnw.dreamvisitor.util;

import org.bukkit.Location;

/**
 * An enum that holds all the relevant configuration keys. Each key has a default value and a type.
 */
public enum ConfigKey {

    DEBUG("debug", true, Boolean.class), // This is intentionally set to true because these default should typically only be used when no database is connected.
    AUTO_RESTART("auto_restart", false, Boolean.class),
    PAUSE_CHAT("pause_chat", false, Boolean.class),
    SOFT_WHITELIST("soft_whitelist", false, Boolean.class),
    DISABLE_PVP("disable_pvp", false, Boolean.class),
    PLAYER_LIMIT("player_limit", -1, Integer.class),
    RESOURCE_PACK_REPO("resource_pack_repo", "WOFTNW/Dragonspeak", String.class),
    HUB_LOCATION("hub_location", null, Location.class),
    LOG_CONSOLE("log_console", false, Boolean.class),
    ENABLE_LOG_CONSOLE_COMMANDS("enable_log_console_commands", false, Boolean.class), // Technically this only need to be read by DreamvisitorHub, but it's a good idea to have it here for redundancy.
    MAIL_DELIVERY_LOCATION_SELECTION_DISTANCE_WEIGHT_MULTIPLIER("mail_delivery_location_selection_distance_weight_multiplier", 1.00, Double.class),
    MAIL_DISTANCE_TO_REWARD_MULTIPLIER("mail_distance_to_reward_multiplier", 0.05, Double.class),
    FLIGHT_ENERGY_CAPACITY("flight_energy_capacity", 400, Integer.class),
    FLIGHT_REGENERATION_POINT("flight_regeneration_point", 200.00, Double.class),
    FLIGHT_ENERGY_REGENERATION("flight_energy_regeneration", 1.00, Double.class),
    FLIGHT_ENERGY_DEPLETION_X_Z_MULTIPLIER("flight_energy_destruction_x_z_multiplier", 1.00, Double.class),
    FLIGHT_ENERGY_DEPLETION_Y_MULTIPLIER("flight_energy_destruction_y_multiplier", 2.00, Double.class),
    DAYS_UNTIL_INACTIVE_TAX("days_until_inactive_tax", 60, Integer.class),
    INACTIVE_TAX_PERCENT("inactive_tax_percent", 0.1, Double.class),
    INACTIVE_DAY_FREQUENCY("inactive_day_frequency", 7, Integer.class),
    INACTIVE_TAX_STOP("inactive_tax_stop", 50000, Integer.class),
    NO_WITHER_NOTICE("no_wither_notice", "Withers cannot be spawned here. You can only spawn Withers in the Wither chamber.", String.class);

    private final String key;
    private final Object defaultValue;
    private final Class<?> type;

    ConfigKey(String key, Object defaultValue, Class<?> type) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public Class<?> getType() {
        return type;
    }

}
