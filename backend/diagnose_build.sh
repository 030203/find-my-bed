#!/bin/bash
# 诊断脚本 - 检查Java版本和Maven配置
echo "=== Java版本检查 ==="
java -version
echo ""
echo "=== JAVA_HOME ==="
echo $JAVA_HOME
echo ""
echo "=== Maven版本 ==="
mvn -version
echo ""
echo "=== Maven编译器插件信息 ==="
mvn help:effective-pom | grep -A 20 "maven-compiler-plugin"
echo ""
echo "=== 检查pom.xml中的Java版本配置 ==="
grep -A 5 "java.version" pom.xml

