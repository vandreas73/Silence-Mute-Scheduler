package hu.vandreas73.silencescheduler.accessManager

import android.content.Context
import android.content.Intent
import hu.vandreas73.silencescheduler.R

class DNDAccessManager(context: Context) : AccessManager(context) {
    override fun getPreferenceName(): String {
        return "DNDAccess"
    }

    override fun getIntent(): Intent {
        return Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
    }

    override fun alertTitle(): String {
        return context.getString(R.string.dnd_access)
    }

    override fun alertMessage(): String {
        return String.format(
            context.getString(R.string.dnd_permission_request_message),
        )
    }

    override fun alertPositiveButtonText(): String {
        return context.getString(R.string.dnd_positive_btn_text)
    }

    override fun isManditorary(): Boolean {
        return true
    }
}