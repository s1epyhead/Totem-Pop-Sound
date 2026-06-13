# Totem Audio v3 — Minecraft 1.21.11 Fabric (client-side)

Custom totem pop sounds for CrystalPvP.

## Features
- Different sound/pitch/volume for YOUR pops vs ENEMY pops
- Drop custom .wav files into a folder, pick them in the config (no restart)
- Chat alert with per-player pop counter: `[Totem] Player popped! (x3)`
- "Replace Vanilla Sound" toggle (mutes only minecraft:item.totem.use)
- Test buttons to preview sounds
- ModMenu config screen (no keybind needed)

## Crash-proofing
- No Fabric API dependency, no keybindings (the 1.21.11 KeyMapping API change crashed v2)
- Every mixin is optional (`required: false`, `defaultRequire: 0`) — if a future MC version
  changes internals, the mod silently degrades instead of crashing the game
- All handlers wrapped in catch(Throwable)

## Build
Requires JDK 21+ installed (21 or 24 both fine).
```
gradlew.bat build        (Windows)
./gradlew build          (Mac/Linux)
```
Jar: `build/libs/totemaudio-3.0.0.jar`

## Custom sounds
1. Open ModMenu → Totem Audio → "Open Sounds Folder"
2. Drop `.wav` files in (convert .ogg/.mp3 to .wav with any free online converter)
3. Click "Self Sound"/"Enemy Sound" to cycle to your file
4. Hit Test buttons to preview

Config file: `config/totemaudio.json` • Sounds: `config/totemaudio_sounds/`
