package com.scheduledaudioplayer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 重复类型枚举
 */
enum class RepeatType(val value: Int) {
    DAILY(0),           // 每日
    WORKDAY(1),         // 工作日（周一到周五）
    SPECIFIC_DATE(2),   // 具体日期
    ONCE(3)             // 仅一次
}

/**
 * 定时任务数据实体
 */
@Entity(tableName = "schedule_tasks")
data class ScheduleTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val audioPath: String,
    val audioName: String,
    val enabled: Boolean,
    val startTimeHour: Int,
    val startTimeMinute: Int,
    val endTimeHour: Int,
    val endTimeMinute: Int,
    val repeatType: Int,
    val specificDate: String?, // 格式: yyyy-MM-dd, 仅当 repeatType == SPECIFIC_DATE 时有效
    val workDays: Int, // 位标志位,周一=1, 周二=2, 周三=4, 周四=8, 周五=16, 周六=32, 周日=64
    val loopPlayback: Boolean = false // 是否循环播放
)
