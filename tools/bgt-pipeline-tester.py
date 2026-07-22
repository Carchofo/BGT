#!/usr/bin/env python3
"""
BGT Pipeline Tester — agente sintetico local que simula un usuario de Telegram
pidiendo un juego nuevo o reportando un fallo, para probar los prompts reales
de produccion (BGT Agent Test Endpoint reusa exactamente la logica de
BGT Telegram Bot pero sin escribir en las colas reales).

Modelo persona: qwen3.5:4b (conversacional, ligero, suficiente para mensajes
cortos tipo Telegram). Modelo agente actualizador: qwen2.5-coder:14b (el mismo
que usa produccion) via el endpoint de test.

Uso:
  python bgt-pipeline-tester.py --mode game --goal "Quieres pedir Wingspan porque
      tiene automa oficial" --photos foto1.jpg foto2.jpg
  python bgt-pipeline-tester.py --mode modify --goal "El track Terrorifico de
      Criaturas Maravillosas esta vacio, quieres reportarlo"
"""
import argparse
import base64
import json
import sys
import urllib.parse
import urllib.request
from datetime import datetime
from pathlib import Path

OLLAMA_URL = "http://192.168.0.25:11434/api/generate"
TEST_ENDPOINT = "http://192.168.0.25:5678/webhook/bgt-test-turn"
COMMUNITY_ENDPOINT = "http://192.168.0.25:5678/webhook/bgt-community-submit"
SEARXNG_URL = "http://192.168.0.25:8080/search"
PERSONA_MODEL = "qwen3.5:4b"
DISCOVERY_MODEL = "qwen2.5-coder:14b"  # mismo modelo que usa el fixer/router de produccion
MAX_TURNS = 8
LOG_DIR = Path(__file__).parent / "pipeline-tests"

IMPLEMENTED_GAMES = ["Spooktacular", "Criaturas Maravillosas", "Tiletum", "Piratas de Maracaibo",
                     "Castle Combo", "Cascadia", "Coimbra", "Friday"]
CANDIDATE_GAMES = ["Viticulture", "Spirit Island", "Wingspan", "Arkham Horror LCG", "Pandemic",
                   "Robinson Crusoe", "Gloomhaven: Jaws of the Lion", "7 Wonders Duel", "Too Many Bones"]


def searxng_query(q):
    url = f"{SEARXNG_URL}?q={urllib.parse.quote(q)}&format=json&language=es"
    with urllib.request.urlopen(url, timeout=25) as r:
        return json.loads(r.read().decode("utf-8"))


def discover_game_candidate():
    """Busca en foros (Reddit/BGG via SearXNG, gratis y local) un juego con
    modo solitario del que se hable bien y que no este ya en BGT ni en la cola
    de candidatos. Usa qwen2.5-coder:14b (mismo modelo que produccion) para
    elegir uno de los resultados reales, no inventado."""
    queries = [
        "board game solo mode automa recommendation reddit soloboardgaming 2026",
        "best new solo board game 2026 boardgamegeek",
    ]
    snippets = []
    for q in queries:
        try:
            res = searxng_query(q)
            for r in res.get("results", [])[:8]:
                snippets.append(f"- {r.get('title','')}: {r.get('content','')[:200]} ({r.get('url','')})")
        except Exception as e:
            print(f"[discover] búsqueda falló para '{q}': {e}")
    if not snippets:
        return None

    excluded = ", ".join(IMPLEMENTED_GAMES + CANDIDATE_GAMES)
    prompt = (
        "Estos son resultados reales de foros (Reddit/BGG) sobre juegos de mesa con modo solitario:\n\n"
        + "\n".join(snippets)
        + f"\n\nYa tenemos implementados o en cola: {excluded}. "
        "Elige UN juego mencionado en los resultados de arriba (no de tu conocimiento previo) que NO esté "
        "en esa lista y tenga buena fama de modo solitario. Responde SOLO JSON: "
        '{"juego": "nombre exacto", "por_que": "una frase citando lo que dicen los foros", "fuente_url": "url del snippet que lo menciona"}. '
        "Si ningún resultado sirve, responde exactamente: NINGUNO"
    )
    resp = post_json(OLLAMA_URL, {"model": DISCOVERY_MODEL, "prompt": prompt, "stream": False, "format": "json"})
    raw = resp.get("response", "").strip()
    if raw == "NINGUNO" or not raw:
        return None
    try:
        return json.loads(raw)
    except Exception:
        print(f"[discover] respuesta no parseable: {raw[:200]}")
        return None


def post_json(url, payload, timeout=180):
    data = json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(url, data=data, headers={"Content-Type": "application/json"})
    with urllib.request.urlopen(req, timeout=timeout) as r:
        return json.loads(r.read().decode("utf-8"))


def persona_turn(goal, history):
    """Genera el siguiente mensaje del usuario sintetico dado el objetivo y el historial."""
    system = (
        "Eres un usuario random de un grupo de Telegram de fans de juegos de mesa. "
        "Escribes como una persona normal: mensajes cortos, informales, a veces con "
        "erratas, nunca formato de lista. Tu objetivo en esta conversacion es: "
        f"{goal}\n"
        "Responde SOLO con el siguiente mensaje que escribirias, nada mas (sin comillas, "
        "sin explicar que eres una IA). Si el bot ya te ha dado toda la info que necesitas "
        "y no queda nada por decir, responde exactamente: FIN_CONVERSACION"
    )
    convo = "\n".join(f"{'Bot' if r=='bot' else 'Tu'}: {t}" for r, t in history)
    prompt = f"Historial:\n{convo}\n\nEscribe tu siguiente mensaje:" if history else "Escribe tu primer mensaje al bot:"
    # think:False es necesario -- qwen3.5 en este Ollama (0.30.10) devuelve "response" vacio
    # si el modo thinking queda activado por defecto (bug conocido, ver memoria de sesiones previas).
    resp = post_json(OLLAMA_URL, {"model": PERSONA_MODEL, "system": system, "prompt": prompt, "stream": False, "think": False})
    return resp.get("response", "").strip()


