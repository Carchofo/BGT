#!/usr/bin/env python3
"""
BGT BGG Names — busca en BoardGameGeek los nombres localizados de un juego
por idioma/edicion (ej. Friday -> Viernes en la edicion espanola), para
rellenar strings.xml sin adivinar traducciones.

BGG exige desde finales de 2025 registrar una app y usar un token Bearer
(ver https://boardgamegeek.com/using_the_xml_api). Poner el token en la
variable de entorno BGG_API_TOKEN o pasarlo con --token.

Uso:
  python bgt-bgg-names.py "Friday" --token TU_TOKEN
  python bgt-bgg-names.py --bgg-id 194879 --token TU_TOKEN

Integracion prevista: paso automatico cuando una peticion de "juego nuevo"
se completa (bot Telegram / formulario web) y antes de mover el candidato
a implementado -- ver bgt-games-vault/decisions/ para el enganche exacto.
"""
import argparse
import os
import sys
import urllib.request
import urllib.parse
import xml.etree.ElementTree as ET

BASE = "https://boardgamegeek.com/xmlapi2"


def bgg_get(path, token):
    req = urllib.request.Request(
        f"{BASE}/{path}",
        headers={
            "User-Agent": "BGT-BoardGameTools/1.0 (contacto: rafel@spicyoffers.com)",
            "Authorization": f"Bearer {token}",
        },
    )
    with urllib.request.urlopen(req, timeout=20) as r:
        return r.read()


def search_game(name, token):
    q = urllib.parse.quote(name)
    xml = bgg_get(f"search?query={q}&type=boardgame", token)
    root = ET.fromstring(xml)
    results = []
    for item in root.findall("item"):
        bgg_id = item.get("id")
        name_el = item.find("name")
        year_el = item.find("yearpublished")
        results.append({
            "id": bgg_id,
            "name": name_el.get("value") if name_el is not None else "?",
            "year": year_el.get("value") if year_el is not None else "?",
        })
    return results


def get_localized_names(bgg_id, token):
    """Devuelve lista de (idioma, nombre) por cada version/edicion con idioma marcado."""
    xml = bgg_get(f"thing?id={bgg_id}&versions=1", token)
    root = ET.fromstring(xml)
    out = []
    for item in root.findall("item"):
        for version in item.findall("./versions/item"):
            name_el = version.find("name")
            name = name_el.get("value") if name_el is not None else None
            lang = None
            for link in version.findall("link"):
                if link.get("type") == "language":
                    lang = link.get("value")
            if name and lang:
                out.append((lang, name))
    # dedup conservando el primero de cada idioma
    seen = set()
    dedup = []
    for lang, name in out:
        if lang not in seen:
            seen.add(lang)
            dedup.append((lang, name))
    return dedup


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("name", nargs="?", help="Nombre del juego a buscar")
    ap.add_argument("--bgg-id", help="ID de BGG directo (evita la búsqueda)")
    ap.add_argument("--token", default=os.environ.get("BGG_API_TOKEN"),
                     help="Token Bearer de BGG (o variable de entorno BGG_API_TOKEN)")
    args = ap.parse_args()

    if not args.token:
        print("Falta el token de BGG. Registra la app en tu cuenta de BGG y pasa --token o "
              "define BGG_API_TOKEN. Ver https://boardgamegeek.com/using_the_xml_api", file=sys.stderr)
        sys.exit(1)

    bgg_id = args.bgg_id
    if not bgg_id:
        if not args.name:
            print("Indica un nombre de juego o --bgg-id.", file=sys.stderr)
            sys.exit(1)
        candidates = search_game(args.name, args.token)
        if not candidates:
            print(f"Sin resultados para '{args.name}'.")
            sys.exit(0)
        print(f"Resultados para '{args.name}':")
        for c in candidates[:8]:
            print(f"  BGG #{c['id']} — {c['name']} ({c['year']})")
        bgg_id = candidates[0]["id"]
        print(f"\nUsando el primero: BGG #{bgg_id}\n")

    names = get_localized_names(bgg_id, args.token)
    if not names:
        print("No se encontraron ediciones con idioma marcado en BGG para este juego.")
    else:
        print(f"Nombres localizados (BGG #{bgg_id}):")
        for lang, name in names:
            print(f"  {lang}: {name}")
