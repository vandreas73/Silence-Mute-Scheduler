# MuteUntil — design

Date: 2026-09-16
Status: approved

## Purpose

Mute the phone until a clock time you pick. At that time the phone unmutes by
itself. One screen, one job.

MuteUntil replaces the "Mute now" half of the older Silence Scheduler app. The
recurring mute feature is dropped on purpose.

## Decisions

| Topic | Decision |
|---|---|
| Name | MuteUntil |
| Package | `hu.vandreas73.muteuntil` |
| Location | `Silence-Mute-Scheduler/MuteUntil/` |
| What "mute" means | `AudioManager.RINGER_MODE_SILENT` |
| What "unmute" means | `AudioManager.RINGER_MODE_NORMAL`, always |
| Alarm API | `AlarmManager.setExactAndAllowWhileIdle` |
| Language | English only |
| Time format | Follows the system 12/24-hour setting. No in-app option. |
| minSdk | 36 (Android 16) |
| compileSdk / targetSdk | 37 |
| Toolchain | Gradle 9.7.1, AGP 9.4.0 (built-in Kotlin), Kotlin 2.4.20, Compose BOM 2026.09.00 |

AGP 9 supplies Kotlin itself. Do not add the `org.jetbrains.kotlin.android`
plugin. The build fails if you do.

## Behaviour

1. The screen shows a time picker and the current state.
2. You pick a time and press **Mute until hh:mm**. The ringer goes silent.
3. A notification appears: "Muted until hh:mm". It carries an **Unmute now**
   action. It is ongoing, so you cannot swipe it away.
4. At the chosen time the phone returns to normal and the notification clears.
5. **Unmute now**, in the app or in the notification, ends the mute early and
   cancels the alarm.
6. A time earlier than now means tomorrow.
7. A reboot does not lose the pending unmute.

## State

One value is the single source of truth:

```
muteUntilEpochMillis: Long?    // null means "not muted"
```

It lives in **device-protected** `SharedPreferences`, so `BootReceiver` can read
it on `LOCKED_BOOT_COMPLETED`, before the user unlocks the phone.

There is no second `isMuted` flag. Two flags can disagree; one value cannot.

## Components

```
hu.vandreas73.muteuntil
  MainActivity.kt            Compose host
  AppGraph.kt                manual singletons — one store, one controller
  MuteController.kt          the only code that touches the ringer
  MuteStateStore.kt          device-protected prefs, exposes StateFlow<Long?>
  TargetTime.kt              pure time maths, unit-tested
  AlarmScheduler.kt          wraps AlarmManager
  MuteNotification.kt        builds and cancels notifications
  UnmuteReceiver.kt          target of BOTH the alarm and the notification action
  BootReceiver.kt            re-arms after a restart
  permissions/AccessGate.kt  checks and requests the special accesses
  ui/MuteUntilScreen.kt      the one screen
  ui/theme/                  Material 3 theme, follows system dark mode
```

### Why one controller

The unmute path has three entry points: the alarm fires, the user taps the
notification action, the user presses the button. All three call
`MuteController.unmuteNow()`. The old app spread this logic across two classes
and they drifted apart. One function, three callers.

## Contracts

Every component is written against these exact signatures.

```kotlin
// TargetTime.kt  — pure, no Android imports
object TargetTime {
    /**
     * The next instant matching [hour]:[minute] that is strictly after [nowMillis].
     * If that time has already passed today, returns tomorrow's occurrence.
     */
    fun resolveTarget(
        hour: Int,
        minute: Int,
        nowMillis: Long,
        zone: java.time.ZoneId = java.time.ZoneId.systemDefault(),
    ): Long

    /** Formats [epochMillis] as "hh:mm" honouring the system 12/24-hour setting. */
    fun formatClock(epochMillis: Long, use24Hour: Boolean, zone: java.time.ZoneId = java.time.ZoneId.systemDefault()): String
}

// MuteStateStore.kt
class MuteStateStore(context: Context) {
    val muteUntil: kotlinx.coroutines.flow.StateFlow<Long?>
    fun get(): Long?
    fun set(epochMillis: Long)
    fun clear()
}

// AlarmScheduler.kt
class AlarmScheduler(context: Context) {
    fun canScheduleExact(): Boolean
    fun schedule(triggerAtMillis: Long)
    fun cancel()
}

// MuteNotification.kt
class MuteNotification(context: Context) {
    fun showMuted(untilMillis: Long)
    fun cancelMuted()
    /** Used when the ringer cannot be restored, so the user is never left silent without knowing. */
    fun showProblem(text: String)
}

// MuteController.kt
class MuteController(
    context: Context,
    store: MuteStateStore,
    alarms: AlarmScheduler,
    notifications: MuteNotification,
) {
    sealed interface MuteResult {
        data object Ok : MuteResult
        data object NoPolicyAccess : MuteResult
        data object NoExactAlarm : MuteResult
    }

    fun muteUntil(targetMillis: Long): MuteResult
    fun unmuteNow()
    /** Called on boot. Unmutes if the deadline passed while off, else re-arms. */
    fun restoreAfterBoot()
}

// AppGraph.kt
object AppGraph {
    fun store(context: Context): MuteStateStore
    fun controller(context: Context): MuteController
}
```

`UnmuteReceiver` handles action `hu.vandreas73.muteuntil.UNMUTE` and calls
`AppGraph.controller(context).unmuteNow()`. It is the target of the alarm
`PendingIntent` and of the notification action.

## Permissions

| Access | Why | Behaviour when missing |
|---|---|---|
| `ACCESS_NOTIFICATION_POLICY` | required to set the ringer silent | Mute disabled, banner links to settings |
| `SCHEDULE_EXACT_ALARM` | guarantees the unmute happens | Mute disabled, banner links to settings |
| `POST_NOTIFICATIONS` | the muted notification | mute still works, no notification |
| `RECEIVE_BOOT_COMPLETED` | survive a reboot | schedule is lost on reboot |
| Battery optimisation exemption | HyperOS defers alarms | prompt once, dismissible |
| Xiaomi autostart | HyperOS blocks the broadcast | prompt once, dismissible |

An unmute we cannot guarantee is worse than no mute at all. That is why the two
mandatory accesses disable the button rather than warn.

## Error handling

- Ringer cannot be restored, because policy access was revoked while muted:
  clear the stored state and post a problem notification. Never leave the phone
  silent with no explanation.
- The muted notification is `ongoing`, so the Unmute now action cannot be swiped
  away by accident.
- Picking the time that equals now rolls to tomorrow, because the comparison is
  strictly greater than.

## Testing

JVM unit tests cover `TargetTime.resolveTarget`:

- a time later today
- a time earlier today, which must roll to tomorrow
- a time exactly equal to now, which must roll to tomorrow
- midnight
- a daylight-saving transition

On-device checks on the emulator, driven by adb:

```bash
adb shell cmd notification allow_dnd hu.vandreas73.muteuntil
adb shell cmd appops set hu.vandreas73.muteuntil SCHEDULE_EXACT_ALARM allow
adb shell pm grant hu.vandreas73.muteuntil android.permission.POST_NOTIFICATIONS
adb shell dumpsys audio | grep "mode (internal)"     # NORMAL or SILENT
```

HyperOS autostart and its battery killer cannot be tested on an emulator. Those
stay a manual check on the real phone.

## Out of scope

Recurring schedules, Hungarian strings, an in-app 12/24-hour option, a
foreground service. The foreground service is the fallback if exact alarms prove
unreliable on HyperOS in real use.
