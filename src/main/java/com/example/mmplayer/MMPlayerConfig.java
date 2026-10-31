package com.example.mmplayer;

import com.zabyt.lib.config.ConfigEntry;
import com.zabyt.lib.config.SimpleConfig;

public class MMPlayerConfig extends SimpleConfig {

    private static MMPlayerConfig instance;

    @ConfigEntry(label = "Music volume", min = 0.0, max = 100.0, step = 1)
    public int musicVolume = 100;

    protected MMPlayerConfig() {
        super("mmplayer");
    }

    public float volumeScalar() {
        return Math.clamp(musicVolume, 0, 100) / 100.0f;
    }

    public static MMPlayerConfig get() {
        if (instance == null) {
            instance = load("mmplayer", MMPlayerConfig.class);
        }
        return instance;
    }
}
