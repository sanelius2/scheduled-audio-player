package com.scheduledaudioplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.scheduledaudioplayer.adapter.TaskAdapter
import com.scheduledaudioplayer.data.AppDatabase
import com.scheduledaudioplayer.data.ScheduleTask
import com.scheduledaudioplayer.data.SmsControlSettings
import com.scheduledaudioplayer.databinding.ActivityMainBinding
import com.scheduledaudioplayer.manager.AlarmManagerWrapper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.app.AlarmManager

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: AppDatabase
    private lateinit var alarmManager: AlarmManagerWrapper
    private lateinit var smsSettings: SmsControlSettings
    private lateinit var taskAdapter: TaskAdapter
    
    private val tasks = mutableListOf<ScheduleTask>()
    
    // 权限请求结果
    private val audioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            selectAudioFile()
        } else {
            Toast.makeText(this, "需要权限才能导入音频", Toast.LENGTH_SHORT).show()
        }
    }
    
    private val smsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (!allGranted) {
            Toast.makeText(this, "部分权限未授予,短信控制功能可能无法使用", Toast.LENGTH_SHORT).show()
        }
    }
    
    // 对话框内选音频时的回调，选完后更新对话框显示
    private var pendingAudioCallback: ((path: String, name: String) -> Unit)? = null

    private val audioPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // 持久化读取权限，防止重启后 URI 失效
            try {
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}

            val uriString = it.toString()
            val fileName = getFileName(it) ?: uriString.substringAfterLast('/').ifBlank { "未知音频" }
            val callback = pendingAudioCallback
            if (callback != null) {
                // 对话框场景：直接回调更新对话框
                callback(uriString, fileName)
            } else {
                // 其他场景（保留兼容）
                showTaskDialog(null, uriString, fileName)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        database = AppDatabase.getDatabase(this)
        alarmManager = AlarmManagerWrapper(this)
        smsSettings = SmsControlSettings(this)
        
        setupUI()
        checkPermissions()
        loadTasks()
        scheduleAlarms()
    }
    
    private fun setupUI() {
        // 设置RecyclerView
        taskAdapter = TaskAdapter(
            tasks = tasks,
            onTaskClick = { task -> showTaskDialog(task) },
            onTaskToggle = { task -> toggleTask(task) },
            onTaskDelete = { task -> deleteTask(task) }
        )
        binding.recyclerViewTasks.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = taskAdapter
        }
        
        // 添加任务按钮
        binding.fabAddTask.setOnClickListener {
            if (tasks.size >= 3) {
                Toast.makeText(this, "最多支持3个定时任务", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showTaskDialog(null)
        }
        
        // 立即播放按钮
        binding.btnPlayNow.setOnClickListener {
            showPlayNowDialog()
        }
        
        // 短信设置按钮
        binding.btnSmsSettings.setOnClickListener {
            showSmsSettingsDialog()
        }
    }
    
    private fun checkPermissions() {
        val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        if (ContextCompat.checkSelfPermission(this, audioPermission) != PackageManager.PERMISSION_GRANTED) {
            audioPermissionLauncher.launch(audioPermission)
        }
        
        val smsPermissions = arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
        
        val needSmsPermissions = smsPermissions.any { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }
        
        if (needSmsPermissions) {
            smsPermissionLauncher.launch(smsPermissions)
        }
        
        // 检查通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!Settings.canDrawOverlays(this)) {
                AlertDialog.Builder(this)
                    .setTitle("需要通知权限")
                    .setMessage("请授予通知权限以接收播放提醒")
                    .setPositiveButton("去设置") { _, _ ->
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                        }
                        startActivity(intent)
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }
        }

        // Android 12+ 检查精确闹钟权限（定时播放的核心权限）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!am.canScheduleExactAlarms()) {
                AlertDialog.Builder(this)
                    .setTitle("需要精确闹钟权限")
                    .setMessage("定时播放需要「精确闹钟」权限才能准时触发，请在设置中开启。")
                    .setPositiveButton("去开启") { _, _ ->
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.parse("package:$packageName")
                        }
                        startActivity(intent)
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }
        }
    }
    
    private fun loadTasks() {
        lifecycleScope.launch {
            val loadedTasks = database.scheduleTaskDao().getAllTasks()
            tasks.clear()
            tasks.addAll(loadedTasks)
            taskAdapter.notifyDataSetChanged()
        }
    }
    
    private fun scheduleAlarms() {
        lifecycleScope.launch {
            val enabledTasks = database.scheduleTaskDao().getEnabledTasks()
            alarmManager.setAllTasksAlarms(enabledTasks)
        }
    }
    
    private fun selectAudioFile() {
        audioPickerLauncher.launch("audio/*")
    }
    
    private fun importAudio(uri: Uri) {
        val uriString = uri.toString()
        val fileName = getFileName(uri) ?: uriString.substringAfterLast('/').ifBlank { "未知音频" }
        showTaskDialog(null, uriString, fileName)
    }
    
    private fun getFilePath(uri: Uri): String {
        // 直接返回 URI 字符串，兼容 content:// 和 file:// 两种格式
        return uri.toString()
    }
    
    private fun getFileName(uri: Uri): String? {
        // 先尝试从 ContentResolver 查询显示名
        val projection = arrayOf(android.provider.MediaStore.Audio.Media.DISPLAY_NAME)
        return try {
            contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                val col = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.DISPLAY_NAME)
                if (col >= 0 && cursor.moveToFirst()) cursor.getString(col) else null
            }
        } catch (_: Exception) {
            null
        } ?: uri.lastPathSegment?.substringAfterLast('/')
    }
    
    private fun showTaskDialog(existingTask: ScheduleTask?, initialAudioPath: String? = null, initialAudioName: String? = null) {
        val isEdit = existingTask != null
        val dialogView = layoutInflater.inflate(R.layout.dialog_task_config, null)
        
        // 初始化对话框UI
        val taskNameInput = dialogView.findViewById<android.widget.EditText>(R.id.etTaskName)
        val startTimeText = dialogView.findViewById<android.widget.TextView>(R.id.tvStartTime)
        val endTimeText = dialogView.findViewById<android.widget.TextView>(R.id.tvEndTime)
        val audioText = dialogView.findViewById<android.widget.TextView>(R.id.tvAudio)
        val repeatTypeSpinner = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerRepeatType)
        val workdayCheckGroup = dialogView.findViewById<android.widget.LinearLayout>(R.id.layoutWorkdays)
        val specificDateText = dialogView.findViewById<android.widget.TextView>(R.id.tvSpecificDate)
        val enabledSwitch = dialogView.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switchEnabled)
        val loopPlaybackSwitch = dialogView.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switchLoopPlayback)
        val btnSelectAudio = dialogView.findViewById<android.widget.Button>(R.id.btnSelectAudio)
        
        // 初始化数据
        val name = if (isEdit) existingTask!!.name else "任务 ${tasks.size + 1}"
        val audioPath = if (isEdit) existingTask!!.audioPath else (initialAudioPath ?: "")
        val audioName = if (isEdit) existingTask!!.audioName else (initialAudioName ?: getString(R.string.no_audio_selected))
        val enabled = if (isEdit) existingTask!!.enabled else true
        val loopPlayback = if (isEdit) existingTask!!.loopPlayback else false
        
        taskNameInput.setText(name)
        startTimeText.text = if (isEdit) 
            String.format("%02d:%02d", existingTask!!.startTimeHour, existingTask!!.startTimeMinute)
        else "09:00"
        
        endTimeText.text = if (isEdit) 
            String.format("%02d:%02d", existingTask!!.endTimeHour, existingTask!!.endTimeMinute)
        else "18:00"
        
        audioText.text = audioName
        
        val repeatTypes = resources.getStringArray(R.array.repeat_types)
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, repeatTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        repeatTypeSpinner.adapter = adapter
        
        val selectedRepeatType = if (isEdit) existingTask!!.repeatType else 0
        repeatTypeSpinner.setSelection(selectedRepeatType)
        
        // 工作日选择
        val workdayCheckboxes = mutableMapOf<Int, android.widget.CheckBox>()
        val days = arrayOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
        val dayValues = arrayOf(1, 2, 4, 8, 16, 32, 64)
        
        for (i in days.indices) {
            val checkBox = android.widget.CheckBox(this)
            checkBox.text = days[i]
            if (isEdit) {
                checkBox.isChecked = (existingTask!!.workDays and dayValues[i]) != 0
            }
            workdayCheckboxes[dayValues[i]] = checkBox
            workdayCheckGroup.addView(checkBox)
        }
        
        enabledSwitch.isChecked = enabled
        loopPlaybackSwitch.isChecked = loopPlayback
        
        var selectedAudioPath = audioPath
        var selectedAudioName = audioName
        var selectedStartTime = if (isEdit) 
            Pair(existingTask!!.startTimeHour, existingTask!!.startTimeMinute)
        else Pair(9, 0)
        var selectedEndTime = if (isEdit) 
            Pair(existingTask!!.endTimeHour, existingTask!!.endTimeMinute)
        else Pair(18, 0)
        var selectedSpecificDate = if (isEdit) existingTask!!.specificDate else ""
        
        // 设置时间选择器
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        startTimeText.setOnClickListener {
            val timePicker = android.app.TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    selectedStartTime = Pair(hourOfDay, minute)
                    startTimeText.text = timeFormat.format(
                        Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, hourOfDay)
                            set(Calendar.MINUTE, minute)
                        }.time
                    )
                },
                selectedStartTime.first,
                selectedStartTime.second,
                true
            )
            timePicker.show()
        }
        
        endTimeText.setOnClickListener {
            val timePicker = android.app.TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    selectedEndTime = Pair(hourOfDay, minute)
                    endTimeText.text = timeFormat.format(
                        Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, hourOfDay)
                            set(Calendar.MINUTE, minute)
                        }.time
                    )
                },
                selectedEndTime.first,
                selectedEndTime.second,
                true
            )
            timePicker.show()
        }
        
        // 选择音频：通过 callback 将结果回传到对话框
        btnSelectAudio.setOnClickListener {
            pendingAudioCallback = { path, name ->
                selectedAudioPath = path
                selectedAudioName = name
                audioText.text = name
                pendingAudioCallback = null
            }
            selectAudioFile()
        }
        
        // 监听音频选择结果（已通过 pendingAudioCallback 处理，此处保留占位）
        lifecycleScope.launch {
            // 无需额外逻辑
        }
        
        // 重复类型变化监听
        repeatTypeSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                val repeatType = position
                when (repeatType) {
                    0 -> { // 每日
                        workdayCheckGroup.visibility = android.view.View.GONE
                        specificDateText.visibility = android.view.View.GONE
                    }
                    1 -> { // 工作日
                        workdayCheckGroup.visibility = android.view.View.VISIBLE
                        specificDateText.visibility = android.view.View.GONE
                    }
                    2 -> { // 具体日期
                        workdayCheckGroup.visibility = android.view.View.GONE
                        specificDateText.visibility = android.view.View.VISIBLE
                    }
                    3 -> { // 仅一次
                        workdayCheckGroup.visibility = android.view.View.GONE
                        specificDateText.visibility = android.view.View.GONE
                    }
                }
            }
            
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }
        
        // 具体日期选择
        specificDateText.setOnClickListener {
            val datePicker = android.app.DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    selectedSpecificDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    specificDateText.text = selectedSpecificDate
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }
        
        // 创建对话框
        AlertDialog.Builder(this)
            .setTitle(if (isEdit) "编辑任务" else "添加任务")
            .setView(dialogView)
            .setPositiveButton("保存") { _, _ ->
                val taskName = taskNameInput.text.toString()
                val repeatType = repeatTypeSpinner.selectedItemPosition
                
                // 获取选中的工作日
                var workDays = 0
                for ((value, checkbox) in workdayCheckboxes) {
                    if (checkbox.isChecked) {
                        workDays += value
                    }
                }
                
                val task = ScheduleTask(
                    id = if (isEdit) existingTask!!.id else 0,
                    name = taskName,
                    audioPath = selectedAudioPath,
                    audioName = selectedAudioName,
                    enabled = enabledSwitch.isChecked,
                    startTimeHour = selectedStartTime.first,
                    startTimeMinute = selectedStartTime.second,
                    endTimeHour = selectedEndTime.first,
                    endTimeMinute = selectedEndTime.second,
                    repeatType = repeatType,
                    specificDate = if (repeatType == 2) selectedSpecificDate else null,
                    workDays = workDays,
                    loopPlayback = loopPlaybackSwitch.isChecked
                )
                
                saveTask(task)
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun saveTask(task: ScheduleTask) {
        lifecycleScope.launch {
            if (task.id == 0L) {
                database.scheduleTaskDao().insertTask(task)
            } else {
                database.scheduleTaskDao().updateTask(task)
            }
            loadTasks()
            scheduleAlarms()
        }
    }
    
    private fun toggleTask(task: ScheduleTask) {
        lifecycleScope.launch {
            val updatedTask = task.copy(enabled = !task.enabled)
            database.scheduleTaskDao().updateTask(updatedTask)
            loadTasks()
            scheduleAlarms()
        }
    }
    
    private fun deleteTask(task: ScheduleTask) {
        AlertDialog.Builder(this)
            .setTitle("删除任务")
            .setMessage("确定要删除任务「${task.name}」吗?")
            .setPositiveButton("删除") { _, _ ->
                lifecycleScope.launch {
                    database.scheduleTaskDao().deleteTask(task)
                    alarmManager.cancelScheduleAlarm(task.id)
                    loadTasks()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun showSmsSettingsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_sms_settings, null)
        val phoneInput = dialogView.findViewById<android.widget.EditText>(R.id.etControlNumber)
        val startCommandInput = dialogView.findViewById<android.widget.EditText>(R.id.etStartCommand)
        val stopCommandInput = dialogView.findViewById<android.widget.EditText>(R.id.etStopCommand)
        
        phoneInput.setText(smsSettings.controlNumber)
        startCommandInput.setText(smsSettings.startCommand)
        stopCommandInput.setText(smsSettings.stopCommand)
        
        AlertDialog.Builder(this)
            .setTitle("短信控制设置")
            .setView(dialogView)
            .setPositiveButton("保存") { _, _ ->
                smsSettings.controlNumber = phoneInput.text.toString()
                smsSettings.startCommand = startCommandInput.text.toString()
                smsSettings.stopCommand = stopCommandInput.text.toString()
                Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 「立即播放 + 定时停止」对话框
     */
    private fun showPlayNowDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_play_now, null)

        val audioText = dialogView.findViewById<android.widget.TextView>(R.id.tvPlayNowAudio)
        val btnSelect = dialogView.findViewById<android.widget.Button>(R.id.btnPlayNowSelectAudio)
        val stopMinutesInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etStopMinutes)
        val loopSwitch = dialogView.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switchPlayNowLoop)

        var selectedAudioPath = ""
        var selectedAudioName = getString(R.string.no_audio_selected)

        btnSelect.setOnClickListener {
            pendingAudioCallback = { path, name ->
                selectedAudioPath = path
                selectedAudioName = name
                audioText.text = name
                pendingAudioCallback = null
            }
            selectAudioFile()
        }

        AlertDialog.Builder(this)
            .setTitle("立即播放")
            .setView(dialogView)
            .setPositiveButton("开始播放") { _, _ ->
                if (selectedAudioPath.isBlank()) {
                    Toast.makeText(this, "请先选择音频文件", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val stopMinutes = stopMinutesInput.text.toString().toIntOrNull() ?: 0
                val loop = loopSwitch.isChecked

                // 立即启动播放服务
                com.scheduledaudioplayer.service.AudioPlayService.startPlay(
                    this, selectedAudioPath, selectedAudioName, loop
                )

                // 设置定时停止闹钟（0 分钟表示不自动停止）
                val alarmMgr = AlarmManagerWrapper(this)
                alarmMgr.cancelImmediateStopAlarm() // 取消旧的（如果有）
                if (stopMinutes > 0) {
                    alarmMgr.scheduleImmediateStopAlarm(stopMinutes)
                    Toast.makeText(this, "已开始播放，将在 $stopMinutes 分钟后停止", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "已开始播放", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
}
