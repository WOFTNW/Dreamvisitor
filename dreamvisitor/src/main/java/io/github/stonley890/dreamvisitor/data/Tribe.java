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
    @NotNull
    private String icon = "\uD83D\uDC09"; // Dragon emoji by default

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

        HIVE.icon = "<:HiveIcon:1059558826929569884>";
        ICE.icon = "<:IceIcon:996925736508203058>";
        LEAF.icon = "<:LeafIcon:1059555593096011796>";
        MUD.icon = "<:MudIcon:996925738026537101>";
        NIGHT.icon = "<:NightIcon:996925740476018758>";
        RAIN.icon = "<:RainIcon:996925741562331136>";
        SAND.icon = "<:SandIcon:996925743055507517>";
        SEA.icon = "<:SeaIcon:996925744980688896>";
        SILK.icon = "<:SilkIcon:1059548640810639361>";
        SKY.icon = "<:SkyIcon:996925746436128809>";
        HUMAN.icon = "\uD83E\uDDCD";
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

    /**
     * Get the icon of this tribe.
     *
     * @return The Discord emoji for this tribe.
     */
    @NotNull
    public String getIcon() { return icon; }
}
