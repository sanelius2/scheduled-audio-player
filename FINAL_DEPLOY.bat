@echo off
REM 切换到脚本所在目录
cd /d "%~dp0"

echo ========================================
echo   最终部署脚本
echo ========================================
echo.
echo 当前目录: %CD%
echo.

REM 步骤1: 初始化Git仓库
echo [1/5] 初始化Git仓库...
if not exist ".git" (
    git init
    if %ERRORLEVEL% NEQ 0 (
        echo 错误: Git init 失败
        echo 请检查Git是否已安装
        pause
        exit /b 1
    )
    echo Git仓库已初始化
) else (
    echo Git仓库已存在
)
echo.

REM 步骤2: 配置Git用户
echo [2/5] 配置Git用户...
git config user.name "sanelius2"
git config user.email "sanelius@163.com"
echo Git用户已配置
echo.

REM 步骤3: 添加所有文件
echo [3/5] 添加所有文件...
git add .
echo 文件已添加
echo.

REM 步骤4: 创建提交
echo [4/5] 创建提交...
git commit -m "Complete Android Scheduled Audio Player project
- Audio file import and management
- 3 scheduled tasks with multiple repeat modes
- SMS remote control (start/stop)
- Automatic play/stop functionality
- Material Design 3 UI
- GitHub Actions for automatic building

Developer: sanelius2
Email: sanelius@163.com"
if %ERRORLEVEL% NEQ 0 (
    echo 警告: 提交可能失败或没有更改
)
echo.
echo 提交已创建
echo.

REM 步骤5: 添加远程仓库
echo [5/5] 添加远程仓库...
git remote remove origin 2>nul
git remote add origin https://github.com/sanelius2/scheduled-audio-player.git
git branch -M main
echo 远程仓库已配置
echo.

echo ========================================
echo   Git仓库准备完成！
echo ========================================
echo.
echo 下一步: 执行推送命令
echo.
echo 命令: git push -u origin main
echo.
echo 或者直接按任意键继续推送...
pause >nul

echo.
echo ========================================
echo   正在推送到GitHub...
echo ========================================
echo.
echo 重要提示：
echo   - 当提示输入 Username 时，输入: sanelius2
echo   - 当提示输入 Password 时，粘贴 Personal Access Token
echo   - 不是 GitHub 密码，而是 GitHub Token！
echo.
echo 如果还没有 Token，请访问:
echo   https://github.com/settings/tokens
echo.
echo ========================================
echo.

git push -u origin main

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   推送成功！
    echo ========================================
    echo.
    echo 仓库地址: https://github.com/sanelius2/scheduled-audio-player
    echo Actions: https://github.com/sanelius2/scheduled-audio-player/actions
    echo.
    echo 下一步：
    echo 1. 访问仓库，确认文件已上传
    echo 2. 等待GitHub Actions自动构建（5-10分钟）
    echo 3. 下载APK文件
    echo.
) else (
    echo.
    echo ========================================
    echo   推送失败
    echo ========================================
    echo.
    echo 可能原因：
    echo 1. Token 无效或权限不足
    echo 2. 仓库已在 GitHub 上创建
    echo 3. 网络问题
    echo.
    echo 解决方案：
    echo 1. 手动创建仓库: https://github.com/new
    echo 2. 仓库名: scheduled-audio-player
    echo 3. 设置为 Public
    echo 4. 重新运行此脚本
    echo.
)

pause
