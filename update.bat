@echo off
chcp 65001 > nul
echo ====================================
echo   SmartBudget 자동 업데이트
echo ====================================
echo.
echo 최신 APK 다운로드 중...
curl -L -o app-debug.apk "https://github.com/hermell/game/releases/download/latest/app-debug.apk"
if %errorlevel% neq 0 (
    echo 다운로드 실패. 인터넷 연결을 확인하세요.
    pause
    exit /b 1
)
echo 다운로드 완료!
echo.
echo 폰에 설치 중... (폰이 USB로 연결되어 있는지 확인하세요)
adb install -r app-debug.apk
if %errorlevel% neq 0 (
    echo 설치 실패. 폰 연결 상태를 확인하세요.
) else (
    echo.
    echo 설치 완료! SmartBudget이 최신 버전으로 업데이트됐습니다.
)
del app-debug.apk
echo.
pause
