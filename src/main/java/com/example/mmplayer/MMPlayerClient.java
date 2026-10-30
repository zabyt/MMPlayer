package com.example.mmplayer;

import com.mojang.blaze3d.platform.InputConstants;
import com.zabyt.lib.ZabytLib;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MMPlayerClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("mmplayer");

    public static final KeyMapping OPEN_MUSIC_PLAYER = new KeyMapping(
            "key.mmplayer.open",
            InputConstants.KEY_M,
            KeyMapping.Category.MISC);

    @Override
    public void onInitializeClient() {
        ZabytLib.registerConfigUser("mmplayer");
        KeyMappingHelper.registerKeyMapping(OPEN_MUSIC_PLAYER);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            MusicPlayer.tick(client);
            while (OPEN_MUSIC_PLAYER.consumeClick()) {
                if (!(client.gui.screen() instanceof MusicPlayerScreen)) {
                    client.gui.setScreen(new MusicPlayerScreen(null));
                }
            }
        });

        LOGGER.info("MMPlayer initialized - press M to open the music player");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("mmplayer", path);
    }
}
