"""Generate Fightura logo (256x256 PNG, no LITE branding)."""
from PIL import Image, ImageDraw, ImageFont, ImageFilter
import os

SIZE = 256
OUTPUT = "src/main/resources/fightura_logo.png"

def main():
    img = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    # Backdrop: deep blue → purple radial gradient feel via two ellipses
    bg = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
    bgd = ImageDraw.Draw(bg)
    bgd.rounded_rectangle((8, 8, SIZE - 8, SIZE - 8), radius=44,
                          fill=(34, 18, 70, 255))
    bgd.rounded_rectangle((8, 8, SIZE - 8, SIZE - 8), radius=44,
                          outline=(150, 120, 255, 255), width=4)
    img.paste(bg, (0, 0), bg)

    # Glow circle behind the F
    glow = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    gd.ellipse((52, 52, SIZE - 52, SIZE - 52), fill=(98, 70, 200, 130))
    glow = glow.filter(ImageFilter.GaussianBlur(radius=10))
    img = Image.alpha_composite(img, glow)

    d = ImageDraw.Draw(img)

    # Pick a chunky bold font — fall back to default if Inter/Verdana not present
    font = None
    for candidate in ("arialbd.ttf", "Verdana Bold.ttf", "verdanab.ttf",
                      "C:/Windows/Fonts/arialbd.ttf",
                      "C:/Windows/Fonts/verdanab.ttf"):
        try:
            font = ImageFont.truetype(candidate, 168)
            break
        except OSError:
            continue
    if font is None:
        font = ImageFont.load_default()

    # F glyph centered
    text = "F"
    bbox = d.textbbox((0, 0), text, font=font)
    tw = bbox[2] - bbox[0]
    th = bbox[3] - bbox[1]
    tx = (SIZE - tw) // 2 - bbox[0]
    ty = (SIZE - th) // 2 - bbox[1] - 6

    # Drop shadow
    shadow = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
    sd = ImageDraw.Draw(shadow)
    sd.text((tx + 4, ty + 6), text, font=font, fill=(0, 0, 0, 180))
    shadow = shadow.filter(ImageFilter.GaussianBlur(radius=2))
    img = Image.alpha_composite(img, shadow)

    d = ImageDraw.Draw(img)
    d.text((tx, ty), text, font=font, fill=(255, 255, 255, 255))

    # Sword crossbar through the middle of the F (decorative)
    cy = SIZE // 2 + 6
    d.rectangle((46, cy - 6, SIZE - 46, cy + 6),
                fill=(220, 60, 60, 230), outline=(255, 220, 90, 255), width=2)
    # tiny pommel
    d.ellipse((SIZE - 64, cy - 16, SIZE - 32, cy + 16),
              fill=(255, 220, 90, 255), outline=(220, 60, 60, 255), width=2)

    img.save(OUTPUT, 'PNG', optimize=True)
    print(f"wrote {OUTPUT} ({os.path.getsize(OUTPUT)} bytes)")


if __name__ == '__main__':
    main()
