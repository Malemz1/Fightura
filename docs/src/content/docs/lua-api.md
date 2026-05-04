---
title: Lua API
description: The fightura global available to every Figura avatar.
---

Fightura registers a global Lua object named `fightura` for every Figura avatar. It exposes Epic Fight state queries, joint pose data, and runtime bone overrides.

```lua
if fightura:isAvailable() then
  -- this entity has an Epic Fight patch attached
end
```

## State queries

### `isAvailable() -> boolean`
True if the entity has an Epic Fight `LivingEntityPatch` attached. Always check this first; downstream queries return `nil`/`false` if it's absent.

### `isEpicFightMode() -> boolean`
True when the player is in Epic Fight battle stance (sword drawn, etc.).

### `isAttacking() -> boolean`
True for the duration of an Epic Fight attack animation.

### `hasPose() -> boolean`
True if a fresh pose snapshot was captured this frame. Use this in `events.RENDER` before calling joint queries.

```lua
function events.WORLD_TICK()
  if fightura:isAttacking() then
    -- play your custom hit-reaction animation
  end
end
```

## Joint pose

These read from the most recent Epic Fight pose snapshot (updated every frame the entity is rendered).

### `getJointRotation(name: string) -> Vector3 | nil`
Returns the joint's rotation as ZYX Euler angles in **radians**. Returns `nil` if the joint name doesn't exist in the current armature.

```lua
function events.RENDER(delta)
  if not fightura:hasPose() then return end
  local rot = fightura:getJointRotation("Hand_L")
  if rot then
    models.MyArm.LowerArm:setRot(
      math.deg(rot.x), math.deg(rot.y), math.deg(rot.z)
    )
  end
end
```

### `getJointMatrix(name: string) -> Matrix4 | nil`
Returns the joint's full 4×4 transform as a `FiguraMat4`. Best for matrix-driven rigs.

```lua
local headMat = fightura:getJointMatrix("Head")
if headMat then
  models.MyHat:setMatrix(headMat)
end
```

### `getJoints() -> string[]`
Returns every joint name in the current entity's Epic Fight armature. Use it to discover what's available — different mobs/players might have different rigs.

```lua
for _, joint in pairs(fightura:getJoints()) do
  print(joint)
end
```

## Bone bindings

If your avatar uses part names Fightura doesn't recognize out of the box, bind them once on load. See [Naming Conventions](/naming/) for the full alias list.

### `mapBone(alias: string, joint: string)`
Bind a part name (or `parentType` name) to an Epic Fight joint. Case-insensitive on the alias side. Joint name must match Epic Fight exactly (`Head`, `Chest`, `Arm_L`, `Hand_L`, `Thigh_L`, `Leg_L`, etc.).

```lua
function events.LOAD()
  fightura:mapBone("MyTailRoot", "Pelvis")
  fightura:mapBone("LSleeveCuff", "Hand_L")
end
```

Override only applies to **this** avatar — does not leak to other players.

### `clearBone(alias: string)`
Remove a single override.

```lua
fightura:clearBone("MyTailRoot")
```

### `clearBones()`
Remove every override this avatar has set.

### `getSupportedBones() -> string[]`
Return the list of every built-in alias Fightura recognizes (case-insensitive lowercase form). Useful for inspecting what won't need a `mapBone` call.

## Epic Fight events

Fightura fires Lua events when the entity's Epic Fight state changes — no polling required. Each event is a `LuaEvent` field on the `fightura` API; register handlers with `:register(func)`.

```lua
function events.LOAD()
  fightura.attackStart:register(function(motion)
    -- motion is the current living motion name (e.g. "ATTACK")
    animations.MyAttackReact:play()
  end)

  fightura.attackEnd:register(function()
    animations.MyAttackReact:stop()
  end)

  fightura.modeChange:register(function(newMode, oldMode)
    if newMode == "BATTLE" then
      animations.DrawSword:play()
    elseif newMode == "MINING" then
      animations.SheathSword:play()
    end
  end)

  fightura.hurtStart:register(function()
    animations.HurtFlinch:play()
  end)

  fightura.knockDown:register(function()
    animations.KnockedDown:play()
  end)

  fightura.motionChange:register(function(newMotion, oldMotion)
    -- e.g. transitioning from "IDLE" to "WALK"
  end)
end
```

| Event | Args | Fires when |
| --- | --- | --- |
| `attackStart` | `motion: string` | An Epic Fight attack animation begins |
| `attackEnd` | (none) | The attack animation finishes |
| `hurtStart` | (none) | The entity enters a hurt state |
| `knockDown` | (none) | The entity is knocked down |
| `modeChange` | `newMode, oldMode: string` | `playerMode` changes (`BATTLE` / `MINING` / `DEFAULT`) |
| `motionChange` | `newMotion, oldMotion: string` | Current living motion changes |

Events fire on the client tick. Polling APIs (`isAttacking()`, `isEpicFightMode()`) still work and are equivalent for logic that runs every frame.

## Lifecycle pattern

A reasonable starting script:

```lua
function events.LOAD()
  -- Bind any non-standard part names once.
  fightura:mapBone("HornLeft", "Head")
  fightura:mapBone("HornRight", "Head")
  fightura:mapBone("TailRoot", "Pelvis")
end

function events.WORLD_TICK()
  if fightura:isAvailable() and fightura:isAttacking() then
    -- Trigger custom reaction animations.
  end
end

function events.RENDER(delta)
  if not fightura:hasPose() then return end

  -- Per-frame transforms driven by Epic Fight pose.
  local hand = fightura:getJointRotation("Hand_L")
  if hand then
    models.weapon_glow:setRot(math.deg(hand.x), math.deg(hand.y), math.deg(hand.z))
  end
end
```

## What's not exposed

Some things deliberately aren't in the API to keep the surface small:

- Direct armature mutation (read-only access only)
- Triggering Epic Fight skills
- Network packet sniffing
- Vanilla model state queries (use Figura's `vanilla_model` API for that)

If you need something that isn't here, open an issue on the [GitHub repo](https://github.com/Malemz1/Fightura/issues).

## Next

- [Troubleshooting](/troubleshooting/) — what to do if a method returns `nil` when you expect data
