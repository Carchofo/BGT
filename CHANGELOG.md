# Changelog — BGT Board Game Tools

Format: `[vX.Y] YYYY-MM-DD — descripción breve`
Cada versión: **Added** (nuevo), **Fixed** (bug), **Changed** (mejora).

---

## [v1.1] 2026-06-22 — Pipeline bugs, About screen, R8

### Added
- **Bug reporter** in-app: icono 🐛 en HomeScreen → dialog → POST a n8n en PC → GitHub Issue automático con sugerencia de fix de Qwen
- **About screen**: Ko-fi donation card, créditos autores fan-made, links GitHub/BGG
- **README.md**: instrucciones install, build, release, créditos
- **n8n bug pipeline** (`bgt-bug-pipeline.json`): bug report → GitHub Issue → Qwen fix → comentario automático

### Fixed
- **versionCode hardcoded a 1**: actualizaciones silenciosas fallaban al instalar (Android rechaza APK con mismo versionCode). Ahora viene de `github.run_number` — monotónico, garantiza instalación correcta
- **HomeViewModel favoritos desconectado**: favoritos se perdían al reiniciar la app. Wired a DataStore correctamente
- **@OptIn faltante en BugReportDialog**: fix de compilación potencial

### Changed
- R8 + shrinkResources habilitados en release — APK más pequeño, sideload más rápido
- proguard-rules.pro: reglas para Kotlin/coroutines/DataStore/Compose/ViewModel

---

## [v1.0] 2026-06-17 — Lanzamiento inicial

### Added
- 7 juegos: Maracaibo (Jordán), Tiletum (Titus), Criaturas Maravillosas (Tingent), Castle Combo (Anton), Coimbra (Bot interferencia), Cascadia (calculadora), Spooktacular (Killtron)
- HomeScreen con búsqueda, favoritos, filtros por feature (Solo/Scoring/Rules), vista lista/grid
- Auto-update: comprueba GitHub Releases al arrancar, descarga e instala APK firmado
- DisclaimerScreen legal (se muestra una vez)
- Idiomas: es, en, fr, de, it
- GitHub Actions CI: tag `vX.Y` → build → APK firmado → GitHub Release

---

<!-- TEMPLATE para próximas versiones:

## [vX.Y] YYYY-MM-DD — título

### Added
- 

### Fixed
- Bug #NNN: descripción. Reportado por: [user/internal]. Archivo: `ScreenX.kt:line`

### Changed
- 

-->
