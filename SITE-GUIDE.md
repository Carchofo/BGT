# Guía de `site/index.html` — para no perderse

Archivo único de ~860 líneas (HTML+CSS+JS autocontenido). Esta guía es el mapa para
encontrar cosas rápido y saber qué está hecho vs pendiente. Actualizar cada vez que se
toque el archivo de forma significativa.

## Mapa de secciones (HTML)

| Línea | Sección | Qué hace |
|-------|---------|----------|
| — | `<nav class="topnav">` | Menú fijo (`position:fixed`) con logo, hamburguesa (móvil) y enlaces |
| — | `<header class="hero">` | Logo, título, CTAs (Descargar APK / Únete a Telegram), badges |
| 357 | `#juegos` | Grid de 8 tarjetas de juego + tarjeta "+" (pedir juego). Cada tarjeta enlaza a su hilo de GitHub Discussions |
| 465 | `#comunidad` | Tarjetas: fotos, feedback, agentes IA, bot Telegram, rangos, foro |
| 506 | `#pedir-juego` | Formulario de 3 tipos: juego nuevo / modificación / variante de automa |
| 596 | `#donaciones` | Ko-fi (activable) + GitHub Sponsors (activo) |
| — | `<footer>` | Disclaimer legal + enlace GitHub |

## Bloques CSS (por comentario `/* ... */`)

| Línea | Bloque |
|-------|--------|
| 42 | HERO |
| 94 | SECTIONS (genérico) |
| 102 | GAMES GRID |
| 151 | COMMUNITY |
| 174 | FORM |
| 214 | DONATIONS |
| 235 | FOOTER |
| 244 | TOPNAV (incluye animación `@keyframes navBump`) |

**Paleta** (variables `:root`, extraídas por píxel del logo de Gemini):
`--navy:#153356` `--orange:#F38222` `--blue:#1B6BAA` `--cream:#F9F6EC` — ver
`bgt-games-vault/decisions/2026-07-23-foro-y-marca.md` para el porqué.

## Bloques JS (IIFEs, en orden de aparición)

| Línea | Bloque | Qué hace |
|-------|--------|----------|
| 619 | Menú hamburguesa | Toggle del `.nav-links` en móvil |
| 638 | Versión/descargas GitHub | Fetch a la API de Releases, rellena versión + contador + link APK |
| 663 | Bot de Telegram | Activa los 3 enlaces `telegram-link*` si `TELEGRAM_BOT_USERNAME` no es null |
| 682 | Donaciones (Ko-fi) | Activa el botón si `KOFI_USERNAME` no es null |
| 697 | Formulario | Lógica completa: mostrar/ocultar campos por tipo, multi-foto→base64, validación, envío secuencial al webhook |
| 838 | Indicador scroll | Animación "bump" del nav al llegar arriba/abajo de la página |

**Variables activables por nombre** (buscar y cambiar un valor, no tocar lógica):
- `TELEGRAM_BOT_USERNAME` (línea ~665) — hoy `'Xeft_bot'`, activo
- `KOFI_USERNAME` (línea ~684) — hoy `null`, pendiente de cuenta Ko-fi

## Assets (`site/assets/`)

| Archivo | Origen | Uso |
|---------|--------|-----|
| `logo-icon.png` | Logo Gemini, recortado + transparencia real (flood fill) | favicon, nav, hero |
| `logo-full.png` | Ídem, con texto "BOARD GAME TOOLS" | disponible, no usado aún |
| `bg-fondo1.jpg`, `bg-lapiz.jpg` | Fondos hechos por Rafel (Gemini) | body / sección Comunidad, atenuados con overlay |
| `banner_*.jpg` | Assets reales de la app Android (drawables) | tarjetas de juego — **NO buscar sustitutos en Google Images, ver AGENTS.md regla 7** |
| `ic_launcher.webp` | Icono viejo de la app | ya no se usa en el HTML, queda por si acaso |
| `banner_friday.jpg` | Foto propia de Rafel (portada española real, confirmado que es suya) | tarjeta de Viernes |

"Friday" se renombró a **"Viernes"** en toda la app y la web (2026-07-23) — es el nombre
real de la edición española. Ver nota de deuda técnica en `AGENTS.md` sobre nombres
hardcodeados antes de añadir más idiomas.

## Workflows de GitHub Actions relacionados

- `.github/workflows/deploy-pages.yml` — publica `site/` a GitHub Pages en cada push a
  `main` que toque `site/**`. **Pendiente: Rafel debe activar Settings → Pages → Source:
  GitHub Actions (una vez, 10s) para que el primer despliegue funcione.**
- `.github/workflows/release.yml` — build/firma/publica el APK (no toca `site/`).

## Estado: hecho vs pendiente

### ✅ Hecho
- Paleta y logo armonizados con el diseño de Gemini
- Menú fijo con animación de límite de scroll
- Formulario de 3 tipos con multi-foto (Opus diseñó los campos)
- Tarjetas de juego enlazan a GitHub Discussions (comentarios + reacciones = puntuación,
  sin backend propio)
- Despliegue automático a GitHub Pages configurado

### ⏳ Pendiente (acción de Rafel, no delegable)
- Activar Pages (Settings → Pages → Source: GitHub Actions)
- Activar Discussions (Settings → General → Features) + crear GitHub PAT para que
  BGT Forum Agent y BGT Group Announce funcionen
- Cuenta Ko-fi → cambiar `KOFI_USERNAME`
- Crear el grupo de Telegram (el bot funciona en modo prueba en cualquier chat mientras tanto)

### 🔧 Pendiente (yo, cuando toque)
- Crear los 8 hilos semilla de Discussions y cambiar los enlaces de búsqueda por la URL
  directa de cada hilo
- Aplicar la guía de tipos de automa al prompt de `variante_de_automa` — **con cuidado**,
  ver `bgt-games-vault/learnings/2026-07-22-game-request-extraction-bug.md` (el prompt ya
  tiene un problema de fiabilidad conocido, no añadir complejidad sin testear con
  `tools/bgt-pipeline-tester.py` primero)
- Créditos de autor en pantalla "Acerca de" de la app + web (variantes de automa)

## Cómo probar cambios antes de subir

```bash
cd site && python -m http.server 8899
```
Luego abrir con el navegador (`mcp__Claude_Browser__preview_start` con
`http://localhost:8899/index.html`) — **nunca fiarse del Artifact de Claude para
verificar assets locales**, no carga `site/assets/` (rutas relativas rotas ahí).
