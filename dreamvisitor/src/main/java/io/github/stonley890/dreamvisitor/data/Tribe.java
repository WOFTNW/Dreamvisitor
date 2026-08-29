package io.github.stonley890.dreamvisitor.data;

import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public enum Tribe {

    HIVE,
    ICE,
    LEAF,
    MUD,
    NIGHT,
    RAIN,
    SAND,
    SEA,
    SILK,
    SKY,
    HUMAN;

    @NotNull
    private String name = "Undefined.";
    @NotNull
    private ChatColor color = ChatColor.WHITE;

    static {
        HIVE.name = "Hive";
        ICE.name = "Ice";
        LEAF.name = "Leaf";
        MUD.name = "Mud";
        NIGHT.name = "Night";
        RAIN.name = "Rain";
        SAND.name = "Sand";
        SEA.name = "Sea";
        SILK.name = "Silk";
        SKY.name = "Sky";
        HUMAN.name = "Human";

        HIVE.color = ChatColor.GOLD;
        ICE.color = ChatColor.AQUA;
        LEAF.color = ChatColor.DARK_GREEN;
        MUD.color = ChatColor.RED;
        NIGHT.color = ChatColor.DARK_PURPLE;
        RAIN.color = ChatColor.GREEN;
        SAND.color = ChatColor.YELLOW;
        SEA.color = ChatColor.BLUE;
        SILK.color = ChatColor.LIGHT_PURPLE;
        SKY.color = ChatColor.DARK_RED;
        HUMAN.color = ChatColor.GRAY;
    }

    /**
     * Get the name of this tribe without the -Wing suffix.
     *
     * @return The name.
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Get the name of this tribe with the -Wing suffix.
     *
     * @return The team name.
     */
    @NotNull
    @Contract(pure = true)
    public String getTeamName() { return this == HUMAN ? name : name + "Wing"; }

    /**
     * Get the color of this tribe.
     *
     * @return The {@link ChatColor} of this tribe.
     */
    @NotNull
    public ChatColor getColor() {
        return color;
    }

}
