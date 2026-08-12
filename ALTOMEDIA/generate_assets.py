#!/usr/bin/env python3
"""Generate seluruh aset gambar Play Store untuk WARKIT berdasarkan tema warung Indonesia.

Menghasilkan:
- ic_launcher (512x512 PNG) untuk Play Console
- feature graphic (1024x500 PNG)
- 3 screenshot mobile (1080x1920 PNG) - landscape gameplay karena game landscape
- promo graphic (180x120) opsional
"""
import os, math, random
from PIL import Image, ImageDraw, ImageFont

OUT = os.path.join(os.path.dirname(__file__), "store-listing")
os.makedirs(OUT, exist_ok=True)

# Palet warna tema warung Indonesia
WARM_BG = (253, 246, 227)       # #FDF6E3
ROOF = (231, 111, 81)            # #E76F51
ROOF_DARK = (191, 82, 58)
WOOD = (141, 110, 99)           # #8D6E63
WOOD_DARK = (93, 64, 55)
CREAM = (255, 243, 224)
GREEN = (67, 160, 71)           # #43A047
BLUE = (144, 202, 249)          # #90CAF9
GOLD = (255, 213, 79)           # #FFD54F
RED = (229, 57, 53)             # #E53935
TEXT_DARK = (62, 44, 28)        # #3E2C1C
TEXT_BROWN = (93, 64, 55)
HUD_BG = (255, 248, 235)
WHITE = (255, 255, 255)

def font(size, bold=True):
    paths = [
        "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf" if bold else "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
        "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
    ]
    for p in paths:
        if os.path.exists(p):
            return ImageFont.truetype(p, size)
    return ImageFont.load_default()

