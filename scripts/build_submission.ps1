# Build lab submission: screenshots, docx report, pdf export.
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
Push-Location $Root

Write-Host "=== Step 1: Capture screenshots ==="
& "$Root\scripts\capture_screenshots.ps1"

Write-Host "=== Step 2: Generate Word report ==="
python "$Root\scripts\generate_report.py"

Write-Host "=== Step 3: Export PDF ==="
python "$Root\scripts\export_pdf.py"

Write-Host "=== Done ==="
Pop-Location
