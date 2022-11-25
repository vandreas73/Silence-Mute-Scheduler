package hu.vandreas73.silencescheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.os.UserManagerCompat
import hu.vandreas73.silencescheduler.data.ScheduleListDatabase

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val action: String? = intent.getAction()
        Log.i(
            "mylog", "Received action: $action, user unlocked: " + UserManagerCompat
                .isUserUnlocked(context)
        )

        val alarmSetter = hu.vandreas73.silencescheduler.AlarmSetter(context)
        alarmSetter.setAlarmFromSavedData()
        if (Intent.ACTION_LOCKED_BOOT_COMPLETED != action) {
            val database = ScheduleListDatabase.getDatabase(context)
            val dao = database.shoppingItemDao()
            val scheduleItems = dao.getAll()
            val calc = CalculatorNextWeekDay()
            for (item in scheduleItems) {
                val muteFrom =
                    calc.getCalendar(item.muteFromDayOfWeek, item.muteFromHour, item.muteFromMin)
                alarmSetter.setMuteAlarm(muteFrom)
                val muteUntil =
                    calc.getCalendar(item.muteUntilDayOfWeek, item.muteUntilHour, item.muteUntilMin)
                alarmSetter.setUnmuteAlarm(muteUntil)
            }
        }

    }
}