def draw_warung_building(d, x, y, w, h, scale=1.0):
    """Gambar bangunan warung sederhana pada posisi (x,y) ukuran w,h."""
    # Atap
    roof_h = int(h * 0.35)
    d.polygon([(x, y + roof_h), (x + w//2, y), (x + w, y + roof_h)],
              fill=ROOF_DARK)
    # Garis atap
    d.line([(x, y + roof_h), (x + w, y + roof_h)], fill=ROOF, width=max(2, int(3*scale)))
    # Body
    d.rectangle([x, y + roof_h, x + w, y + h], fill=CREAM, outline=WOOD, width=max(2, int(2*scale)))
    # Pintu
    dw = int(w * 0.3); dh = int(h * 0.45)
    dx = x + (w - dw)//2; dy = y + h - dh
    d.rectangle([dx, dy, dx + dw, dy + dh], fill=WOOD_DARK, outline=WOOD, width=max(1,int(2*scale)))
    # Handle pintu
    d.ellipse([dx + dw - int(8*scale), dy + dh//2 - int(4*scale),
               dx + dw + int(2*scale), dy + dh//2 + int(4*scale)], fill=GOLD)
    # Jendela kiri
    ww = int(w * 0.18); wh = int(h * 0.22)
    d.rectangle([x + int(w*0.08), y + roof_h + int(h*0.1),
                 x + int(w*0.08) + ww, y + roof_h + int(h*0.1) + wh], fill=BLUE, outline=WOOD, width=max(1,int(2*scale)))
    # Jendela kanan
    d.rectangle([x + w - int(w*0.08) - ww, y + roof_h + int(h*0.1),
                 x + w - int(w*0.08), y + roof_h + int(h*0.1) + wh], fill=BLUE, outline=WOOD, width=max(1,int(2*scale)))
    # Papan nama
    bw = int(w*0.7); bh = int(h*0.1)
    d.rectangle([x + (w-bw)//2, y + roof_h - bh//2, x + (w+bw)//2, y + roof_h + bh//2],
                fill=ROOF_DARK, outline=WOOD_DARK, width=max(1,int(2*scale)))

def draw_shelf(d, x, y, w, h):
    """Gambar rak produk."""
    d.rectangle([x, y, x + w, y + h], fill=WOOD, outline=WOOD_DARK, width=2)
    # Produk di rak (kotak warna)
    colors = [GREEN, GOLD, RED, (255, 183, 77), (171, 71, 188)]
    cols = 5
    cw = w // cols
    for i in range(cols):
        cx = x + i * cw + cw//4
        cy = y + h//4
        ch = h//2
        d.rectangle([cx, cy, cx + cw//2, cy + ch], fill=colors[i % len(colors)], outline=WOOD_DARK)

def draw_customer(d, x, y, color=GREEN, size=20):
    """Gambar pelanggan sederhana (lingkaran kepala + body)."""
    r = size // 3
    d.ellipse([x, y, x + r*2, y + r*2], fill=(255, 224, 178))  # kepala
    d.ellipse([x - size//4, y + r*2, x + r*2 + size//4, y + r*2 + size], fill=color)  # body

# ============================================================
# 1. APP ICON 512x512
# ============================================================
def gen_icon():
    img = Image.new("RGBA", (512, 512), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Background gradient circle
    cx, cy = 256, 256
    for r in range(256, 0, -2):
        t = r / 256
        col = (
            int(253 * t + 255 * (1-t)),
            int(246 * t + 243 * (1-t)),
            int(227 * t + 224 * (1-t)),
            255
        )
        d.ellipse([cx-r, cy-r, cx+r, cy+r], fill=col)
    # Warung building di tengah
    draw_warung_building(d, 96, 140, 320, 260, scale=3.0)
    # Embel "WARKIT" kecil di bawah
    d.rectangle([130, 380, 382, 440], fill=ROOF_DARK)
    f = font(54, bold=True)
    text = "WARKIT"
    bbox = d.textbbox((0, 0), text, font=f)
    tw = bbox[2] - bbox[0]
    d.text((256 - tw//2, 388), text, fill=WHITE, font=f)
    img.save(os.path.join(OUT, "ic_launcher_512.png"))
    # Versi round
    mask = Image.new("L", (512, 512), 0)
    ImageDraw.Draw(mask).ellipse([0, 0, 511, 511], fill=255)
    round_icon = Image.new("RGBA", (512, 512), (0,0,0,0))
    round_icon.paste(img, (0,0), mask)
    round_icon.save(os.path.join(OUT, "ic_launcher_round_512.png"))
    print("OK ic_launcher_512.png, ic_launcher_round_512.png")

# ============================================================
# 2. FEATURE GRAPHIC 1024x500
# ============================================================
def gen_feature_graphic():
    img = Image.new("RGB", (1024, 500), WARM_BG)
    d = ImageDraw.Draw(img)
    # Background gradient stripes (langit)
    for i in range(0, 250, 4):
        t = i / 250
        col = (
            int(253 + (255-253)*t),
            int(246 + (248-246)*t),
            int(227 + (235-227)*t),
        )
        d.rectangle([0, i, 1024, i+4], fill=col)
    # Tanah
    d.rectangle([0, 380, 1024, 500], fill=(222, 184, 135))
    # Beberapa warung di latar
    draw_warung_building(d, 60, 230, 160, 150, scale=1.2)
    draw_warung_building(d, 820, 240, 150, 140, scale=1.1)
    # Warung utama besar di tengah
    draw_warung_building(d, 360, 180, 300, 200, scale=2.0)
    # Judul
    f_big = font(110, bold=True)
    f_small = font(42, bold=True)
    title = "WARKIT"
    bbox = d.textbbox((0, 0), title, font=f_big)
    tw = bbox[2] - bbox[0]
    # Bayangan
    d.text((512 - tw//2 + 3, 33), title, fill=(0,0,0,128), font=f_big)
    d.text((512 - tw//2, 30), title, fill=ROOF, font=f_big)
    sub = "Idle Warung Indonesia"
    bbox = d.textbbox((0, 0), sub, font=f_small)
    sw = bbox[2] - bbox[0]
    d.text((512 - sw//2, 155), sub, fill=TEXT_DARK, font=f_small)
    # Tagline bawah
    f_tag = font(28, bold=False)
    tag = "Bangun warung kecil menjadi jaringan terbesar di Indonesia"
    bbox = d.textbbox((0, 0), tag, font=f_tag)
    tw2 = bbox[2] - bbox[0]
    d.text((512 - tw2//2, 410), tag, fill=TEXT_BROWN, font=f_tag)
    img.save(os.path.join(OUT, "feature_graphic_1024x500.png"))
    print("OK feature_graphic_1024x500.png")

# ============================================================
# 3. SCREENSHOTS 1080x1920 (portrait frame, content landscape game)
# ============================================================
def gen_screenshot(num, title, desc, accent):
    """Screenshot portrait 1080x1920 dengan mockup gameplay landscape di tengah."""
    img = Image.new("RGB", (1080, 1920), (240, 240, 245))
    d = ImageDraw.Draw(img)
    # Header gradient
    for i in range(0, 200):
        t = i / 200
        col = (
            int(accent[0] * (1-t*0.5) + 255*t*0.5),
            int(accent[1] * (1-t*0.5) + 255*t*0.5),
            int(accent[2] * (1-t*0.5) + 255*t*0.5),
        )
        d.rectangle([0, i, 1080, i+1], fill=col)
    # Logo kecil
    d.ellipse([60, 60, 160, 160], fill=ROOF)
    draw_warung_building(d, 70, 75, 80, 75, scale=0.8)
    f_logo = font(56, bold=True)
    d.text((190, 80), "WARKIT", fill=WHITE, font=f_logo)
    f_sub = font(28, bold=False)
    d.text((190, 148), "Idle Warung Indonesia", fill=WHITE, font=f_sub)
    # Frame game landscape (16:9) di tengah
    fx, fy, fw, fh = 80, 280, 920, 518
    d.rectangle([fx-6, fy-6, fx+fw+6, fy+fh+6], fill=(60,60,70))
    d.rectangle([fx, fy, fx+fw, fy+fh], fill=WARM_BG)
    # --- Mockup gameplay (game landscape) ---
    # Lantai
    d.rectangle([fx, fy+fh-120, fx+fw, fy+fh], fill=(222, 184, 135))
    # Dinding
    d.rectangle([fx, fy, fx+fw, fy+fh-120], fill=(255, 248, 235))
    # HUD atas
    d.rectangle([fx, fy, fx+fw, fy+70], fill=HUD_BG)
    f_hud = font(26, bold=True)
    d.text((fx+20, fy+18), "Rp 12.500.000", fill=TEXT_DARK, font=f_hud)
    d.text((fx+fw//2-60, fy+18), "Hari 5", fill=TEXT_BROWN, font=f_hud)
    d.text((fx+fw-200, fy+18), "Reputasi: 350", fill=GOLD, font=f_hud)
    # Rak kiri
    draw_shelf(d, fx+40, fy+100, 200, 200)
    draw_shelf(d, fx+40, fy+330, 200, 150)
    # Rak kanan
    draw_shelf(d, fx+fw-240, fy+100, 200, 200)
    draw_shelf(d, fx+fw-240, fy+330, 200, 150)
    # Kasir tengah
    d.rectangle([fx+fw//2-60, fy+200, fx+fw//2+60, fy+330], fill=WOOD, outline=WOOD_DARK, width=3)
    d.rectangle([fx+fw//2-40, fy+220, fx+fw//2+40, fy+260], fill=GOLD)
    # Pelanggan
    draw_customer(d, fx+fw//2-100, fy+380, GREEN, 36)
    draw_customer(d, fx+fw//2+40, fy+390, (171, 71, 188), 32)
    draw_customer(d, fx+fw//2+120, fy+400, (255, 183, 77), 34)
    # Tombol aksi bawah
    btns = [("🏪", GREEN), ("📦", ROOF), ("👤", BLUE), ("🎯", GOLD), ("🇮🇩", (0, 139, 139))]
    bw = fw // 5
    for i, (emoji, c) in enumerate(btns):
        bx = fx + i * bw + 10
        d.rounded_rectangle([bx, fy+fh-110, bx+bw-20, fy+fh-20], radius=12, fill=c)
    # --- Akhir mockup ---
    # Judul fitur di bawah mockup
    f_title = font(64, bold=True)
    bbox = d.textbbox((0, 0), title, font=f_title)
    tw = bbox[2] - bbox[0]
    d.text((540 - tw//2, 870), title, fill=accent, font=f_title)
    # Deskripsi
    f_desc = font(36, bold=False)
    # Wrap text manual
    words = desc.split()
    lines = []
    cur = ""
    for w in words:
        test = (cur + " " + w).strip()
        bbox = d.textbbox((0, 0), test, font=f_desc)
        if bbox[2] - bbox[0] > 920 and cur:
            lines.append(cur)
            cur = w
        else:
            cur = test
    if cur:
        lines.append(cur)
    y = 970
    for line in lines:
        bbox = d.textbbox((0, 0), line, font=f_desc)
        lw = bbox[2] - bbox[0]
        d.text((540 - lw//2, y), line, fill=TEXT_DARK, font=f_desc)
        y += 52
    # Point list di bawah
    f_pt = font(34, bold=True)
    points = {
        1: ["Layani pelanggan & naikkan reputasi", "Kelola 50+ produk warung", "Rekrut & latih pegawai", "Buka cabang ke 10 provinsi"],
        2: ["Program member Silver/Gold/Platinum", "Pembayaran QRIS & Dompet Digital", "Pusat distribusi nasional", "Hadapi kompetitor nasional"],
        3: ["Raih gelar Raja Warung Indonesia", "Mode Endless Empire tanpa batas", "Event musiman & festival", "13 prestasi untuk diklaim"],
    }.get(num, [])
    y = 1200
    for pt in points:
        d.ellipse([120, y+6, 150, y+36], fill=accent)
        d.text((540 - 20, y), "•", fill=WHITE, font=f_pt)
        d.text((170, y), pt, fill=TEXT_BROWN, font=f_pt)
        y += 65
    # Footer
    d.rectangle([0, 1820, 1080, 1920], fill=accent)
    d.text((540 - 180, 1840), "WARKIT by ALTOMEDIA", fill=WHITE, font=f_pt)
    img.save(os.path.join(OUT, f"screenshot_{num}_mobile.png"))
    print(f"OK screenshot_{num}_mobile.png")

# ============================================================
# 4. PROMO GRAPHIC 180x120 (opsional, lama tapi berguna)
# ============================================================
def gen_promo():
    img = Image.new("RGB", (180, 120), WARM_BG)
    d = ImageDraw.Draw(img)
    draw_warung_building(d, 30, 30, 120, 80, scale=0.7)
    f = font(20, bold=True)
    d.text((55, 96), "WARKIT", fill=ROOF, font=f)
    img.save(os.path.join(OUT, "promo_graphic_180x120.png"))
    print("OK promo_graphic_180x120.png")

if __name__ == "__main__":
    gen_icon()
    gen_feature_graphic()
    gen_screenshot(1, "Kelola Warungmu", "Mulai dari warung kecil, layani pelanggan, restock rak, dan kembangkan bisnis jadi jaringan nasional.", GREEN)
    gen_screenshot(2, "Modernisasi Layanan", "Terima pembayaran QRIS, luncurkan program member, bangun pusat distribusi nasional, dan hadapi persaingan level nasional.", BLUE)
    gen_screenshot(3, "Bangun Kerajaan Bisnis", "Raih gelar Raja Warung Indonesia, buka semua provinsi, dan mainkan mode Endless Empire tanpa batas.", GOLD)
    gen_promo()
    print("Semua gambar store tergenerate di", OUT)
