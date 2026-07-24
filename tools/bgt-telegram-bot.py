import os, json, time, requests

TOKEN   = "8877283469:AAGOAPNiQr1W1Osfg1m10HpirRRgYEo8mO8"
CHAT_ID = -1004373692684
API     = f"https://api.telegram.org/bot{TOKEN}"

JUEGOS = [
    "🎃 Spooktacular — Bot Killtron",
    "🐾 Criaturas Maravillosas — Bot Tingent",
    "🏛️ Tiletum — Bot Titus",
    "🏴‍☠️ Piratas de Maracaibo — Bot Jordán",
    "🏰 Castle Combo — Bot Anton",
    "🎓 Coimbra — Bot interferencia",
    "🦆 Cascadia — Calculadora de puntuación",
    "📅 Viernes — Modo solitario",
]

BIENVENIDA = """👋 ¡Bienvenido/a a *Board Game Tools*!

🎲 Somos una comunidad de jugadores de mesa que juegan en solitario con bots IA fanmade.

📱 *La app es gratuita, sin anuncios y sin registro.*

Comandos disponibles:
/juegos — Ver juegos soportados
/reportar — Reportar un fallo
/votar — Pedir un juego nuevo
/web — Ir a la web

¡Buenas partidas! 🃏"""

def send(chat_id, text, parse_mode="Markdown"):
    requests.post(f"{API}/sendMessage", json={
        "chat_id": chat_id,
        "text": text,
        "parse_mode": parse_mode
    })

def get_updates(offset=None):
    params = {"timeout": 30}
    if offset:
        params["offset"] = offset
    r = requests.get(f"{API}/getUpdates", params=params, timeout=35)
    return r.json().get("result", [])

def handle(update):
    msg = update.get("message") or update.get("edited_message")
    if not msg:
        return

    chat_id = msg["chat"]["id"]
    text    = msg.get("text", "")
    user    = msg.get("from", {}).get("first_name", "")

    # Bienvenida a nuevos miembros
    if msg.get("new_chat_members"):
        for m in msg["new_chat_members"]:
            if not m.get("is_bot"):
                send(chat_id, f"👋 ¡Buenos días, *{m['first_name']}*! Bienvenido/a al grupo de *Board Game Tools*.\n\n{BIENVENIDA}")
        return

    if not text.startswith("/"):
        return

    cmd = text.split()[0].split("@")[0].lower()

    if cmd == "/start" or cmd == "/help":
        send(chat_id, BIENVENIDA)

    elif cmd == "/juegos":
        lista = "\n".join(f"• {j}" for j in JUEGOS)
        send(chat_id, f"🎲 *Juegos soportados actualmente:*\n\n{lista}\n\n📱 Descarga: https://github.com/Carchofo/BGT")

    elif cmd == "/reportar":
        send(chat_id, f"🐛 *Reportar un fallo*\n\nDescribe el problema aquí en el grupo o abre un issue en:\nhttps://github.com/Carchofo/BGT/issues\n\nIncluye: juego, paso donde falla, y si puedes una captura.")

    elif cmd == "/votar":
        send(chat_id, f"🗳️ *Pedir un juego nuevo*\n\nDi qué juego quieres ver en BGT y la comunidad vota.\n\nTambién puedes pedirlo en:\nhttps://bgt.rafel.dev/#pedir-juego")

    elif cmd == "/web":
        send(chat_id, "🌐 Web de BGT: https://bgt.rafel.dev")

if __name__ == "__main__":
    print("✅ BGT Bot arrancado — escuchando...")
    offset = None
    while True:
        try:
            updates = get_updates(offset)
            for u in updates:
                handle(u)
                offset = u["update_id"] + 1
        except Exception as e:
            print(f"Error: {e}")
            time.sleep(5)
