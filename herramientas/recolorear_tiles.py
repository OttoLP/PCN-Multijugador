#!/usr/bin/env python3
"""Genera los tiles y el monstruo del juego con la paleta apagada a partir de los originales.

Uso (desde la raiz del repo, requiere Pillow: pip install pillow):

    python herramientas/recolorear_tiles.py

Lee herramientas/tiles-originales/*.png (colores brillantes originales) y escribe
cliente/src/tiles/*.png. Tambien dibuja la fuente (fuente.png, 3 x 3 tiles) y recolorea al
monstruo (herramientas/sprites-originales -> cliente/src/spritesmonstruo) en morado para que
resalte sobre el fondo apagado.
Para cambiar el ambiente del juego, ajusta PALETA y vuelve a ejecutarlo: siempre parte
de los originales, asi que se puede repetir sin que los colores se acumulen.
"""
import colorsys
from pathlib import Path

from PIL import Image, ImageDraw

RAIZ = Path(__file__).resolve().parent.parent
ORIGINALES = RAIZ / "herramientas" / "tiles-originales"
DESTINO = RAIZ / "cliente" / "src" / "tiles"
SPRITES_ORIGINALES = RAIZ / "herramientas" / "sprites-originales"
SPRITES_DESTINO = RAIZ / "cliente" / "src" / "spritesmonstruo"

# Por tile: (cambio de tono en grados, factor de saturacion, factor de brillo).
# Saturacion y brillo bajos = colores apagados; el fondo se ve oscuro y los personajes resaltan.
PALETA = {
    "pasto": (0, 0.40, 0.46),
    "arbol": (0, 0.55, 0.46),
    "agua":  (-28, 0.38, 0.46),   # pantano: de azul brillante a verde turbio
    "arena": (0, 0.45, 0.60),
    "muro":  (0, 0.50, 0.70),
    "suelo": (0, 0.55, 0.66),
}


# Verde del pasto original. El fondo del tile del arbol es este mismo verde: se recolorea igual que
# el pasto para que cada arbol no quede dentro de un cuadro de otro tono.
VERDE_PASTO = (34, 177, 76)


def apagar(imagen, cambio_tono, saturacion, brillo, solo_color=False, fondo_como_pasto=False):
    """solo_color=True deja intactos los pixeles casi grises (contorno y ojo del monstruo)."""
    salida = Image.new("RGBA", imagen.size)
    pixeles = []
    for r, g, b, a in imagen.getdata():
        tono, sat, bri = cambio_tono, saturacion, brillo
        if fondo_como_pasto and (r, g, b) == VERDE_PASTO:
            tono, sat, bri = PALETA["pasto"]
        h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
        if solo_color and s <= 0.15:
            pixeles.append((r, g, b, a))
            continue
        h = (h + tono / 360) % 1.0
        r2, g2, b2 = colorsys.hsv_to_rgb(h, min(1, s * sat), min(1, v * bri))
        pixeles.append((round(r2 * 255), round(g2 * 255), round(b2 * 255), a))
    salida.putdata(pixeles)
    return salida


def dibujar_fuente():
    """Fuente redonda de 48 x 48 px (3 x 3 tiles de 16 px) sobre suelo de piedra."""
    imagen = Image.new("RGBA", (48, 48), (74, 66, 56, 255))
    pincel = ImageDraw.Draw(imagen)
    # losas del suelo
    for i in range(0, 48, 8):
        pincel.line([(i, 0), (i, 47)], fill=(66, 59, 50, 255))
        pincel.line([(0, i), (47, i)], fill=(66, 59, 50, 255))
    pincel.ellipse([5, 5, 42, 42], fill=(54, 56, 64, 255))      # sombra
    pincel.ellipse([4, 4, 43, 43], fill=(104, 108, 118, 255))   # borde de piedra
    pincel.ellipse([7, 7, 40, 40], fill=(78, 82, 92, 255))      # cara interior
    pincel.ellipse([9, 9, 38, 38], fill=(62, 96, 104, 255))     # agua
    pincel.ellipse([12, 12, 35, 35], fill=(72, 112, 120, 255))  # agua mas clara
    for y, x0, x1 in [(17, 15, 22), (23, 26, 32), (30, 16, 24)]:  # ondas
        pincel.line([(x0, y), (x1, y)], fill=(118, 160, 168, 255))
    pincel.ellipse([19, 19, 28, 28], fill=(120, 124, 134, 255)) # pedestal
    pincel.ellipse([21, 21, 26, 26], fill=(150, 154, 164, 255)) # surtidor
    return imagen


def main():
    DESTINO.mkdir(parents=True, exist_ok=True)
    for nombre, (tono, sat, brillo) in PALETA.items():
        original = Image.open(ORIGINALES / f"{nombre}.png").convert("RGBA")
        apagar(original, tono, sat, brillo, fondo_como_pasto=(nombre == "arbol")).save(DESTINO / f"{nombre}.png")
        print("tile", nombre)
    dibujar_fuente().save(DESTINO / "fuente.png")
    print("tile fuente (3 x 3)")
    monstruo = Image.open(SPRITES_ORIGINALES / "monstruo.png").convert("RGBA")
    apagar(monstruo, 140, 1.0, 1.25, solo_color=True).save(SPRITES_DESTINO / "monstruo.png")
    print("sprite monstruo")


if __name__ == "__main__":
    main()
