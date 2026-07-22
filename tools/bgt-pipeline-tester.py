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
import urllib.request
from datetime import datetime
from pathlib import Path

OLLAMA_URL = "http://192.168.0.25:11434/api/generate"
TEST_ENDPOINT = "http://192.168.0.25:5678/webhook/bgt-test-turn"
COMMUNITY_ENDPOINT = "http://192.168.0.25:5678/webhook/bgt-community-submit"
PERSONA_MODEL = "qwen3.5:4b"
MAX_TURNS = 8
LOG_DIR = Path(__file__).parent / "pipeline-tests"


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
    ap.add_argument("--mode", choices=["game", "modify"], required=True)
    ap.add_argument("--goal", required=True, help="Qué quiere conseguir el usuario sintético, en lenguaje natural")
    ap.add_argument("--photos", nargs="*", default=[], help="Rutas de imágenes a enviar como assets")
    args = ap.parse_args()
    run_conversation(args.mode, args.goal, args.photos)
