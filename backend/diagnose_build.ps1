# PowerShell诊断脚本
Write-Host "=== Java版本检查 ===" -ForegroundColor Green
java -version
Write-Host ""
Write-Host "=== JAVA_HOME ===" -ForegroundColor Green
$env:JAVA_HOME
Write-Host ""
Write-Host "=== Maven版本 ===" -ForegroundColor Green
mvn -version
Write-Host ""
Write-Host "=== 检查pom.xml中的Java版本配置 ===" -ForegroundColor Green
Select-String -Path "pom.xml" -Pattern "java.version" -Context 0,5

