---
title: การติดตั้ง
description: ติดตั้ง Fightura ร่วมกับ Figura และ Epic Fight
---

## เวอร์ชันที่ต้องใช้

| ส่วนประกอบ | เวอร์ชัน |
| --- | --- |
| Minecraft | `1.20.1` |
| Forge | `47.x` (ทดสอบกับ `47.4.10`) |
| Java | `17` |
| Figura | `0.1.5+1.20.1-forge` ขึ้นไป |
| Epic Fight | `20.14.15.1` ขึ้นไป |

Fightura เป็นมอด **client-only** — server ไม่ต้องลง ถ้าเล่นเซิร์ฟเวอร์กับเพื่อน เฉพาะคนที่อยากใช้ฟีเจอร์ลงที่ตัวเอง

## ขั้นตอนติดตั้ง

1. ดาวน์โหลด `fightura-1.0.0.jar` ล่าสุดจากหน้า [Releases](https://github.com/Malemz1/Fightura/releases)

2. เปิดโฟลเดอร์ `mods/` ของ instance Minecraft ที่ใช้

   - **CurseForge / Prism / MultiMC:** กด *Open Folder* ที่ instance แล้วเข้า `mods/`
   - **Vanilla launcher:** `%APPDATA%\.minecraft\mods` (Windows) หรือ `~/Library/Application Support/minecraft/mods` (macOS)

3. โยน 3 jar นี้ลงใน `mods/`:

   ```
   fightura-1.0.0.jar
   figura-0.1.5+1.20.1-forge-mc.jar
   epic-fight-20.14.15.1-mc1.20.1-forge.jar
   ```

4. เปิด client ใน log ตอนโหลด mod ควรเห็น:

   ```
   [Fightura] Fightura starting
   [Fightura] Fightura registering events
   [Fightura] Fightura attached render layer to Epic Fight PLAYER renderer
   ```

## ตรวจว่าใช้งานได้

1. ใส่ avatar Figura (เมนูสามจุดในเกม)
2. กดสลับโหมด Epic Fight battle (default key: `R`)
3. เคลื่อนไหวและโจมตี — body ของ Figura ควรตามท่า Epic Fight
4. ของบนหัว (ผม, หมวก) ควรติดอยู่บนหัวระหว่างโจมตี

ถ้า body ลอยอยู่เหนือท่าจริง หรือของไม่ตามไป ดู [แก้ปัญหา](/Fightura/th/troubleshooting/)

## ถอนการติดตั้ง

ลบไฟล์ `fightura-1.0.0.jar` ออก จบ ไม่มี config ค้างที่ไหนนอก jar — Figura และ Epic Fight ใช้งานต่อได้ตามปกติ

## ต่อไป

- [รูปแบบการตั้งชื่อ](/Fightura/th/naming/) — Fightura ตัดสินใจยังไงว่า part ไหนตาม joint ไหน
- [Lua API](/Fightura/th/lua-api/) — bind ชื่อ bone กำหนดเอง + อ่านสถานะ Epic Fight ในสคริปต์
