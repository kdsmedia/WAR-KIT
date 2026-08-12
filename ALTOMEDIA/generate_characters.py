#!/usr/bin/env python3
"""Generate 2D cartoon character sprite-sheet atlases for WARKIT.

Each character is rendered as a horizontal strip of frames:
  - 6 walk-cycle frames (legs & arms swinging naturally)
  - 2 idle frames (slight breathing / picking)
Frames are drawn with PIL vector shapes (cartoon flat style) so the
character has a real head, torso, two arms and two legs that move.

Output: app/src/main/assets/characters/<type>.png  (one row, N frames)
Frame size: 96 x 128 px.  Frame count per type: 8.
"""
import os, math
from PIL import Image, ImageDraw

OUT = os.path.abspath(os.path.join(os.path.dirname(__file__),
    "..", "app", "src", "main", "assets", "characters"))
os.makedirs(OUT, exist_ok=True)

FW, FH = 96, 128        # frame width / height
WALK_FRAMES = 6
IDLE_FRAMES = 2
FRAMES = WALK_FRAMES + IDLE_FRAMES

# Skin tones
SKIN = (255, 224, 178)
SKIN_SHADE = (236, 200, 158)

# Palette per customer type: (shirt, shirt_dark, pants, hair/hat, accessory)
PALETTES = {
    "ibu":      dict(shirt=(236, 64, 122),  shirt_d=(194, 24, 91),  pants=(93, 64, 55),  hair=(121, 85, 72),  acc="kerudung"),
    "anak":     dict(shirt=(126, 87, 194),  shirt_d=(81, 45, 168),  pants=(66, 66, 66),  hair=(62, 39, 35),   acc="ransel"),
    "ojol":     dict(shirt=(38, 166, 154),  shirt_d=(0, 121, 107),  pants=(55, 71, 79),  hair=(33, 33, 33),   acc="helm"),
    "petani":   dict(shirt=(102, 187, 106), shirt_d=(56, 142, 60),  pants=(141, 110, 99),hair=(62, 39, 35),   acc="caping"),
    "karyawan": dict(shirt=(92, 107, 192),  shirt_d=(48, 63, 159),  pants=(55, 71, 79),  hair=(33, 33, 33),   acc="dasi"),
    "kasir":    dict(shirt=(231, 111, 81),  shirt_d=(184, 82, 58),  pants=(93, 64, 55),  hair=(62, 39, 35),   acc="nametag"),
}


def lerp(a, b, t):
    return a + (b - a) * t


