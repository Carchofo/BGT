# BGT — Análisis de pantallas reales (2026-07-14)

Generado para ajustar los prompts de los 5 agentes n8n (search, catalog, assets, community, images) a las pantallas reales de la app.

## Juegos soportados (8)

| Juego | Pantalla | Bot | Qué hace |
|-------|----------|-----|----------|
| Spooktacular | SoloModeScreen | Killtron | Simulador bot por cartas: 3 mazos (A/B/C, 9 cartas), prioridad colores, tabs Setup/Juego/Puntos |
| Criaturas Maravillosas | CMSoloModeScreen | Tingent | Mazo 12 cartas, tracker 24 pos, 4 dificultades. ⚠️ Track Terrorífico (4★) SIN VERIFICAR (todo NONE) |
| Tiletum | TiletumSoloScreen | Titus | 13 cartas acción + 8 desafío, fases SETUP→PLAYING→KING→FAIR×4→FINAL, dados valor/color |
| Piratas de Maracaibo | MaracaiboSoloScreen | Jordán | ViewModel persistente, marcador mejora 0-3, 5 acciones, dificultad 0-6 cartas B, expansión |
| Castle Combo | CastleComboSoloScreen | Anton | Solo contadores (monedas/llaves/descuentos) → PV por dificultad. ¿Falta mecánica de mazo del bot? |
| Cascadia | CascadiaScoreScreen | — | Solo calculadora: 4 variantes carta (A-D) × 5 animales + hábitats + naturaleza. Tablas hardcodeadas |
| Coimbra | CoimbraSoloScreen | Bot Interferencia | Dado virtual + tablas bloqueo, ronda 1-4, calculadora. Sin ViewModel (estado se pierde). Bot probablemente incompleto |
| Friday | FridaySoloScreen | — | Tracker manual: PV/ronda/fuerza peligro. Candidato a simulador de mazo |

## Qué investigar por juego (input para agentes n8n)

- **Spooktacular**: reglamento solo Killtron (prioridades/desempates), FAQ BGG, imágenes 9 cartas mazos A/B/C
- **Criaturas Maravillosas**: PRIORIDAD MÁXIMA — fotos/escaneo track Terrorífico 4★ del tablero físico; reglas Tingent (empates océanos, recarga), erratas BGG
- **Tiletum**: reglamento automa Titus (fases Rey/Feria exactas, dados amarillos), texto oficial 13+8 cartas
- **Maracaibo (Piratas)**: reglas solo Jordán (mazos A/B, expansión), tabla saqueo/exploración por mejora
- **Castle Combo**: reglas oficiales solo Anton — verificar si tiene mazo/patrón de robo (la app solo tiene contadores)
- **Cascadia**: verificar tablas puntuación A/B/C/D contra cartas oficiales, expansión Landmarks
- **Coimbra**: reglas completas bot solo (app solo cubre bloqueo dados+ciudades), tramos rating oficiales
- **Friday**: composición exacta mazo, reglas envejecimiento/piratas

## Placeholders
- MonstersScreen (guía 20 monstruos) y ScoreBoardScreen (historial/récords) — "En desarrollo", sin entrada desde Home
- 4 slots "Próximamente" (g5-g8) en Home

## Infraestructura
- **UpdateChecker**: autoupdate OTA vía GitHub releases (Carchofo/BGT)
- **ApkInstaller**: descarga+instala APK con DownloadManager/FileProvider
- **BugReporter**: POST JSON a http://192.168.0.25:5678/webhook/bgt-bug, fallback mailto

## Bugs corregidos en esta sesión (PC, 2026-07-14)
1. `build.gradle.kts` raíz + app: plugin `kotlin.android` incompatible con AGP 9 (Kotlin integrado) — eliminado; `kotlinOptions` → `kotlin { compilerOptions }`
2. HomeScreen.kt:117 — `GameFeature.SCORE` → `SCORING` (no compilaba)
3. CMSoloModeScreen + TiletumSoloScreen — tabs Scoring/Rules mapeados a índices 1/2 en vez de 2/3 (Puntuación inalcanzable)
4. HomeScreen.kt:328 — añadido "Friday" al dropdown de BugReporter
