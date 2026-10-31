package com.example.mmplayer;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.zabyt.lib.ZabytLib;

public class MMPlayerModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> ZabytLib.createConfigScreen("mmplayer", MMPlayerConfig.get(), parent);
    }
}
