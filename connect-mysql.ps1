# Script để kết nối MySQL nhanh (PowerShell)

Write-Host "=== Kết nối MySQL Database ===" -ForegroundColor Cyan
Write-Host ""

# Kiểm tra container có đang chạy không
$containerRunning = docker ps --filter "name=charity-mysql" --format "{{.Names}}"

if (-not $containerRunning) {
    Write-Host "❌ Container MySQL chưa chạy!" -ForegroundColor Red
    Write-Host "Đang start container..." -ForegroundColor Yellow
    docker-compose up -d mysql
    Start-Sleep -Seconds 5
}

# Đọc password từ .env file (nếu có)
$envFile = ".\.env"
$password = "root"  # Default

if (Test-Path $envFile) {
    $envContent = Get-Content $envFile
    foreach ($line in $envContent) {
        if ($line -match "DBMS_PASSWORD=(.+)") {
            $password = $matches[1].Trim()
            break
        }
    }
}

Write-Host "📊 Thông tin kết nối:" -ForegroundColor Green
Write-Host "   Host: localhost" -ForegroundColor Gray
Write-Host "   Port: 3307" -ForegroundColor Gray
Write-Host "   Database: charity_platform" -ForegroundColor Gray
Write-Host "   Username: root" -ForegroundColor Gray
Write-Host ""

Write-Host "🔌 Đang kết nối..." -ForegroundColor Yellow
Write-Host ""

# Kết nối vào MySQL
docker exec -it charity-mysql mysql -u root -p$password charity_platform

