#!/bin/bash

echo "正在安装前端依赖..."
npm install

if [ $? -ne 0 ]; then
    echo "依赖安装失败，请检查网络连接和npm配置"
    exit 1
fi

echo ""
echo "依赖安装完成！"
echo ""
echo "正在启动开发服务器..."
echo "访问地址: http://localhost:3000"
echo "后端API地址: http://localhost:8080"
echo ""

npm run dev