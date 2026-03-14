@echo off
chcp 65001 >nul
echo ========================================
echo   部署到 sanelius2 GitHub账号
echo ========================================
echo.

cd /d C:\Users\sanelius\Desktop\手机定时播放

if %ERRORLEVEL% NEQ 0 (
    echo 错误：无法进入项目目录
    echo.
    pause
    exit /b 1
)

echo [1/6] 检查Git仓库...
if not exist ".git" (
    echo 错误：不是Git仓库
    echo.
    pause
    exit /b 1
)
echo ✓ Git仓库检查通过
echo.

echo [2/6] 更新Git用户配置...
git config user.name "sanelius2"
git config user.email "sanelius@163.com"
echo ✓ Git配置已更新
echo.

echo [3/6] 检查现有远程仓库...
git remote -v
echo.
set /p DELETE_REMOTE=是否删除现有远程仓库？(Y/N):
if /i "%DELETE_REMOTE%"=="Y" (
    git remote remove origin
    echo ✓ 已删除现有远程仓库
) else (
    echo ✓ 保留现有远程仓库
)
echo.

echo [4/6] 添加新的远程仓库...
git remote add origin https://github.com/sanelius2/scheduled-audio-player.git
echo ✓ 远程仓库已添加
echo.

echo [5/6] 设置主分支...
git branch -M main
echo ✓ 主分支已设置为 main
echo.

echo ========================================
echo   重要提示
echo ========================================
echo.
echo 1. 请确保已在GitHub上登录 sanelius2 账号
echo.
echo 2. 推送时需要输入：
echo    Username: sanelius2
echo    Password: (Personal Access Token)
echo.
echo 3. 如果推送失败，可能需要：
echo    - 在GitHub上手动创建仓库
echo      访问: https://github.com/new
echo    - 仓库名: scheduled-audio-player
echo    - 设置为 Public
echo.
echo 4. Personal Access Token 创建步骤：
echo    访问: https://github.com/settings/tokens
echo    点击: Generate new token (classic)
echo    勾选: repo 权限
echo    点击: Generate token
echo    复制并保存token（只显示一次！）
echo.
echo ========================================
echo.

set /p READY=是否继续推送？(Y/N):
if /i not "%READY%"=="Y" (
    echo 已取消推送
    echo.
    echo 您可以稍后手动执行：
    echo   git push -u origin main
    echo.
    pause
    exit /b 0
)

echo.
echo [6/6] 正在推送代码到GitHub...
echo.
echo 如果提示输入密码，请粘贴 Personal Access Token
echo (不是GitHub密码！)
echo.

git push -u origin main

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   推送成功！
    echo ========================================
    echo.
    echo 仓库地址: https://github.com/sanelius2/scheduled-audio-player
    echo Actions页面: https://github.com/sanelius2/scheduled-audio-player/actions
    echo.
    echo 下一步：
    echo 1. 访问仓库，确认文件已上传
    echo 2. 等待GitHub Actions自动构建
    echo 3. 构建完成后下载APK文件
    echo.
) else (
    echo.
    echo ========================================
    echo   推送失败
    echo ========================================
    echo.
    echo 可能的原因：
    echo 1. GitHub账号未登录或登录了错误的账号
    echo 2. Personal Access Token无效或权限不足
    echo 3. 仓库已在GitHub上创建
    echo.
    echo 解决方案：
    echo 1. 检查: git remote -v
    echo 2. 手动创建仓库: https://github.com/new
    echo 3. 参考详细指南: REPO_INSTRUCTIONS.md
    echo.
)

pause
