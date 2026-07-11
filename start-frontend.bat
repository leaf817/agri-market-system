@echo off
chcp 65001 >nul
cd /d %~dp0frontend
if not exist node_modules (
  echo 首次运行，正在安装前端依赖...
  call npm install
)
echo 正在启动前端（Vite，端口 5173）...
call npm run dev
pause
