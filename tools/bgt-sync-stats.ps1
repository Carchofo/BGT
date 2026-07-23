# bgt-sync-stats.ps1
# Lee aportes de shared/bgt-community/, acumula bot_ratings y comentarios,
# actualiza site/assets/game-stats.json y game-comments.json, hace git commit+push.
# Tarea programada: cada hora.

$SharedDir  = "C:\Users\perka\self-hosted-ai-starter-kit\shared\bgt-community"
$RepoDir    = "C:\Users\perka\Desktop\bgt"
$StatsFile  = "$RepoDir\site\assets\game-stats.json"
$CommentsFile = "$RepoDir\site\assets\game-comments.json"
$ProcessedLog = "$SharedDir\stats-processed.json"

# ── Cargar estado actual ──
$stats   = Get-Content $StatsFile   | ConvertFrom-Json
$comments = Get-Content $CommentsFile | ConvertFrom-Json
$processed = if (Test-Path $ProcessedLog) { Get-Content $ProcessedLog | ConvertFrom-Json } else { @{} }

$changed = $false

# ── Procesar archivos .md nuevos ──
Get-ChildItem $SharedDir -Filter "*.md" | Where-Object {
    -not $processed.PSObject.Properties[$_.Name]
} | ForEach-Object {
    $file = $_
    $content = Get-Content $file.FullName -Raw -ErrorAction SilentlyContinue
    if (-not $content) { return }

    # Detectar tipo por el contenido del archivo
    if ($content -match "type.*bot_rating|VOTO BOT") {
        # Extraer juego y estrellas
        $game = if ($content -match "juego[:\s]+([^\n]+)") { $matches[1].Trim() } else { $null }
        $stars = if ($content -match "estrellas[:\s]+(\d)") { [int]$matches[1] } else { 0 }

        if ($game -and $stars -ge 2 -and $stars -le 5) {
            $gameProp = $stats.PSObject.Properties[$game]
            if ($gameProp) {
                $entry = $gameProp.Value
                if ($entry -is [int] -or $entry -is [long]) {
                    # migrar formato plano a objeto
                    $entry = [PSCustomObject]@{ score = $entry; bot_sum = 0; bot_count = 0 }
                }
                $entry.bot_sum   = ($entry.bot_sum   -as [int]) + $stars
                $entry.bot_count = ($entry.bot_count -as [int]) + 1
                $stats.$game = $entry
                $changed = $true
                Write-Host "  bot_rating: $game $stars★ (avg $([math]::Round($entry.bot_sum / $entry.bot_count, 1)))"
            }
        }
    }
    elseif ($content -match "type.*comment|COMENTARIO") {
        $game   = if ($content -match "juego[:\s]+([^\n]+)")  { $matches[1].Trim() } else { $null }
        $author = if ($content -match "autor[:\s]+([^\n]+)")  { $matches[1].Trim() } else { "Anónimo" }
        $text   = if ($content -match "texto[:\s]+([^\n]+)")  { $matches[1].Trim() } else { $null }
        if (-not $text -and $content -match "COMENTARIO\r?\n.*\r?\n.*\r?\n(.+)") { $text = $matches[1].Trim() }

        if ($game -and $text) {
            $gameProp = $comments.PSObject.Properties[$game]
            if ($gameProp) {
                $list = [System.Collections.Generic.List[object]]$gameProp.Value
                $list.Add([PSCustomObject]@{
                    id     = [System.Guid]::NewGuid().ToString("N").Substring(0,8)
                    author = $author
                    text   = $text
                    date   = (Get-Date -Format "yyyy-MM-ddTHH:mm:ssZ")
                })
                $comments.$game = $list.ToArray()
                $changed = $true
                Write-Host "  comentario: $game por $author"
            }
        }
    }

    # Marcar como procesado
    $processed | Add-Member -NotePropertyName $file.Name -NotePropertyValue (Get-Date -Format "o") -Force
}

if (-not $changed) {
    Write-Host "Sin cambios nuevos."
    exit 0
}

# ── Guardar archivos actualizados ──
$stats    | ConvertTo-Json -Depth 5 | Set-Content $StatsFile    -Encoding UTF8
$comments | ConvertTo-Json -Depth 5 | Set-Content $CommentsFile -Encoding UTF8
$processed | ConvertTo-Json -Depth 3 | Set-Content $ProcessedLog -Encoding UTF8

# ── Git commit + push ──
Push-Location $RepoDir
git add "site/assets/game-stats.json" "site/assets/game-comments.json"
$stamp = Get-Date -Format "yyyy-MM-dd HH:mm"
git commit -m "auto: sync stats+comments desde aportes comunidad ($stamp)"
git push
Pop-Location

Write-Host "Stats y comentarios sincronizados y pusheados."