def run_conversation(mode, goal, photos):
    history = []
    session = None
    transcript = [f"# Test de pipeline BGT — modo `{mode}`\n", f"**Objetivo del usuario sintético:** {goal}\n"]
    print(f"=== Iniciando test modo={mode} ===")

    for turn in range(1, MAX_TURNS + 1):
        user_msg = persona_turn(goal, history)
        if user_msg == "FIN_CONVERSACION" or not user_msg:
            print("(persona sintética decide terminar la conversación)")
            break
        history.append(("user", user_msg))
        transcript.append(f"**Turno {turn} — usuario:** {user_msg}\n")
        print(f"[usuario] {user_msg}")

        result = post_json(TEST_ENDPOINT, {"mode": mode, "session": session, "text": user_msg})
        session = result.get("session")
        reply = result.get("reply", "(sin respuesta)")
        history.append(("bot", reply))
        transcript.append(f"**Turno {turn} — bot:** {reply}\n")
        print(f"[bot]      {reply}")

        if result.get("completed"):
            transcript.append(f"\n**PETICIÓN COMPLETADA en el turno {turn}.**\n")
            transcript.append(f"\n```json\n{json.dumps(result.get('raw', {}), ensure_ascii=False, indent=2)}\n```\n")
            print(">>> Petición marcada como completa por el agente.")
            break
    else:
        transcript.append(f"\n**NO se completó en {MAX_TURNS} turnos (posible bucle o prompt poco claro).**\n")
        print(f">>> AVISO: no se completó tras {MAX_TURNS} turnos.")

    if photos:
        transcript.append("\n## Envío de assets (fotos)\n")
        for p in photos:
            path = Path(p)
            if not path.exists():
                transcript.append(f"- ⚠️ {p} no existe, se omite.\n")
                continue
            b64 = base64.b64encode(path.read_bytes()).decode("utf-8")
            game_name = None
            if session and isinstance(session.get("draft"), dict):
                game_name = session["draft"].get("nombre_juego") or session["draft"].get("juego")
            payload = {
                "game": game_name or "General",
                "type": "photo",
                "message": f"[TEST] asset enviado por el agente sintético para: {goal}",
                "version": "test", "device": "bgt-pipeline-tester", "ts": int(datetime.now().timestamp() * 1000),
                "image": b64,
            }
            try:
                r = post_json(COMMUNITY_ENDPOINT, payload, timeout=60)
                transcript.append(f"- 📷 `{path.name}` enviada → `{r.get('id')}` (análisis de visión en curso, revisar en `shared/bgt-community/`)\n")
                print(f"[foto] {path.name} enviada, id={r.get('id')}")
            except Exception as e:
                transcript.append(f"- ❌ error enviando `{path.name}`: {e}\n")
                print(f"[foto] ERROR enviando {path.name}: {e}")

    LOG_DIR.mkdir(exist_ok=True)
    log_file = LOG_DIR / f"{mode}_{int(datetime.now().timestamp())}.md"
    log_file.write_text("\n".join(transcript), encoding="utf-8")
    print(f"\nTranscripción guardada en {log_file}")
    return log_file


if __name__ == "__main__":
    ap = argparse.ArgumentParser(description="Simula un usuario probando el bot de BGT")
    ap.add_argument("--mode", choices=["game", "modify"], required=False)
    ap.add_argument("--goal", required=False, help="Qué quiere conseguir el usuario sintético, en lenguaje natural")
    ap.add_argument("--photos", nargs="*", default=[], help="Rutas de imágenes a enviar como assets")
    ap.add_argument("--discover", action="store_true",
                     help="Busca en foros (SearXNG) un juego real con buena fama de modo solitario "
                          "que no esté ya en BGT, y prueba a pedirlo con /juego_nuevo automáticamente")
    args = ap.parse_args()

    if args.discover:
        print("=== Descubriendo candidato en foros (Reddit/BGG vía SearXNG) ===")
        found = discover_game_candidate()
        if not found:
            print("No se encontró ningún candidato nuevo en esta pasada. Nada que probar.")
            sys.exit(0)
        print(f"[discover] Candidato encontrado: {found.get('juego')} — {found.get('por_que')}")
        print(f"[discover] Fuente: {found.get('fuente_url')}")
        goal = (
            f"Quieres pedir el juego \"{found.get('juego')}\" para BGT. Motivo: {found.get('por_que')} "
            f"Lo has visto comentado en foros de juegos de mesa, en concreto aquí: {found.get('fuente_url')}"
        )
        log_file = run_conversation("game", goal, [])
        with open(log_file, "a", encoding="utf-8") as f:
            f.write(f"\n## Origen del candidato (descubrimiento automático)\n"
                    f"- Juego: {found.get('juego')}\n- Razón: {found.get('por_que')}\n"
                    f"- Fuente: {found.get('fuente_url')}\n")
    else:
        if not args.mode or not args.goal:
            ap.error("--mode y --goal son obligatorios salvo que uses --discover")
        run_conversation(args.mode, args.goal, args.photos)
