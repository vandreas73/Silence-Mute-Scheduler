package hu.vandreas73.silencescheduler.accessManager

import android.content.Context
import android.content.Intent
import hu.vandreas73.silencescheduler.R

class XiaomiAccessManager(context: Context) : AccessManager(context) {
    override fun getPreferenceName(): String {
        return "XiaomiPreference"
    }


    override fun getIntent(): Intent {
        val intent = Intent()
        intent.setClassName(
            "com.miui.securitycenter",
            "com.miui.permcenter.autostart.AutoStartManagementActivity"
        )
        return intent
    }

    override fun alertTitle(): String {
        return context.getString(R.string.xiaomi_access_alert_title)
    }

    override fun alertMessage(): String {
        return context.getString(R.string.xiaomi_access_alert_text)
    }

    override fun alertPositiveButtonText(): String {
        return context.getString(R.string.xiaomi_positive_btn_text)
    }

    override fun isManditorary(): Boolean {
        return false
    }
}