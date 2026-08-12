#!/usr/bin/env python3
"""Generate modern 2D cartoon menu icons (transparent background) for WARKIT.

Each icon is a flat cartoon illustration on a transparent canvas (no background),
drawn with PIL vector shapes. Output: app/src/main/assets/icons/<name>.png
Size: 96x96 px. Used by the action button bar.
"""
import os, math
from PIL import Image, ImageDraw

OUT = os.path.abspath(os.path.join(os.path.dirname(__file__),
    "..", "app", "src", "main", "assets", "icons"))
os.makedirs(OUT, exist_ok=True)
S = 96

# palette
C = {
    "wood": (141, 110, 99), "wood_d": (93, 64, 55),
    "roof": (231, 111, 81), "roof_d": (184, 82, 58),
    "cream": (253, 246, 227), "gold": (255, 213, 79), "gold_d": (255, 179, 0),
    "green": (102, 187, 106), "green_d": (56, 142, 60),
    "blue": (66, 165, 245), "blue_d": (30, 136, 229),
    "purple": (171, 71, 188), "purple_d": (123, 31, 162),
    "red": (239, 83, 80), "red_d": (211, 47, 47),
    "teal": (38, 166, 154), "teal_d": (0, 121, 107),
    "indigo": (92, 107, 192), "indigo_d": (48, 63, 159),
    "brown": (141, 110, 99), "brown_d": (93, 64, 55),
    "orange": (255, 167, 38), "orange_d": (245, 124, 0),
    "cyan": (38, 198, 218), "cyan_d": (0, 151, 167),
    "amber": (255, 193, 7), "amber_d": (255, 152, 0),
    "skin": (255, 224, 178),
    "white": (255, 255, 255), "dark": (62, 44, 28),
}

def canvas():
    img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    return img, ImageDraw.Draw(img)

def save(img, name):
    img.save(os.path.join(OUT, f"{name}.png"))

# ---- icons ----
def icon_warehouse():
    img, d = canvas()
    # box/crate
    d.rounded_rectangle([20, 30, 76, 80], radius=6, fill=C["wood"], outline=C["wood_d"], width=3)
    # lid
    d.rounded_rectangle([16, 24, 80, 36], radius=5, fill=C["wood_d"])
    # planks
    d.line([(20, 50), (76, 50)], fill=C["wood_d"], width=3)
    d.line([(20, 66), (76, 66)], fill=C["wood_d"], width=3)
    # label
    d.rounded_rectangle([34, 54, 62, 62], radius=2, fill=C["gold"])
    save(img, "warehouse")

def icon_upgrade():
    img, d = canvas()
    # up arrow in circle
    d.ellipse([14, 14, 82, 82], fill=C["indigo"], outline=C["indigo_d"], width=4)
    # arrow
    d.polygon([(48, 28), (66, 50), (56, 50), (56, 70), (40, 70), (40, 50), (30, 50)], fill=C["white"])
    # stars
    for sx in (28, 68):
        star(d, sx, 60, 5, C["gold"])
    save(img, "upgrade")

def icon_employees():
    img, d = canvas()
    # two heads
    d.ellipse([20, 22, 44, 46], fill=C["skin"], outline=C["dark"], width=2)
    d.ellipse([52, 26, 74, 48], fill=C["skin"], outline=C["dark"], width=2)
    # bodies
    d.polygon([(20, 46), (44, 46), (42, 78), (22, 78)], fill=C["blue"])
    d.polygon([(52, 48), (74, 48), (74, 78), (54, 78)], fill=C["green"])
    # tie
    d.polygon([(31, 46), (33, 46), (33, 60), (32, 62), (31, 60)], fill=C["red"])
    save(img, "employees")

def icon_decoration():
    img, d = canvas()
    # potted plant
    d.polygon([(34, 60), (62, 60), (58, 82), (38, 82)], fill=C["roof_d"])
    d.rectangle([40, 56, 56, 62], fill=C["wood_d"])
    # leaves
    d.ellipse([26, 24, 50, 52], fill=C["green"])
    d.ellipse([46, 20, 72, 50], fill=C["green_d"])
    d.ellipse([36, 14, 60, 40], fill=C["green"])
    save(img, "decoration")

def icon_products():
    img, d = canvas()
    # shopping bag
    d.rounded_rectangle([26, 34, 70, 82], radius=6, fill=C["orange"], outline=C["orange_d"], width=3)
    # handle
    d.arc([34, 20, 62, 44], 180, 360, fill=C["dark"], width=4)
    # items
    d.rounded_rectangle([34, 44, 46, 56], radius=2, fill=C["red"])
    d.rounded_rectangle([50, 40, 64, 54], radius=2, fill=C["blue"])
    save(img, "products")

def icon_missions():
    img, d = canvas()
    # target
    d.ellipse([16, 16, 80, 80], fill=C["white"], outline=C["red"], width=4)
    d.ellipse([28, 28, 68, 68], fill=C["red"])
    d.ellipse([38, 38, 58, 58], fill=C["white"])
    d.ellipse([44, 44, 52, 52], fill=C["red"])
    # arrow
    d.polygon([(50, 50), (78, 30), (70, 24), (82, 18), (84, 30), (76, 32)], fill=C["dark"])
    save(img, "missions")

