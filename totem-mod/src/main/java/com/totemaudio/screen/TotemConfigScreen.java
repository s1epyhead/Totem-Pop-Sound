package com.totemaudio.screen;

import com.totemaudio.TotemAudioMod;
import com.totemaudio.config.TotemConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.function.Consumer;

public class TotemConfigScreen extends Screen {

    private final Screen parent;
    private final TotemConfig cfg;
    private static final int W = 220, H = 20, GAP = 24;

    public TotemConfigScreen(Screen parent) {
        super(Component.literal("Totem Audio"));
        this.parent = parent;
        this.cfg = TotemAudioMod.CONFIG;
    }

    @Override
    protected void init() {
        int cx = width / 2 - W / 2;
        int y = 36;

        addToggle(cx, y, "Mod Enabled", cfg.enabled, v -> cfg.enabled = v); y += GAP;
        addToggle(cx, y, "Replace Vanilla Sound", cfg.replaceVanilla, v -> cfg.replaceVanilla = v); y += GAP;
        addToggle(cx, y, "Chat Alert on Enemy Pop", cfg.chatAlert, v -> cfg.chatAlert = v); y += GAP;
        addSlider(cx, y, "Self Pitch: ",   cfg.selfPitch,  0.5, 2.0, v -> cfg.selfPitch = v); y += GAP;
        addSlider(cx, y, "Enemy Pitch: ",  cfg.enemyPitch, 0.5, 2.0, v -> cfg.enemyPitch = v); y += GAP;
        addSlider(cx, y, "Self Volume: ",  cfg.selfVolume,  0.0, 2.0, v -> cfg.selfVolume = v); y += GAP;
        addSlider(cx, y, "Enemy Volume: ", cfg.enemyVolume, 0.0, 2.0, v -> cfg.enemyVolume = v); y += GAP;

        addRenderableWidget(Button.builder(Component.literal("\uD83D\uDCC2 Open Sounds Folder (.wav)"),
                b -> openFolder()).bounds(cx, y, W, H).build()); y += GAP;

        addSoundPicker(cx, y, "Self Sound", true);  y += GAP;
        addSoundPicker(cx, y, "Enemy Sound", false); y += GAP;

        // test buttons
        addRenderableWidget(Button.builder(Component.literal("Test Self"),
                b -> test(true)).bounds(cx, y, W / 2 - 2, H).build());
        addRenderableWidget(Button.builder(Component.literal("Test Enemy"),
                b -> test(false)).bounds(cx + W / 2 + 2, y, W / 2 - 2, H).build());
        y += GAP + 4;

        addRenderableWidget(Button.builder(Component.literal("Done"),
                b -> onClose()).bounds(cx, y, W, H).build());
    }

    private void test(boolean self) {
        try {
            String file = self ? cfg.selfSound : cfg.enemySound;
            float vol   = self ? cfg.selfVolume : cfg.enemyVolume;
            float pit   = self ? cfg.selfPitch  : cfg.enemyPitch;
            if (file != null && !file.isEmpty()) {
                TotemAudioMod.playWav(file, vol, pit);
            } else if (minecraft != null && minecraft.player != null && minecraft.level != null) {
                TotemAudioMod.PLAYING_OWN = true;
                try {
                    minecraft.level.playLocalSound(minecraft.player.getX(), minecraft.player.getY(),
                            minecraft.player.getZ(), net.minecraft.sounds.SoundEvents.TOTEM_USE,
                            net.minecraft.sounds.SoundSource.PLAYERS, vol, pit, false);
                } finally { TotemAudioMod.PLAYING_OWN = false; }
            }
        } catch (Throwable ignored) {}
    }

    private void openFolder() {
        Path dir = TotemConfig.soundsFolder();
        new Thread(() -> {
            try {
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().open(dir.toFile());
                    return;
                }
            } catch (Throwable ignored) {}
            try { // windows fallback
                Runtime.getRuntime().exec(new String[]{"explorer", dir.toAbsolutePath().toString()});
            } catch (Throwable ignored) {}
        }, "TotemAudio-OpenFolder").start();
    }

    private void addToggle(int x, int y, String label, boolean initial, Consumer<Boolean> setter) {
        final boolean[] st = {initial};
        addRenderableWidget(Button.builder(tog(label, st[0]), b -> {
            st[0] = !st[0];
            setter.accept(st[0]);
            b.setMessage(tog(label, st[0]));
        }).bounds(x, y, W, H).build());
    }

    private Component tog(String label, boolean on) {
        return Component.literal(label + ": " + (on ? "\u00A7aON" : "\u00A7cOFF"));
    }

    private void addSlider(int x, int y, String label, float init, double min, double max, Consumer<Float> setter) {
        double norm = (init - min) / (max - min);
        addRenderableWidget(new AbstractSliderButton(x, y, W, H,
                Component.literal(label + String.format("%.2f", init)), norm) {
            @Override protected void updateMessage() {
                setMessage(Component.literal(label + String.format("%.2f", min + value * (max - min))));
            }
            @Override protected void applyValue() {
                setter.accept((float) (min + value * (max - min)));
            }
        });
    }

    private void addSoundPicker(int x, int y, String label, boolean isSelf) {
        String[] f = TotemConfig.soundsFolder().toFile()
                .list((d, n) -> n.toLowerCase().endsWith(".wav"));
        final String[] files = f == null ? new String[0] : f;
        String cur = isSelf ? cfg.selfSound : cfg.enemySound;
        final int[] idx = {indexOf(files, cur)};
        addRenderableWidget(Button.builder(pickLabel(label, cur), b -> {
            idx[0] = (idx[0] + 1) % (files.length + 1);
            String picked = idx[0] == 0 ? "" : files[idx[0] - 1];
            if (isSelf) cfg.selfSound = picked; else cfg.enemySound = picked;
            b.setMessage(pickLabel(label, picked));
        }).bounds(x, y, W, H).build());
    }

    private Component pickLabel(String label, String file) {
        return Component.literal(label + ": \u00A7e" + (file == null || file.isEmpty() ? "Vanilla" : file));
    }

    private int indexOf(String[] arr, String v) {
        if (v == null || v.isEmpty()) return 0;
        for (int i = 0; i < arr.length; i++) if (arr[i].equals(v)) return i + 1;
        return 0;
    }

    @Override
    public void onClose() {
        try { cfg.save(); } catch (Throwable ignored) {}
        if (minecraft != null) minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float delta) {
        super.render(g, mx, my, delta);
        g.drawCenteredString(font, title, width / 2, 14, 0xFFFFFF);
    }
}
