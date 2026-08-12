#!/usr/bin/env python3
"""Jadikan ic_launcher_512.png (gambar upload user) sebagai ikon launcher resmi.

Membuat semua varian ikon Android dari 1 sumber 512x512:
  - mipmap-mdpi..xxxhdpi/ic_launcher.png
  - mipmap-mdpi..xxxhdpi/ic_launcher_round.png
  - mipmap-anydpi-v26/ic_launcher.xml  (Adaptive Icon, foreground = gambar user)
  - mipmap-anydpi-v26/ic_launcher_round.xml
  - ic_launcher_foreground.png (foreground adaptive, 432x432 aman, tidak crop)
  - res/values/ic_launcher_background.xml (warna latar adaptive)

Gambar user di-resize PRESISI ke 512x512 (tidak ada crop) karena sudah pas.
"""
import os
from PIL import Image, ImageOps

SRC = "/tmp/ic_launcher_512_orig.png"
RES = os.path.abspath(os.path.join(os.path.dirname(__file__),
    "..", "app", "src", "main", "res"))

# Buka & konversi ke RGBA
img = Image.open(SRC).convert("RGBA")
print("sumber:", img.size, img.mode)

# Pastikan tepat 512x512 (resize presisi, TANPA crop)
if img.size != (512, 512):
    img = img.resize((512, 512), Image.LANCZOS)
base512 = img
base512.save(os.path.join(RES, "ic_launcher_512.png"))
print("OK ic_launcher_512.png (512x512, tanpa crop)")

DENSITIES = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}

# --- ic_launcher.png & ic_launcher_round.png per density ---
for density, size in DENSITIES.items():
    out_dir = os.path.join(RES, "mipmap-" + density)
    os.makedirs(out_dir, exist_ok=True)
    # square
    sq = base512.resize((size, size), Image.LANCZOS)
    sq.save(os.path.join(out_dir, "ic_launcher.png"))
    # round (mask lingkaran)
    rnd = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    mask = Image.new("L", (size, size), 0)
    from PIL import ImageDraw
    ImageDraw.Draw(mask).ellipse([0, 0, size - 1, size - 1], fill=255)
    rnd.paste(sq, (0, 0), mask)
    rnd.save(os.path.join(out_dir, "ic_launcher_round.png"))
    print(f"OK mipmap-{density}/ic_launcher.png + ic_launcher_round.png ({size})")

# --- Foreground adaptive icon (432x432, area aman di tengah) ---
# Adaptif icon: total 108dp, foreground 432x432 (108dp*4) dengan safe zone ~72%.
# Gambar user (512x512) di-kecilkan ke 432x432 (tidak crop) lalu diletakkan di
# tengah kanvas 432x432 transparan -> tidak ada yang terpotong oleh mask adaptive.
FW = 432
fg = Image.new("RGBA", (FW, FW), (0, 0, 0, 0))
scaled = base512.resize((FW, FW), Image.LANCZOS)
fg.paste(scaled, (0, 0), scaled)
for density, mult in {"mdpi": 1, "hdpi": 1.5, "xhdpi": 2, "xxhdpi": 3, "xxxhdpi": 4}.items():
    out_dir = os.path.join(RES, "mipmap-" + density)
    os.makedirs(out_dir, exist_ok=True)
    s = int(FW * mult)
    fg_d = fg.resize((s, s), Image.LANCZOS)
    fg_d.save(os.path.join(out_dir, "ic_launcher_foreground.png"))
print("OK ic_launcher_foreground.png (semua density, 432x432, tidak crop)")

# --- Adaptive Icon XML (v26) ---
for name in ("ic_launcher", "ic_launcher_round"):
    xml = f'''<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background"/>
    <foreground android:drawable="@mipmap/ic_launcher_foreground"/>
</adaptive-icon>
'''
    out_dir = os.path.join(RES, "mipmap-anydpi-v26")
    os.makedirs(out_dir, exist_ok=True)
    with open(os.path.join(out_dir, name + ".xml"), "w") as f:
        f.write(xml)
    print(f"OK mipmap-anydpi-v26/{name}.xml")

# --- Warna background adaptive (krem hangat) ---
colors_dir = os.path.join(RES, "values")
os.makedirs(colors_dir, exist_ok=True)
bg_path = os.path.join(colors_dir, "ic_launcher_background.xml")
with open(bg_path, "w") as f:
    f.write('''<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="ic_launcher_background">#FFF3E0</color>
</resources>
''')
print("OK values/ic_launcher_background.xml")

print("\\nSelesai. ic_launcher_512.png kini menjadi ikon launcher resmi.")
