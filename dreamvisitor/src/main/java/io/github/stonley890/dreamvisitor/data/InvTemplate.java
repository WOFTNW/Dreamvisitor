package io.github.stonley890.dreamvisitor.data;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class InvTemplate implements ConfigurationSerializable {

    private ItemStack[] contents;
    private String name;

    public InvTemplate(ItemStack[] contents, String name) {
        this.contents = contents;
        this.name = name;
    }

    public ItemStack[] getContents() {
        return contents;
    }

    public String getName() {
        return name;
    }

    public void setContents(ItemStack[] contents) {
        this.contents = contents;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        InvTemplate that = (InvTemplate) o;
        return Arrays.equals(contents, that.contents) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(contents), name);
    }

    @NotNull
    @Contract("_ -> new")
    public static InvTemplate deserialize(@NotNull Map<String, Object> map) {

        ArrayList<ItemStack> contents = (ArrayList<ItemStack>) map.get("contents");

        return new InvTemplate(
                contents.toArray(new ItemStack[0]),
                (String) map.get("name"));
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        return Map.of("contents", contents, "name", name);
    }
}
