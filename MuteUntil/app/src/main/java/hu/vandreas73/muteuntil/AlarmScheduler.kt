package hu.vandreas73.muteuntil

import android.app.AlarmManager
import android.content.Context

/** Arms and cancels the single exact alarm that unmutes the phone. */
class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun canScheduleExact(): Boolean = alarmManager.canScheduleExactAlarms()

    fun schedule(triggerAtMillis: Long) {
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            unmuteIntent(),
        )
    }

    fun cancel() {
        alarmManager.cancel(unmuteIntent())
    }

    /** The same PendingIntent every time, so [cancel] matches what [schedule] armed. */
    private fun unmuteIntent() = UnmuteReceiver.pendingIntent(context, REQUEST_CODE)

    private companion object {
        const val REQUEST_CODE = 0
    }
}
