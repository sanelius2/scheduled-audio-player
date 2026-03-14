# 部署到 GitHub 脚本
$ErrorActionPreference = "Stop"

# 切换到脚本所在目录
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptPath

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  部署到 GitHub" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "当前目录: $PWD" -ForegroundColor Yellow
Write-Host ""

# 检查是否为 Git 仓库
if (-not (Test-Path ".git")) {
    Write-Host "错误: 不是 Git 仓库" -ForegroundColor Red
    Write-Host ""
    Write-Host "请确认在项目文件夹中运行此脚本" -ForegroundColor Yellow
    Write-Host ""
    Read-Host "按回车键退出"
    exit 1
}

# 添加远程仓库
Write-Host "[步骤 1/3] 添加远程仓库..." -ForegroundColor Yellow
try {
    git remote add origin https://github.com/sanelius2/scheduled-audio-player.git 2>$null
} catch {
    Write-Host "远程仓库已存在，先删除..." -ForegroundColor Yellow
    git remote remove origin
    git remote add origin https://github.com/sanelius2/scheduled-audio-player.git
}
Write-Host "远程仓库已添加" -ForegroundColor Green
Write-Host ""

# 设置主分支
Write-Host "[步骤 2/3] 设置主分支..." -ForegroundColor Yellow
git branch -M main
Write-Host "主分支已设置" -ForegroundColor Green
Write-Host ""

# 推送代码
Write-Host "[步骤 3/3] 推送代码到 GitHub..." -ForegroundColor Yellow
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  重要提示" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "当提示输入时：" -ForegroundColor Yellow
Write-Host "  Username: sanelius2" -ForegroundColor White
Write-Host "  Password: (粘贴 Personal Access Token)" -ForegroundColor White
Write-Host ""
Write-Host "注意: Password 不是 GitHub 密码，而是 Token！" -ForegroundColor Yellow
Write-Host ""
Write-Host "如果还没有 Token，请访问:" -ForegroundColor Yellow
Write-Host "  https://github.com/settings/tokens" -ForegroundColor Cyan
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

try {
    git push -u origin main

    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "  推送成功！" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "仓库地址: https://github.com/sanelius2/scheduled-audio-player" -ForegroundColor Cyan
    Write-Host "Actions: https://github.com/sanelius2/scheduled-audio-player/actions" -ForegroundColor Cyan
    Write-Host ""
} catch {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "  推送失败" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "可能原因:" -ForegroundColor Yellow
    Write-Host "1. Token 无效或权限不足" -ForegroundColor White
    Write-Host "2. 仓库已在 GitHub 上创建" -ForegroundColor White
    Write-Host "3. 网络问题" -ForegroundColor White
    Write-Host ""
    Write-Host "解决方案:" -ForegroundColor Yellow
    Write-Host "1. 手动创建仓库: https://github.com/new" -ForegroundColor White
    Write-Host "2. 仓库名: scheduled-audio-player" -ForegroundColor White
    Write-Host "3. 设置为 Public" -ForegroundColor White
    Write-Host "4. 重新运行此脚本" -ForegroundColor White
    Write-Host ""
}

Write-Host ""
Read-Host "按回车键退出"
