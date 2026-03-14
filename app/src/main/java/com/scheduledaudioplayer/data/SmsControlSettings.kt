package com.scheduledaudioplayer.data

import android.content.Context
import android.content.SharedPreferences

/**
 * 短信控制设置管理
 */
class SmsControlSettings(context: Context) {
    
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(
        "sms_control_settings",
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_CONTROL_NUMBER = "control_number"
        private const val KEY_START_COMMAND = "start_command"
        private const val KEY_STOP_COMMAND = "stop_command"
        
        const val DEFAULT_START_COMMAND = "start"
        const val DEFAULT_STOP_COMMAND = "stop"
    }
    
    var controlNumber: String
        get() = sharedPrefs.getString(KEY_CONTROL_NUMBER, "") ?: ""
        set(value) = sharedPrefs.edit().putString(KEY_CONTROL_NUMBER, value).apply()
    
    var startCommand: String
        get() = sharedPrefs.getString(KEY_START_COMMAND, DEFAULT_START_COMMAND) ?: DEFAULT_START_COMMAND
        set(value) = sharedPrefs.edit().putString(KEY_START_COMMAND, value).apply()
    
    var stopCommand: String
        get() = sharedPrefs.getString(KEY_STOP_COMMAND, DEFAULT_STOP_COMMAND) ?: DEFAULT_STOP_COMMAND
        set(value) = sharedPrefs.edit().putString(KEY_STOP_COMMAND, value).apply()
    
    fun clear() {
        sharedPrefs.edit().clear().apply()
    }
}
