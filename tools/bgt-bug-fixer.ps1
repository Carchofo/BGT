# BGT Bug Fixer — router hibrido Ollama/Claude, ramas bot/*, gate de tests obligatorio.
# Diseno acordado (Opus + Sonnet, sesion 2026-07-22): Ollama qwen2.5-coder:14b intenta primero
# los bugs "trivial" (triage ya lo marca en el frontmatter via BGT Bug Intake); "complex" y los
# que fallan el gate/heuristicas de Ollama escalan a Claude Code, con tope diario de presupuesto.
# El agente NUNCA toca main: solo propone rama + gate de tests. Merge y release siguen siendo humanos.
# Uso: powershell -File tools\bgt-bug-fixer.ps1   (o via tarea programada)

$ErrorActionPreference = "Stop"
$RepoDir       = "C:\Users\perka\Desktop\bgt"
$BugsDir       = "C:\Users\perka\self-hosted-ai-starter-kit\shared\bgt-bugs"
$JavaHome      = "C:\Program Files\Android\Android Studio\jbr"
$LogDir        = "$RepoDir\tools\fixer-logs"
$OllamaUrl     = "http://192.168.0.25:11434/api/generate"
$AnnounceUrl   = "http://192.168.0.25:5678/webhook/bgt-announce"
$MaxClaudePerDay = 5
$MaxDiffFiles  = 3
$MaxDiffLines  = 60
$ForbiddenPaths = @("build.gradle.kts", ".github/", "keystore.properties")

New-Item -ItemType Directory -Force $LogDir | Out-Null
$env:JAVA_HOME = $JavaHome

function Send-GroupAnnounce($message) {
    try {
        $body = @{ message = $message; source = 'bgt-bug-fixer' } | ConvertTo-Json
        Invoke-RestMethod -Uri $AnnounceUrl -Method Post -Body $body -ContentType "application/json" -TimeoutSec 10 | Out-Null
    } catch { Write-Host "Aviso: no se pudo anunciar al grupo ($_)" }
}

function Get-ClaudeUsageToday {
    $f = "$LogDir\claude-usage-$(Get-Date -Format yyyy-MM-dd).count"
    if (Test-Path $f) { return [int](Get-Content $f -Raw) } else { return 0 }
}
function Add-ClaudeUsage {
    $f = "$LogDir\claude-usage-$(Get-Date -Format yyyy-MM-dd).count"
    $n = (Get-ClaudeUsageToday) + 1
    Set-Content $f $n
}

function Invoke-Ollama($model, $prompt, $timeoutSec = 180) {
    $body = @{ model = $model; prompt = $prompt; stream = $false } | ConvertTo-Json -Depth 5
    $resp = Invoke-RestMethod -Uri $OllamaUrl -Method Post -Body $body -ContentType "application/json" -TimeoutSec $timeoutSec
    return $resp.response
}

function Test-DiffHeuristics($archivoProbable) {
    # Heurísticas gratis antes de gastar minutos de Gradle (acordado Opus+Sonnet).
    $stat = git diff --stat main 2>&1
    $files = git diff --name-only main 2>&1
    if (-not $files) { return @{ok=$false; reason="diff vacio"} }
    $fileCount = ($files -split "`n" | Where-Object { $_ }).Count
    if ($fileCount -gt $MaxDiffFiles) { return @{ok=$false; reason="toca $fileCount archivos (max $MaxDiffFiles)"} }
    foreach ($forbidden in $ForbiddenPaths) {
        if ($files -match [regex]::Escape($forbidden)) { return @{ok=$false; reason="toca ruta prohibida: $forbidden"} }
    }
    $lineCount = (git diff main --numstat 2>&1 | ForEach-Object { ($_ -split "`t")[0..1] } | Where-Object { $_ -match '^\d+$' } | Measure-Object -Sum).Sum
    if ($lineCount -gt $MaxDiffLines) { return @{ok=$false; reason="diff de $lineCount lineas (max $MaxDiffLines)"} }
    if ($archivoProbable -and $files -notmatch [regex]::Escape($archivoProbable)) {
        return @{ok=$false; reason="no toca el archivo esperado ($archivoProbable)"}
    }
    return @{ok=$true; reason="ok"}
}

