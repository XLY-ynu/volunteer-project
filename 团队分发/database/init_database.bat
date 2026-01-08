@echo off
chcp 65001 >nul
echo ========================================
echo   志愿者系统数据库初始化脚本
echo ========================================
echo.

set /p MYSQL_PASSWORD=请输入MySQL root密码: 

echo.
echo 正在初始化数据库...
echo.

mysql -u root -p%MYSQL_PASSWORD% < "%~dp0volunteer_complete.sql"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   数据库初始化成功！
    echo ========================================
    echo.
    echo 默认账号：
    echo   管理员: admin / admin123
    echo   组织端: org1 / admin123
    echo.
    echo 请修改 backend/src/main/resources/application.yml
    echo 中的数据库密码为: %MYSQL_PASSWORD%
    echo.
) else (
    echo.
    echo ========================================
    echo   数据库初始化失败！
    echo ========================================
    echo.
    echo 请检查：
    echo   1. MySQL服务是否已启动
    echo   2. 密码是否正确
    echo   3. mysql命令是否在PATH中
    echo.
)

pause
