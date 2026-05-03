---
title: แก้ปัญหา
description: ปัญหาที่พบบ่อยกับ Fightura และวิธีแก้
---

## Body ลอยอยู่เหนือท่าจริง

**อาการ:** Body ของ Figura ยืนอยู่ระดับเดิม ขณะที่ Epic Fight body roll/dive ติดพื้น

**สาเหตุ:** avatar มี part ที่ชื่อไม่ตรง alias list ในตัว → fallback ไปยัง render path ปกติของ Figura ที่ไม่ตามท่า EF

**แก้:** เปลี่ยนชื่อ part ให้ตรง [naming convention](/Fightura/th/naming/) หรือ bind ใน Lua:

```lua
function events.LOAD()
  fightura:mapBone("MyChest", "Chest")
  fightura:mapBone("LeftUpperArm01", "Arm_L")
  -- ... สำหรับทุก part body ที่ยังไม่ map
end
```

## Head accessory ลงไปอยู่ระดับอก

**อาการ:** ผม/หมวก render อยู่ระดับ body ไม่ใช่บนหัว

**สาเหตุ:** part ของ accessory นี้ไม่มี `parentType` (Head) ที่จับได้ และชื่อก็ไม่อยู่ในรายการ alias

**แก้:** ตั้ง parent type ของ part เป็น Head ใน `.bbmodel` หรือ bind:

```lua
fightura:mapBone("MyHairRoot", "Head")
```

## Avatar ไม่ animate เลย (Lua animation นิ่ง)

**อาการ:** Idle wave, blinking, sword-draw animation ที่เขียนใน `events.RENDER` ไม่เล่น

**สาเหตุ:** Fightura build เก่าไม่ยิง `renderEvent` อัปเดตเป็น jar ล่าสุด

**ตรวจสอบ:** ใส่ใน Lua:

```lua
function events.RENDER(delta)
  print("rendering!", delta)
end
```

ถ้าไม่เห็นอะไรใน chat → render event ไม่ยิง — ตรวจว่าใช้ Fightura 1.0.0 หรือใหม่กว่า และ Figura โหลดถูกต้อง

## Mod โหลดไม่ขึ้น — `ClassCastException` หรือ `LinkageError`

**อาการ:** Crash ตอน load world, log มีคำว่า Fightura

**สาเหตุ:** Figura/Epic Fight version ไม่ตรง Fightura ทดสอบกับ:

- Figura `0.1.5+1.20.1-forge`
- Epic Fight `20.14.15.1`

**แก้:**
1. ดู crash report ที่ `crash-reports/crash-*.txt`
2. ถ้าเขียน "mixin failed" หรือ "ClassCastException involving FighturaPlayerRendererAccessor" — ตรวจว่า Fightura build ด้วย mixingradle (มี `fightura.refmap.json` ใน jar; ถ้าหายต้อง rebuild)
3. ถ้าเขียนว่า class ของ Figura/Epic Fight หาย — ตรวจว่ามอดทั้งสองลงเวอร์ชันถูกต้อง

Fightura ห่อ initialization ด้วย `try { ... } catch (LinkageError | RuntimeException)` อยู่แล้ว ส่วนใหญ่เวอร์ชันไม่ตรงจะ log error แทน crash ทั้งเกม

## Custom alias ไม่ทำงาน

**อาการ:** เรียก `fightura:mapBone("MyPart", "Hand_L")` แล้วแต่ part ไม่ตาม joint

**ลองตรวจ:**

1. **`events.LOAD` ยิงจริงมั้ย?** ใส่ `print("LOAD fired")` เพื่อยืนยัน
2. **ชื่อ part ถูกต้องมั้ย?** ใน Figura/Blockbench ชื่อ case-sensitive แต่ใน `mapBone` case-insensitive ลอง `print(fightura:getSupportedBones())` ดูว่าอะไรลงทะเบียนไว้
3. **ชื่อ joint ถูกต้องมั้ย?** ชื่อ joint **case-sensitive** และต้องตรง Epic Fight เป๊ะ ลอง `print(fightura:getJoints())` ดู joint ที่มีจริงสำหรับ entity ปัจจุบัน
4. **มี script อื่นเรียก `clearBones()` หรือเปล่า?** Override จะถูกล้างตอน world unload — แต่ script ที่เขียนเลอะเทอะอาจเรียก `clearBones()` โดยไม่ตั้งใจ

## Mod โหลดได้แต่ accessory ไม่ตามอะไร

**อาการ:** player render ปกติแต่ accessory ค้างอยู่ตำแหน่ง vanilla model ไม่สนใจท่า Epic Fight

**สาเหตุ:** Matrix bridge mixin (`FighturaPartMatrixMixin`) ไม่ถูก apply ส่วนใหญ่หมายถึง Figura เวอร์ชันใหม่กว่าที่ Fightura ทดสอบเปลี่ยน internal call site ของ `recalculate()`

**แก้:** เปิด issue ที่ [GitHub](https://github.com/Malemz1/Fightura/issues) พร้อม:
- Figura version
- Epic Fight version
- Screenshot ของ rendering ที่พัง

นี่คือปัญหา compatibility ที่ต้อง update ฝั่ง Fightura

## เปิด log ละเอียด

Fightura ใช้ logger มาตรฐานของ Forge ดูเฉพาะข้อความ Fightura:

```
grep -i "fightura" logs/latest.log
```

ควรเห็นอย่างน้อย:

```
[Fightura] Fightura starting
[Fightura] Fightura registering events
[Fightura] Fightura attached render layer to Epic Fight PLAYER renderer
[Fightura] Fightura attached render layer to Epic Fight first-person renderer
```

ถ้า 2 บรรทัดล่างหายไป — `PatchedRenderersEvent.Modify` ไม่มาถึงเรา ส่วนใหญ่หมายถึง Epic Fight ไม่โหลด หรือมี mod อื่น cancel event

## ติดอยู่ ?

เปิด issue ที่ <https://github.com/Malemz1/Fightura/issues> พร้อม:

1. ไฟล์ `latest.log` (หรือส่วนที่เกี่ยวข้อง)
2. crash report ถ้ามี
3. version ของ Forge / Figura / Epic Fight / Fightura
4. คำอธิบายว่าคาดหวังอะไร และเห็นอะไรจริง
