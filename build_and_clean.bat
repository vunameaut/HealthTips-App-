@echo off
echo ============================================
echo CLEANING AND REBUILDING PROJECT
echo ============================================
echo.

cd /d "d:\app\HealthTips-App-"

echo [1/3] Cleaning project...
call gradlew clean

echo.
echo [2/3] Building project...
call gradlew build

echo.
echo [3/3] Done!
echo.
echo ============================================
echo BUILD COMPLETED
echo ============================================
echo.
echo If you see errors related to resources not found,
echo please restart Android Studio and do:
echo 1. File ^> Invalidate Caches / Restart
echo 2. Build ^> Clean Project
echo 3. Build ^> Rebuild Project
echo.
pause

