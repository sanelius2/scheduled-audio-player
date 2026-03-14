# 重新部署到正确GitHub账号的步骤

## 当前问题
项目被创建到了错误的GitHub账号。需要重新部署到 `sanelius2` 账号。

## 解决方案

### 方式一: 使用GitHub CLI (推荐但需要先登录)

#### 步骤1: 登录到sanelius2账号

在PowerShell中执行:

```bash
gh auth login
```

按照提示操作:
1. 选择: `GitHub.com`
2. 选择: `HTTPS`
3. 选择: `Login with a web browser` (推荐)
4. 在浏览器中登录 `sanelius2` 账号
5. 输入密码: `!@#$1qaz2wsX`
6. 授权GitHub CLI访问

#### 步骤2: 创建仓库并推送

登录成功后,执行:

```bash
cd 'C:\Users\sanelius\Desktop\手机定时播放'
gh repo create scheduled-audio-player --public --source=. --remote=origin --push
```

### 方式二: 手动在GitHub创建

#### 步骤1: 在GitHub创建新仓库

1. 在浏览器中访问: https://github.com/new
2. **重要**: 确保当前登录的是 `sanelius2` 账号
   - 检查右上角头像是否显示 `sanelius2`
   - 如果不是,请先登出并登录 `sanelius2`
3. 填写仓库信息:
   - Repository name: `scheduled-audio-player`
   - Description: `Android定时播放器 - 可定时播放音频文件,支持多种定时模式和短信远程控制`
   - Public ✅
4. **不要勾选**:
   - ✅ Add a README file
   - ✅ Add .gitignore
   - ✅ Choose a license
5. 点击 `Create repository`

#### 步骤2: 推送代码到新仓库

创建仓库后,GitHub会显示推送命令。在PowerShell中执行:

```bash
cd 'C:\Users\sanelius\Desktop\手机定时播放'

# 添加远程仓库
git remote add origin https://github.com/sanelius2/scheduled-audio-player.git

# 设置主分支
git branch -M main

# 推送代码
git push -u origin main
```

**注意**: 推送时可能需要认证:
- 用户名: `sanelius2`
- 密码: 使用Personal Access Token (个人访问令牌),不是GitHub密码

### 方式三: 使用个人访问令牌

如果推送时需要认证,需要创建个人访问令牌:

#### 创建个人访问令牌

1. 访问: https://github.com/settings/tokens
2. 确保登录的是 `sanelius2` 账号
3. 点击 `Generate new token` → `Generate new token (classic)`
4. 填写:
   - Note: `Scheduled Audio Player`
   - Expiration: 选择 `No expiration` 或较长时间
   - 勾选权限: `repo` (这会自动勾选所有repo相关权限)
5. 点击 `Generate token`
6. **重要**: 立即复制生成的令牌 (只显示一次!)

#### 使用令牌推送

```bash
git push -u origin main
```

当提示输入用户名和密码时:
- Username: `sanelius2`
- Password: 粘贴刚才复制的Personal Access Token

## 验证部署

推送成功后,验证:

1. 访问: https://github.com/sanelius2/scheduled-audio-player
2. 确认仓库存在且包含所有文件
3. 点击 `Actions` 标签页,查看是否有workflow运行
4. 等待构建完成后,下载APK测试

## 清理错误仓库

如果需要删除之前创建的错误仓库:

1. 访问: https://github.com/sanelius/scheduled-audio-player
2. 点击 `Settings` 标签页
3. 滚动到页面底部的 `Danger Zone`
4. 点击 `Delete this repository`
5. 输入仓库名称 `sanelius/scheduled-audio-player`
6. 点击 `I understand the consequences, delete this repository`

## 故障排除

### 问题1: 推送时提示认证失败

**解决方案**:
- 确保使用的是 `sanelius2` 账号
- 使用Personal Access Token而不是GitHub密码
- 检查令牌是否有 `repo` 权限

### 问题2: 远程仓库URL错误

**解决方案**:
```bash
# 查看当前远程仓库
git remote -v

# 修改远程仓库URL
git remote set-url origin https://github.com/sanelius2/scheduled-audio-player.git

# 验证修改
git remote -v
```

### 问题3: 权限不足

**解决方案**:
- 确认 `sanelius2` 账号有权限创建公开仓库
- 检查账号状态是否正常
- 确认没有超出仓库数量限制

## 快速命令参考

```bash
# 1. 删除旧远程仓库
cd 'C:\Users\sanelius\Desktop\手机定时播放'
git remote remove origin

# 2. 添加新远程仓库
git remote add origin https://github.com/sanelius2/scheduled-audio-player.git

# 3. 设置主分支
git branch -M main

# 4. 推送代码
git push -u origin main

# 5. 验证推送
git remote -v
```

## 下一步

成功推送到 `sanelius2` 账号后:

1. 访问新仓库: https://github.com/sanelius2/scheduled-audio-player
2. 等待GitHub Actions自动构建
3. 下载APK文件进行测试
4. 根据需要更新文档中的仓库链接

---

**注意**: 请务必确认所有操作都在 `sanelius2` 账号下进行!
