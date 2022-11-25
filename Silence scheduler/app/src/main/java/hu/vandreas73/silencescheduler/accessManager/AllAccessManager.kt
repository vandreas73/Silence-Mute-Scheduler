package hu.vandreas73.silencescheduler.accessManager

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService

class AllAccessManager(val context: Context) {
    fun checkAndRequestPermissions(): Boolean {
        val notificationManager =
            context.getSystemService<NotificationManager>() as NotificationManager
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            val dndAccessManager = DNDAccessManager(context)
            dndAccessManager.specialPermissionAlert()
        }

        if (Build.MANUFACTURER.toString().equals("Xiaomi")) {
            val xiaomiAccessManager = XiaomiAccessManager(context)
            xiaomiAccessManager.specialPermissionAlert()
        } else if (Build.MANUFACTURER.toString().equals("Huawei")) {
            val huaweiAccessManager = HuaweiAccessManager(context)
            huaweiAccessManager.specialPermissionAlert()
        }

        if (arePermissionsGranted()) return true
        return false;
    }

    fun arePermissionsGranted(): Boolean {
        val notificationManager =
            context.getSystemService<NotificationManager>() as NotificationManager
        if (notificationManager.isNotificationPolicyAccessGranted)
            return true
        return false
    }
}