# Fightura

Fightura is a free Forge `1.20.1` client mod by `RowletDev` that lets `Figura` avatars render correctly while `Epic Fight` is active.

## Goal

One pipeline. Every Figura part that maps to an Epic Fight joint follows that joint. Everything else falls through to Figura's normal render. No accessory vs full-body distinction, no mode flags.

## Requirements

- Minecraft `1.20.1`
- Forge `47.x`
- Java `17`
- Figura `0.1.5+1.20.1-forge`
- Epic Fight `20.14.15.1+`

## Install

Drop into your client `mods/`:

- `fightura-1.0.0.jar`
- Figura
- Epic Fight

Server-side: nothing required. Each player who wants the feature installs it on their client.

## How It Works

Fightura attaches a custom layer to Epic Fight's player render pipeline. Every frame:

1. Snapshot Epic Fight's current pose (armature + joint matrices) for the player
2. For each Figura part in the avatar, resolve a target joint:
   - by `parentType` — `ParentType.Head` → joint `Head`, `Body` → `Chest`, etc.
   - by part `name` — recognized aliases (see below)
   - by Lua override (see [`mapBone`](#lua-overrides) below)
3. If a joint is resolved, the part's matrix becomes `ancestor⁻¹ × joint × local` — the part anchors to that joint's current Epic Fight pose
4. If no joint is found, Figura's normal recalculation runs — child parts inherit through their ancestor automatically

Result: head accessories follow the head joint, sleeves follow the arms, custom bend bones (`LArmLower`, `LeftKnee`) follow forearm/shin, anything else inherits through its parent.

## Naming Convention

If your avatar's part names match any of these, Fightura tracks them automatically — no Lua needed.

### English (most common)

| Aliases | Epic Fight joint |
|---|---|
| `Head`, `Neck`, `Hat` | `Head` |
| `Body`, `Chest`, `Torso`, `UpperChest`, `Jacket` | `Chest` |
| `Spine`, `Spine1`, `Spine2`, `Waist` | `Spine` / `Chest` |
| `Hips`, `Hip`, `Pelvis` | `Pelvis` |
| `LeftArm`, `LeftShoulder`, `LeftUpperArm`, `LeftSleeve` | `Arm_L` |
| `LeftHand`, `LeftForearm`, `LeftLowerArm`, `LeftElbow`, `LArmLower` | `Hand_L` |
| `RightArm`, `RightShoulder`, `RightUpperArm`, `RightSleeve` | `Arm_R` |
| `RightHand`, `RightForearm`, `RightLowerArm`, `RightElbow`, `RArmLower` | `Hand_R` |
| `LeftLeg`, `LeftThigh`, `LeftUpperLeg`, `LeftPants` | `Thigh_L` |
| `LeftFoot`, `LeftShin`, `LeftLowerLeg`, `LeftKnee`, `LLegLower` | `Leg_L` |
| `RightLeg`, `RightThigh`, `RightUpperLeg`, `RightPants` | `Thigh_R` |
| `RightFoot`, `RightShin`, `RightLowerLeg`, `RightKnee`, `RLegLower` | `Leg_R` |

Names are case-insensitive.

### Mixamo standard
`mixamorig:Head`, `mixamorig:Hips`, `mixamorig:Spine`, `mixamorig:LeftArm`, `mixamorig:LeftForeArm`, `mixamorig:LeftHand`, `mixamorig:LeftUpLeg`, `mixamorig:LeftLeg`, `mixamorig:LeftFoot` (and `Right*` mirrors).

### Blender Rigify
`upper_arm.L`, `forearm.L`, `hand.L`, `thigh.L`, `shin.L`, `foot.L` (and `.R` mirrors). Plus `spine.001` … `spine.003`.

### VRM / VRoid
`J_Bip_C_Head`, `J_Bip_C_Hips`, `J_Bip_C_Spine`, `J_Bip_L_UpperArm`, `J_Bip_L_LowerArm`, `J_Bip_L_Hand`, `J_Bip_L_UpperLeg`, `J_Bip_L_LowerLeg`, `J_Bip_L_Foot` (and `R` mirrors).

Call `fightura:getSupportedBones()` from a script to dump the full alias list.

## Lua Overrides

Fightura exposes a small Lua API for runtime overrides and Epic Fight state queries.

### Custom bone mapping

If your avatar uses a name we don't recognize, bind it:

```lua
function events.LOAD()
  fightura:mapBone("MyTailRoot", "Pelvis")
  fightura:mapBone("LeftSleeveCuff", "Hand_L")
  fightura:mapBone("HornLeft", "Head")
end
```

`mapBone` is name-based. The first arg is the alias (matches `parentType` name OR part name, case-insensitive). The second is an Epic Fight joint name (`Head`, `Chest`, `Arm_L`, `Hand_L`, `Thigh_L`, `Leg_L`, etc.).

```lua
fightura:clearBone("MyTailRoot")  -- remove one
fightura:clearBones()              -- remove all overrides for this avatar
```

### Epic Fight state queries

```lua
fightura:isAvailable()     -- true if Epic Fight has a patch on this entity
fightura:isEpicFightMode() -- true when in battle stance
fightura:isAttacking()     -- true during attack animation
fightura:hasPose()         -- true if a pose snapshot is available this frame
```

### Joint queries

```lua
local rot  = fightura:getJointRotation("Hand_L")  -- FiguraVec3, radians (ZYX euler)
local mat  = fightura:getJointMatrix("Head")       -- FiguraMat4
local list = fightura:getJoints()                  -- all joint names in current armature
```

See [examples/figura/fightura_example.lua](examples/figura/fightura_example.lua).

## Build

```powershell
.\gradlew.bat clean build
```

Output: `build/libs/fightura-1.0.0.jar`

Dependencies are read from the path in [gradle.properties](gradle.properties).

## Scope

Fightura aims to make Figura avatars work with Epic Fight without any model format change. Avatars built for normal Figura rendering should work as-is when their part names match the conventions above; otherwise a few `mapBone` calls in `events.LOAD` cover the gap.
