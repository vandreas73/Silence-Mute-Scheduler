package hu.vandreas73.muteuntil

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Re-arms the pending unmute after the device restarts. */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            -> AppGraph.controller(context).restoreAfterBoot()
        }
    }
}
