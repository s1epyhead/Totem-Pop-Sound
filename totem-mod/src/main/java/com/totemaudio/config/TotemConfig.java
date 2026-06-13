package com.totemaudio.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class TotemConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean enabled        = true;
    public boolean replaceVanilla = true;   // mute the vanilla totem sound
    public boolean chatAlert      = true;   // "[Totem] Name popped! (x2)"
    public String  selfSound      = "";     // .wav filename, "" = vanilla sound
    public String  enemySound     = "";
    public float   selfVolume     = 1.0f;
    public float   enemyVolume    = 1.0f;
    public float   selfPitch      = 1.0f;
    public float   enemyPitch     = 1.3f;

    public static Path soundsFolder() {
        Path dir = FabricLoader.getInstance().getConfigDir().resolve("totemaudio_sounds");
        try { Files.createDirectories(dir); } catch (Throwable ignored) {}
        return dir;
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("totemaudio.json");
    }

    public static TotemConfig load() {
        try {
            if (Files.exists(file())) {
                try (Reader r = Files.newBufferedReader(file())) {
                    TotemConfig c = GSON.fromJson(r, TotemConfig.class);
                    if (c != null) return c;
                }
            }
        } catch (Throwable ignored) {}
        TotemConfig c = new TotemConfig();
        c.save();
        return c;
    }

    public void save() {
        try (Writer w = Files.newBufferedWriter(file())) {
            GSON.toJson(this, w);
        } catch (Throwable ignored) {}
    }
}
