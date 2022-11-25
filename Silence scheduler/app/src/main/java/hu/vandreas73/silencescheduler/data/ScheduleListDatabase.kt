package hu.vandreas73.silencescheduler.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ScheduleItem::class], version = 1)
abstract class ScheduleListDatabase : RoomDatabase() {
    abstract fun shoppingItemDao(): ScheduleItemDao

    companion object {
        fun getDatabase(applicationContext: Context): ScheduleListDatabase {
            return Room.databaseBuilder(
                applicationContext,
                ScheduleListDatabase::class.java,
                "schedule-list.db"
            )
                .allowMainThreadQueries()
                .build()
        }
    }
}