# 定时播放器 (Scheduled Audio Player)

一个功能强大的Android应用,可以定时播放选定的音频文件,支持多种定时模式和短信远程控制。

## 功能特性

### 📱 核心功能
- **音频文件导入**: 从设备存储导入任意音频文件
- **定时任务管理**: 最多支持3个独立的定时任务
- **多种重复模式**:
  - 每日重复
  - 工作日重复(可选周一至周日)
  - 具体日期(一次性)
  - 仅一次播放
- **精确时间控制**: 设置开始和结束时间
- **自动停止**: 在设定时间自动停止播放

### 📲 短信控制
- 通过指定号码的短信远程控制
- 自定义启动/停止命令(默认: start/stop)
- 安全验证: 只响应来自授权号码的指令

### ⏰ 闹钟系统
- 使用系统AlarmManager实现精准定时
- 支持设备休眠时唤醒播放
- 前台服务确保播放稳定

## 使用方法

### 基本设置

1. **导入音频**:
   - 点击"+"按钮添加新任务
   - 点击"选择音频"按钮从设备存储中选择音频文件

2. **配置定时任务**:
   - 设置任务名称
   - 选择要播放的音频文件
   - 设置开始和结束时间
   - 选择重复类型:
     - **每日**: 每天定时播放
     - **工作日**: 选择特定的星期几重复
     - **具体日期**: 在指定日期播放一次
     - **仅一次**: 在下一个设定时间播放一次
   - 启用/禁用任务开关

3. **短信控制**:
   - 点击"短信控制设置"按钮
   - 输入控制号码(只有这个号码发来的短信才能控制)
   - 设置启动和停止命令
   - 保存设置

### 短信指令示例

```
启动播放: start
停止播放: stop
```

## 技术架构

### 技术栈
- **语言**: Kotlin
- **最小SDK**: Android 7.0 (API 24)
- **目标SDK**: Android 14 (API 34)
- **架构**: MVVM
- **数据库**: Room
- **UI组件**: Material Design 3

### 主要组件

```
app/src/main/java/com/scheduledaudioplayer/
├── MainActivity.kt                 # 主Activity
├── data/                          # 数据层
│   ├── ScheduleTask.kt           # 任务数据模型
│   ├── ScheduleTaskDao.kt        # 数据访问对象
│   ├── AppDatabase.kt            # Room数据库
│   └── SmsControlSettings.kt     # 短信设置管理
├── service/                       # 服务
│   └── AudioPlayService.kt       # 音频播放服务
├── receiver/                      # 广播接收器
│   ├── AlarmReceiver.kt          # 闹钟接收器
│   ├── StopPlayReceiver.kt       # 停止播放接收器
│   └── SmsControlReceiver.kt     # 短信控制接收器
├── manager/                       # 管理器
│   └── AlarmManager.kt           # 闹钟管理器
└── adapter/                       # 适配器
    └── TaskAdapter.kt            # 任务列表适配器
```

## 权限说明

应用需要以下权限:

- `READ_MEDIA_AUDIO` (Android 13+) / `READ_EXTERNAL_STORAGE` (Android 12-): 读取音频文件
- `RECEIVE_SMS`: 接收短信控制指令
- `READ_SMS`: 读取短信内容
- `SCHEDULE_EXACT_ALARM`: 设置精准闹钟
- `FOREGROUND_SERVICE`: 前台服务播放音频
- `WAKE_LOCK`: 保持设备唤醒

## 构建和发布

### 使用GitHub Actions

项目已配置GitHub Actions自动构建:

1. **触发构建**:
   - 推送代码到main/master分支
   - 创建Pull Request
   - 手动触发workflow

2. **获取APK**:
   - 构建完成后,在Actions页面下载APK文件
   - Debug版本: `app-debug.apk`
   - Release版本: `app-release-unsigned.apk`

### 本地构建

```bash
# 克隆仓库
git clone https://github.com/sanelius/手机定时播放.git
cd 手机定时播放

# 构建Debug版本
./gradlew assembleDebug

# 构建Release版本
./gradlew assembleRelease

# APK输出位置
# Debug: app/build/outputs/apk/debug/app-debug.apk
# Release: app/build/outputs/apk/release/app-release-unsigned.apk
```

## 注意事项

1. **短信控制安全**:
   - 务必设置正确的控制号码
   - 建议使用专用SIM卡或虚拟号码
   - 定期检查短信设置,防止误操作

2. **电池优化**:
   - 前台服务会消耗一定电量
   - 建议将应用加入电池优化白名单
   - 避免在不必要时启用多个任务

3. **设备休眠**:
   - 应用使用WAKE_LOCK保持设备唤醒
   - 确保授予"允许后台运行"权限
   - 部分厂商系统可能需要额外设置

4. **文件权限**:
   - Android 10+需要授予存储访问权限
   - 建议使用内部存储或SD卡
   - 避免选择加密或受保护的文件

## GitHub仓库

- **仓库地址**: https://github.com/sanelius/scheduled-audio-player
- **自动构建**: GitHub Actions自动构建APK
- **下载APK**: 访问Actions页面下载最新构建

## 开发者

- **作者**: sanelius
- **邮箱**: sanelius@163.com
- **GitHub**: https://github.com/sanelius

## 许可证

本项目仅供个人学习和使用。

## 常见问题

### Q: 短信控制不工作?
A: 请检查:
1. 是否授予短信相关权限
2. 控制号码是否正确
3. 命令字是否与设置一致(区分大小写)

### Q: 定时播放不准确?
A: 请确保:
1. 已授予"精准闹钟"权限
2. 应用未被电池优化限制
3. 系统时间准确

### Q: 播放中断?
A: 可能原因:
1. 其他应用抢占音频焦点
2. 系统资源不足
3. 应用被系统限制后台运行

## 更新日志

### Version 1.0.0
- ✅ 初始版本发布
- ✅ 支持音频导入和播放
- ✅ 3个定时任务
- ✅ 多种重复模式
- ✅ 短信远程控制
- ✅ 自动停止功能
