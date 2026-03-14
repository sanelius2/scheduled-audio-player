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
 * 定时停止播放接收器
 * - 正常定时任务：只重设下一次"结束"闹钟，不动"开始"闹钟
 * - 立即播放的延迟停止：直接停止，不重设任何闹钟
 */
class StopPlayReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // 立即停止播放（无需等待数据库）
        AudioPlayService.stopPlay(context)

        // 判断是否为「立即播放」模式的延迟停止
        val isImmediateStop = intent.getBooleanExtra(AlarmManagerWrapper.EXTRA_IMMEDIATE_STOP, false)
        if (isImmediateStop) {
            // 一次性停止，无需重调度，直接返回
            return
        }

        val taskId = intent.getLongExtra(AlarmReceiver.EXTRA_TASK_ID, -1L)
        if (taskId == -1L) return

        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "scheduledaudioplayer:StopPlayReceiver"
        ).apply { acquire(30_000L) }

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val task = db.scheduleTaskDao().getTaskById(taskId) ?: return@launch

                if (!task.enabled) return@launch

                // 重复任务：只重新调度下一次"结束"闹钟，不触碰"开始"闹钟
                when (task.repeatType) {
                    RepeatType.DAILY.value,
                    RepeatType.WORKDAY.value -> {
                        AlarmManagerWrapper(context).scheduleNextEndAlarm(task)
                    }
                }
            } finally {
                wakeLock.release()
                pendingResult.finish()
            }
        }
    }
}
