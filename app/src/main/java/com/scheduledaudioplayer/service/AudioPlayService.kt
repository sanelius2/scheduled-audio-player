package com.scheduledaudioplayer.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.scheduledaudioplayer.MainActivity

class AudioPlayService : Service() {

    private val binder = LocalBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var currentAudioPath: String? = null
    private var isPlaying = false
    private var loopPlayback = false

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "audio_play_channel"
        const val ACTION_START_PLAY = "com.scheduledaudioplayer.START_PLAY"
        const val ACTION_STOP_PLAY = "com.scheduledaudioplayer.STOP_PLAY"
        const val EXTRA_AUDIO_PATH = "audio_path"
        const val EXTRA_AUDIO_NAME = "audio_name"
        const val EXTRA_LOOP_PLAYBACK = "loop_playback"

        fun startPlay(context: Context, audioPath: String, audioName: String, loopPlayback: Boolean = false) {
            val intent = Intent(context, AudioPlayService::class.java).apply {
                action = ACTION_START_PLAY
                putExtra(EXTRA_AUDIO_PATH, audioPath)
                putExtra(EXTRA_AUDIO_NAME, audioName)
                putExtra(EXTRA_LOOP_PLAYBACK, loopPlayback)
                // 传递 URI 读取权限给 Service
                if (audioPath.startsWith("content://")) {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopPlay(context: Context) {
            val intent = Intent(context, AudioPlayService::class.java).apply {
                action = ACTION_STOP_PLAY
            }
            context.startService(intent)
        }
    }

    inner class LocalBinder : Binder() {
        fun getService(): AudioPlayService = this@AudioPlayService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        // Android O+ 要求 startForegroundService 后必须立刻调用 startForeground
        // 先用占位通知顶上，避免 ANR
        showNotification("准备播放...", false)
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_PLAY -> {
                val audioPath = intent.getStringExtra(EXTRA_AUDIO_PATH)
                val audioName = intent.getStringExtra(EXTRA_AUDIO_NAME)
                loopPlayback = intent.getBooleanExtra(EXTRA_LOOP_PLAYBACK, false)
                if (!audioPath.isNullOrEmpty() && !audioName.isNullOrEmpty()) {
                    startPlaying(audioPath, audioName)
                } else {
                    stopSelf()
                }
            }
            ACTION_STOP_PLAY -> {
                releasePlayer()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            else -> {
                // 未知 action，停止服务
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startPlaying(audioPath: String, audioName: String) {
        // 先释放旧播放器，但不 stopSelf
        releasePlayer()

        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )

                // 兼容 content:// URI 和普通文件路径
                if (audioPath.startsWith("content://")) {
                    setDataSource(this@AudioPlayService, Uri.parse(audioPath))
                } else {
                    setDataSource(audioPath)
                }

                setOnErrorListener { _, what, extra ->
                    this@AudioPlayService.isPlaying = false
                    showNotification("播放出错 (${what}/${extra})", false)
                    false
                }

                setOnPreparedListener { mp ->
                    mp.isLooping = loopPlayback
                    mp.start()
                    this@AudioPlayService.isPlaying = true
                    this@AudioPlayService.currentAudioPath = audioPath
                    val displayName = if (loopPlayback) "$audioName (循环)" else audioName
                    showNotification(displayName, true)
                }

                setOnCompletionListener {
                    this@AudioPlayService.isPlaying = false
                    if (!loopPlayback) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                }

                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showNotification("无法打开音频文件", false)
            // 不立刻 stopSelf，让通知留一会儿让用户看到错误
        }
    }

    /**
     * 只释放 MediaPlayer，不调用 stopSelf / stopForeground
     */
    private fun releasePlayer() {
        mediaPlayer?.apply {
            try {
                if (isPlaying) stop()
                reset()
                release()
            } catch (_: Exception) {}
        }
        mediaPlayer = null
        isPlaying = false
        currentAudioPath = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "音频播放通知",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "显示音频播放状态"
            }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun showNotification(contentText: String, ongoing: Boolean) {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("定时播放器")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(ongoing)
            .setWhen(0)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    fun isCurrentlyPlaying(): Boolean = isPlaying
    fun getCurrentAudioPath(): String? = currentAudioPath

    override fun onDestroy() {
        releasePlayer() // 只释放，不递归调 stopSelf
        super.onDestroy()
    }
}
