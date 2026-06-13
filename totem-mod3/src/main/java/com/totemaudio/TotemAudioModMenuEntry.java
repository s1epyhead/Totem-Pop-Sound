package com.totemaudio;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.totemaudio.screen.TotemConfigScreen;

public class TotemAudioModMenuEntry implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return TotemConfigScreen::new;
    }
}
