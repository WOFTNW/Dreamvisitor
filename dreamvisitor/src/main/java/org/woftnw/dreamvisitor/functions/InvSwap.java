package org.woftnw.dreamvisitor.functions;

import org.woftnw.dreamvisitor.data.PlayerUtility;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.woftnw.dreamvisitor.data.type.DVUser;

public class InvSwap {

    public static void swapInventories(@NotNull Player player) {

        DVUser user = PlayerUtility.getUser(player);

        ItemStack[] invContents;

        if (user.isUsingCreativeInv()) {
            user.setCreativeInv(player.getInventory().getContents());
            if (user.getSurvivalInv() == null) invContents = null;
            else invContents = user.getSurvivalInv();
        } else {
            user.setSurvivalInv(player.getInventory().getContents());
            if (user.getCreativeInv() == null) invContents = null;
            else invContents = user.getCreativeInv();
        }

        if (invContents == null) player.getInventory().clear();
        else player.getInventory().setContents(invContents);
        user.setUsingCreativeInv(!user.isUsingCreativeInv());

        PlayerUtility.saveUser(user);

    }

}
