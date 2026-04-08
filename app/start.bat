@echo off
echo正在安装前端依赖...
npm install

if %errorlevel% neq 0 (
    echo 依赖安装失败，请检查网络连接和npm配置
    pause
    exit /b 1
)

echo.
echo 依赖安装完成！
echo.
echo正在启动开发服务器...
echo访地址: http://localhost:3000
echo后APIPI地址: http://localhost:8080
echo.

npm run dev