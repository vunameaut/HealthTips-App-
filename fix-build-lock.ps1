# Script khắc phục vấn đề build lock trong HealthTips-App
# Sử dụng khi gặp lỗi "Unable to delete directory"

Write-Host "=== Script khắc phục Build Lock cho HealthTips-App ===" -ForegroundColor Cyan
Write-Host ""

# Bước 1: Dừng Gradle Daemon
Write-Host "[1/6] Dừng tất cả Gradle Daemon processes..." -ForegroundColor Yellow
& .\gradlew --stop
Start-Sleep -Seconds 2

# Bước 2: Đóng Android Studio nếu đang mở (yêu cầu thủ công)
Write-Host "[2/6] Vui lòng đóng Android Studio và Windows Explorer nếu đang mở thư mục này!" -ForegroundColor Red
Write-Host "Nhấn Enter khi đã đóng..." -ForegroundColor Yellow
Read-Host

# Bước 3: Tìm và kill các process Java đang giữ thư mục build
Write-Host "[3/6] Tìm và dừng các Java processes liên quan..." -ForegroundColor Yellow
Get-Process | Where-Object { $_.ProcessName -like "*java*" -or $_.ProcessName -like "*gradle*" } | ForEach-Object {
    Write-Host "  - Dừng process: $($_.ProcessName) (PID: $($_.Id))" -ForegroundColor Gray
    try {
        Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue
    } catch {
        Write-Host "    Không thể dừng process này" -ForegroundColor DarkGray
    }
}
Start-Sleep -Seconds 2

# Bước 4: Xóa thư mục build thủ công
Write-Host "[4/6] Xóa thư mục build..." -ForegroundColor Yellow
$buildPath = ".\app\build"
if (Test-Path $buildPath) {
    try {
        Remove-Item -Path $buildPath -Recurse -Force -ErrorAction Stop
        Write-Host "  ✓ Đã xóa thành công thư mục build" -ForegroundColor Green
    } catch {
        Write-Host "  × Không thể xóa thư mục build tự động" -ForegroundColor Red
        Write-Host "  Vui lòng xóa thủ công thư mục: $buildPath" -ForegroundColor Yellow
        Write-Host "  Nhấn Enter sau khi đã xóa thủ công..." -ForegroundColor Yellow
        Read-Host
    }
} else {
    Write-Host "  - Thư mục build không tồn tại (đã sạch)" -ForegroundColor Gray
}

# Bước 5: Xóa .gradle cache
Write-Host "[5/6] Xóa Gradle cache..." -ForegroundColor Yellow
$gradlePath = ".\.gradle"
if (Test-Path $gradlePath) {
    try {
        Remove-Item -Path $gradlePath -Recurse -Force -ErrorAction Stop
        Write-Host "  ✓ Đã xóa Gradle cache" -ForegroundColor Green
    } catch {
        Write-Host "  × Không thể xóa Gradle cache (không quan trọng)" -ForegroundColor DarkGray
    }
}

# Bước 6: Chạy Gradle clean
Write-Host "[6/6] Chạy Gradle clean..." -ForegroundColor Yellow
$env:JAVA_HOME = "D:\java"
$env:PATH = "D:\java\bin;$env:PATH"
& .\gradlew clean

Write-Host ""
Write-Host "=== Hoàn thành! ===" -ForegroundColor Green
Write-Host ""
Write-Host "Giờ bạn có thể chạy lại build với:" -ForegroundColor Cyan
Write-Host "  .\gradlew assembleDebug" -ForegroundColor White
Write-Host ""

