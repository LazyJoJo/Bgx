#!/bin/bash
# 项目运行诊断脚本

echo "=== 项目运行诊断 ==="
echo

# 1. 检查Java环境
echo "1. Java环境检查..."
java -version
echo

# 2. 检查Maven环境
echo "2. Maven环境检查..."
mvn -v
echo

# 3. 检查依赖冲突
echo "3. 依赖冲突检查..."
mvn dependency:analyze-duplicate
echo

# 4. 检查编译状态
echo "4. 编译状态检查..."
mvn clean compile
echo

# 5.启动服务（带详细日志）
echo "5.启动服务诊断..."
mvn spring-boot:run -Ddebug=true -Dlogging.level.root=DEBUG

# 6. 保存日志
echo "6. 保存诊断日志..."
mvn spring-boot:run > startup.log 2>&1 &
sleep 10
kill %1 2>/dev/null || true
echo "诊断日志已保存到 startup.log"