def icon_branches():
    img, d = canvas()
    # map pin + shops
    d.polygon([(48, 20), (72, 44), (48, 84), (24, 44)], fill=C["teal"], outline=C["teal_d"], width=3)
    d.ellipse([36, 38, 60, 62], fill=C["white"])
    # mini shop
    d.rectangle([40, 44, 56, 56], fill=C["roof_d"])
    save(img, "branches")

def icon_operations():
    img, d = canvas()
    # gear
    gear(d, 48, 48, 26, 8, C["indigo"], C["indigo_d"])
    d.ellipse([38, 38, 58, 58], fill=C["white"])
    d.ellipse([44, 44, 52, 52], fill=C["indigo"])
    save(img, "operations")

def icon_business():
    img, d = canvas()
    # briefcase
    d.rounded_rectangle([18, 34, 78, 78], radius=8, fill=C["brown"], outline=C["brown_d"], width=3)
    d.rounded_rectangle([40, 24, 56, 36], radius=4, fill=C["brown_d"])
    d.line([(18, 52), (78, 52)], fill=C["brown_d"], width=3)
    d.rectangle([42, 48, 54, 56], fill=C["gold"])
    save(img, "business")

def icon_events():
    img, d = canvas()
    # calendar
    d.rounded_rectangle([20, 24, 76, 80], radius=6, fill=C["white"], outline=C["red_d"], width=3)
    d.rectangle([20, 24, 76, 38], fill=C["red"])
    d.rounded_rectangle([30, 16, 38, 30], radius=3, fill=C["dark"])
    d.rounded_rectangle([58, 16, 66, 30], radius=3, fill=C["dark"])
    # star on date
    star(d, 48, 58, 9, C["gold"])
    save(img, "events")

def icon_provinces():
    img, d = canvas()
    # map of indonesia-ish (group of islands)
    d.rounded_rectangle([14, 22, 82, 74], radius=8, fill=C["cyan"], outline=C["cyan_d"], width=3)
    for (x, y, r) in [(30, 40, 9), (48, 50, 11), (66, 42, 8), (40, 62, 7), (60, 64, 6)]:
        d.ellipse([x - r, y - r, x + r, y + r], fill=C["green"])
    # pin
    d.polygon([(48, 30), (56, 42), (48, 60), (40, 42)], fill=C["red"])
    save(img, "provinces")

def icon_modern():
    img, d = canvas()
    # credit card + phone
    d.rounded_rectangle([18, 30, 58, 70], radius=6, fill=C["purple"], outline=C["purple_d"], width=3)
    d.rectangle([24, 40, 52, 46], fill=C["gold"])
    d.rounded_rectangle([40, 22, 78, 60], radius=6, fill=C["blue"], outline=C["blue_d"], width=3)
    d.rounded_rectangle([46, 30, 72, 52], radius=3, fill=C["white"])
    save(img, "modern")

def icon_achievements():
    img, d = canvas()
    # trophy
    d.polygon([(34, 24), (62, 24), (58, 50), (38, 50)], fill=C["gold"], outline=C["gold_d"], width=2)
    d.rectangle([44, 50, 52, 62], fill=C["gold_d"])
    d.rectangle([34, 62, 62, 70], fill=C["gold_d"])
    # handles
    d.arc([24, 28, 38, 44], 90, 270, fill=C["gold_d"], width=4)
    d.arc([58, 28, 72, 44], 270, 90, fill=C["gold_d"], width=4)
    star(d, 48, 36, 5, C["white"])
    save(img, "achievements")

def icon_open():
    img, d = canvas()
    # open sign / door
    d.rounded_rectangle([26, 18, 70, 82], radius=6, fill=C["green"], outline=C["green_d"], width=4)
    d.rounded_rectangle([34, 28, 62, 74], radius=4, fill=C["cream"])
    d.rounded_rectangle([38, 40, 58, 52], radius=3, fill=C["green_d"])
    d.ellipse([54, 56, 60, 62], fill=C["gold"])
    save(img, "open")

def star(d, cx, cy, r, color):
    pts = []
    for i in range(10):
        ang = -math.pi / 2 + i * math.pi / 5
        rr = r if i % 2 == 0 else r * 0.45
        pts.append((cx + math.cos(ang) * rr, cy + math.sin(ang) * rr))
    d.polygon(pts, fill=color)

def gear(d, cx, cy, r, teeth, color, dark):
    import math as m
    pts = []
    for i in range(teeth * 2):
        ang = i * m.pi / teeth
        rr = r if i % 2 == 0 else r * 0.78
        pts.append((cx + m.cos(ang) * rr, cy + m.sin(ang) * rr))
    d.polygon(pts, fill=color, outline=dark)


ICONS = {
    "warehouse": icon_warehouse, "upgrade": icon_upgrade, "employees": icon_employees,
    "decoration": icon_decoration, "products": icon_products, "missions": icon_missions,
    "branches": icon_branches, "operations": icon_operations, "business": icon_business,
    "events": icon_events, "provinces": icon_provinces, "modern": icon_modern,
    "achievements": icon_achievements, "open": icon_open,
}
for name, fn in ICONS.items():
    fn()
    print(f"OK icons/{name}.png")
print("Done. Ikon kartun 2D (transparan) selesai.")
