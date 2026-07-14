# BGT Bug Fixer — procesa bugs de la cola y propone fixes en ramas bot/*
# Diseño human-in-the-loop: el agente NUNCA toca main; propone rama + gate de tests.
# Uso: powershell -File tools\bgt-bug-fixer.ps1   (o via tarea programada)

$ErrorActionPreference = "Stop"
$RepoDir  = "C:\Users\perka\Desktop\bgt"
$BugsDir  = "C:\Users\perka\self-hosted-ai-starter-kit\shared\bgt-bugs"
$JavaHome = "C:\Program Files\Android\Android Studio\jbr"
$LogDir   = "$RepoDir\tools\fixer-logs"

New-Item -ItemType Directory -Force $LogDir | Out-Null
$env:JAVA_HOME = $JavaHome

$bugs = Get-ChildItem "$BugsDir\*.md" -ErrorAction SilentlyContinue | Where-Object {
    (Get-Content $_.FullName -Raw) -match "status: new"
}
if (-not $bugs) { Write-Host "No hay bugs nuevos."; exit 0 }

foreach ($bug in $bugs) {
    $content = Get-Content $bug.FullName -Raw
    $id = [IO.Path]::GetFileNameWithoutExtension($bug.Name)
    $branch = "bot/$id"
    $log = "$LogDir\$id.log"
    Write-Host "=== Procesando $id ==="

    # Marcar en curso para que otra ejecucion no lo duplique
    (Get-Content $bug.FullName -Raw) -replace "status: new", "status: fixing" | Set-Content $bug.FullName -Encoding utf8

    Push-Location $RepoDir
    try {
        git checkout main 2>&1 | Out-Null
        git checkout -B $branch 2>&1 | Out-Null

        # Claude Code en modo headless propone el fix (con test que reproduce el bug)
        $prompt = @"
Eres el agente fixer de BGT (app Android Kotlin/Compose). Corrige este bug reportado por un usuario:

$content

Reglas:
1. Localiza la causa raiz en el codigo y corrigela con el cambio minimo.
2. Si es viable, anade un test unitario en app/src/test que reproduzca el bug (falla antes del fix, pasa despues).
3. No toques build.gradle.kts ni la firma. No hagas commit; solo modifica archivos.
4. Si el reporte es demasiado vago para actuar, crea el archivo TRIAGE.md en la raiz explicando que informacion falta, y no toques nada mas.
"@
        claude -p $prompt --permission-mode acceptEdits 2>&1 | Tee-Object $log | Out-Null

        if (Test-Path "$RepoDir\TRIAGE.md") {
            (Get-Content $bug.FullName -Raw) -replace "status: fixing", "status: needs-info" | Set-Content $bug.FullName -Encoding utf8
            Add-Content $bug.FullName "`n## Triage`n$(Get-Content "$RepoDir\TRIAGE.md" -Raw)"
            Remove-Item "$RepoDir\TRIAGE.md" -Force
            git checkout main 2>&1 | Out-Null; git branch -D $branch 2>&1 | Out-Null
            continue
        }

        # GATE: compilacion + tests. Sin verde no hay propuesta.
        & .\gradlew testDebugUnitTest assembleDebug --no-daemon 2>&1 | Add-Content $log
        if ($LASTEXITCODE -ne 0) {
            (Get-Content $bug.FullName -Raw) -replace "status: fixing", "status: gate-failed" | Set-Content $bug.FullName -Encoding utf8
            git checkout main 2>&1 | Out-Null; git branch -D $branch 2>&1 | Out-Null
            Write-Host "GATE FALLIDO para $id (ver $log)"
            continue
        }

        git add -A
        git commit -m "bot: fix $id (auto-propuesto, gate de tests verde)" 2>&1 | Out-Null
        git push -u origin $branch 2>&1 | Add-Content $log
        (Get-Content $bug.FullName -Raw) -replace "status: fixing", "status: proposed`nbranch: $branch`npr: https://github.com/Carchofo/BGT/compare/main...$branch" | Set-Content $bug.FullName -Encoding utf8
        git checkout main 2>&1 | Out-Null
        Write-Host "PROPUESTO: $branch — revisar y mergear en GitHub"
    }
    catch {
        (Get-Content $bug.FullName -Raw) -replace "status: fixing", "status: error" | Set-Content $bug.FullName -Encoding utf8
        Write-Host "ERROR en ${id}: $_"
    }
    finally { Pop-Location }
}
