# bgt-organic-likes.ps1
# Simula actividad orgánica: añade 1-2 likes a juegos aleatorios (ponderados por popularidad actual).
# Ejecutar 2-3 veces por semana via tarea programada. Resultado: contadores creíbles y crecientes.

$StatsFile = "C:\Users\perka\Desktop\bgt\site\assets\game-stats.json"
$RepoDir   = "C:\Users\perka\Desktop\bgt"

$stats = Get-Content $StatsFile | ConvertFrom-Json

$games = @(
    @{name="Spooktacular";           weight=3},
    @{name="Criaturas Maravillosas"; weight=5},
    @{name="Tiletum";                weight=2},
    @{name="Piratas de Maracaibo";   weight=3},
    @{name="Castle Combo";           weight=5},
    @{name="Coimbra";                weight=2},
    @{name="Cascadia";               weight=4},
    @{name="Viernes";                weight=3}
)

# Selección ponderada: construir pool y sacar 1 o 2 únicos
$pool = @()
foreach ($g in $games) { for ($i=0; $i -lt $g.weight; $i++) { $pool += $g.name } }
$pool = $pool | Sort-Object {Get-Random}

# Cuántos juegos reciben like esta vez: 1 o 2
$count = if ((Get-Random -Minimum 1 -Maximum 10) -le 6) { 1 } else { 2 }
$chosen = $pool | Select-Object -Unique | Select-Object -First $count

$changed = $false
foreach ($game in $chosen) {
    $entry = $stats.$game
    if ($null -eq $entry) { continue }

    if ($entry -is [int] -or $entry -is [long]) {
        $entry = [PSCustomObject]@{ score=0; bot_sum=0; bot_count=0; likes=$entry }
    }
    if (-not $entry.PSObject.Properties['likes']) {
        $entry | Add-Member -NotePropertyName 'likes' -NotePropertyValue 0
    }

    $entry.likes = ($entry.likes -as [int]) + 1
    $stats.$game = $entry
    $changed = $true
    Write-Host "  +1 like → $game (total $($entry.likes))"
}

if (-not $changed) { Write-Host "Nada que hacer."; exit 0 }

$stats | ConvertTo-Json -Depth 5 | Set-Content $StatsFile -Encoding UTF8

Push-Location $RepoDir
git add "site/assets/game-stats.json"
$stamp = Get-Date -Format "yyyy-MM-dd"
git commit -m "auto: likes organicos $stamp"
git push
Pop-Location

Write-Host "Likes actualizados y pusheados."
