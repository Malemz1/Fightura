---
title: Lua API
description: global fightura ที่มีให้ทุก avatar Figura เรียกใช้
---

Fightura ลงทะเบียน global Lua ชื่อ `fightura` ให้ avatar Figura ทุกตัว มีฟีเจอร์: query สถานะ Epic Fight, อ่าน joint pose, และ override ชื่อ bone ตอน runtime

```lua
if fightura:isAvailable() then
  -- entity นี้มี Epic Fight patch ติดอยู่
end
```

## Query สถานะ

### `isAvailable() -> boolean`
true ถ้า entity มี `LivingEntityPatch` ของ Epic Fight ติดอยู่ ตรวจตัวนี้เป็นอันดับแรกทุกครั้ง — ถ้าไม่มี method อื่น ๆ จะคืน `nil` หรือ `false`

### `isEpicFightMode() -> boolean`
true ตอน player อยู่ใน battle stance ของ Epic Fight (ชักดาบ ฯลฯ)

### `isAttacking() -> boolean`
true ตลอด animation การโจมตีของ Epic Fight

### `hasPose() -> boolean`
true ถ้ามี pose snapshot ปัจจุบันของ frame นี้ ใช้ใน `events.RENDER` ก่อนเรียก joint query

```lua
function events.WORLD_TICK()
  if fightura:isAttacking() then
    -- เล่น animation reaction ที่ทำเอง
  end
end
```

## อ่าน joint pose

อ่านจาก pose snapshot ล่าสุดของ Epic Fight (update ทุก frame ที่ entity ถูก render)

### `getJointRotation(name: string) -> Vector3 | nil`
คืน rotation ของ joint เป็น ZYX Euler ในหน่วย **radians** คืน `nil` ถ้าไม่พบ joint ใน armature ปัจจุบัน

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
คืน 4×4 matrix ของ joint เต็มรูปแบบ (เป็น `FiguraMat4`) เหมาะกับ rig ที่ใช้ matrix

```lua
local headMat = fightura:getJointMatrix("Head")
if headMat then
  models.MyHat:setMatrix(headMat)
end
```

### `getJoints() -> string[]`
คืนรายการชื่อ joint ทั้งหมดใน armature ของ entity ตอนนี้ ใช้สำรวจว่ามีอะไรบ้าง — mob/player คนละตัวอาจมี rig ต่างกัน

```lua
for _, joint in pairs(fightura:getJoints()) do
  print(joint)
end
```

## Bone binding

ถ้า avatar ใช้ชื่อ part ที่ Fightura ไม่รู้จัก ผูกครั้งเดียวตอน load ดู [รูปแบบการตั้งชื่อ](/Fightura/th/naming/) สำหรับรายการ alias เต็ม

### `mapBone(alias: string, joint: string)`
ผูกชื่อ part (หรือชื่อ `parentType`) กับ Epic Fight joint Alias ฝั่ง case-insensitive ส่วนชื่อ joint ต้องตรงเป๊ะ (`Head`, `Chest`, `Arm_L`, `Hand_L`, `Thigh_L`, `Leg_L`, ...)

```lua
function events.LOAD()
  fightura:mapBone("MyTailRoot", "Pelvis")
  fightura:mapBone("LSleeveCuff", "Hand_L")
end
```

Override มีผลเฉพาะ avatar **ตัวนี้** ไม่ leak ไปยังผู้เล่นอื่น

### `clearBone(alias: string)`
ลบ override อันเดียว

```lua
fightura:clearBone("MyTailRoot")
```

### `clearBones()`
ลบ override ทั้งหมดของ avatar นี้

### `getSupportedBones() -> string[]`
คืนรายการ alias built-in ทั้งหมดที่ Fightura รู้จัก (ในรูปแบบ lowercase) ใช้ตรวจว่าชื่อไหนไม่ต้อง `mapBone`

## Pattern พื้นฐาน

สคริปต์เริ่มต้นที่เหมาะสม:

```lua
function events.LOAD()
  -- ผูกชื่อ part ที่ไม่ standard ครั้งเดียว
  fightura:mapBone("HornLeft", "Head")
  fightura:mapBone("HornRight", "Head")
  fightura:mapBone("TailRoot", "Pelvis")
end

function events.WORLD_TICK()
  if fightura:isAvailable() and fightura:isAttacking() then
    -- trigger animation reaction
  end
end

function events.RENDER(delta)
  if not fightura:hasPose() then return end

  -- transform per-frame ขับโดย Epic Fight pose
  local hand = fightura:getJointRotation("Hand_L")
  if hand then
    models.weapon_glow:setRot(math.deg(hand.x), math.deg(hand.y), math.deg(hand.z))
  end
end
```

## สิ่งที่ตั้งใจไม่ exposeเพื่อให้ surface เล็ก เราไม่ใส่:

- การแก้ armature โดยตรง (อ่านอย่างเดียว)
- trigger Epic Fight skill
- sniff network packet
- query สถานะ vanilla model (ใช้ Figura `vanilla_model` API แทน)

ถ้าต้องการอะไรนอกเหนือจากนี้ เปิด issue ที่ [GitHub](https://github.com/Malemz1/Fightura/issues)

## ต่อไป

- [แก้ปัญหา](/Fightura/th/troubleshooting/) — ทำยังไงเมื่อ method คืน `nil` ทั้งที่ควรมีข้อมูล