def draw_character(d, ox, oy, p, walk_phase, idle=False, flip=False):
    """Draw one cartoon character frame centered at (ox, oy) baseline.
    walk_phase in radians drives leg/arm swing. idle=True for breathing pose.
    """
    # Coordinates (character ~84px tall, centered horizontally in 96 frame)
    cx = ox + FW // 2
    ground = oy + FH - 10          # feet baseline
    hip_y = ground - 34            # hip
    shoulder_y = ground - 78       # shoulders
    head_cy = ground - 96          # head center
    head_r = 15

    swing = 0.0 if idle else math.sin(walk_phase)
    swing2 = 0.0 if idle else math.sin(walk_phase + math.pi)
    # body bob (vertical) for walk
    bob = 0.0 if idle else abs(math.sin(walk_phase)) * -2.0
    shoulder_y += bob
    head_cy += bob
    hip_y += bob * 0.5

    shirt = p["shirt"]; shirt_d = p["shirt_d"]; pants = p["pants"]; hair = p["hair"]

    # ---- Legs (drawn first, behind body) ----
    leg_len = 30
    # left leg
    la = swing * 0.5          # leg angle amplitude
    lx0, ly0 = cx - 6, hip_y
    lx1 = lx0 + math.sin(la) * 14
    ly1 = ly0 + math.cos(la) * leg_len
    # right leg
    ra = swing2 * 0.5
    rx0, ry0 = cx + 6, hip_y
    rx1 = rx0 + math.sin(ra) * 14
    ry1 = ry0 + math.cos(ra) * leg_len
    # pants (upper leg)
    d.line([(lx0, ly0), (lx1, ly1)], fill=pants, width=8)
    d.line([(rx0, ry0), (rx1, ry1)], fill=pants, width=8)
    # shoes
    d.ellipse([lx1 - 5, ly1 - 3, lx1 + 7, ly1 + 5], fill=(33, 33, 33))
    d.ellipse([rx1 - 5, ry1 - 3, rx1 + 7, ry1 + 5], fill=(33, 33, 33))

    # ---- Torso (shirt) ----
    torso_top = shoulder_y
    torso_bot = hip_y
    # trapezoid torso
    d.polygon([(cx - 13, torso_top), (cx + 13, torso_top),
               (cx + 11, torso_bot), (cx - 11, torso_bot)], fill=shirt)
    # shirt shading side
    d.polygon([(cx + 4, torso_top), (cx + 13, torso_top),
               (cx + 11, torso_bot), (cx + 4, torso_bot)], fill=shirt_d)

    # ---- Arms (swing opposite to legs) ----
    arm_len = 26
    # left arm swings with right leg (opposite)
    ala = swing2 * 0.6
    asx0, asy0 = cx - 13, shoulder_y + 2
    aex0 = asx0 + math.sin(ala) * 12
    aey0 = asy0 + math.cos(ala) * arm_len
    # right arm
    ara = swing * 0.6
    arx0, ary0 = cx + 13, shoulder_y + 2
    arx1 = arx0 + math.sin(ara) * 12
    ary1 = ary0 + math.cos(ara) * arm_len
    d.line([(asx0, asy0), (aex0, aey0)], fill=shirt_d, width=7)
    d.line([(arx0, ary0), (arx1, ary1)], fill=shirt, width=7)
    # hands
    d.ellipse([aex0 - 4, aey0 - 4, aex0 + 4, aey0 + 4], fill=SKIN)
    d.ellipse([arx1 - 4, ary1 - 4, arx1 + 4, ary1 + 4], fill=SKIN)

    # ---- Neck ----
    d.line([(cx, shoulder_y - 2), (cx, head_cy + head_r - 2)], fill=SKIN_SHADE, width=6)

    # ---- Head ----
    d.ellipse([cx - head_r, head_cy - head_r, cx + head_r, head_cy + head_r], fill=SKIN)
    # ear
    d.ellipse([cx - head_r - 2, head_cy - 3, cx - head_r + 3, head_cy + 5], fill=SKIN_SHADE)
    # eyes
    d.ellipse([cx - 6, head_cy - 3, cx - 3, head_cy], fill=(33, 33, 33))
    d.ellipse([cx + 3, head_cy - 3, cx + 6, head_cy], fill=(33, 33, 33))
    # mouth (smile)
    d.arc([cx - 5, head_cy + 2, cx + 5, head_cy + 9], 10, 170, fill=(141, 110, 99), width=2)
    # cheek blush
    d.ellipse([cx - 11, head_cy + 3, cx - 7, head_cy + 7], fill=(255, 171, 145))
    d.ellipse([cx + 7, head_cy + 3, cx + 11, head_cy + 7], fill=(255, 171, 145))

    # ---- Hair / accessory ----
    acc = p["acc"]
    if acc == "kerudung":
        d.chord([cx - head_r - 2, head_cy - head_r - 2, cx + head_r + 2, head_cy + head_r + 2],
                180, 360, fill=hair)
        d.rectangle([cx - head_r, head_cy - 2, cx + head_r, head_cy + 4], fill=hair)
    elif acc == "helm":
        d.chord([cx - head_r - 3, head_cy - head_r - 4, cx + head_r + 3, head_cy + head_r],
                0, 360, fill=(33, 33, 33))
        d.rectangle([cx - head_r - 3, head_cy - 4, cx + head_r + 3, head_cy + 2], fill=(33, 33, 33))
        d.rectangle([cx - 4, head_cy - head_r - 4, cx + 4, head_cy - 2], fill=(236, 64, 122))
    elif acc == "caping":
        d.polygon([(cx - head_r - 8, head_cy - 2), (cx + head_r + 8, head_cy - 2),
                   (cx, head_cy - head_r - 6)], fill=(205, 162, 98))
    elif acc == "dasi":
        d.polygon([(cx - 3, shoulder_y), (cx + 3, shoulder_y), (cx + 2, shoulder_y + 12),
                   (cx, shoulder_y + 14), (cx - 2, shoulder_y + 12)], fill=(229, 57, 53))
    elif acc == "ransel":
        d.rounded_rectangle([cx - 16, shoulder_y - 6, cx - 8, shoulder_y + 18], radius=4,
                            fill=(255, 183, 77))
    elif acc == "nametag":
        d.rounded_rectangle([cx - 6, shoulder_y + 4, cx + 8, shoulder_y + 12], radius=2,
                            fill=(255, 213, 79))
    else:
        # default hair cap
        d.chord([cx - head_r - 1, head_cy - head_r - 1, cx + head_r + 1, head_cy + head_r + 1],
                180, 360, fill=hair)


def make_atlas(name, palette):
    img = Image.new("RGBA", (FW * FRAMES, FH), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    for i in range(WALK_FRAMES):
        phase = (i / WALK_FRAMES) * 2 * math.pi
        draw_character(d, i * FW, 0, palette, phase, idle=False)
    for j in range(IDLE_FRAMES):
        phase = (j / IDLE_FRAMES) * 2 * math.pi
        draw_character(d, (WALK_FRAMES + j) * FW, 0, palette, phase, idle=True)
    path = os.path.join(OUT, f"{name}.png")
    img.save(path)
    print(f"OK {name}.png  ({FRAMES} frames, {FW}x{FH}) -> {path}")


for name, pal in PALETTES.items():
    make_atlas(name, pal)

print("Done. Atlas karakter 2D kartun selesai.")
