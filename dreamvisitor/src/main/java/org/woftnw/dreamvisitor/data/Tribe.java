package org.woftnw.dreamvisitor.data;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
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
    SKY;

    @NotNull
    private String name = "Undefined.";
    @NotNull
    private TextColor color = NamedTextColor.WHITE;

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

        HIVE.color = NamedTextColor.GOLD;
        ICE.color = NamedTextColor.AQUA;
        LEAF.color = NamedTextColor.DARK_GREEN;
        MUD.color = NamedTextColor.RED;
        NIGHT.color = NamedTextColor.DARK_PURPLE;
        RAIN.color = NamedTextColor.GREEN;
        SAND.color = NamedTextColor.YELLOW;
        SEA.color = NamedTextColor.BLUE;
        SILK.color = NamedTextColor.LIGHT_PURPLE;
        SKY.color = NamedTextColor.DARK_RED;
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
    public String getTeamName() {
        return name + "Wing";
    }

    /**
     * Get the color of this tribe.
     *
     * @return The {@link TextColor} of this tribe.
     */
    @NotNull
    public TextColor getColor() {
        return color;
    }

}
