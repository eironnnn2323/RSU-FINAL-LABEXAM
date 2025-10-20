@echo off
echo ========================================
echo   Starting RSU Registration Backend
echo ========================================
echo.
cd rsu-registration-backend
echo Backend directory: %CD%
echo.
echo Starting Spring Boot application...
echo This will take about 10-15 seconds...
echo.
echo Backend will be available at: http://localhost:8080
echo.
echo IMPORTANT: Keep this window OPEN!
echo Press Ctrl+C to stop the backend server
echo.
echo ========================================
echo.
mvn spring-boot:run
