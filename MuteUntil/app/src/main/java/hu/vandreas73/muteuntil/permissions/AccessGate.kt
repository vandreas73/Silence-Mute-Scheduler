package hu.vandreas73.muteuntil.permissions

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import hu.vandreas73.muteuntil.R

/** One access the app asks the user for. */
enum class Access {
    NotificationPolicy,
    ExactAlarm,
    PostNotifications,
    BatteryOptimisation,
    XiaomiAutostart,
}

/** What the app may do right now. */
data class AccessState(
    val notificationPolicy: Boolean,
    val exactAlarm: Boolean,
    val postNotifications: Boolean,
    val batteryOptimisation: Boolean,
    val xiaomiPhone: Boolean,
) {
    /**
     * True when the app cannot promise the unmute. An unmute we cannot guarantee
     * is worse than no mute, so the screen disables the mute button in this case.
     */
    val mandatoryMissing: Boolean get() = !notificationPolicy || !exactAlarm
}

/** Reads every access the app cares about. */
fun readAccessState(context: Context): AccessState {
    val notifications = context.getSystemService(NotificationManager::class.java)
    val alarms = context.getSystemService(AlarmManager::class.java)
    val power = context.getSystemService(PowerManager::class.java)
    return AccessState(
        notificationPolicy = notifications.isNotificationPolicyAccessGranted,
        exactAlarm = alarms.canScheduleExactAlarms(),
        postNotifications = context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED,
        batteryOptimisation = power.isIgnoringBatteryOptimizations(context.packageName),
        xiaomiPhone = Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true),
    )
}

/** Holds the last read [AccessState] and opens the matching system screens. */
@Stable
class AccessGateState internal constructor(private val context: Context) {

    private val prompts = PromptStore(context)

    var access: AccessState by mutableStateOf(readAccessState(context))
        private set

    /** True when the last [open] found no such screen on this phone. */
    var settingsUnavailable: Boolean by mutableStateOf(false)
        private set

    private var handled: Set<Access> by mutableStateOf(prompts.handled())

    /** True once the user has opened or dismissed the prompt for [access]. */
    fun isHandled(access: Access): Boolean = access in handled

    /** Records that the user dealt with [access], so the prompt never returns. */
    fun markHandled(access: Access) {
        prompts.markHandled(access)
        handled = handled + access
    }

    fun refresh() {
        access = readAccessState(context)
    }

    /**
     * Opens the system screen for [target]. The Xiaomi autostart activity is absent
     * on every other brand, so a failure sets [settingsUnavailable] instead of
     * throwing.
     *
     * A screen that opened counts as handled. The app cannot read the Xiaomi
     * autostart setting, so whether the user granted it there is unknowable, and
     * asking again every launch is worse than trusting them once.
     */
    fun open(target: Access) {
        val intent = intentFor(target, context.packageName)
        if (intent == null) {
            settingsUnavailable = true
            return
        }
        settingsUnavailable = try {
            context.startActivity(intent)
            markHandled(target)
            false
        } catch (_: ActivityNotFoundException) {
            true
        } catch (_: SecurityException) {
            true
        }
    }
}

private fun intentFor(target: Access, packageName: String): Intent? = when (target) {
    Access.NotificationPolicy ->
        Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)

    Access.ExactAlarm ->
        Intent(
            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
            Uri.fromParts("package", packageName, null),
        )

    Access.BatteryOptimisation ->
        Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)

    Access.XiaomiAutostart ->
        Intent().setClassName(
            "com.miui.securitycenter",
            "com.miui.permcenter.autostart.AutoStartManagementActivity",
        )

    // A runtime permission. The gate asks for it with a permission launcher.
    Access.PostNotifications -> null
}

/**
 * Creates the gate state and reads it again on every resume, so a return from a
 * settings screen updates the UI.
 */
