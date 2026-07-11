@echo off
chcp 65001 >nul
cd /d %~dp0agri-market-server
echo 正在启动后端（Spring Boot，端口 8081）...
mvn spring-boot:run
pause
