@echo off
echo Building Lycommons AAR file...
echo.

cd /d "%~dp0"

echo Cleaning previous builds...
call .\gradlew clean
if %errorlevel% neq 0 (
    echo Clean failed, but continuing...
)

echo.
echo Building release AAR...
call .\gradlew :mylib:assembleRelease

if %errorlevel% equ 0 (
    echo.
    echo ✅ AAR build successful!
    echo.
    echo 📦 AAR file location:
    echo   %cd%\mylib\build\outputs\aar\mylib-release.aar
    echo.
    echo 📋 Next steps:
    echo   1. Copy the AAR file to your target project
    echo   2. Add it to your app/libs/ folder
    echo   3. Add to dependencies: implementation(files("libs/mylib-release.aar"))
    echo   4. Add all required dependencies (see documentation)
    echo.
) else (
    echo.
    echo ❌ AAR build failed!
    echo.
    echo 🔧 Troubleshooting:
    echo   - Check that all dependencies are properly configured
    echo   - Try building with: .\gradlew :mylib:assembleDebug
    echo   - See documentation for manual distribution options
    echo.
)

pause
