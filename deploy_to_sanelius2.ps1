# 部署到sanelius2账号的脚本
# 使用方法: 在PowerShell中运行: .\deploy_to_sanelius2.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  部署到sanelius2 GitHub账号" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 更新Git配置
Write-Host "[1/5] 更新Git用户配置..." -ForegroundColor Yellow
git config user.name "sanelius2"
git config user.email "sanelius@163.com"
Write-Host "✓ Git配置已更新" -ForegroundColor Green

# 删除旧远程仓库
Write-Host ""
Write-Host "[2/5] 检查并删除旧远程仓库..." -ForegroundColor Yellow
$remotes = git remote -v
if ($remotes -match "origin") {
    $originUrl = git remote get-url origin
    Write-Host "发现现有远程仓库: $originUrl" -ForegroundColor Yellow
    $delete = Read-Host "是否删除旧远程仓库? (Y/N)"
    if ($delete -eq "Y" -or $delete -eq "y") {
        git remote remove origin
        Write-Host "✓ 已删除旧远程仓库" -ForegroundColor Green
    }
} else {
    Write-Host "✓ 没有需要删除的远程仓库" -ForegroundColor Green
}

# 添加新远程仓库
Write-Host ""
Write-Host "[3/5] 添加sanelius2账号的远程仓库..." -ForegroundColor Yellow
git remote add origin https://github.com/sanelius2/scheduled-audio-player.git
Write-Host "✓ 远程仓库已添加" -ForegroundColor Green

# 设置主分支
Write-Host ""
Write-Host "[4/5] 设置主分支..." -ForegroundColor Yellow
git branch -M main
Write-Host "✓ 主分支已设置为main" -ForegroundColor Green

# 推送代码
Write-Host ""
Write-Host "[5/5] 推送代码到sanelius2账号..." -ForegroundColor Yellow
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  重要提示" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. 请确保已完成以下操作:" -ForegroundColor Yellow
Write-Host "   - 在GitHub上登录sanelius2账号" -ForegroundColor White
Write-Host "   - 创建了GitHub Personal Access Token" -ForegroundColor White
Write-Host "     (访问: https://github.com/settings/tokens)" -ForegroundColor White
Write-Host "   - Token需要repo权限" -ForegroundColor White
Write-Host ""
Write-Host "2. 推送时需要输入:" -ForegroundColor Yellow
Write-Host "   Username: sanelius2" -ForegroundColor White
Write-Host "   Password: (粘贴您的Personal Access Token)" -ForegroundColor White
Write-Host ""
Write-Host "3. 如果推送失败,可能需要:" -ForegroundColor Yellow
Write-Host "   - 在GitHub上手动创建仓库" -ForegroundColor White
Write-Host "     (访问: https://github.com/new)" -ForegroundColor White
Write-Host "   - 仓库名: scheduled-audio-player" -ForegroundColor White
Write-Host "   - 设置为Public" -ForegroundColor White
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$ready = Read-Host "是否继续推送? (Y/N)"
if ($ready -eq "Y" -or $ready -eq "y") {
    Write-Host ""
    Write-Host "正在推送到GitHub..." -ForegroundColor Yellow
    Write-Host ""

    git push -u origin main

    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "  推送成功!" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "仓库地址: https://github.com/sanelius2/scheduled-audio-player" -ForegroundColor Cyan
        Write-Host "Actions页面: https://github.com/sanelius2/scheduled-audio-player/actions" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "下一步:" -ForegroundColor Yellow
        Write-Host "1. 访问仓库,确认文件已上传" -ForegroundColor White
        Write-Host "2. 等待GitHub Actions自动构建" -ForegroundColor White
        Write-Host "3. 构建完成后下载APK文件" -ForegroundColor White
        Write-Host ""
    } else {
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Red
        Write-Host "  推送失败" -ForegroundColor Red
        Write-Host "========================================" -ForegroundColor Red
        Write-Host ""
        Write-Host "可能的原因:" -ForegroundColor Yellow
        Write-Host "1. GitHub账号未登录或登录了错误的账号" -ForegroundColor White
        Write-Host "2. Personal Access Token无效或权限不足" -ForegroundColor White
        Write-Host "3. 仓库已在GitHub上创建" -ForegroundColor White
        Write-Host ""
        Write-Host "解决方案:" -ForegroundColor Yellow
        Write-Host "1. 检查: git remote -v" -ForegroundColor White
        Write-Host "2. 手动创建仓库: https://github.com/new" -ForegroundColor White
        Write-Host "3. 参考详细指南: REPO_INSTRUCTIONS.md" -ForegroundColor White
        Write-Host ""
    }
} else {
    Write-Host ""
    Write-Host "已取消推送。" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "您可以稍后手动执行:" -ForegroundColor Yellow
    Write-Host "  git push -u origin main" -ForegroundColor Cyan
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "文档:" -ForegroundColor Yellow
Write-Host "  - REPO_INSTRUCTIONS.md: 详细部署指南" -ForegroundColor White
Write-Host "  - README_ZH.md: 项目说明" -ForegroundColor White
Write-Host ""
