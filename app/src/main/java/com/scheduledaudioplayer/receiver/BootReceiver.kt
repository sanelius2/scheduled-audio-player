package com.scheduledaudioplayer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.scheduledaudioplayer.data.AppDatabase
import com.scheduledaudioplayer.manager.AlarmManagerWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 开机广播接收器：手机重启后恢复所有已启用的定时任务
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON") return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val enabledTasks = db.scheduleTaskDao().getEnabledTasks()
                val alarmManager = AlarmManagerWrapper(context)
                alarmManager.setAllTasksAlarms(enabledTasks)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
