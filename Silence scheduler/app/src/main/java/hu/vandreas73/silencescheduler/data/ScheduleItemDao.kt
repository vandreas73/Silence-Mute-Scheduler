package hu.vandreas73.silencescheduler.data

import androidx.room.*

@Dao
interface ScheduleItemDao {
    @Query("SELECT * FROM scheduleitem")
    fun getAll(): List<ScheduleItem>

    @Insert
    fun insert(scheduleItems: ScheduleItem): Long

    @Update
    fun update(scheduleItem: ScheduleItem)

    @Delete
    fun deleteItem(scheduleItem: ScheduleItem)
}