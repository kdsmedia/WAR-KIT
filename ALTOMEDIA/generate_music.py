#!/usr/bin/env python3
"""Synthesize a pleasant looping background music for WARKIT.

Generates a calm, warm Indonesian-tinged idle-game loop (pentatonic-based)
as a WAV file suitable for looping in Android MediaPlayer.
No external audio libs required beyond numpy.
"""
import os, struct, math
import numpy as np

OUT = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "res", "raw", "bgm.wav")
OUT = os.path.abspath(OUT)
os.makedirs(os.path.dirname(OUT), exist_ok=True)

SR = 22050  # sample rate (mono, smaller for APK)
BPM = 92.0
BEAT = 60.0 / BPM  # seconds per beat
BARS = 8          # 8 bars loop
BEATS_PER_BAR = 4
TOTAL_BEATS = BARS * BEATS_PER_BAR
DUR = BEAT * TOTAL_BEATS
N = int(SR * DUR)
t = np.arange(N) / SR

# Pentatonic scale frequencies (C major pentatonic) for warm Asian/Indonesian feel
# C4=261.63, D4=293.66, E4=329.63, G4=392.00, A4=440.00, C5=523.25, D5=587.33, E5=659.25, G5=783.99
def note(freq, start, dur, amp=0.22, harmonics=(1.0, 0.4, 0.18, 0.08)):
    """Soft additive synth note with slight decay."""
    s = np.zeros(N)
    si = int(start * SR)
    ei = min(int((start + dur) * SR), N)
    if ei <= si:
        return s
    tt = np.arange(ei - si) / SR
    env = np.exp(-tt / (dur * 1.6))  # decay
    # gentle attack
    atk = int(0.012 * SR)
    if atk > 0 and atk < len(env):
        env[:atk] *= np.linspace(0, 1, atk)
    wave = np.zeros_like(tt)
    for h, hamp in enumerate(harmonics, start=1):
        wave += hamp * np.sin(2 * math.pi * freq * h * tt)
    s[si:ei] = amp * env * wave
    return s

# Melody (pentatonic), gentle uplifting loop — note (beat_position, freq, duration_beats)
melody = [
    (0,  523.25, 1.0), (1,  587.33, 1.0), (2,  659.25, 1.5), (3.5, 587.33, 0.5),
    (4,  523.25, 1.0), (5,  392.00, 1.0), (6,  440.00, 2.0),
    (8,  659.25, 1.0), (9,  587.33, 1.0), (10, 523.25, 1.5), (11.5, 440.00, 0.5),
    (12, 392.00, 1.0), (13, 440.00, 1.0), (14, 523.25, 2.0),
    (16, 587.33, 1.0), (17, 523.25, 1.0), (18, 392.00, 2.0),
    (20, 440.00, 1.0), (21, 523.25, 1.0), (22, 587.33, 1.0), (23, 659.25, 1.0),
    (24, 523.25, 2.0), (26, 587.33, 2.0),
    (28, 659.25, 1.0), (29, 783.99, 1.0), (30, 587.33, 2.0),
]
# Bass line (lower octave, longer notes)
bass = [
    (0,  130.81, 4.0), (4,  174.61, 4.0),
    (8,  196.00, 4.0), (12, 130.81, 4.0),
    (16, 174.61, 4.0), (20, 196.00, 4.0),
    (24, 130.81, 4.0), (28, 174.61, 4.0),
]
# Soft pad chords (sustained)
pad = [
    (0,  261.63, 8.0), (8,  329.63, 8.0),
    (16, 392.00, 8.0), (24, 261.63, 8.0),
]

mix = np.zeros(N)
for pos, f, d in melody:
    mix += note(f, pos * BEAT, d * BEAT, amp=0.18)
for pos, f, d in bass:
    mix += note(f, pos * BEAT, d * BEAT, amp=0.20, harmonics=(1.0, 0.3, 0.1))
for pos, f, d in pad:
    mix += note(f, pos * BEAT, d * BEAT, amp=0.07, harmonics=(1.0, 0.5, 0.25, 0.12))

# Soft saturation & normalize
mix = np.tanh(mix * 1.3)
peak = np.max(np.abs(mix)) or 1.0
mix = mix / peak * 0.85

# Gentle fade in/out at loop edges for seamless loop (very short)
fade = int(0.05 * SR)
mix[:fade] *= np.linspace(0, 1, fade)
mix[-fade:] *= np.linspace(1, 0, fade)

# Convert to 16-bit PCM mono (kecil & cukup berkualitas untuk BGM loop)
pcm16 = np.int16(np.clip(mix * 32767, -32768, 32767))
mono = pcm16

with open(OUT, "wb") as f:
    # WAV header (mono, 16-bit)
    data = mono.tobytes()
    f.write(b"RIFF")
    f.write(struct.pack("<I", 36 + len(data)))
    f.write(b"WAVE")
    f.write(b"fmt ")
    f.write(struct.pack("<IHHIIHH", 16, 1, 1, SR, SR * 2, 2, 16))
    f.write(b"data")
    f.write(struct.pack("<I", len(data)))
    f.write(data)

size_kb = os.path.getsize(OUT) // 1024
print(f"OK bgm.wav -> {OUT} ({size_kb} KB, {DUR:.1f}s loop, {BARS} bars, mono {SR}Hz)")
