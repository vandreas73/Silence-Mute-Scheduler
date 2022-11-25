package hu.vandreas73.silencescheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.util.Log
import java.util.*

class AlarmSetter(val context: Context) {
    private var sharedPreferences: SharedPreferences
    private val deviceProtectedContext: Context

    init {
        deviceProtectedContext = context.createDeviceProtectedStorageContext()
        sharedPreferences =
            deviceProtectedContext.getSharedPreferences("alarmPreferences", Context.MODE_PRIVATE)
        Log.d("mylog", "AlarmSetter constructor")
    }

    var dayOfYearKey = "dayOfYear"
    var hourKey = "hour"
    var minuteKey = "minute"
    var validKey = "valid";
    fun getHour() = sharedPreferences.getInt(hourKey, 0)
    fun getMinute() = sharedPreferences.getInt(minuteKey, 0)
    fun getDayOfYear() = sharedPreferences.getInt(dayOfYearKey, 0)
    fun getValid() = sharedPreferences.getInt(validKey, 0)

    fun saveData(alarmCalendar: Calendar) {
        val editor = sharedPreferences.edit()
        editor.putInt(dayOfYearKey, alarmCalendar.get(Calendar.DAY_OF_YEAR))
        editor.putInt(hourKey, alarmCalendar.get(Calendar.HOUR_OF_DAY))
        editor.putInt(minuteKey, alarmCalendar.get(Calendar.MINUTE))
        editor.putInt(validKey, 1)
        editor.commit()
    }

    fun setAlarmFromSavedData() {
        val dayOfYear = getDayOfYear()
        val hour: Int = getHour()
        val minute: Int = getMinute()
        val valid: Int = getValid()
        if (valid == 1) {
            val alarmCalendar = Calendar.getInstance()
            alarmCalendar.set(Calendar.DAY_OF_YEAR, dayOfYear)
            alarmCalendar.set(Calendar.HOUR_OF_DAY, hour)
            alarmCalendar.set(Calendar.MINUTE, minute)
            alarmCalendar.set(Calendar.SECOND, 0)
            alarmCalendar.set(Calendar.MILLISECOND, 0)

            setUnmuteAlarm(alarmCalendar)
        }
    }

    fun setUnmuteAlarm(alarmCalendar: Calendar){
        val pendingIntent = createUnmuteIntent(alarmCalendar)
        val alarmTime = alarmCalendar.timeInMillis
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
    }

    fun setMuteAlarm(alarmCalendar: Calendar) {
        val pendingIntent = createMuteIntent(alarmCalendar)
        val alarmTime = alarmCalendar.timeInMillis
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
    }

    private fun createUnmuteIntent(alarmCalendar: Calendar): PendingIntent{
        val intent = Intent(context, hu.vandreas73.silencescheduler.MuteUnmuteReceiver::class.java)
        intent.action = "hu.vandreas73.silencescheduler.unmute"
        return createPendingIntent(alarmCalendar, intent)
    }

    private fun createMuteIntent(alarmCalendar: Calendar): PendingIntent {
        val intent = Intent(context, hu.vandreas73.silencescheduler.MuteUnmuteReceiver::class.java)
        intent.action = "hu.vandreas73.silencescheduler.mute"
        return createPendingIntent(alarmCalendar, intent)
    }

    private fun createPendingIntent(alarmCalendar: Calendar, intent: Intent): PendingIntent {
        intent.putExtra("weeklyRepeating", false)
        intent.putExtra("dayOfYear", alarmCalendar.get(Calendar.DAY_OF_YEAR))
        intent.putExtra("hourOfDay", alarmCalendar.get(Calendar.HOUR_OF_DAY))
        intent.putExtra("minute", alarmCalendar.get(Calendar.MINUTE))
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        disableEnableBootReceiver(context)
        return pendingIntent
    }

    fun cancelSavedAlarm(){
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, hu.vandreas73.silencescheduler.MuteUnmuteReceiver::class.java)
        intent.action = "hu.vandreas73.silencescheduler.unmute"
        intent.putExtra("weeklyRepeating", false)
        intent.putExtra("dayOfYear", getDayOfYear())
        intent.putExtra("hourOfDay", getHour())
        intent.putExtra("minute", getMinute())
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        try{
            alarmManager.cancel(pendingIntent)
        }
        catch(e: Exception){
            Log.e("mylog", e.toString())
        }
        val editor = sharedPreferences.edit()
        editor.putInt(validKey, 0)
        val ns = hu.vandreas73.silencescheduler.NotificationSender(
            context,
            "Mute cancelled",
            "Notifys when you press Unmute now button"
        )
        ns.sendNotification(0, "Mute cancelled")
    }


    fun cancelUnmuteAlarm(calendar: Calendar){
        val pendingIntent = createUnmuteIntent(calendar)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try{
            alarmManager.cancel(pendingIntent)
        }
        catch(e: Exception){
            Log.e("mylog", e.toString())
        }
    }

    fun cancelMuteAlarm(calendar: Calendar){
        val pendingIntent = createMuteIntent(calendar)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try{
            alarmManager.cancel(pendingIntent)
        }
        catch(e: Exception){
            Log.e("mylog", e.toString())
        }
    }

    fun disableEnableBootReceiver(context: Context) {
        val receiver = ComponentName(context, hu.vandreas73.silencescheduler.BootReceiver::class.java)
        context.packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
        )
        context.packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
        )
    }


}