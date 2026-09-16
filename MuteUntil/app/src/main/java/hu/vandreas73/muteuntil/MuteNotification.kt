package hu.vandreas73.muteuntil

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import androidx.core.app.NotificationCompat

/** Builds, posts and cancels the app notifications. */
class MuteNotification(private val context: Context) {

    private val manager = context.getSystemService(NotificationManager::class.java)

    init {
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
        )
        // A problem means the phone is STILL silent. That warning must not arrive
        // silently itself, so it gets its own high importance channel.
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_PROBLEM_ID,
                CHANNEL_PROBLEM_NAME,
                NotificationManager.IMPORTANCE_HIGH,
            )
        )
    }

    /** Posts the ongoing "Muted until hh:mm" notification with its Unmute now action. */
    fun showMuted(untilMillis: Long) {
        val clock = TargetTime.formatClock(untilMillis, DateFormat.is24HourFormat(context))
        val notification = builder()
            .setContentTitle(context.getString(R.string.notification_muted_title))
            .setContentText(context.getString(R.string.notification_muted_text, clock))
            .setOngoing(true)
            .addAction(
                R.drawable.ic_volume_mute,
                context.getString(R.string.action_unmute_now),
                UnmuteReceiver.pendingIntent(context, UNMUTE_ACTION_REQUEST_CODE),
            )
            .build()
        post(ID_MUTED, notification)
    }

    fun cancelMuted() {
        manager.cancel(ID_MUTED)
    }

    /** Used when the ringer cannot be restored, so the user is never left silent without knowing. */
    fun showProblem(text: String) {
        val notification = builder(CHANNEL_PROBLEM_ID)
            .setContentTitle(context.getString(R.string.notification_problem_title))
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .build()
        post(ID_PROBLEM, notification)
    }

    private fun builder(channelId: String = CHANNEL_ID) = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_volume_mute)
        .setContentIntent(openAppIntent())
        .setShowWhen(false)

    private fun openAppIntent(): PendingIntent = PendingIntent.getActivity(
        context,
        OPEN_APP_REQUEST_CODE,
        Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    /** A missing POST_NOTIFICATIONS permission must never break the mute itself. */
    private fun post(id: Int, notification: Notification) {
        try {
            manager.notify(id, notification)
        } catch (_: SecurityException) {
            // No notification permission. The mute still works.
        }
    }

    private companion object {
        const val CHANNEL_ID = "mute_state"
        const val CHANNEL_NAME = "Mute status"
        const val CHANNEL_PROBLEM_ID = "mute_problem"
        const val CHANNEL_PROBLEM_NAME = "Problems"
        const val ID_MUTED = 1
        const val ID_PROBLEM = 2
        const val UNMUTE_ACTION_REQUEST_CODE = 1
        const val OPEN_APP_REQUEST_CODE = 2
    }
}
