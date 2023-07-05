package hu.vandreas73.silencescheduler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder

class NotificationSender(
    val context: Context,
    chanelName: String,
    val chanelDescription: String? = null
) {
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val chanelId = chanelName
    val channel = NotificationChannel(chanelId, chanelName, importance).apply {
        description = chanelDescription
    }

    init {
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }


    fun sendNotification(id: Int, title: String, content: String? = null) {
        val resultIntent = Intent(context, MainActivity::class.java)
        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(resultIntent)
            getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        val builder = NotificationCompat.Builder(context, chanelId)
            .setSmallIcon(R.drawable.ic_baseline_volume_mute_24)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(resultPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(id, builder.build())
        }
    }
}