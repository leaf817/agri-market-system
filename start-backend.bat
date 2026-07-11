@echo off
chcp 65001 >nul
cd /d %~dp0backend
echo 正在启动后端（Spring Boot，端口 8080）...
mvn spring-boot:run
pause
