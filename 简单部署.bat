@echo off
chcp 65001 >nul
echo ========================================
echo   简单部署到 GitHub
echo ========================================
echo.

cd /d C:\Users\sanelius\Desktop\手机定时播放

echo 步骤1: 添加远程仓库...
git remote add origin https://github.com/sanelius2/scheduled-audio-player.git
if %ERRORLEVEL% NEQ 0 (
    echo 远程仓库已存在，先删除...
    git remote remove origin
    git remote add origin https://github.com/sanelius2/scheduled-audio-player.git
)

echo.
echo 步骤2: 设置主分支...
git branch -M main

echo.
echo 步骤3: 推送代码到 GitHub...
echo.
echo 重要提示：
echo   - 当提示输入 Username 时，输入：sanelius2
echo   - 当提示输入 Password 时，粘贴 Personal Access Token
echo   - 不是 GitHub 密码，而是 GitHub Token！
echo.
echo 如果还没有 Token，请访问：
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
    echo 仓库地址：https://github.com/sanelius2/scheduled-audio-player
    echo Actions：https://github.com/sanelius2/scheduled-audio-player/actions
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
    echo 1. 手动创建仓库：https://github.com/new
    echo 2. 仓库名：scheduled-audio-player
    echo 3. 设置为 Public
    echo 4. 重新运行此脚本
    echo.
)

pause
