#!/usr/bin/env python3
"""Patch semua *Dialog.kt agar memakai popup modern (UiTheme.applyPopup).

- Inject `UiTheme.applyPopup(this)` setelah super.onCreate(savedInstanceState)
  di onCreate setiap dialog.
- Ubah latar root #FDF6E3 -> transparan agar latar popup rounded (window bg) terlihat.
"""
import os, re, glob

UI_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__),
    "..", "app", "src", "main", "java", "com", "altomedia", "warkit", "ui"))

PATCHED = 0
for path in glob.glob(os.path.join(UI_DIR, "*Dialog.kt")):
    src = open(path, encoding="utf-8").read()
    orig = src

    # 1) Inject applyPopup in onCreate (after super.onCreate(...))
    if "UiTheme.applyPopup(this)" not in src:
        src = re.sub(
            r"(override fun onCreate\(savedInstanceState: Bundle\?\)\s*\{\s*super\.onCreate\(savedInstanceState\))",
            r"\1\n        UiTheme.applyPopup(this)",
            src, count=1)

    # 2) Root background: FDF6E3 -> transparent (window popup bg shows rounded card)
    src = src.replace('setBackgroundColor(Color.parseColor("#FDF6E3"))',
                      'setBackgroundColor(Color.TRANSPARENT)')

    if src != orig:
        open(path, "w", encoding="utf-8").write(src)
        PATCHED += 1
        print(f"patched {os.path.basename(path)}")

print(f"\nTotal dialog dipatch: {PATCHED}")
