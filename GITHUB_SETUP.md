# GitHub仓库设置指南

## 方式一:使用GitHub CLI (推荐)

如果已安装GitHub CLI (gh),执行以下命令:

```bash
# 1. 登录GitHub
gh auth login

# 2. 创建仓库并推送
gh repo create 手机定时播放 --public --source=. --remote=origin --push
```

## 方式二:手动创建

### 步骤1:在GitHub创建仓库

1. 访问 https://github.com/new
2. 仓库名称: `手机定时播放`
3. 设置为 Public 或 Private
4. 不要初始化README、.gitignore或LICENSE(因为项目已有这些文件)
5. 点击"Create repository"

### 步骤2:推送代码到GitHub

```bash
cd "C:\Users\sanelius\Desktop\手机定时播放"

# 添加远程仓库
git remote add origin https://github.com/sanelius/手机定时播放.git

# 如果使用HTTPS,需要配置凭据
# 推送代码
git branch -M main
git push -u origin main
```

### 步骤3:配置GitHub Secrets (可选但推荐)

为了让GitHub Actions能够成功构建,可以配置GitHub Secrets:

1. 访问仓库的 Settings > Secrets and variables > Actions
2. 添加以下Secrets (如果需要签名):
   - `KEYSTORE_FILE`: Base64编码的keystore文件
   - `KEYSTORE_PASSWORD`: Keystore密码
   - `KEY_ALIAS`: Key别名
   - `KEY_PASSWORD`: Key密码

注意:当前配置生成的是未签名的APK,可以直接安装使用。

### 步骤4:手动构建

推送代码后,GitHub Actions会自动触发构建:

1. 访问仓库的 Actions 标签页
2. 等待构建完成
3. 在构建详情页面下载APK文件:
   - `debug-apk`: app-debug.apk (可以安装到任何设备)
   - `release-apk`: app-release-unsigned.apk (未签名的Release版本)

## 方式三:使用个人访问令牌

如果需要使用密码推送到GitHub,需要使用个人访问令牌:

### 创建个人访问令牌

1. 访问 https://github.com/settings/tokens
2. 点击"Generate new token" (classic)
3. 选择权限: `repo` (完整控制私有仓库)
4. 生成并复制令牌

### 使用令牌推送

```bash
# 推送时,用户名使用GitHub用户名,密码使用个人访问令牌
git push -u origin main
```

## 当前Git状态

```bash
# 查看当前状态
git status

# 查看提交历史
git log --oneline

# 查看远程仓库
git remote -v
```

## 故障排除

### 问题1:推送时认证失败

解决方案:
- 使用个人访问令牌代替密码
- 确保远程URL格式正确
- 检查SSH密钥配置

### 问题2:GitHub Actions构建失败

解决方案:
- 检查gradlew文件是否有执行权限
- 查看Actions日志中的错误信息
- 确保所有必要的依赖都正确配置

### 问题3:中文仓库名称问题

某些Git客户端可能不支持中文路径,遇到问题可以:

```bash
# 临时设置Git忽略文件大小写
git config core.ignorecase true

# 或者使用英文仓库名
git remote set-url origin https://github.com/sanelius/scheduled-audio-player.git
```

## 下一步

推送代码后,您可以:

1. 在手机上安装APK测试功能
2. 根据需要修改功能和UI
3. 添加更多自定义功能
4. 发布到应用商店

## 技术支持

如有问题,请查看:
- GitHub Actions文档: https://docs.github.com/en/actions
- Gradle构建文档: https://developer.android.com/studio/build
- Android开发者指南: https://developer.android.com/
