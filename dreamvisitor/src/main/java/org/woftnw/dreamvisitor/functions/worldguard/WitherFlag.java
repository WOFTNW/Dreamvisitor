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
import org.jetbrains.annotations.NotNull;

public class WitherFlag extends FlagValueChangeHandler<StateFlag.State> {

    public static final Factory FACTORY = new Factory();

    public static class Factory extends Handler.Factory<WitherFlag> {
        @Override
        public WitherFlag create(Session session) {
            // create an instance of a handler for the particular session
            // if you need to pass certain variables based on, for example, the player
            // whose session this is, do it here
            return new WitherFlag(session);
        }
    }

    // construct with your desired flag to track changes
    public WitherFlag(Session session) {
        super(session, Dreamvisitor.WITHER);
    }

    // ... override handler methods here

    @Override
    protected void onInitialValue(@NotNull LocalPlayer player, ApplicableRegionSet set, StateFlag.State value) {}

    @Override
    protected boolean onSetValue(@NotNull LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, StateFlag.State currentValue, StateFlag.State lastValue, MoveType moveType) {return true;}

    @Override
    protected boolean onAbsentValue(@NotNull LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, StateFlag.State lastValue, MoveType moveType) {return true;}
}