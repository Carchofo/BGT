# BGT Release — numeración semántica automática y publicación vía GitHub Actions
# Uso: powershell -File tools\release-bgt.ps1 [-Bump patch|minor|major] [-Notes "cambios"]
# El tag v* dispara .github/workflows/release.yml (build firmado + GitHub Release,
# que UpdateChecker distribuye OTA a las apps instaladas).

param(
    [ValidateSet("patch", "minor", "major")] [string]$Bump = "patch",
    [string]$Notes = ""
)
$ErrorActionPreference = "Stop"
$RepoDir = "C:\Users\perka\Desktop\bgt"
Push-Location $RepoDir

try {
    git checkout main; git pull --ff-only

    # Última versión desde tags (v1.2 o v1.2.3)
    $tags = git tag --list "v*" | ForEach-Object {
        $v = $_.TrimStart("v").Split(".")
        [pscustomobject]@{ Tag = $_; Maj = [int]$v[0]; Min = [int]$v[1]; Pat = if ($v.Count -gt 2) { [int]$v[2] } else { 0 } }
    } | Sort-Object Maj, Min, Pat
    $last = $tags | Select-Object -Last 1
    if (-not $last) { $last = [pscustomobject]@{ Maj = 1; Min = 0; Pat = 0 } }

    switch ($Bump) {
        "major" { $new = "v$($last.Maj + 1).0.0" }
        "minor" { $new = "v$($last.Maj).$($last.Min + 1).0" }
        "patch" { $new = "v$($last.Maj).$($last.Min).$($last.Pat + 1)" }
    }

    # Changelog: notas + commits desde el último tag
    $range = if ($last.Tag) { "$($last.Tag)..HEAD" } else { "HEAD" }
    $commits = git log $range --oneline --no-merges
    $date = Get-Date -Format "yyyy-MM-dd"
    $entry = "## $new ($date)`n`n$Notes`n`n### Commits`n" + (($commits | ForEach-Object { "- $_" }) -join "`n") + "`n`n"
    $changelog = "$RepoDir\CHANGELOG.md"
    $old = if (Test-Path $changelog) { Get-Content $changelog -Raw } else { "# Changelog BGT`n`n" }
    ($old -replace "(# Changelog BGT\s*\n)", "`$1`n$entry") | Set-Content $changelog -Encoding utf8

    git add CHANGELOG.md
    git commit -m "release: $new"
    git tag $new
    git push origin main
    git push origin $new
    Write-Host "Release $new lanzada. GitHub Actions construye y publica el APK firmado."
    Write-Host "Seguimiento: https://github.com/Carchofo/BGT/actions"

    try {
        $announceBody = @{
            message = "🎃 *BGT $new* ya está en camino. $Notes`n`nGitHub Actions está construyendo el APK firmado — llegará por autoupdate en unos minutos. Gracias a quien reportó o pidió algo de esta versión 🙌"
            source  = 'release-bgt'
        } | ConvertTo-Json
        Invoke-RestMethod -Uri "http://192.168.0.25:5678/webhook/bgt-announce" -Method Post -Body $announceBody -ContentType "application/json" -TimeoutSec 10 | Out-Null
    } catch { Write-Host "Aviso: no se pudo anunciar la release al grupo ($_)" }
}
finally { Pop-Location }
