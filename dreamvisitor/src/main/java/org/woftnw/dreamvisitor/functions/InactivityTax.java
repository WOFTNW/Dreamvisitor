package org.woftnw.dreamvisitor.functions;

import com.earth2me.essentials.Essentials;
import org.woftnw.dreamvisitor.Dreamvisitor;
import org.woftnw.dreamvisitor.data.PlayerUtility;
import net.essentialsx.api.v2.services.BalanceTop;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class InactivityTax {

    /**
     * This function should be run at least once per day. It will not actually execute unless enough time has passed.
     */
    public static void tax() {

        final Dreamvisitor plugin = Dreamvisitor.getPlugin();
        final FileConfiguration config = plugin.getConfig();

        final int taxFrequency = config.getInt("inactiveDayFrequency");
        if (taxFrequency < 1) return; // Return if inactiveDayFrequency is 0 or less

        long lastInactiveTax = config.getLong("lastInactiveTax");
        if (lastInactiveTax != 0) {
            Instant lastTax = Instant.ofEpochMilli(lastInactiveTax);
            final Duration durationSinceLastTax = Duration.between(lastTax, Instant.now());
            if (durationSinceLastTax.minusDays(taxFrequency).isNegative()) return; // Return if last tax was less than lastTax days ago
        }


        plugin.getLogger().info("Collecting inactivity taxes");

        final Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        if (ess == null) {
            plugin.getLogger().warning("Dreamvisitor cannot tax players today because Essentials is not enabled.");
            return;
        }
        final BalanceTop balanceTop = ess.getBalanceTop();

        int inactiveDays = config.getInt("daysUntilInactiveTax");
        final BigDecimal percent = BigDecimal.valueOf(config.getDouble("inactiveTaxPercent"));
        if (percent.equals(BigDecimal.ZERO)) {
            plugin.getLogger().info("Tax percent is zero. Canceling");
            return;
        }
        if (percent.compareTo(BigDecimal.ONE) > 0) {
            plugin.getLogger().warning("Tax percent is greater than one. It must be between 0.0 and 1.0.");
            return;
        }
        final BigDecimal minBalance = BigDecimal.valueOf(config.getInt("inactiveTaxStop"));

        // Recalculate balance top.
        balanceTop.calculateBalanceTopMapAsync().thenRun(() -> {
            final Map<UUID, BalanceTop.Entry> balanceTopCache = balanceTop.getBalanceTopCache();
            // Check each UUID
            for (Map.Entry<UUID, BalanceTop.Entry> entry : balanceTopCache.entrySet()) {
                final UUID uuid = entry.getKey();
                try {

                    // Get balance
                    final BigDecimal balance = entry.getValue().getBalance();

                    // Continue if balance is minimum or lower
                    if (balance.compareTo(minBalance) <= 0) continue;

                    // Get last online and continue if not inactive
                    final Instant lastLogout = PlayerUtility.getLastLogout(uuid);
                    final Duration durationSinceLastLogout =  Duration.between(lastLogout, Instant.now());
                    if (durationSinceLastLogout.minusDays(inactiveDays).isNegative()) continue;

                    // If inactive, calculate new amount
                    BigDecimal newBalance = balance.subtract(balance.multiply(percent));

                    // If new balance is less than minimum, set it to the minimum
                    if (newBalance.compareTo(minBalance) < 0) newBalance = minBalance;

                    // Save new balance
                    ess.getUser(uuid).setMoney(newBalance);

                } catch (Exception e) {
                    // Essentials must be enabled to get to this point, so this should be impossible.
                    plugin.getLogger().warning("Inactivity tax failed: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            plugin.getLogger().info("Finished collecting inactivity taxes.");

        });
    }

}