function Invoke-GradleGate($log) {
    & "$RepoDir\gradlew.bat" testDebugUnitTest assembleDebug --no-daemon 2>&1 | Add-Content $log
    return $LASTEXITCODE -eq 0
}

function Try-OllamaFix($bugContent, $archivoProbable, $log) {
    # Contexto masticado: se le da el archivo probable ya leido (RAG minimo, sin Qdrant todavia).
    $srcFile = if ($archivoProbable) {
        Get-ChildItem -Path "$RepoDir\app\src\main\java" -Recurse -Filter $archivoProbable -ErrorAction SilentlyContinue | Select-Object -First 1
    } else { $null }
    $fileContent = if ($srcFile) { Get-Content $srcFile.FullName -Raw } else { "(archivo no localizado automaticamente, describe donde crees que esta el problema)" }

    for ($attempt = 1; $attempt -le 3; $attempt++) {
        $prompt = @"
Eres un fixer de bugs para BGT, app Android Kotlin/Compose. Aplica el MINIMO cambio necesario.

BUG REPORTADO:
$bugContent

ARCHIVO PROBABLE ($archivoProbable):
$fileContent

Reglas estrictas:
- Devuelve SOLO el contenido completo y corregido del archivo $archivoProbable, nada mas (sin explicaciones, sin markdown).
- No toques build.gradle.kts, .github/, keystore.properties.
- Si no encuentras una causa clara, devuelve exactamente la palabra: NEEDS_INFO
"@
        if ($attempt -gt 1) {
            $prompt += "`n`nEl intento anterior fallo el gate de compilacion/tests con este error:`n$(Get-Content $log -Tail 40 -Raw)`nCorrigelo."
        }
        $result = Invoke-Ollama "qwen2.5-coder:14b" $prompt 240
        if ($result.Trim() -eq "NEEDS_INFO" -or -not $srcFile) { return @{ok=$false; reason="Ollama no pudo localizar el fix"} }

        Set-Content -Path $srcFile.FullName -Value $result -Encoding utf8 -NoNewline

        $heur = Test-DiffHeuristics $archivoProbable
        if (-not $heur.ok) {
            git checkout -- $srcFile.FullName 2>&1 | Out-Null
            return @{ok=$false; reason="heuristica: $($heur.reason)"}
        }

        if (Invoke-GradleGate $log) {
            # Revisor barato: qwen2.5-coder:7b confirma que el diff corresponde al bug (acordado Opus).
            $diff = git diff main -- $srcFile.FullName 2>&1 | Out-String
            $reviewPrompt = "Bug: $bugContent`n`nDiff propuesto:`n$diff`n`nRESPONDE SOLO 'si' o 'no': ¿este diff corrige razonablemente el bug descrito?"
            $review = (Invoke-Ollama "qwen2.5-coder:7b" $reviewPrompt 60).Trim().ToLower()
            if ($review -notmatch "^s") {
                git checkout -- $srcFile.FullName 2>&1 | Out-Null
                return @{ok=$false; reason="revisor 7b rechazo el diff"}
            }
            return @{ok=$true}
        }
    }
    git checkout -- $srcFile.FullName 2>&1 | Out-Null
    return @{ok=$false; reason="gate fallido tras 3 intentos"}
}

function Invoke-ClaudeFix($bugContent, $log) {
    $prompt = @"
Eres el agente fixer de BGT (app Android Kotlin/Compose). Corrige este bug reportado por un usuario:

$bugContent

Reglas:
1. Localiza la causa raiz en el codigo y corrigela con el cambio minimo.
2. Si es viable, anade un test unitario en app/src/test que reproduzca el bug (falla antes del fix, pasa despues).
3. No toques build.gradle.kts ni la firma. No hagas commit; solo modifica archivos.
4. Si el reporte es demasiado vago para actuar, crea el archivo TRIAGE.md en la raiz explicando que informacion falta, y no toques nada mas.
"@
    claude -p $prompt --permission-mode acceptEdits 2>&1 | Tee-Object $log | Out-Null
    Add-ClaudeUsage
}

