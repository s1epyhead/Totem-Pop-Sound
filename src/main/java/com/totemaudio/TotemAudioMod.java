package com.totemaudio;

import com.totemaudio.config.TotemConfig;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TotemAudioMod implements ClientModInitializer {

    public static TotemConfig CONFIG = new TotemConfig();

    /** Pop counter per entity this session (for callouts). */
    public static final Map<UUID, Integer> POPS = new HashMap<>();

    /** True while WE are replaying the vanilla sound, so the mute mixin lets it through. */
    public static volatile boolean PLAYING_OWN = false;

    @Override
    public void onInitializeClient() {
        CONFIG = TotemConfig.load();
    }

    public static boolean isSelf(Entity e) {
        try {
            Player p = Minecraft.getInstance().player;
            return p != null && p.getUUID().equals(e.getUUID());
        } catch (Throwable t) { return false; }
    }

    /** Plays a user .wav file with volume + pitch. Fully sandboxed: can never crash the game. */
    public static void playWav(String fileName, float volume, float pitch) {
        Path file = TotemConfig.soundsFolder().resolve(fileName);
        if (!Files.isRegularFile(file)) return;
        final float vol = Math.max(0f, Math.min(2f, volume));
        final float pit = Math.max(0.25f, Math.min(4f, pitch));
        Thread t = new Thread(() -> {
            try (AudioInputStream raw = AudioSystem.getAudioInputStream(file.toFile())) {
                AudioFormat s = raw.getFormat();
                AudioFormat pcm = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                        s.getSampleRate(), 16, s.getChannels(), s.getChannels() * 2, s.getSampleRate(), false);
                try (AudioInputStream in = AudioSystem.getAudioInputStream(pcm, raw)) {
                    AudioFormat out = new AudioFormat(pcm.getSampleRate() * pit, 16, pcm.getChannels(), true, false);
                    SourceDataLine line = AudioSystem.getSourceDataLine(out);
                    line.open(out);
                    line.start();
                    byte[] buf = new byte[4096];
                    int n;
                    while ((n = in.read(buf)) > 0) {
                        for (int i = 0; i + 1 < n; i += 2) {
                            int smp = (short) ((buf[i] & 0xFF) | (buf[i + 1] << 8));
                            smp = (int) (smp * vol);
                            if (smp > 32767) smp = 32767;
                            if (smp < -32768) smp = -32768;
                            buf[i] = (byte) smp;
                            buf[i + 1] = (byte) (smp >> 8);
                        }
                        line.write(buf, 0, n);
                    }
                    line.drain();
                    line.close();
                }
            } catch (Throwable ignored) {}
        }, "TotemAudio-WAV");
        t.setDaemon(true);
        t.start();
    }
}
