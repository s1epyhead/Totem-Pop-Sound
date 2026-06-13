package com.totemaudio.mixin;

import com.totemaudio.TotemAudioMod;
import com.totemaudio.config.TotemConfig;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundManager.class)
public class VanillaMuteMixin {

    @Inject(method = "play", at = @At("HEAD"), cancellable = true, require = 0)
    private void totemaudio$mute(SoundInstance sound, CallbackInfo ci) {
        try {
            TotemConfig cfg = TotemAudioMod.CONFIG;
            if (cfg == null || !cfg.enabled || !cfg.replaceVanilla) return;
            if (TotemAudioMod.PLAYING_OWN) return; // our own replay passes through
            if (sound == null) return;
            String id = String.valueOf(sound.getLocation());
            if ("minecraft:item.totem.use".equals(id)) ci.cancel();
        } catch (Throwable ignored) {}
    }
}
