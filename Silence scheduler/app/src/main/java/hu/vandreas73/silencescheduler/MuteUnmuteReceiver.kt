package hu.vandreas73.silencescheduler

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.util.Log
import androidx.core.content.getSystemService
import androidx.core.os.UserManagerCompat
import java.util.*


class MuteUnmuteReceiver : BroadcastReceiver() {
    private lateinit var audioManager: AudioManager
    private lateinit var context: Context
    private lateinit var intent: Intent

    override fun onReceive(context: Context, intent: Intent) {
        this.context = context
        this.intent = intent
        val action = intent.action
        Log.i("mylog", "Received action: $action, user unlocked: " + UserManagerCompat.isUserUnlocked(context))

        val notificationManager =
            context.getSystemService<NotificationManager>() as NotificationManager
        if (notificationManager.isNotificationPolicyAccessGranted) {
            audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (intent.action.equals("hu.vandreas73.silencescheduler.unmute")){
                handleUnmute()
            }
            else if (intent.action.equals("hu.vandreas73.silencescheduler.mute")){
                handleMute()
            }
        }
    }

    private fun handleUnmute(){
        val notificationSender =
            NotificationSender(context, context.getString(R.string.unmuted_chanel_name), context.getString(R.string.unmuted_chanel_description))
        notificationSender.sendNotification(0, context.getString(R.string.unmuted_notification_title))
        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
        val cal = calendarIfWeeklyRepeating()
        val alarmSetter = hu.vandreas73.silencescheduler.AlarmSetter(context)
        if (cal != null){
            alarmSetter.setUnmuteAlarm(cal)
        } else {
            alarmSetter.invalidateAlarm()
        }

    }

    private fun handleMute(){
        audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
        val cal = calendarIfWeeklyRepeating()
        if (cal != null) {
            val alarmSetter = hu.vandreas73.silencescheduler.AlarmSetter(context)
            alarmSetter.setMuteAlarm(cal)
        }
    }

    private fun calendarIfWeeklyRepeating(): Calendar? {
        val weeklyRepeating = intent.extras?.getBoolean("weeklyRepeating", false)
        if (weeklyRepeating == true) {
            val c = Calendar.getInstance()
            c.set(Calendar.DAY_OF_YEAR, intent.extras!!.getInt("dayOfYear"))
            c.set(Calendar.HOUR_OF_DAY, intent.extras!!.getInt("hourOfDay"))
            c.set(Calendar.MINUTE, intent.extras!!.getInt("minute"))
            c.add(Calendar.WEEK_OF_YEAR, 1)
            return c
        }
        return null
    }
}