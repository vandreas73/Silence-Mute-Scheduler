package hu.vandreas73.muteuntil.permissions

import android.content.Context

/**
 * Remembers which optional prompts the user has already dealt with.
 *
 * Xiaomi keeps the autostart setting private. No public API reads it, so "the user
 * has visited that screen" is the only signal this app can act on. Without a record
 * that survives a restart, the autostart card comes back on every launch, even
 * after the user grants autostart.
 */
internal class PromptStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun handled(): Set<Access> =
        Access.entries.filterTo(mutableSetOf()) { prefs.getBoolean(it.name, false) }

    fun markHandled(access: Access) {
        prefs.edit().putBoolean(access.name, true).apply()
    }

    private companion object {
        const val PREFS_NAME = "prompts"
    }
}
