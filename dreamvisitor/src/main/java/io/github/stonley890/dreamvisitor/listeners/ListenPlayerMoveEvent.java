package io.github.stonley890.dreamvisitor.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.InvTemplate;
import io.github.stonley890.dreamvisitor.functions.InvTemplates;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public class ListenPlayerMoveEvent implements Listener {

    @EventHandler
    public void onPlayerMoveEvent(@NotNull PlayerMoveEvent event) {
        // Inventory template region flag
        if (event.getTo() == null) return;
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(event.getPlayer());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(event.getTo()));
        String invTemplateName = set.queryValue(localPlayer, Dreamvisitor.INV_TEMPLATE);

        InvTemplate template = InvTemplates.getInvTemplateByName(invTemplateName);

        if (template == null) InvTemplates.unapplyPlayer(event.getPlayer());
        else InvTemplates.applyToPlayer(event.getPlayer(), template, false);
    }

}
