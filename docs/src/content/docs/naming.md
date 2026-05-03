---
title: Naming Conventions
description: How Fightura maps Figura parts to Epic Fight joints by name.
---

Fightura tracks every Figura part by:

1. **`parentType`** — Figura's built-in body region (Head, Body, LeftArm, …)
2. **part name** — case-insensitive match against a built-in alias dictionary
3. **Lua override** — your script can bind any name to any joint at runtime

If any of these resolve to an Epic Fight joint, that part follows the joint. If none resolve, the part falls through to Figura's normal render and inherits through its parent.

## Epic Fight joints

These are the joint names you can target:

| Joint | Position |
| --- | --- |
| `Root` | Entity origin |
| `Pelvis` | Hip |
| `Spine` | Lower torso |
| `Chest` | Upper torso |
| `Head` | Neck/head |
| `Arm_L`, `Arm_R` | Upper arm (shoulder pivot) |
| `Hand_L`, `Hand_R` | Forearm and hand |
| `Thigh_L`, `Thigh_R` | Upper leg |
| `Leg_L`, `Leg_R` | Lower leg and foot |

## Built-in part-name aliases

All matching is case-insensitive. Pick whichever style your modeling tool exports.

### English (most common)

| Aliases | Maps to |
| --- | --- |
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

### Mixamo standard

If you imported a rig from Mixamo, the bones already work:

| Mixamo bone | Maps to |
| --- | --- |
| `mixamorig:Head`, `mixamorig:Neck` | `Head` |
| `mixamorig:Hips` | `Pelvis` |
| `mixamorig:Spine`, `Spine1`, `Spine2` | `Spine` / `Chest` |
| `mixamorig:LeftShoulder`, `LeftArm` | `Arm_L` |
| `mixamorig:LeftForeArm`, `LeftHand` | `Hand_L` |
| `mixamorig:LeftUpLeg` | `Thigh_L` |
| `mixamorig:LeftLeg`, `LeftFoot` | `Leg_L` |
| (Right side mirrors above) | |

### Blender Rigify

| Bone | Maps to |
| --- | --- |
| `head`, `neck` | `Head` |
| `spine`, `spine.001` | `Spine` |
| `spine.002`, `spine.003` | `Chest` |
| `shoulder.L`, `upper_arm.L` | `Arm_L` |
| `forearm.L`, `hand.L` | `Hand_L` |
| `thigh.L` | `Thigh_L` |
| `shin.L`, `foot.L` | `Leg_L` |
| (`.R` mirrors above) | |

### VRM / VRoid

| Bone | Maps to |
| --- | --- |
| `J_Bip_C_Head`, `J_Bip_C_Neck` | `Head` |
| `J_Bip_C_Hips` | `Pelvis` |
| `J_Bip_C_Spine`, `Chest`, `UpperChest` | `Spine` / `Chest` |
| `J_Bip_L_Shoulder`, `UpperArm` | `Arm_L` |
| `J_Bip_L_LowerArm`, `Hand` | `Hand_L` |
| `J_Bip_L_UpperLeg` | `Thigh_L` |
| `J_Bip_L_LowerLeg`, `Foot` | `Leg_L` |
| (Right side mirrors) | |

## Custom names — Lua override

If your part names don't match any of the above, bind them once in `events.LOAD`:

```lua
function events.LOAD()
  fightura:mapBone("MyTailRoot", "Pelvis")
  fightura:mapBone("LeftSleeveCuff", "Hand_L")
  fightura:mapBone("HornLeft", "Head")
end
```

The first argument is your part name (or `parentType` name); the second is one of the Epic Fight joints from the table at the top.

To remove an override:

```lua
fightura:clearBone("MyTailRoot")  -- one
fightura:clearBones()              -- all of this avatar's overrides
```

To inspect what's available at runtime:

```lua
print(fightura:getSupportedBones())  -- list of every built-in alias
print(fightura:getJoints())          -- list of joints in the entity's current armature
```

## Part hierarchy still works

You only need to bind a part if you want its **own** transform to come from an Epic Fight joint. Children of a bound part inherit through Figura's normal flow.

For example, if you bind `LeftShoulder` to `Arm_L`, child parts named `LeftElbowAccessory` (with no special mapping) will move with the upper arm automatically — no extra binding needed.

## Next

- [Lua API](/lua-api/) — full list of Lua methods and Epic Fight state queries
- [Troubleshooting](/troubleshooting/) — what to do when a part doesn't follow
