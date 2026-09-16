package hu.vandreas73.muteuntil

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager

/** The only code in the app that touches the ringer. */
class MuteController(
    private val context: Context,
    private val store: MuteStateStore,
    private val alarms: AlarmScheduler,
    private val notifications: MuteNotification,
) {

    private val audio = context.getSystemService(AudioManager::class.java)
    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    sealed interface MuteResult {
        data object Ok : MuteResult
        data object NoPolicyAccess : MuteResult
        data object NoExactAlarm : MuteResult
    }

    /** Silences the ringer until [targetMillis], but only if the unmute is guaranteed. */
    fun muteUntil(targetMillis: Long): MuteResult {
        if (!notificationManager.isNotificationPolicyAccessGranted) return MuteResult.NoPolicyAccess
        if (!alarms.canScheduleExact()) return MuteResult.NoExactAlarm

        audio.ringerMode = AudioManager.RINGER_MODE_SILENT
        store.set(targetMillis)
        alarms.schedule(targetMillis)
        notifications.showMuted(targetMillis)
        return MuteResult.Ok
    }

    /** Ends the mute now. The alarm, the notification action and the button all call this. */
    fun unmuteNow() {
        val restored = restoreRinger()
        store.clear()
        alarms.cancel()
        notifications.cancelMuted()
        if (!restored) {
            notifications.showProblem(context.getString(R.string.problem_cannot_unmute))
        }
    }

    /** Called on boot. Unmutes if the deadline passed while off, else re-arms. */
    fun restoreAfterBoot() {
        val deadline = store.get() ?: return
        if (deadline <= System.currentTimeMillis()) {
            unmuteNow()
        } else {
            alarms.schedule(deadline)
            notifications.showMuted(deadline)
        }
    }

    /** Returns false when the policy access is gone and the ringer stays silent. */
    private fun restoreRinger(): Boolean {
        if (!notificationManager.isNotificationPolicyAccessGranted) return false
        return try {
            audio.ringerMode = AudioManager.RINGER_MODE_NORMAL
            true
        } catch (_: SecurityException) {
            false
        }
    }
}
