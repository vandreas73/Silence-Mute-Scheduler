package hu.vandreas73.muteuntil

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Target of both the scheduled alarm and the "Unmute now" notification action. */
class UnmuteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_UNMUTE) return
        AppGraph.controller(context).unmuteNow()
    }

    companion object {
        const val ACTION_UNMUTE = "hu.vandreas73.muteuntil.UNMUTE"

        /**
         * Builds the broadcast that unmutes the phone. Callers pass their own
         * [requestCode] so the alarm and the notification action keep separate
         * PendingIntents.
         */
        fun pendingIntent(context: Context, requestCode: Int): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                requestCode,
                Intent(context, UnmuteReceiver::class.java).setAction(ACTION_UNMUTE),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
    }
}
