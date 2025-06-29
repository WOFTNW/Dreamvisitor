package org.woftnw.dreamvisitor.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class TribeUtil {

    public static final Tribe[] tribes = Tribe.values();

    /**
     * Parse a string to a {@link Tribe}. This works both with and without the -Wing suffix.
     * @param string the string to parse.
     * @return the {@link Tribe} associated with that name, or null if none is found.
     */
    @Nullable
    public static Tribe parse(@Nullable String string) {
        if (string == null) return null;
        for (Tribe tribe : tribes) {
            if (Objects.equals(string.toUpperCase(), tribe.getName().toUpperCase()) || Objects.equals(string.toUpperCase(), tribe.getTeamName().toUpperCase()))
                return tribe;
        }
        return null;
    }

    /**
     * Get the index of a given tribe.
     * @param tribe the {@link Tribe} to search for.
     * @return the index of the tribe.
     */
    public static int indexOf(@NotNull Tribe tribe) {
        for (int i = 0; i < tribes.length; i++) {
            if (tribe == tribes[i]) return i;
        }
        throw new NullPointerException("That tribe does not exist!");
    }
}
