"""Genera docs/Entrega.pdf: portada + enlaces del repositorio y del video.

Uso:
  pip install reportlab
  python docs/generar_entrega.py --repo https://github.com/... --video https://youtu.be/... \
      --universidad "Nombre de la universidad"
"""
import argparse
from datetime import date
from pathlib import Path

from reportlab.lib.colors import HexColor
from reportlab.lib.pagesizes import letter
from reportlab.pdfgen import canvas

PENDIENTE = "(pendiente)"
MESES = ["enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto",
         "septiembre", "octubre", "noviembre", "diciembre"]

p = argparse.ArgumentParser()
p.add_argument("--repo", default=PENDIENTE)
p.add_argument("--video", default=PENDIENTE)
p.add_argument("--universidad", default="Unicolombo")
p.add_argument("--autor", default="Samuel David Ospina de Ávila")
p.add_argument("--profesor", default="Ing. John Arrieta")
p.add_argument("--ciudad", default="Cartagena de Indias, Colombia")
p.add_argument("--salida", default=str(Path(__file__).with_name("Entrega.pdf")))
a = p.parse_args()

hoy = date.today()
fecha = f"{hoy.day} de {MESES[hoy.month - 1]} de {hoy.year}"
TINTA, SUAVE, ACENTO = HexColor("#1B2430"), HexColor("#5B6878"), HexColor("#C98A2B")
W, H = letter
c = canvas.Canvas(a.salida, pagesize=letter)
c.setTitle("Taller UDP y Arquitectura Hexagonal: Samuel David Ospina De Avila")
c.setAuthor(a.autor)


def centrado(y, texto, fuente="Helvetica", tam=12, color=TINTA):
    c.setFont(fuente, tam)
    c.setFillColor(color)
    c.drawCentredString(W / 2, y, texto)


# ---------------- Página 1: portada ----------------
centrado(H - 100, a.universidad.upper(), "Helvetica-Bold", 22)
centrado(H - 124, "Sistemas Distribuidos", tam=13, color=SUAVE)
c.setStrokeColor(ACENTO)
c.setLineWidth(2)
c.line(W / 2 - 60, H - 142, W / 2 + 60, H - 142)

centrado(H / 2 + 90, "Conversión de Temperatura", "Helvetica-Bold", 26)
centrado(H / 2 + 60, "Cliente-Servidor con UDP/IP y Arquitectura Hexagonal", tam=14)
centrado(H / 2 + 36, "Ejercicio 3 · Taller del segundo corte", tam=12, color=SUAVE)

centrado(H / 2 - 50, "Presentado por", tam=11, color=SUAVE)
centrado(H / 2 - 70, a.autor, "Helvetica-Bold", 15)
centrado(H / 2 - 115, "Presentado a", tam=11, color=SUAVE)
centrado(H / 2 - 135, a.profesor, "Helvetica-Bold", 13)

centrado(128, a.ciudad, tam=12)
centrado(110, fecha, tam=11, color=SUAVE)
c.showPage()

# ---------------- Página 2: enlaces ----------------
x, y = 72, H - 90
c.setFont("Helvetica-Bold", 20)
c.setFillColor(TINTA)
c.drawString(x, y, "Enlaces de la entrega")
c.setStrokeColor(ACENTO)
c.line(x, y - 10, x + 60, y - 10)


def enlace(y, titulo, url, descripcion):
    c.setFont("Helvetica-Bold", 13)
    c.setFillColor(TINTA)
    c.drawString(x, y, titulo)
    c.setFont("Helvetica", 12)
    c.setFillColor(HexColor("#1F5FAF") if url != PENDIENTE else HexColor("#B03A2E"))
    c.drawString(x, y - 20, url)
    if url != PENDIENTE:
        ancho = c.stringWidth(url, "Helvetica", 12)
        c.linkURL(url, (x, y - 24, x + ancho, y - 8), relative=0)
    c.setFont("Helvetica", 10.5)
    c.setFillColor(SUAVE)
    c.drawString(x, y - 38, descripcion)


enlace(y - 60, "Repositorio (GitHub)", a.repo,
       "Código del servidor y del cliente, pruebas, README con capturas y guía de ejecución.")
enlace(y - 140, "Video de sustentación (YouTube)", a.video,
       "Prueba de la aplicación y explicación del código respecto a UDP y Arquitectura Hexagonal.")

c.showPage()
c.save()
print("PDF generado en", a.salida)