$bugs = Get-ChildItem "$BugsDir\*.md" -ErrorAction SilentlyContinue | Where-Object {
    (Get-Content $_.FullName -Raw) -match "status: new"
}
if (-not $bugs) { Write-Host "No hay bugs nuevos."; exit 0 }

foreach ($bug in $bugs) {
    $content  = Get-Content $bug.FullName -Raw
    $id       = [IO.Path]::GetFileNameWithoutExtension($bug.Name)
    $branch   = "bot/$id"
    $log      = "$LogDir\$id.log"
    $difficulty = if ($content -match "difficulty:\s*(\S+)") { $matches[1] } else { "complex" }
    $archivoProbable = if ($content -match "archivo_probable:\s*`"?([^`"\r\n]+)`"?") { $matches[1].Trim() } else { $null }
    Write-Host "=== Procesando $id (difficulty=$difficulty) ==="

    (Get-Content $bug.FullName -Raw) -replace "status: new", "status: fixing" | Set-Content $bug.FullName -Encoding utf8

    Push-Location $RepoDir
    try {
        git checkout main 2>&1 | Out-Null
        git checkout -B $branch 2>&1 | Out-Null

        $usedOllama = $false
        $resolved = $false

        if ($difficulty -eq "trivial" -and $archivoProbable) {
            $r = Try-OllamaFix $content $archivoProbable $log
            $usedOllama = $true
            if ($r.ok) { $resolved = $true }
            else { Write-Host "Ollama no resolvio $id ($($r.reason)) — escalando si hay presupuesto" }
        }

        if (-not $resolved) {
            if ((Get-ClaudeUsageToday) -ge $MaxClaudePerDay) {
                (Get-Content $bug.FullName -Raw) -replace "status: fixing", "status: queued-claude" | Set-Content $bug.FullName -Encoding utf8
                git checkout main 2>&1 | Out-Null; git branch -D $branch 2>&1 | Out-Null
                Write-Host "Presupuesto Claude agotado hoy ($MaxClaudePerDay) — $id queda en queued-claude"
                continue
            }
            git checkout -- . 2>&1 | Out-Null  # descarta cualquier resto del intento de Ollama
            Invoke-ClaudeFix $content $log

            if (Test-Path "$RepoDir\TRIAGE.md") {
                (Get-Content $bug.FullName -Raw) -replace "status: fixing", "status: needs-info" | Set-Content $bug.FullName -Encoding utf8
                Add-Content $bug.FullName "`n## Triage`n$(Get-Content "$RepoDir\TRIAGE.md" -Raw)"
                Remove-Item "$RepoDir\TRIAGE.md" -Force
                git checkout main 2>&1 | Out-Null; git branch -D $branch 2>&1 | Out-Null
                continue
            }

            if (-not (Invoke-GradleGate $log)) {
                (Get-Content $bug.FullName -Raw) -replace "status: fixing", "status: gate-failed" | Set-Content $bug.FullName -Encoding utf8
                git checkout main 2>&1 | Out-Null; git branch -D $branch 2>&1 | Out-Null
                Write-Host "GATE FALLIDO para $id (ver $log)"
                continue
            }
        }

        git add -A
        $fixer = if ($usedOllama -and $resolved) { "Ollama qwen2.5-coder:14b" } else { "Claude Code" }
        git commit -m "bot: fix $id (auto-propuesto por $fixer, gate de tests verde)" 2>&1 | Out-Null
        git push -u origin $branch 2>&1 | Add-Content $log
        (Get-Content $bug.FullName -Raw) -replace "status: fixing", "status: proposed`nbranch: $branch`nfixed_by: $fixer`npr: https://github.com/Carchofo/BGT/compare/main...$branch" | Set-Content $bug.FullName -Encoding utf8
        git checkout main 2>&1 | Out-Null
        Write-Host "PROPUESTO ($fixer): $branch — revisar y mergear en GitHub"
        Send-GroupAnnounce "🔧 *$fixer* propuso un fix para \`$id\`. Rafel lo revisará antes de publicarlo. Gracias a quien reportó este fallo 🙌"
    }
    catch {
        (Get-Content $bug.FullName -Raw) -replace "status: fixing", "status: error" | Set-Content $bug.FullName -Encoding utf8
        Write-Host "ERROR en ${id}: $_"
    }
    finally { Pop-Location }
}
