package com.scheduledaudioplayer.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import com.scheduledaudioplayer.data.ScheduleTask
import com.scheduledaudioplayer.receiver.AlarmReceiver
import com.scheduledaudioplayer.receiver.StopPlayReceiver
import java.util.Calendar

/**
 * 闹钟管理器，用于设置定时任务
 */
class AlarmManagerWrapper(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * 检查是否有精确闹钟权限（Android 12+）
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    /**
     * 设置定时任务（开始 + 结束两侧都设置），用于首次设置或整体刷新
     */
    fun setScheduleAlarm(task: ScheduleTask) {
        if (!task.enabled) {
            cancelScheduleAlarm(task.id)
            return
        }
        scheduleNextStartAlarm(task)
        scheduleNextEndAlarm(task)
    }

    /**
     * 只重新调度下一次"开始播放"闹钟（由 AlarmReceiver 触发后调用）
     */
    fun scheduleNextStartAlarm(task: ScheduleTask) {
        val triggerTime = getNextTriggerTime(task.startTimeHour, task.startTimeMinute)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_TASK_ID, task.id)
            putExtra(AlarmReceiver.EXTRA_IS_START, true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            getStartRequestCode(task.id),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        setExactAlarm(triggerTime, pendingIntent)
    }

    /**
     * 只重新调度下一次"结束播放"闹钟（由 StopPlayReceiver 触发后调用）
     */
    fun scheduleNextEndAlarm(task: ScheduleTask) {
        val endTime = getNextTriggerTime(task.endTimeHour, task.endTimeMinute)

        val intent = Intent(context, StopPlayReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_TASK_ID, task.id)
            putExtra(AlarmReceiver.EXTRA_IS_START, false)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            getStopRequestCode(task.id),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        setExactAlarm(endTime, pendingIntent)
    }

    /**
     * 设置一次性延迟停止闹钟（用于「立即播放 + N分钟后停止」功能）
     * requestCode 用 IMMEDIATE_STOP_REQUEST_CODE，避免与任务闹钟冲突
     */
    fun scheduleImmediateStopAlarm(delayMinutes: Int) {
        val triggerTime = System.currentTimeMillis() + delayMinutes * 60_000L

        val intent = Intent(context, StopPlayReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_TASK_ID, -1L) // 无关联任务
            putExtra(EXTRA_IMMEDIATE_STOP, true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            IMMEDIATE_STOP_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        setExactAlarm(triggerTime, pendingIntent)
    }

    /**
     * 取消「立即播放」的延迟停止闹钟
     */
    fun cancelImmediateStopAlarm() {
        val intent = Intent(context, StopPlayReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            IMMEDIATE_STOP_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )?.let { alarmManager.cancel(it) }
    }

    /**
     * 计算下一次触发时间：如果今天的该时间还没过就用今天，否则用明天
     */
    fun getNextTriggerTime(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return calendar.timeInMillis
    }

    /**
     * 设置精确闹钟，兼容各 Android 版本
     */
    private fun setExactAlarm(triggerTime: Long, pendingIntent: PendingIntent) {
        try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent
                        )
                    } else {
                        // 降级为不精确闹钟
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent
                        )
                    }
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent
                    )
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
                else -> {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            }
        } catch (e: SecurityException) {
            // 没有精确闹钟权限，降级
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    /**
     * 取消定时任务
     */
    fun cancelScheduleAlarm(taskId: Long) {
        val startIntent = Intent(context, AlarmReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            getStartRequestCode(taskId),
            startIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )?.let { alarmManager.cancel(it) }

        val stopIntent = Intent(context, StopPlayReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            getStopRequestCode(taskId),
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )?.let { alarmManager.cancel(it) }
    }

    /**
     * 批量设置所有启用任务的闹钟
     */
    fun setAllTasksAlarms(tasks: List<ScheduleTask>) {
        for (task in tasks) {
            cancelScheduleAlarm(task.id)
        }
        for (task in tasks) {
            if (task.enabled) {
                setScheduleAlarm(task)
            }
        }
    }

    companion object {
        const val EXTRA_IMMEDIATE_STOP = "immediate_stop"
        const val IMMEDIATE_STOP_REQUEST_CODE = 99999
        fun getStartRequestCode(taskId: Long) = taskId.toInt()
        fun getStopRequestCode(taskId: Long) = (taskId + 10000).toInt()
    }
}
