@echo off
echo =====================================
echo  Smart Product Comparator
echo  Advanced Algorithms Demo
echo  KLH CSE 2026-2027 Team 4
echo =====================================
echo.
cd /d "%~dp0"
if not exist backend\bin mkdir backend\bin
echo [1/2] Compiling Java backend...
javac -d backend\bin backend\src\*.java
if %errorlevel% neq 0 (
    echo COMPILE ERROR. Please ensure JDK is installed.
    pause
    exit /b 1
)
echo [2/2] Starting server at http://localhost:8080
echo.
echo Open your browser at: http://localhost:8080
echo Press Ctrl+C to stop.
echo.
cd backend
java -cp bin Main
