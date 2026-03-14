package com.scheduledaudioplayer.data

import androidx.room.*

@Dao
interface ScheduleTaskDao {
    
    @Query("SELECT * FROM schedule_tasks ORDER BY id")
    suspend fun getAllTasks(): List<ScheduleTask>
    
    @Query("SELECT * FROM schedule_tasks WHERE enabled = 1")
    suspend fun getEnabledTasks(): List<ScheduleTask>
    
    @Query("SELECT * FROM schedule_tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): ScheduleTask?
    
    @Insert
    suspend fun insertTask(task: ScheduleTask): Long
    
    @Update
    suspend fun updateTask(task: ScheduleTask)
    
    @Delete
    suspend fun deleteTask(task: ScheduleTask)
    
    @Query("DELETE FROM schedule_tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Long)
}
