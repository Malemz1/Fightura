---
title: Installation
description: How to install Fightura alongside Figura and Epic Fight.
---

## Requirements

| Component | Version |
| --- | --- |
| Minecraft | `1.20.1` |
| Forge | `47.x` (tested with `47.4.10`) |
| Java | `17` |
| Figura | `0.1.5+1.20.1-forge` or newer |
| Epic Fight | `20.14.15.1` or newer |

Fightura is **client-only**. Servers don't need it. If you play on a server with friends, only the players who want the visual integration need to install it.

## Install steps

1. Download the latest `fightura-1.0.0.jar` from the [Releases page](https://github.com/Malemz1/Fightura/releases).

2. Open your Minecraft instance's `mods/` folder.

   - **CurseForge / Prism / MultiMC:** click *Open Folder* on the instance, then go into `mods/`
   - **Vanilla launcher:** `%APPDATA%\.minecraft\mods` on Windows, `~/Library/Application Support/minecraft/mods` on macOS

3. Drop these three jars into `mods/`:

   ```
   fightura-1.0.0.jar
   figura-0.1.5+1.20.1-forge-mc.jar
   epic-fight-20.14.15.1-mc1.20.1-forge.jar
   ```

4. Launch the client. You should see this in the log around mod loading:

   ```
   [Fightura] Fightura starting
   [Fightura] Fightura registering events
   [Fightura] Fightura attached render layer to Epic Fight PLAYER renderer
   ```

## Verify it works

1. Equip any Figura avatar (the dot menu in-game)
2. Switch to Epic Fight battle mode (default key: `R`)
3. Move and attack — your Figura body should follow the Epic Fight animation
4. Head accessories (hair, hat) should stick to the head during attacks

If the avatar's body floats above the actual combat pose, or accessories don't follow, see [Troubleshooting](/troubleshooting/).

## Uninstall

Just delete `fightura-1.0.0.jar`. No config files persist outside the jar; Figura and Epic Fight continue to work normally.

## Next

- [Naming Conventions](/naming/) — how Fightura decides which Figura part follows which joint
- [Lua API](/lua-api/) — bind custom bone names + read Epic Fight state from your scripts
