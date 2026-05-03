---
title: Troubleshooting
description: Common issues with Fightura and how to fix them.
---

## Body floats above the actual combat pose

**Symptom:** The Figura body stays at standing height while the Epic Fight body rolls/dives at ground level.

**Cause:** The avatar has body parts whose names don't match the alias list, so they fall through to Figura's default render.

**Fix:** Either rename the parts to match the [naming conventions](/naming/), or bind them in Lua:

```lua
function events.LOAD()
  fightura:mapBone("MyChest", "Chest")
  fightura:mapBone("LeftUpperArm01", "Arm_L")
  -- ... etc for each unmapped body part
end
```

## Head accessories appear at chest level

**Symptom:** Hair/hat is rendering near the body instead of on top of the head.

**Cause:** The accessory part has no recognizable `parentType` (Head) and its name isn't a known alias.

**Fix:** Either set the part's parent type to Head in your `.bbmodel`, or bind it:

```lua
fightura:mapBone("MyHairRoot", "Head")
```

## Avatar doesn't animate at all (Lua animations look frozen)

**Symptom:** Idle wave, blinking, sword-draw animations defined in `events.RENDER` don't play.

**Cause:** Older Fightura builds didn't fire `renderEvent`. Update to the latest jar.

**Verify:** In your Lua script, this should print on every frame the avatar is rendered:

```lua
function events.RENDER(delta)
  print("rendering!", delta)
end
```

If you see nothing, the render event isn't firing — make sure you're on Fightura 1.0.0 or newer, and Figura is loaded correctly.

## Mod won't load — `ClassCastException` or `LinkageError`

**Symptom:** Crash on world load, log mentions Fightura.

**Cause:** Most likely a Figura/Epic Fight version mismatch. Fightura tests against:

- Figura `0.1.5+1.20.1-forge`
- Epic Fight `20.14.15.1`

**Fix:**
1. Check the crash report at `crash-reports/crash-*.txt`
2. If it mentions "mixin failed" or "ClassCastException involving FighturaPlayerRendererAccessor" — verify Fightura was built with mixingradle (look for `fightura.refmap.json` inside the jar; if missing, rebuild)
3. If it mentions Figura/Epic Fight class missing — check those mods are installed at the correct versions

Fightura's mod constructor wraps initialization in a `try { ... } catch (LinkageError | RuntimeException)` block, so version mismatches usually log an error instead of crashing the game outright.

## Custom bone alias doesn't apply

**Symptom:** You called `fightura:mapBone("MyPart", "Hand_L")` but the part isn't following the joint.

**Things to check:**

1. **Did `events.LOAD` fire?** Add `print("LOAD fired")` to confirm.
2. **Is the part name correct?** Names in Figura/Blockbench are case-sensitive in the model but case-insensitive in `mapBone`. Try `print(fightura:getSupportedBones())` to see what's registered.
3. **Is the joint name correct?** Joint names are **case-sensitive** and must match Epic Fight exactly. Try `print(fightura:getJoints())` to dump valid joints for the current entity.
4. **Did another script's `clearBones()` run?** Each avatar's overrides are cleared when the world unloads, but a sloppy script could call `clearBones()` accidentally.

## Mod loads but accessories aren't following anything

**Symptom:** Player renders normally but accessories sit at default vanilla model positions, ignoring Epic Fight pose.

**Cause:** The matrix bridge mixin (`FighturaPartMatrixMixin`) didn't apply. This usually means a Figura version newer than what Fightura tested against changed the internal `recalculate()` call site.

**Fix:** Open an issue on the [GitHub repo](https://github.com/Malemz1/Fightura/issues) with:
- Your Figura version
- Your Epic Fight version
- A screenshot of the broken rendering

This is a compatibility issue and needs a Fightura update.

## How to enable verbose logs

Fightura's logger uses the standard Forge log channel. To see only Fightura output:

```
grep -i "fightura" logs/latest.log
```

You should see at minimum:

```
[Fightura] Fightura starting
[Fightura] Fightura registering events
[Fightura] Fightura attached render layer to Epic Fight PLAYER renderer
[Fightura] Fightura attached render layer to Epic Fight first-person renderer
```

If the second pair of lines is missing, the `PatchedRenderersEvent.Modify` event didn't reach us — usually means Epic Fight didn't load, or another mod canceled the event.

## Still stuck?

Open an issue at <https://github.com/Malemz1/Fightura/issues> with:

1. Your `latest.log` (or relevant excerpt)
2. The crash report if applicable
3. Versions of Forge / Figura / Epic Fight / Fightura
4. Description of what you expected vs what you see
