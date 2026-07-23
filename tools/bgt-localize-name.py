#!/usr/bin/env python3
"""
BGT Localize Name — busca en internet (SearXNG local) como se llama un juego
de mesa en cada mercado/idioma, para rellenar strings.xml sin adivinar.
Complementa a bgt-bgg-names.py (que depende de un token de BGG que aun no
tenemos) -- este usa busqueda web general, funciona ya mismo.

Uso:
  python bgt-localize-name.py "Friday"
  python bgt-localize-name.py "Coimbra" --langs es fr de
"""
import argparse
import json
import urllib.parse
import urllib.request

SEARXNG_URL = "http://192.168.0.25:8080/search"
OLLAMA_URL = "http://192.168.0.25:11434/api/generate"
MODEL = "qwen3.5:9b"

QUERIES = {
    "es": '"{name}" juego de mesa comprar España tienda',
    "en": '"{name}" board game buy US UK store',
    "fr": '"{name}" jeu de société acheter France boutique',
    "de": '"{name}" Brettspiel kaufen Deutschland Laden',
    "it": '"{name}" gioco da tavolo comprare Italia negozio',
}


def searxng_query(q):
    url = f"{SEARXNG_URL}?q={urllib.parse.quote(q)}&format=json&language=es"
    req = urllib.request.Request(url)
    with urllib.request.urlopen(req, timeout=25) as r:
        return json.loads(r.read().decode("utf-8"))


def ollama_generate(prompt, think=False):
    body = {"model": MODEL, "prompt": prompt, "stream": False, "think": think}
    req = urllib.request.Request(
        OLLAMA_URL, data=json.dumps(body).encode("utf-8"),
        headers={"Content-Type": "application/json"},
    )
    with urllib.request.urlopen(req, timeout=90) as r:
        return json.loads(r.read().decode("utf-8")).get("response", "")


def localize(name, lang):
    query = QUERIES[lang].format(name=name)
    try:
        res = searxng_query(query)
    except Exception as e:
        return {"lang": lang, "error": str(e)}

    snippets = []
    for r in res.get("results", [])[:8]:
        snippets.append(f"- {r.get('title', '')}: {r.get('content', '')[:200]} ({r.get('url', '')})")
    if not snippets:
        return {"lang": lang, "name": None, "reason": "sin resultados"}

    prompt = (
        f"El juego de mesa se llama '{name}' en su edición original/inglesa. "
        f"Aquí hay resultados reales de búsqueda para el mercado de idioma '{lang}':\n\n"
        + "\n".join(snippets)
        + "\n\nResponde SOLO JSON: {\"nombre_localizado\": \"nombre real usado en tiendas/webs de "
        "ese idioma, o null si es el mismo nombre sin traducir, o si no hay evidencia clara\", "
        "\"confianza\": 0-1, \"fuente_url\": \"url del resultado que lo confirma, o null\"}. "
        "No inventes un nombre traducido si no aparece literalmente en los resultados."
    )
    raw = ollama_generate(prompt)
    try:
        parsed = json.loads(raw)
    except Exception:
        parsed = {"nombre_localizado": None, "confianza": 0, "fuente_url": None, "raw": raw[:200]}
    parsed["lang"] = lang
    return parsed


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("name", help="Nombre del juego en su edición original")
    ap.add_argument("--langs", nargs="*", default=list(QUERIES.keys()),
                     choices=list(QUERIES.keys()), help="Idiomas a comprobar")
    args = ap.parse_args()

    print(f"=== Localizando '{args.name}' ===\n")
    for lang in args.langs:
        r = localize(args.name, lang)
        if r.get("error"):
            print(f"[{lang}] ERROR: {r['error']}")
            continue
        nombre = r.get("nombre_localizado")
        conf = r.get("confianza", 0)
        fuente = r.get("fuente_url")
        if nombre:
            print(f"[{lang}] {nombre}  (confianza {conf})")
            if fuente:
                print(f"        fuente: {fuente}")
        else:
            print(f"[{lang}] sin traducción encontrada (se usa '{args.name}' tal cual) — razón: {r.get('reason', r.get('raw', '?'))}")
