# Totem Audio v3.1.0 — Minecraft 1.21.11 Fabric (client-side)

Custom totem pop sounds for CrystalPvP.

## Features
- Different sound/pitch/volume for YOUR pops vs ENEMY pops
- Drop custom `.wav`, `.mp3`, or `.ogg` files into a folder, pick them in the config (no restart)
- "Replace Vanilla Sound" toggle (mutes only `minecraft:item.totem.use`)
- Test buttons to preview your sounds before a fight
- Reset to Defaults button
- ModMenu config screen

## How to add custom sounds
1. Open ModMenu → Totem Audio → **"Open Sounds Folder (.wav, .mp3, .ogg)"**
2. Drop your audio files in
3. Click **"Self Sound"** or **"Enemy Sound"** to cycle to your file
4. Hit **Test Self** or **Test Enemy** to preview

Config file: `config/totemaudio.json` • Sounds folder: `config/totemaudio_sounds/`

## Crash-proofing
- No Fabric API dependency, no keybindings
- Every mixin is optional (`required: false`, `defaultRequire: 0`) — if a future MC version changes internals, the mod silently degrades instead of crashing
- All handlers wrapped in `catch(Throwable)`
## Build from source
Requires JDK 21+.
https://discord.gg/DfDqYvb9CZ
