# 自动部署脚本
# 使用方法:在PowerShell中运行: .\deploy.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Android定时播放器 - 自动部署脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查Git是否已初始化
$gitInitialized = Test-Path ".git"

if (-not $gitInitialized) {
    Write-Host "[1/6] 初始化Git仓库..." -ForegroundColor Yellow
    git init
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Git仓库初始化成功" -ForegroundColor Green
    } else {
        Write-Host "✗ Git仓库初始化失败" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "[1/6] Git仓库已存在,跳过初始化" -ForegroundColor Green
}

# 配置Git用户
Write-Host ""
Write-Host "[2/6] 配置Git用户信息..." -ForegroundColor Yellow
git config user.name "sanelius"
git config user.email "sanelius@163.com"
Write-Host "✓ Git用户配置完成" -ForegroundColor Green

# 添加所有文件
Write-Host ""
Write-Host "[3/6] 添加文件到Git..." -ForegroundColor Yellow
git add .

# 检查是否有需要提交的更改
$changes = git status --short
if ($changes) {
    # 创建提交
    Write-Host "✓ 文件已添加" -ForegroundColor Green
    Write-Host ""
    Write-Host "[4/6] 创建提交..." -ForegroundColor Yellow
    git commit -m "Initial commit: Android Scheduled Audio Player with full functionality

Features:
- Audio file import and management
- 3 scheduled tasks with multiple repeat modes
- SMS remote control (start/stop)
- Automatic play/stop functionality
- Material Design 3 UI
- GitHub Actions for automatic building

Developer: sanelius
Email: sanelius@163.com"
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ 提交创建成功" -ForegroundColor Green
    } else {
        Write-Host "✗ 提交创建失败" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "✓ 没有新的更改需要提交" -ForegroundColor Green
}

# 检查远程仓库
Write-Host ""
Write-Host "[5/6] 检查远程仓库..." -ForegroundColor Yellow
$remotes = git remote
if ($remotes -match "origin") {
    $originUrl = git remote get-url origin
    Write-Host "✓ 远程仓库已配置: $originUrl" -ForegroundColor Green
} else {
    Write-Host "⚠ 未找到远程仓库" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "请选择操作:" -ForegroundColor Cyan
    Write-Host "1. 使用GitHub CLI自动创建并推送 (推荐)" -ForegroundColor White
    Write-Host "2. 手动在GitHub创建仓库,然后推送到远程" -ForegroundColor White
    Write-Host "3. 仅在本地完成,稍后手动推送" -ForegroundColor White
    Write-Host ""

    $choice = Read-Host "请输入选项 (1-3)"

    switch ($choice) {
        "1" {
            Write-Host ""
            Write-Host "正在使用GitHub CLI创建仓库..." -ForegroundColor Yellow
            Write-Host "注意: 首次使用需要登录" -ForegroundColor Yellow
            Write-Host ""

            # 尝试使用gh创建仓库
            gh repo create 手机定时播放 --public --source=. --remote=origin --push

            if ($LASTEXITCODE -eq 0) {
                Write-Host ""
                Write-Host "✓ 仓库创建并推送成功!" -ForegroundColor Green
                Write-Host ""
                Write-Host "仓库地址: https://github.com/sanelius/手机定时播放" -ForegroundColor Cyan
                Write-Host ""
                Write-Host "下一步:" -ForegroundColor Yellow
                Write-Host "1. 访问仓库的Actions标签页查看构建状态" -ForegroundColor White
                Write-Host "2. 构建完成后,下载APK文件安装到手机" -ForegroundColor White
            } else {
                Write-Host ""
                Write-Host "✗ GitHub CLI创建失败,可能需要先登录" -ForegroundColor Red
                Write-Host ""
                Write-Host "请执行以下命令后重新运行脚本:" -ForegroundColor Yellow
                Write-Host "  gh auth login" -ForegroundColor Cyan
                exit 1
            }
        }
        "2" {
            Write-Host ""
            Write-Host "请在浏览器中完成以下步骤:" -ForegroundColor Yellow
            Write-Host ""
            Write-Host "1. 访问: https://github.com/new" -ForegroundColor White
            Write-Host "2. 仓库名称: 手机定时播放" -ForegroundColor White
            Write-Host "3. 设置为Public或Private" -ForegroundColor White
            Write-Host "4. 不要初始化README、.gitignore或LICENSE" -ForegroundColor White
            Write-Host "5. 点击'Create repository'" -ForegroundColor White
            Write-Host ""
            $ready = Read-Host "完成上述步骤后,输入Y继续"
            if ($ready -eq "Y" -or $ready -eq "y") {
                Write-Host ""
                Write-Host "正在添加远程仓库..." -ForegroundColor Yellow
                git remote add origin https://github.com/sanelius/手机定时播放.git
                git branch -M main

                Write-Host ""
                Write-Host "正在推送到GitHub..." -ForegroundColor Yellow
                Write-Host "注意: 推送时可能需要GitHub用户名和密码" -ForegroundColor Yellow
                Write-Host "      (密码应使用个人访问令牌,而非GitHub密码)" -ForegroundColor Yellow
                Write-Host ""

                git push -u origin main

                if ($LASTEXITCODE -eq 0) {
                    Write-Host ""
                    Write-Host "✓ 推送成功!" -ForegroundColor Green
                    Write-Host ""
                    Write-Host "仓库地址: https://github.com/sanelius/手机定时播放" -ForegroundColor Cyan
                } else {
                    Write-Host ""
                    Write-Host "✗ 推送失败" -ForegroundColor Red
                    Write-Host ""
                    Write-Host "常见问题:" -ForegroundColor Yellow
                    Write-Host "1. 认证失败: 使用个人访问令牌代替GitHub密码" -ForegroundColor White
                    Write-Host "2. 网络问题: 检查网络连接" -ForegroundColor White
                    Write-Host "3. 权限问题: 确保有仓库写入权限" -ForegroundColor White
                }
            }
        }
        "3" {
            Write-Host ""
            Write-Host "✓ 本地准备完成" -ForegroundColor Green
            Write-Host ""
            Write-Host "稍后可以手动执行以下命令推送:" -ForegroundColor Yellow
            Write-Host "  git remote add origin https://github.com/sanelius/手机定时播放.git" -ForegroundColor Cyan
            Write-Host "  git branch -M main" -ForegroundColor Cyan
            Write-Host "  git push -u origin main" -ForegroundColor Cyan
            exit 0
        }
        default {
            Write-Host "无效的选项" -ForegroundColor Red
            exit 1
        }
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  部署完成!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "项目文件:" -ForegroundColor Yellow
Write-Host "  - README.md: 项目说明文档" -ForegroundColor White
Write-Host "  - GITHUB_SETUP.md: GitHub设置详细指南" -ForegroundColor White
Write-Host "  - .github/workflows/build.yml: GitHub Actions配置" -ForegroundColor White
Write-Host ""
Write-Host "技术支持:" -ForegroundColor Yellow
Write-Host "  - 开发者: sanelius" -ForegroundColor White
Write-Host "  - 邮箱: sanelius@163.com" -ForegroundColor White
Write-Host ""
