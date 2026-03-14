package com.scheduledaudioplayer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.scheduledaudioplayer.data.AppDatabase
import com.scheduledaudioplayer.data.RepeatType
import com.scheduledaudioplayer.manager.AlarmManagerWrapper
import com.scheduledaudioplayer.service.AudioPlayService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 定时开始播放接收器
 * 触发时从数据库读取任务，启动播放，并重新调度下一次闹钟
 */
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_IS_START = "is_start"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        if (taskId == -1L) return

        // 获取 WakeLock 防止 CPU 在协程执行前休眠
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "scheduledaudioplayer:AlarmReceiver"
        ).apply { acquire(30_000L) } // 最多持有 30 秒

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val task = db.scheduleTaskDao().getTaskById(taskId) ?: return@launch

                if (!task.enabled) return@launch

                // 启动播放
                AudioPlayService.startPlay(context, task.audioPath, task.audioName, task.loopPlayback)

                // 重新调度下一次"开始"闹钟（重复任务）
                // 注意：只重设开始侧，不动结束侧，避免互相覆盖
                val alarmMgr = AlarmManagerWrapper(context)
                when (task.repeatType) {
                    RepeatType.DAILY.value,
                    RepeatType.WORKDAY.value -> {
                        alarmMgr.scheduleNextStartAlarm(task)
                    }
                    RepeatType.SPECIFIC_DATE.value,
                    RepeatType.ONCE.value -> {
                        // 一次性任务：播放后禁用，避免重复触发
                        db.scheduleTaskDao().updateTask(task.copy(enabled = false))
                    }
                }
            } finally {
                wakeLock.release()
                pendingResult.finish()
            }
        }
    }
}
