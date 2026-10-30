package com.example.mmplayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.Identifier;


public final class MusicPlayer {

    private static SoundInstance current;
    private static Identifier currentId;

    private MusicPlayer() {
    }

    public static void play(Minecraft minecraft, Identifier fileId) {
        SoundManager sounds = minecraft.getSoundManager();


        minecraft.getMusicManager().stopPlaying();
        stopCurrent(sounds);

        DirectSoundInstance instance = new DirectSoundInstance(fileId, isStreamed(fileId));
        SoundEngine.PlayResult result = sounds.play(instance);
        if (result != SoundEngine.PlayResult.NOT_STARTED) {
            current = instance;
            currentId = fileId;
        }
    }

    private static boolean isStreamed(Identifier fileId) {
        for (MusicLibrary.Track track : MusicLibrary.all()) {
            if (track.fileId().equals(fileId)) {
                return track.stream();
            }
        }
        return true;
    }

    public static void stop(Minecraft minecraft) {
        stopCurrent(minecraft.getSoundManager());
    }

    private static void stopCurrent(SoundManager sounds) {
        if (current != null) {
            sounds.stop(current);
            current = null;
            currentId = null;
        }
    }


    public static void tick(Minecraft minecraft) {
        if (current != null && !minecraft.getSoundManager().isActive(current)) {
            current = null;
        }
    }

    public static boolean isPlaying(Identifier fileId) {
        return hasCurrent() && currentId != null && currentId.equals(fileId);
    }

    public static Identifier currentId() {
        return currentId;
    }

    public static boolean hasCurrent() {
        return current != null;
    }
}
