#!/usr/bin/env python3
"""Renders the banner for the TV's home screen and the launcher icons from the logo.

Usage: python3 tools/make_launcher_assets.py   (needs Pillow, numpy and CairoSVG)

The logo is tools/logo.svg, the owner's own: an espresso brown TV with an antenna and five pale
bars on its screen, beside the name. The icons show the TV alone on a square of warm sand, the
banner the whole logo on that sand. Everything is drawn from the SVG at its size, never scaled
from a bitmap.
"""
import io
import os
import re

import cairosvg
import numpy as np
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "androidApp", "src", "main", "res")
LOGO = os.path.join(ROOT, "tools", "logo.svg")

with open(LOGO, encoding="utf-8") as f:
    _SVG = f.read()

ESPRESSO = "#2B211A"   # the TV and the name
LINEN = "#F7F2EA"      # the bars on the TV's screen
SAND = "#EDE5D9"       # the square the icons stand on

# Where the TV stands in the logo's units: from its body's left edge to its right, from the tips
# of its antenna to its body's foot.
MARK = (258.0, 136.0, 770.5, 579.5)
# How much of an icon's width the TV takes.
MARK_SHARE = 0.56


def rgb(hex_color):
    v = hex_color.lstrip("#")
    return tuple(int(v[i:i + 2], 16) for i in (0, 2, 4))


def _group(name):
    return re.search(rf'<g id="{name}">.*?</g>', _SVG, re.S).group(0)


def _png(svg, w, h):
    png = cairosvg.svg2png(bytestring=svg.encode("utf-8"), output_width=w, output_height=h)
    return Image.open(io.BytesIO(png)).convert("RGBA")


def _svg(view, body, background=None):
    x, y, w, h = view
    ground = f'<rect x="{x}" y="{y}" width="{w}" height="{h}" fill="{background}"/>' if background else ""
    return (f'<svg xmlns="http://www.w3.org/2000/svg" width="{w}" height="{h}" viewBox="{x} {y} {w} {h}">'
            f"{ground}{body}</svg>")


def _recolored(body, dark):
    """The logo for a dark page: the TV and the name linen, the bars espresso."""
    if not dark:
        return body
    return body.replace(ESPRESSO, "#000001").replace(LINEN, ESPRESSO).replace("#000001", LINEN)


def logo(size, background=True):
    """The icon, [size] px square: the TV in the middle of its square of sand, or without the
    [background] alone on transparency."""
    x0, y0, x1, y1 = MARK
    side = (x1 - x0) / MARK_SHARE
    cx, cy = (x0 + x1) / 2, (y0 + y1) / 2
    view = (cx - side / 2, cy - side / 2, side, side)
    return _png(_svg(view, _group("mark"), SAND if background else None), size, size)


def _name_right():
    """Where the name ends on the right, in the logo's units, as it is drawn."""
    x, y, w, h = (float(v) for v in re.search(r'viewBox="([^"]+)"', _SVG).group(1).split())
    alpha = np.asarray(_png(_svg((x, y, w, h), _group("name")), int(w), int(h)))[..., 3]
    return x + np.nonzero(alpha.max(axis=0) > 8)[0][-1] + 1


def whole(height, dark=False):
    """The whole logo, the TV beside the name, [height] px tall on transparency, from the tips of
    the antenna to the TV's foot and from its left edge to the name's end."""
    x0, y0, _, y1 = MARK
    view = (x0, y0, _name_right() - x0, y1 - y0)
    body = _recolored(_group("mark") + _group("name"), dark)
    return _png(_svg(view, body), round(height * view[2] / view[3]), height)


def banner(path, w=320, h=180):
    """The whole logo on the icons' sand, in the middle of the banner."""
    s = 4
    img = Image.new("RGBA", (w * s, h * s), rgb(SAND) + (255,))
    art = whole(int(h * s * 0.34))
    img.alpha_composite(art, ((w * s - art.width) // 2, (h * s - art.height) // 2))
    img = img.convert("RGB").resize((w, h), Image.LANCZOS)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path, optimize=True)


def legacy_icon(path, size):
    """The TV in the middle of its square of sand."""
    img = logo(size).convert("RGB")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path, optimize=True)


if __name__ == "__main__":
    banner(os.path.join(RES, "drawable-xhdpi", "banner.png"))
    for density, size in {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}.items():
        legacy_icon(os.path.join(RES, f"mipmap-{density}", "ic_launcher.png"), size)
    print("ok")
