# BGT Backup — copia de seguridad completa (workflows n8n + datos de shared/)
# Para poder migrar a un servidor propio si crecemos, sin depender de tener usuarios activos ya.
# Uso: powershell -File tools\bgt-backup.ps1  (o via tarea programada diaria)

$ErrorActionPreference = "Stop"
$BackupRoot = "C:\Users\perka\bgt-backups"
$SharedDir  = "C:\Users\perka\self-hosted-ai-starter-kit\shared"
$Stamp      = Get-Date -Format "yyyy-MM-dd_HHmm"
$Dest       = "$BackupRoot\$Stamp"
$KeepLast   = 14  # dias de backups a conservar

New-Item -ItemType Directory -Force $Dest | Out-Null

# 1) Workflows n8n completos (incluye BGT Telegram Bot, Community Submit, Bug Intake,
#    Bug Fixer router, Forum Agent, Announce -- todo lo construido esta sesion)
docker exec n8n n8n export:workflow --all --output=/home/node/backup-workflows.json 2>&1 | Out-Null
docker cp n8n:/home/node/backup-workflows.json "$Dest\n8n-workflows.json"

# 2) Credenciales (sin desencriptar -- solo para poder reimportar la estructura,
#    los valores siguen cifrados con la clave de n8n, no se exponen en claro)
docker exec n8n n8n export:credentials --all --output=/home/node/backup-creds.json 2>&1 | Out-Null
docker cp n8n:/home/node/backup-creds.json "$Dest\n8n-credentials-encrypted.json"

# 3) Todo shared/ (bugs, community, rangos, metrics-log, forum-backup, tg-sessions)
Copy-Item -Path $SharedDir -Destination "$Dest\shared" -Recurse -Force

# 4) Comprimir y borrar la carpeta sin comprimir
Compress-Archive -Path "$Dest\*" -DestinationPath "$Dest.zip" -Force
Remove-Item -Recurse -Force $Dest

# 5) Rotacion: conservar solo los ultimos N dias
Get-ChildItem "$BackupRoot\*.zip" | Sort-Object LastWriteTime -Descending | Select-Object -Skip $KeepLast | Remove-Item -Force

Write-Host "Backup completo: $Dest.zip"
Get-ChildItem "$BackupRoot\*.zip" | Measure-Object -Property Length -Sum | ForEach-Object {
    Write-Host "$($_.Count) backups, $([math]::Round($_.Sum/1MB,1)) MB en total"
}
