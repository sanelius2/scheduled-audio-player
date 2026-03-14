package com.scheduledaudioplayer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.scheduledaudioplayer.R
import com.scheduledaudioplayer.data.ScheduleTask
import com.scheduledaudioplayer.databinding.ItemTaskBinding

class TaskAdapter(
    private val tasks: MutableList<ScheduleTask>,
    private val onTaskClick: (ScheduleTask) -> Unit,
    private val onTaskToggle: (ScheduleTask) -> Unit,
    private val onTaskDelete: (ScheduleTask) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {
    
    class TaskViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(task: ScheduleTask, onTaskClick: (ScheduleTask) -> Unit, onTaskToggle: (ScheduleTask) -> Unit, onTaskDelete: (ScheduleTask) -> Unit) {
            binding.tvTaskName.text = task.name
            binding.tvAudio.text = "音频: ${task.audioName}"
            
            val startTime = String.format("%02d:%02d", task.startTimeHour, task.startTimeMinute)
            val endTime = String.format("%02d:%02d", task.endTimeHour, task.endTimeMinute)
            binding.tvStartTime.text = "开始: $startTime"
            binding.tvEndTime.text = "结束: $endTime"
            
            val repeatTypeText = when (task.repeatType) {
                0 -> binding.root.context.getString(R.string.daily)
                1 -> {
                    val workDays = mutableListOf<String>()
                    if (task.workDays and 1 != 0) workDays.add("周日")
                    if (task.workDays and 2 != 0) workDays.add("周一")
                    if (task.workDays and 4 != 0) workDays.add("周二")
                    if (task.workDays and 8 != 0) workDays.add("周三")
                    if (task.workDays and 16 != 0) workDays.add("周四")
                    if (task.workDays and 32 != 0) workDays.add("周五")
                    if (task.workDays and 64 != 0) workDays.add("周六")
                    "工作日: ${workDays.joinToString(", ")}"
                }
                2 -> "日期: ${task.specificDate}"
                3 -> binding.root.context.getString(R.string.once)
                else -> "未知"
            }
            binding.tvRepeatType.text = repeatTypeText

            // 显示循环播放状态
            if (task.loopPlayback) {
                binding.tvLoopPlayback.visibility = android.view.View.VISIBLE
                binding.tvLoopPlayback.text = "循环播放"
            } else {
                binding.tvLoopPlayback.visibility = android.view.View.GONE
            }

            binding.switchEnabled.isChecked = task.enabled
            binding.switchEnabled.setOnCheckedChangeListener { _, _ ->
                onTaskToggle(task)
            }
            
            binding.btnDelete.setOnClickListener {
                onTaskDelete(task)
            }
            
            binding.root.setOnClickListener {
                onTaskClick(task)
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position], onTaskClick, onTaskToggle, onTaskDelete)
    }
    
    override fun getItemCount(): Int = tasks.size
}
