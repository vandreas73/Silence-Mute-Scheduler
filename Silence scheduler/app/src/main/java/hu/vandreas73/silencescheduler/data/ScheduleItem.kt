package hu.vandreas73.silencescheduler.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek

@Entity(tableName = "scheduleitem")
data class ScheduleItem(
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) var id: Long? = null,
    @ColumnInfo(name = "muteFromDay") var muteFromDayOfWeek: DayOfWeek,
    @ColumnInfo(name = "muteFromHour") var muteFromHour: Int,
    @ColumnInfo(name = "muteFromMin") var muteFromMin: Int,
    @ColumnInfo(name = "muteUntilDay") var muteUntilDayOfWeek: DayOfWeek,
    @ColumnInfo(name = "muteUntilHour") var muteUntilHour: Int,
    @ColumnInfo(name = "muteUntilMin") var muteUntilMin: Int,
)