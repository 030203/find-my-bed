# 验证构建修复的脚本
Write-Host "=== 清理Maven缓存 ===" -ForegroundColor Yellow
mvn clean
Write-Host ""
Write-Host "=== 验证Java版本（应该是17或更高）===" -ForegroundColor Green
java -version
Write-Host ""
Write-Host "=== 开始编译（带详细输出）===" -ForegroundColor Green
mvn compile -X 2>&1 | Select-String -Pattern "ERROR|WARNING|compiler|java.version|JDK" | Select-Object -First 30

