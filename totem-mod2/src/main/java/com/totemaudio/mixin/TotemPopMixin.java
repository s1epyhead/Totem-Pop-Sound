package com.totemaudio.mixin;

import com.totemaudio.TotemAudioMod;
import com.totemaudio.config.TotemConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
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
        try {
            totemaudio$handle(packet);
        } catch (Throwable ignored) {} // never let anything escape into vanilla
    }

    @Unique
    private void totemaudio$handle(ClientboundEntityEventPacket packet) {
        if (packet.getEventId() != 35) return; // 35 = totem activation
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

        // pop counter
        int count = TotemAudioMod.POPS.merge(entity.getUUID(), 1, Integer::sum);

        if (custom != null && !custom.isEmpty()) {
            TotemAudioMod.playWav(custom, volume, pitch);
        } else {
            // replay vanilla totem sound at custom pitch; flag lets it bypass the mute
            try {
                TotemAudioMod.PLAYING_OWN = true;
                mc.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(),
                        SoundEvents.TOTEM_USE, SoundSource.PLAYERS, volume, pitch, false);
            } finally {
                TotemAudioMod.PLAYING_OWN = false;
            }
        }

        if (cfg.chatAlert && !self && mc.player != null) {
            mc.player.displayClientMessage(Component.literal(
                    "\u00A7c[Totem] \u00A7f" + entity.getName().getString()
                            + " \u00A77popped! \u00A78(x" + count + ")"), false);
        }
    }
}
