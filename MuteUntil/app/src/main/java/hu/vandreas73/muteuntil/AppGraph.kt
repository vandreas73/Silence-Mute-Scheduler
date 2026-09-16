package hu.vandreas73.muteuntil

import android.content.Context

/**
 * Manual singletons. The store is created once per process, because the UI
 * collects its StateFlow.
 */
object AppGraph {

    @Volatile
    private var storeInstance: MuteStateStore? = null

    @Volatile
    private var controllerInstance: MuteController? = null

    fun store(context: Context): MuteStateStore =
        storeInstance ?: synchronized(this) {
            storeInstance ?: MuteStateStore(context.applicationContext).also { storeInstance = it }
        }

    fun controller(context: Context): MuteController =
        controllerInstance ?: synchronized(this) {
            controllerInstance ?: create(context.applicationContext).also { controllerInstance = it }
        }

    private fun create(app: Context) = MuteController(
        context = app,
        store = store(app),
        alarms = AlarmScheduler(app),
        notifications = MuteNotification(app),
    )
}
