---
title: รูปแบบการตั้งชื่อ
description: Fightura map Figura part ไปยัง Epic Fight joint อย่างไรผ่านชื่อ part
---

Fightura ตามทุก Figura part ผ่าน 3 ทาง:

1. **`parentType`** — กลุ่มโครงร่างที่ Figura สร้างไว้แล้ว (Head, Body, LeftArm, …)
2. **ชื่อ part** — match แบบ case-insensitive กับชุด alias ในตัว
3. **Lua override** — สคริปต์ผูกชื่ออะไรก็ได้กับ joint ใดก็ได้ตอน runtime

ถ้าทางใดทางหนึ่ง resolve ไปยัง Epic Fight joint ได้ → part นั้นเกาะ joint ถ้าไม่ได้เลย → fallback ไปยัง render path ปกติของ Figura และ inherit จาก parent

## รายชื่อ Epic Fight joint

ชื่อ joint ที่สามารถ target ได้:

| Joint | ตำแหน่ง |
| --- | --- |
| `Root` | จุดอ้างอิง entity |
| `Pelvis` | สะโพก |
| `Spine` | ลำตัวล่าง |
| `Chest` | ลำตัวบน (อก) |
| `Head` | คอ/หัว |
| `Arm_L`, `Arm_R` | ต้นแขน (ข้อไหล่) |
| `Hand_L`, `Hand_R` | ปลายแขนและมือ |
| `Thigh_L`, `Thigh_R` | ต้นขา |
| `Leg_L`, `Leg_R` | ขาท่อนล่างและเท้า |

## Alias built-in (ไม่ต้องเขียน Lua)

Match แบบ case-insensitive ทั้งหมด — เลือกใช้ตามรูปแบบที่ tool ของคุณส่งออกมา

### English (เจอบ่อยสุด)

| ชื่อ | Map ไปยัง |
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

ถ้า import rig จาก Mixamo มา bone ใช้งานได้ทันที:

| Mixamo bone | Map ไปยัง |
| --- | --- |
| `mixamorig:Head`, `mixamorig:Neck` | `Head` |
| `mixamorig:Hips` | `Pelvis` |
| `mixamorig:Spine`, `Spine1`, `Spine2` | `Spine` / `Chest` |
| `mixamorig:LeftShoulder`, `LeftArm` | `Arm_L` |
| `mixamorig:LeftForeArm`, `LeftHand` | `Hand_L` |
| `mixamorig:LeftUpLeg` | `Thigh_L` |
| `mixamorig:LeftLeg`, `LeftFoot` | `Leg_L` |
| (ฝั่งขวาเหมือนกัน) | |

### Blender Rigify

| Bone | Map ไปยัง |
| --- | --- |
| `head`, `neck` | `Head` |
| `spine`, `spine.001` | `Spine` |
| `spine.002`, `spine.003` | `Chest` |
| `shoulder.L`, `upper_arm.L` | `Arm_L` |
| `forearm.L`, `hand.L` | `Hand_L` |
| `thigh.L` | `Thigh_L` |
| `shin.L`, `foot.L` | `Leg_L` |
| (`.R` เหมือนกัน) | |

### VRM / VRoid

| Bone | Map ไปยัง |
| --- | --- |
| `J_Bip_C_Head`, `J_Bip_C_Neck` | `Head` |
| `J_Bip_C_Hips` | `Pelvis` |
| `J_Bip_C_Spine`, `Chest`, `UpperChest` | `Spine` / `Chest` |
| `J_Bip_L_Shoulder`, `UpperArm` | `Arm_L` |
| `J_Bip_L_LowerArm`, `Hand` | `Hand_L` |
| `J_Bip_L_UpperLeg` | `Thigh_L` |
| `J_Bip_L_LowerLeg`, `Foot` | `Leg_L` |
| (ฝั่งขวาเหมือนกัน) | |

## ชื่อกำหนดเอง — Lua override

ถ้าชื่อ part ไม่ตรงตารางข้างบน ผูกครั้งเดียวใน `events.LOAD`:

```lua
function events.LOAD()
  fightura:mapBone("MyTailRoot", "Pelvis")
  fightura:mapBone("LeftSleeveCuff", "Hand_L")
  fightura:mapBone("HornLeft", "Head")
end
```

อาร์กิวเมนต์แรกคือชื่อ part หรือ `parentType` ที่ต้องการให้ตรง อาร์กิวเมนต์ที่สองต้องเป็นชื่อ Epic Fight joint จากตารางข้างบนเป๊ะ

ลบ override:

```lua
fightura:clearBone("MyTailRoot")  -- ลบทีละอัน
fightura:clearBones()              -- ลบทุกอันของ avatar นี้
```

ดูสิ่งที่มีตอน runtime:

```lua
print(fightura:getSupportedBones())  -- รายการ alias built-in ทั้งหมด
print(fightura:getJoints())          -- รายการ joint ใน armature ของ entity ตอนนี้
```

## Hierarchy ทำงานต่อปกติ

คุณ bind เฉพาะ part ที่ต้องการให้ **transform ตัวเอง** ตาม Epic Fight joint child ของ part ที่ bound แล้วจะ inherit ผ่าน flow ปกติของ Figura

ตัวอย่าง: ถ้า bind `LeftShoulder` ไปยัง `Arm_L` แล้ว child ชื่อ `LeftElbowAccessory` (ที่ไม่มี mapping พิเศษ) จะตามต้นแขนอัตโนมัติ — ไม่ต้อง bind เพิ่ม

## ต่อไป

- [Lua API](/Fightura/th/lua-api/) — รายการ Lua method ทั้งหมด + การอ่านสถานะ Epic Fight
- [แก้ปัญหา](/Fightura/th/troubleshooting/) — ทำยังไงเมื่อ part ไม่ตาม
