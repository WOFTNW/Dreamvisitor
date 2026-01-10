package io.github.stonley890.dreamvisitor.functions;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.MusicTrack;
import org.bukkit.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MusicManager {
    public List<MusicTrack> TRACKS = new ArrayList<>();
    private final Random random = new Random();

    public void init() {
        TRACKS.add(new MusicTrack(NamespacedKey.fromString("music.tnw.droplets"), "Droplets", 2900));
        TRACKS.add(new MusicTrack(NamespacedKey.fromString("music.tnw.starry_whispers"), "Starry Whispers", 2760));

        runTask();
    }

    public void runTask() {
        MusicTrack trackToPlay = randomTrack();

        Bukkit.getScheduler().runTaskLater(
            Dreamvisitor.getPlugin(),
            () -> {
                MusicTrack musicTrack = randomTrack();
                playTrack(musicTrack);
            },
            trackToPlay.getDuration()
        );
    }

    public MusicTrack randomTrack() {
        int numberOfTracks = TRACKS.size();
        int i = random.nextInt(numberOfTracks);
        return TRACKS.get(i);
    }

    public void playTrack(@NotNull MusicTrack track) {
        Sound sound = Registry.SOUNDS.getOrThrow(track.getKey());
        Bukkit.getOnlinePlayers().forEach(
                player -> player.playSound(player.getLocation(), sound, SoundCategory.MUSIC, 1, 1)
        );
    }

}
