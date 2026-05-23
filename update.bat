@echo off
chcp 437 > nul
echo ====================================
echo   SmartBudget Update
echo ====================================
echo.
echo Downloading latest APK...
powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://github.com/hermell/game/releases/latest/download/app-debug.apk' -OutFile 'app-debug.apk'"
if not exist app-debug.apk (
    echo Download failed. Check your internet connection.
    pause
    exit /b 1
)
echo Download complete!
echo.
echo Installing to phone... (make sure phone is connected via USB)
adb install -r app-debug.apk
if %errorlevel% neq 0 (
    echo Install failed. Check USB connection.
) else (
    echo Install complete!
)
del app-debug.apk 2>nul
echo.
pause
