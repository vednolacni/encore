#!/usr/bin/env python3
"""Renders the banner for the TV's home screen and the launcher icons from the logo and the font.

Usage: python3 tools/make_launcher_assets.py   (needs Pillow and CairoSVG)

The logo is tools/logo.svg: a white TV with an antenna on a sky blue square, a sound wave cut out
of its screen, in the manner of Music Assistant's own logo. It is drawn here from the SVG at every
size, never scaled from a bitmap.
"""
import io
import os
import re

import cairosvg
from PIL import Image, ImageDraw, ImageFont

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "androidApp", "src", "main", "res")
FONT = os.path.join(ROOT, "layers", "ui", "src", "main", "res", "font", "jost.ttf")
LOGO = os.path.join(ROOT, "tools", "logo.svg")

with open(LOGO, encoding="utf-8") as f:
    _SVG = f.read()


def _color(name):
    """A color of the logo, by what it paints: the square's blue, or the TV's white."""
    pattern = r'<rect id="background"[^>]*fill="#(\w{6})"' if name == "background" else r'<path fill="#(\w{6})"'
    v = re.search(pattern, _SVG).group(1)
    return tuple(int(v[i:i + 2], 16) for i in (0, 2, 4))


BLUE = _color("background")
WHITE = _color("mark")
# The SVG's square, and a square that holds just the TV with its antenna.
FULL = 1024
TIGHT = 560


def logo(size, background=True, extent=FULL):
    """The logo on a square of [size] px showing [extent] units of the 1024 unit SVG around its
    middle; without its [background], the white TV alone on transparency."""
    svg = _SVG if background else re.sub(r'<rect id="background"[^>]*/>', "", _SVG)
    x = (FULL - extent) / 2
    svg = svg.replace('viewBox="0 0 1024 1024"', f'viewBox="{x} {x} {extent} {extent}"', 1)
    png = cairosvg.svg2png(bytestring=svg.encode("utf-8"), output_width=size, output_height=size)
    return Image.open(io.BytesIO(png)).convert("RGBA")


def jost(size, weight=400):
    f = ImageFont.truetype(FONT, size)
    vals = []
    for a in f.get_variation_axes():
        name = a["name"] if isinstance(a["name"], str) else a["name"].decode()
        vals.append({"Weight": weight}.get(name, a["default"]))
    f.set_variation_by_axes(vals)
    return f


def banner(path, w=320, h=180):
    """The TV beside the name in white on the logo's blue, both centered on the banner as a group."""
    s = 4
    img = Image.new("RGBA", (w * s, h * s), BLUE + (255,))
    side = int(h * s * 0.46)
    mark = logo(side, background=False, extent=TIGHT)
    size = 40 * s
    f = jost(size, 600)
    d = ImageDraw.Draw(img)
    text = "Encore"
    tw = d.textlength(text, font=f)
    gap = int(20 * s)
    x0 = int((w * s - (side + gap + tw)) / 2)
    y0 = (h * s - side) // 2
    img.alpha_composite(mark, (x0, y0))
    ascent, _ = f.getmetrics()
    # The cap height (0.70 em) centered on the TV.
    cap = 0.70 * size
    d.text((x0 + side + gap, int(h * s / 2 + cap / 2 - ascent)), text, font=f, fill=WHITE)
    img = img.convert("RGB").resize((w, h), Image.LANCZOS)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path, optimize=True)


def legacy_icon(path, size):
    """The whole logo, the TV in the middle of its blue square."""
    img = logo(size).convert("RGB")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path, optimize=True)


if __name__ == "__main__":
    banner(os.path.join(RES, "drawable-xhdpi", "banner.png"))
    for density, size in {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}.items():
        legacy_icon(os.path.join(RES, f"mipmap-{density}", "ic_launcher.png"), size)
    print("ok")