@Composable
fun rememberAccessGateState(): AccessGateState {
    val context = LocalContext.current
    val state = remember(context) { AccessGateState(context) }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) state.refresh()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    return state
}

/**
 * The blocking banner. It appears only when the app cannot promise the unmute, and
 * it sits above the controls, because nothing below it works until it is cleared.
 */
@Composable
fun MandatoryAccess(state: AccessGateState, modifier: Modifier = Modifier) {
    val access = state.access
    if (!access.mandatoryMissing) return

    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AccessCard(
            title = stringResource(R.string.access_required_title),
            body = stringResource(R.string.access_required_body),
            mandatory = true,
        ) {
            if (!access.notificationPolicy) {
                AccessButton(
                    label = stringResource(R.string.access_policy_action),
                    onClick = { state.open(Access.NotificationPolicy) },
                )
            }
            if (!access.exactAlarm) {
                AccessButton(
                    label = stringResource(R.string.access_alarm_action),
                    onClick = { state.open(Access.ExactAlarm) },
                )
            }
        }
        UnavailableNote(state)
    }
}

/**
 * Advice the user can ignore. These cards sit BELOW the buttons, so they never push
 * the one action of the app off the screen.
 */
@Composable
fun OptionalAccess(state: AccessGateState, modifier: Modifier = Modifier) {
    val access = state.access

    val requestNotifications = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { state.refresh() }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (!access.postNotifications && !state.isHandled(Access.PostNotifications)) {
            AccessCard(
                title = stringResource(R.string.access_notifications_title),
                body = stringResource(R.string.access_notifications_body),
                onDismiss = { state.markHandled(Access.PostNotifications) },
            ) {
                AccessButton(
                    label = stringResource(R.string.access_notifications_action),
                    onClick = {
                        state.markHandled(Access.PostNotifications)
                        requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
                    },
                )
            }
        }

        if (!access.batteryOptimisation && !state.isHandled(Access.BatteryOptimisation)) {
            AccessCard(
                title = stringResource(R.string.access_battery_title),
                body = stringResource(R.string.access_battery_body),
                onDismiss = { state.markHandled(Access.BatteryOptimisation) },
            ) {
                AccessButton(
                    label = stringResource(R.string.access_battery_action),
                    onClick = { state.open(Access.BatteryOptimisation) },
                )
            }
        }

        // Xiaomi exposes no API for the autostart state, so the card hides once the
        // user has opened that screen or dismissed the card.
        if (access.xiaomiPhone && !state.isHandled(Access.XiaomiAutostart)) {
            AccessCard(
                title = stringResource(R.string.access_autostart_title),
                body = stringResource(R.string.access_autostart_body),
                onDismiss = { state.markHandled(Access.XiaomiAutostart) },
            ) {
                AccessButton(
                    label = stringResource(R.string.access_autostart_action),
                    onClick = { state.open(Access.XiaomiAutostart) },
                )
            }
        }

        if (!access.mandatoryMissing) UnavailableNote(state)
    }
}

/** Shown when this phone has no such settings screen, which Xiaomi intents can hit. */
@Composable
private fun UnavailableNote(state: AccessGateState) {
    if (!state.settingsUnavailable) return
    Text(
        text = stringResource(R.string.error_settings_unavailable),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
    )
}

/** One card with a title, an explanation and the buttons that fix the problem. */
@Composable
private fun AccessCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    mandatory: Boolean = false,
    onDismiss: (() -> Unit)? = null,
    actions: @Composable ColumnScope.() -> Unit,
) {
    val colors = if (mandatory) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        )
    } else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Card(modifier = modifier.fillMaxWidth(), colors = colors) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = body, style = MaterialTheme.typography.bodyMedium)
            actions()
            if (onDismiss != null) {
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text(stringResource(R.string.action_dismiss))
                }
            }
        }
    }
}

/** A full width button, so long labels never overflow on a narrow phone. */
@Composable
private fun AccessButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(label)
    }
}
