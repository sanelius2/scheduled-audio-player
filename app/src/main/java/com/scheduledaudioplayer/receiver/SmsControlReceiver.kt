package com.scheduledaudioplayer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.os.Build
import com.scheduledaudioplayer.data.AppDatabase
import com.scheduledaudioplayer.data.SmsControlSettings
import com.scheduledaudioplayer.service.AudioPlayService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsControlReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
            val smsSettings = SmsControlSettings(context)
            val controlNumber = smsSettings.controlNumber
            
            if (controlNumber.isEmpty()) {
                return // 未设置控制号码
            }
            
            val pdus = intent.getParcelableArrayExtra("pdus") as? Array<*> ?: return
            
            for (pdu in pdus) {
                val smsMessage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    SmsMessage.createFromPdu(pdu as ByteArray, "3gpp")
                } else {
                    @Suppress("DEPRECATION")
                    SmsMessage.createFromPdu(pdu as ByteArray)
                }
                
                smsMessage?.let { message ->
                    val senderNumber = message.originatingAddress ?: ""
                    val messageBody = message.messageBody?.trim()?.lowercase() ?: ""

                    // 检查是否为控制号码
                    if (senderNumber.contains(controlNumber)) {
                        when {
                            messageBody == smsSettings.startCommand.lowercase() -> {
                                // 启动所有启用的定时任务
                                startAllTasks(context)
                            }
                            messageBody == smsSettings.stopCommand.lowercase() -> {
                                // 停止播放
                                AudioPlayService.stopPlay(context)
                            }
                        }
                    }
                }
            }
        }
    }
    
    private fun startAllTasks(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val enabledTasks = database.scheduleTaskDao().getEnabledTasks()
                
                // 查找当前应该播放的任务
                val calendar = java.util.Calendar.getInstance()
                val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
                val currentMinute = calendar.get(java.util.Calendar.MINUTE)
                val currentDayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK) // 1=周日, 2=周一...
                
                for (task in enabledTasks) {
                    if (shouldPlayTask(task, currentHour, currentMinute, currentDayOfWeek)) {
                        AudioPlayService.startPlay(context, task.audioPath, task.audioName)
                        break // 只播放第一个匹配的任务
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun shouldPlayTask(
        task: com.scheduledaudioplayer.data.ScheduleTask,
        currentHour: Int,
        currentMinute: Int,
        currentDayOfWeek: Int
    ): Boolean {
        // 检查时间
        if (task.startTimeHour > currentHour || 
            (task.startTimeHour == currentHour && task.startTimeMinute > currentMinute)) {
            return false
        }
        
        // 检查结束时间
        if (task.endTimeHour < currentHour || 
            (task.endTimeHour == currentHour && task.endTimeMinute <= currentMinute)) {
            return false
        }
        
        // 检查重复类型
        return when (task.repeatType) {
            com.scheduledaudioplayer.data.RepeatType.DAILY.value -> true
            com.scheduledaudioplayer.data.RepeatType.WORKDAY.value -> {
                // 工作日: 周一(2)到周五(6)
                currentDayOfWeek in 2..6
            }
            com.scheduledaudioplayer.data.RepeatType.SPECIFIC_DATE.value -> {
                val calendar = java.util.Calendar.getInstance()
                val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val today = dateFormat.format(calendar.time)
                today == task.specificDate
            }
            com.scheduledaudioplayer.data.RepeatType.ONCE.value -> {
                // 仅一次,需要另外的逻辑来标记已播放
                true
            }
            else -> false
        }
    }
}
