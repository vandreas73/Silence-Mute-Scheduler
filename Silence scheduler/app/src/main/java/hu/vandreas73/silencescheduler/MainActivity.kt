package hu.vandreas73.silencescheduler

import android.content.*
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import hu.vandreas73.silencescheduler.accessManager.AllAccessManager
import hu.vandreas73.silencescheduler.adapter.ScheduleAdapter
import hu.vandreas73.silencescheduler.data.ScheduleItem
import hu.vandreas73.silencescheduler.data.ScheduleListDatabase
import hu.vandreas73.silencescheduler.databinding.ActivityMainBinding
import hu.vandreas73.silencescheduler.ui.dialogs.NewScheduleItemDialogFragment
import hu.vandreas73.silencescheduler.ui.MuteNowFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity(), MuteNowFragment.MainListener,
    NewScheduleItemDialogFragment.NewScheduleItemDialogListener {

    private lateinit var binding: ActivityMainBinding

    private lateinit var database: ScheduleListDatabase
    private lateinit var alarmSetter: hu.vandreas73.silencescheduler.AlarmSetter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)

        database = ScheduleListDatabase.getDatabase(applicationContext)
    }

    override fun mute(hour: Int, minute: Int) {
        Log.d("mylog", "MainActivity mute")
        val allAccessManager = AllAccessManager(this)
        allAccessManager.checkAndRequestPermissions()
        if (allAccessManager.arePermissionsGranted()) {
            alarmSetter = hu.vandreas73.silencescheduler.AlarmSetter(this)
            val alarmCalendar = nextDayCalculate(hour, minute)
            alarmSetter.saveData(alarmCalendar)
            alarmSetter.setAlarmFromSavedData()

            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT

            alarmSetter.disableEnableBootReceiver(this)

            val br: BroadcastReceiver = MuteUnmuteReceiver()
            val filter = IntentFilter("hu.vandreas73.silencescheduler.mute")
            filter.addAction("hu.vandreas73.silencescheduler.unmute")
            ContextCompat.registerReceiver(this, br, filter, ContextCompat.RECEIVER_NOT_EXPORTED)

            val notificationSender =
                NotificationSender(this, "Muted", "Notifys when app mutes phone")
            val content = "Phone will be muted until ${String.format("%02d", alarmCalendar.get(Calendar.HOUR_OF_DAY))}:${
                String.format("%02d", alarmCalendar.get(Calendar.MINUTE))
            }"
            notificationSender.sendNotification(0, "Muted", content)
        }
    }

    private fun nextDayCalculate(hour: Int, minute: Int): Calendar {
        val alarmCalendar = Calendar.getInstance()
        alarmCalendar.set(Calendar.HOUR_OF_DAY, hour)
        alarmCalendar.set(Calendar.MINUTE, minute)
        alarmCalendar.set(Calendar.SECOND, 0)
        val rightNow = Calendar.getInstance()
        val day = rightNow.get(Calendar.DAY_OF_YEAR)
        if (rightNow.timeInMillis > alarmCalendar.timeInMillis)
        {
            alarmCalendar.set(Calendar.DAY_OF_YEAR, day + 1)
            Log.d("mylog", "AlarmSetter nextDay")
        }
        return alarmCalendar
    }


    override fun unmute() {
        if (AllAccessManager(this).checkAndRequestPermissions()) {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            hu.vandreas73.silencescheduler.AlarmSetter(this).cancelSavedAlarm()
        }
    }


    override fun onScheduleItemCreated(newItem: ScheduleItem) {
        thread {
            val insertId = database.shoppingItemDao().insert(newItem)
            newItem.id = insertId
            val calc = CalculatorNextWeekDay()
            val muteFrom = calc.getCalendar(
                newItem.muteFromDayOfWeek,
                newItem.muteFromHour,
                newItem.muteFromMin
            )
            val muteUntil = calc.getCalendar(
                newItem.muteUntilDayOfWeek,
                newItem.muteUntilHour,
                newItem.muteUntilMin
            )
            val aSetter = hu.vandreas73.silencescheduler.AlarmSetter(this)
            aSetter.setMuteAlarm(muteFrom)
            aSetter.setUnmuteAlarm(muteUntil)

            runOnUiThread {
                adapter.addItem(newItem)
            }
        }
    }

    override fun onScheduleItemChanged(newItem: ScheduleItem) {
        thread {
            database.shoppingItemDao().update(newItem)
            val items = database.shoppingItemDao().getAll()
            runOnUiThread {
                adapter.update(items)
            }
        }
    }

    companion object {
        private lateinit var adapter: ScheduleAdapter

        fun setAdapter(scheduleAdapter: ScheduleAdapter) {
            adapter = scheduleAdapter
        }
    }


}