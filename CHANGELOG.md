# Changelog â€” BGT Board Game Tools

Format: `[vX.Y] YYYY-MM-DD â€” descripciÃ³n breve`
Cada versiÃ³n: **Added** (nuevo), **Fixed** (bug), **Changed** (mejora).

---

## [v0.9.2] 2026-07-27 — Splendor automa (beta)

### Added
- **Splendor** — automa fan-made en pruebas. Mazo de 10 cartas con 3 fases de dificultad creciente, modos Normal y Difícil. Créditos al autor original del automa: [dukefanblue2005](https://boardgamegeek.com/geeklist/273639) (BGG)
- Mensaje de bienvenida para usuarios nuevos
- Sección News en la web con historial de versiones

### Changed
- Icono corregido: fondo navy en lugar de blanco
- Firma de la app renovada (reinstalación única necesaria desde versiones anteriores)

---

## [v1.1] 2026-06-22 â€” Pipeline bugs, About screen, R8

### Added
- **Bug reporter** in-app: icono ðŸ› en HomeScreen â†’ dialog â†’ POST a n8n en PC â†’ GitHub Issue automÃ¡tico con sugerencia de fix de Qwen
- **About screen**: Ko-fi donation card, crÃ©ditos autores fan-made, links GitHub/BGG
- **README.md**: instrucciones install, build, release, crÃ©ditos
- **n8n bug pipeline** (`bgt-bug-pipeline.json`): bug report â†’ GitHub Issue â†’ Qwen fix â†’ comentario automÃ¡tico

### Fixed
- **versionCode hardcoded a 1**: actualizaciones silenciosas fallaban al instalar (Android rechaza APK con mismo versionCode). Ahora viene de `github.run_number` â€” monotÃ³nico, garantiza instalaciÃ³n correcta
- **HomeViewModel favoritos desconectado**: favoritos se perdÃ­an al reiniciar la app. Wired a DataStore correctamente
- **@OptIn faltante en BugReportDialog**: fix de compilaciÃ³n potencial

### Changed
- R8 + shrinkResources habilitados en release â€” APK mÃ¡s pequeÃ±o, sideload mÃ¡s rÃ¡pido
- proguard-rules.pro: reglas para Kotlin/coroutines/DataStore/Compose/ViewModel

---

## [v1.0] 2026-06-17 â€” Lanzamiento inicial

### Added
- 7 juegos: Maracaibo (JordÃ¡n), Tiletum (Titus), Criaturas Maravillosas (Tingent), Castle Combo (Anton), Coimbra (Bot interferencia), Cascadia (calculadora), Spooktacular (Killtron)
- HomeScreen con bÃºsqueda, favoritos, filtros por feature (Solo/Scoring/Rules), vista lista/grid
- Auto-update: comprueba GitHub Releases al arrancar, descarga e instala APK firmado
- DisclaimerScreen legal (se muestra una vez)
- Idiomas: es, en, fr, de, it
- GitHub Actions CI: tag `vX.Y` â†’ build â†’ APK firmado â†’ GitHub Release

---

<!-- TEMPLATE para prÃ³ximas versiones:

## [vX.Y] YYYY-MM-DD â€” tÃ­tulo

### Added
- 

### Fixed
- Bug #NNN: descripciÃ³n. Reportado por: [user/internal]. Archivo: `ScreenX.kt:line`

### Changed
- 

-->

