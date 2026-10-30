# FindIt: PowerShell One-Click Launcher
$env:JAVA_HOME = "C:\Users\DELL\.p2\pool\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.11.v20260515-1531\jre"
$env:PATH = "$env:JAVA_HOME\bin;C:\Users\DELL\apache-maven-3.9.6\bin;$env:PATH"

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  Starting FindIt: Smart Lost and Found Matcher" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Cyan

# Start MySQL if not already running on port 3307
$port3307 = Get-NetTCPConnection -LocalPort 3307 -ErrorAction SilentlyContinue
if (-not $port3307) {
    Write-Host "[1/2] Starting local MySQL Server 8.0 on port 3307..." -ForegroundColor Yellow
    Start-Process -FilePath "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld.exe" -ArgumentList "--datadir=C:\Users\DELL\findit_mysqldata", "--port=3307", "--bind-address=127.0.0.1" -WindowStyle Hidden
    Start-Sleep -Seconds 3
} else {
    Write-Host "[1/2] MySQL Server 8.0 is already running on port 3307." -ForegroundColor Green
}

Write-Host "[2/2] Launching Spring Boot Full-Stack Application..." -ForegroundColor Yellow
Write-Host "Web Application URL: http://localhost:8080/" -ForegroundColor Cyan
Write-Host "Demo Credentials:" -ForegroundColor Gray
Write-Host "  Student: suyash@campus.edu  / Student@123" -ForegroundColor Gray
Write-Host "  Admin:   admin@findit.campus.edu / Admin@123" -ForegroundColor Gray
Write-Host "============================================================" -ForegroundColor Cyan

& "$env:JAVA_HOME\bin\java.exe" -jar "backend\target\findit-backend-1.0.0.jar"

