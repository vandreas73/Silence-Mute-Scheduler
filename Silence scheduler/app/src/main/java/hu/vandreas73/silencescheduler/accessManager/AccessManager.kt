package hu.vandreas73.silencescheduler.accessManager

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatCheckBox
import hu.vandreas73.silencescheduler.R

// Source: https://stackoverflow.com/questions/38995469/how-to-get-miui-security-app-auto-start-permission-programmatically/43108234#43108234
abstract class AccessManager(val context: Context) {

    protected abstract fun getPreferenceName() : String
    protected abstract fun alertTitle() : String
    protected abstract fun alertMessage() : String
    protected abstract fun alertPositiveButtonText() : String
    protected abstract fun getIntent(): Intent
    protected abstract fun isManditorary() : Boolean




    fun specialPermissionAlert(){
        val settings = context.getSharedPreferences(getPreferenceName(), AppCompatActivity.MODE_PRIVATE)
        val saveIfSkip = "skipAccessMessage"
        val skipMessage = settings.getBoolean(saveIfSkip, false)
        if (isManditorary() || !skipMessage) {
            val editor = settings.edit()
            val intent = getIntent()
            if (isCallable(intent)) {
                val dontShowAgain = AppCompatCheckBox(context)
                dontShowAgain.text = context.getString(R.string.dontShowAgain)
                dontShowAgain.setOnCheckedChangeListener { buttonView, isChecked ->
                    editor.putBoolean(saveIfSkip, isChecked)
                    editor.apply()
                }
                val alertDialogBuilder = AlertDialog.Builder(context)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(alertTitle())
                    .setMessage(alertMessage())
                    .setPositiveButton(alertPositiveButtonText()) { dialog, which -> startIntent() }
                    .setNegativeButton(android.R.string.cancel, null)
                if (!isManditorary())
                    alertDialogBuilder.setNeutralButton(context.getString(R.string.granted)) { dialog, _ ->
                        editor.putBoolean(saveIfSkip, true)
                        editor.apply()
                    }
                alertDialogBuilder.show()
            } else {
                editor.putBoolean(saveIfSkip, true)
                editor.apply()
            }
        }
    }

    private fun startIntent(){
        try {
            val intent = getIntent()
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e("mylog", "Failed to launch AutoStart Screen ", e)
        } catch (e: Exception) {
            Log.e("mylog", "Failed to launch AutoStart Screen ", e)
        }
    }


    private fun isCallable(intent: Intent): Boolean {
        val list : List<android.content.pm.ResolveInfo>
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list = context.packageManager.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            )
        } else {
            list = context.packageManager.queryIntentActivities(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
        }
        return list.size > 0
    }
}