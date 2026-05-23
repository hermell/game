@echo off
chcp 437 > nul
echo ====================================
echo   SmartBudget Update
echo ====================================
echo.
echo Downloading latest APK...
curl.exe -L -o app-debug.apk "https://github.com/hermell/game/releases/latest/download/app-debug.apk"
if not exist app-debug.apk (
    echo Download failed. Check your internet connection.
    pause
    exit /b 1
)
echo Download complete!
echo.
echo Installing to phone... (make sure phone is connected via USB)
adb uninstall com.example.smartbudget 2>nul
adb install app-debug.apk
if %errorlevel% neq 0 (
    echo Install failed. Check USB connection.
) else (
    echo Install complete!
)
del app-debug.apk 2>nul
echo.
pause
