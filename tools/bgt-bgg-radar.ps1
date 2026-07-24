# bgt-bgg-radar.ps1
# Consulta BGG para detectar juegos candidatos a BGT:
# - Rating >= 7.2
# - Juegos que YA están en BGT (excluidos)
# - Prioridad ALTA: min_players >= 2 pero tienen mecánica de automa fan-made
# - Prioridad MEDIA: min_players = 1 (solitario oficial)
#
# Estrategia:
#   1. Scrape BGG mechanic/201 (Solo/Solitaire) pages -> IDs de juegos oficialmente solos
#   2. Scrape BGG mechanic/199 (Automa) -> IDs con automa fan-made (el oro para BGT)
#   3. Batch-query XML API para ratings y detalles
#   4. Guardar JSON en vault/candidates/

$OutFile   = "C:\Users\perka\Desktop\bgt\bgt-games-vault\candidates\bgg-solo-radar.json"
$AlreadyIn = @('Spooktacular','Criaturas Maravillosas','Tiletum','Piratas de Maracaibo','Castle Combo','Coimbra','Cascadia','Viernes')
$MinRating = 7.2

$headers = @{ 'User-Agent' = 'BGT-Radar/1.0 (contact: rafel@spicyoffers.com)' }

function Get-GameIdsFromMechanicPage {
    param([int]$MechanicId, [string]$Label, [int]$Pages = 3)
    $ids = @{}
    for ($p = 1; $p -le $Pages; $p++) {
        $url = "https://boardgamegeek.com/boardgamemechanic/$MechanicId/x/linkeditems/boardgamemechanic?sort=rank&pageid=$p"
        try {
            $r = Invoke-WebRequest -Uri $url -Headers $headers -TimeoutSec 20 -UseBasicParsing
            $found = [regex]::Matches($r.Content, 'href="/boardgame/(\d+)/[^"]*"')
            foreach ($m in $found) {
                $id = $m.Groups[1].Value
                if ($id -and -not $ids.ContainsKey($id)) { $ids[$id] = $true }
            }
            Write-Host "  [$Label] Página $p -> $($ids.Count) IDs acumulados"
            Start-Sleep -Milliseconds 800
        } catch { Write-Host "  [$Label] Error página $p: $_" }
    }
    return $ids.Keys
}

function Get-GameDetails {
    param([string[]]$Ids)
    $results = @()
    # BGG API acepta hasta 20 IDs por llamada
    $batches = [System.Collections.Generic.List[string[]]]::new()
    for ($i = 0; $i -lt $Ids.Count; $i += 20) {
        $batches.Add($Ids[$i..([Math]::Min($i+19, $Ids.Count-1))])
    }
    foreach ($batch in $batches) {
        $url = "https://boardgamegeek.com/xmlapi2/thing?id=$($batch -join ',')&stats=1"
        try {
            $r = Invoke-WebRequest -Uri $url -Headers $headers -TimeoutSec 30 -UseBasicParsing
            [xml]$xml = $r.Content
            foreach ($item in $xml.items.item) {
                $name       = ($item.name | Where-Object { $_.type -eq 'primary' }).value
                $minP       = [int]$item.minplayers.value
                $maxP       = [int]$item.maxplayers.value
                $avgRating  = [double]$item.statistics.ratings.average.value
                $bggRank    = ($item.statistics.ratings.ranks.rank | Where-Object { $_.name -eq 'boardgame' }).value
                $mechanics  = ($item.link | Where-Object { $_.type -eq 'boardgamemechanic' }).value
                $hasSolo    = $mechanics -contains 'Solo / Solitaire Game'
                $hasAutoma  = $mechanics -contains 'Automa'
                if ($avgRating -ge $MinRating -and $name) {
                    $results += [PSCustomObject]@{
                        id         = $item.id
                        name       = $name
                        rating     = [Math]::Round($avgRating, 1)
                        bgg_rank   = if ($bggRank -and $bggRank -ne 'Not Ranked') { [int]$bggRank } else { 9999 }
                        min_p      = $minP
                        max_p      = $maxP
                        has_solo   = $hasSolo
                        has_automa = $hasAutoma
                        priority   = if ($hasAutoma -and $minP -ge 2) { 'ALTA — automa fan-made, sin modo solo oficial' }
                                     elseif ($minP -eq 1 -and $hasSolo) { 'MEDIA — solitario oficial' }
                                     elseif ($minP -ge 2) { 'BAJA — no hay automa conocido' }
                                     else { 'MEDIA' }
                        already_in_bgt = ($AlreadyIn -contains $name)
                        bgg_url    = "https://boardgamegeek.com/boardgame/$($item.id)"
                    }
                }
            }
            Start-Sleep -Milliseconds 500
        } catch { Write-Host "  Error batch: $_" }
    }
    return $results
}

Write-Host "`n=== BGT BGG Solo Radar ==="

Write-Host "`n[1/3] Scrapeando mecánica 201 (Solo/Solitaire)..."
$soloIds = Get-GameIdsFromMechanicPage -MechanicId 201 -Label "Solo" -Pages 4

Write-Host "`n[2/3] Scrapeando mecánica 199 (Automa)..."
$automaIds = Get-GameIdsFromMechanicPage -MechanicId 199 -Label "Automa" -Pages 3

# Unir IDs únicos
$allIds = (@($soloIds) + @($automaIds)) | Sort-Object -Unique
Write-Host "`nTotal IDs únicos a consultar: $($allIds.Count)"

Write-Host "`n[3/3] Consultando detalles via BGG XML API..."
$games = Get-GameDetails -Ids $allIds

# Filtrar los que ya están en BGT y ordenar por prioridad + rating
$candidates = $games | Where-Object { -not $_.already_in_bgt } |
              Sort-Object { switch ($_.priority.Substring(0,4)) { 'ALTA' {0} 'MEDI' {1} default {2} } }, { -$_.rating }

$inBGT = $games | Where-Object { $_.already_in_bgt }

$output = [PSCustomObject]@{
    generated    = (Get-Date -Format "yyyy-MM-dd HH:mm")
    min_rating   = $MinRating
    total_found  = $games.Count
    candidates   = $candidates
    already_in_bgt = $inBGT
}

$output | ConvertTo-Json -Depth 6 | Set-Content $OutFile -Encoding UTF8
Write-Host "`n✅ Guardado en: $OutFile"
Write-Host "   Candidatos: $($candidates.Count) | Ya en BGT: $($inBGT.Count)"

# Preview top 10
Write-Host "`n── TOP 10 candidatos ──"
$candidates | Select-Object -First 10 | ForEach-Object {
    Write-Host ("  [{0}] {1} — ★{2} — {3}" -f $_.priority.Substring(0,4), $_.name, $_.rating, "1-$($_.max_p)p")
}
