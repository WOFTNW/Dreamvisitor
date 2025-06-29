package org.woftnw.dreamvisitor.functions.worldguard;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;
import org.woftnw.dreamvisitor.Dreamvisitor;
import org.woftnw.dreamvisitor.functions.Flight;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DragonFlightFlag extends FlagValueChangeHandler<StateFlag.State> {

    public static final Factory FACTORY = new Factory();

    public static class Factory extends Handler.Factory<DragonFlightFlag> {
        @Override
        public DragonFlightFlag create(Session session) {
            // create an instance of a handler for the particular session
            // if you need to pass certain variables based on, for example, the player
            // whose session this is, do it here
            return new DragonFlightFlag(session);
        }
    }

    // construct with your desired flag to track changes
    public DragonFlightFlag(Session session) {
        super(session, Dreamvisitor.DRAGON_FLIGHT);
    }

    // ... override handler methods here

    @Override
    protected void onInitialValue(@NotNull LocalPlayer player, ApplicableRegionSet set, StateFlag.State value) {
        Player bukkitPlayer = Objects.requireNonNull(Bukkit.getPlayer(player.getUniqueId()));
        boolean allowed = value != StateFlag.State.DENY;
        Flight.setFlightRestricted(bukkitPlayer, !allowed);
    }

    @Override
    protected boolean onSetValue(@NotNull LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, StateFlag.State currentValue, StateFlag.State lastValue, MoveType moveType) {
        Player bukkitPlayer = Objects.requireNonNull(Bukkit.getPlayer(player.getUniqueId()));
        boolean allowed = currentValue != StateFlag.State.DENY;
        Flight.setFlightRestricted(bukkitPlayer, !allowed);
        return true;
    }

    @Override
    protected boolean onAbsentValue(@NotNull LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, StateFlag.State lastValue, MoveType moveType) {
        Player bukkitPlayer = Objects.requireNonNull(Bukkit.getPlayer(player.getUniqueId()));
        Flight.setFlightRestricted(bukkitPlayer, false);
        return true;
    }
}