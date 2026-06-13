package com.totemaudio.mixin;

import com.totemaudio.TotemAudioMod;
import com.totemaudio.config.TotemConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class TotemPopMixin {

    @Inject(method = "handleEntityEvent", at = @At("HEAD"))
    private void totemaudio$onEntityEvent(ClientboundEntityEventPacket packet, CallbackInfo ci) {
        try { totemaudio$handle(packet); } catch (Throwable ignored) {}
    }

    @Unique
    private void totemaudio$handle(ClientboundEntityEventPacket packet) {
        if (packet.getEventId() != 35) return;
        TotemConfig cfg = TotemAudioMod.CONFIG;
        if (cfg == null || !cfg.enabled) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Entity entity = packet.getEntity(mc.level);
        if (entity == null) return;

        boolean self  = TotemAudioMod.isSelf(entity);
        float pitch   = self ? cfg.selfPitch  : cfg.enemyPitch;
        float volume  = self ? cfg.selfVolume : cfg.enemyVolume;
        String custom = self ? cfg.selfSound  : cfg.enemySound;

        if (custom != null && !custom.isEmpty()) {
            TotemAudioMod.playWav(custom, volume, pitch);
        } else {
            try {
                TotemAudioMod.PLAYING_OWN = true;
                mc.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(),
                        SoundEvents.TOTEM_USE, SoundSource.PLAYERS, volume, pitch, false);
            } finally {
                TotemAudioMod.PLAYING_OWN = false;
            }
        }
    }
}
