package hu.vandreas73.muteuntil

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val PREFS_NAME = "mute_state"
private const val KEY_MUTE_UNTIL = "mute_until_epoch_millis"

/**
 * The single source of truth: the instant the phone unmutes, or null when not muted.
 * Stored in device-protected storage, so BootReceiver reads it before the user unlocks.
 */
class MuteStateStore(context: Context) {

    private val prefs = context.createDeviceProtectedStorageContext()
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _muteUntil = MutableStateFlow(read())
    val muteUntil: StateFlow<Long?> = _muteUntil.asStateFlow()

    fun get(): Long? = _muteUntil.value

    fun set(epochMillis: Long) {
        // commit(): a reboot right after the mute must not lose the deadline, or the
        // phone stays silent with nothing left to unmute it.
        prefs.edit().putLong(KEY_MUTE_UNTIL, epochMillis).commit()
        _muteUntil.value = epochMillis
    }

    fun clear() {
        // apply(): a lost clear only re-arms an unmute that fires anyway.
        prefs.edit().remove(KEY_MUTE_UNTIL).apply()
        _muteUntil.value = null
    }

    private fun read(): Long? =
        if (prefs.contains(KEY_MUTE_UNTIL)) prefs.getLong(KEY_MUTE_UNTIL, 0L) else null
}
