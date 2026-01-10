package io.github.stonley890.dreamvisitor.data;

import org.bukkit.NamespacedKey;

public class MusicTrack {

    private final NamespacedKey key;
    private final String name;
    private final long duration;

    public MusicTrack(NamespacedKey namespacedKey, String trackName, long tickDuration) {
        key = namespacedKey;
        name = trackName;
        duration = tickDuration;
    }

    public long getDuration() {
        return duration;
    }

    public NamespacedKey getKey() {
        return key;
    }

    public String getName() {
        return name;
    }
}
