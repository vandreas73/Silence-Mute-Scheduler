package hu.vandreas73.muteuntil.ui

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import hu.vandreas73.muteuntil.AppGraph
import hu.vandreas73.muteuntil.MuteController
import hu.vandreas73.muteuntil.R
import hu.vandreas73.muteuntil.TargetTime
import hu.vandreas73.muteuntil.permissions.MandatoryAccess
import hu.vandreas73.muteuntil.permissions.OptionalAccess
import hu.vandreas73.muteuntil.permissions.rememberAccessGateState
import kotlinx.coroutines.launch
import java.time.LocalTime

/** The one screen of the app. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuteUntilScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val use24Hour = remember(context) { DateFormat.is24HourFormat(context) }
    val store = remember(context) { AppGraph.store(context) }
    val muteUntilMillis by store.muteUntil.collectAsState()
    val gate = rememberAccessGateState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val policyMessage = stringResource(R.string.error_no_policy_access)
    val alarmMessage = stringResource(R.string.error_no_exact_alarm)

    val initialTime = remember { LocalTime.now() }
    val pickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = use24Hour,
    )

    // Reads the picked hour and minute, so the label below recomposes as the user
    // turns the dial. "Now" comes fresh on every call, so a press never uses a
    // stale day.
    val resolveTarget = {
        TargetTime.resolveTarget(pickerState.hour, pickerState.minute, System.currentTimeMillis())
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            StatusCard(muteUntilMillis = muteUntilMillis, use24Hour = use24Hour)

            // Blocking problems only. Advice lives below the buttons.
            MandatoryAccess(state = gate, modifier = Modifier.fillMaxWidth())

            TimePicker(state = pickerState)

            Button(
                onClick = {
                    val result = AppGraph.controller(context).muteUntil(resolveTarget())
                    gate.refresh()
                    val message = when (result) {
                        MuteController.MuteResult.Ok -> null
                        MuteController.MuteResult.NoPolicyAccess -> policyMessage
                        MuteController.MuteResult.NoExactAlarm -> alarmMessage
                    }
                    if (message != null) {
                        scope.launch { snackbarHostState.showSnackbar(message) }
                    }
                },
                enabled = !gate.access.mandatoryMissing,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    stringResource(
                        R.string.action_mute_until,
                        TargetTime.formatClock(resolveTarget(), use24Hour),
                    )
                )
            }

            OutlinedButton(
                onClick = { AppGraph.controller(context).unmuteNow() },
                enabled = muteUntilMillis != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.action_unmute_now))
            }

            OptionalAccess(state = gate, modifier = Modifier.fillMaxWidth())
        }
    }
}

/** The status line. A mute is loud on screen, so the user never misses it. */
@Composable
private fun StatusCard(
    muteUntilMillis: Long?,
    use24Hour: Boolean,
    modifier: Modifier = Modifier,
) {
    val muted = muteUntilMillis != null
    val colors = if (muted) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    } else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Card(modifier = modifier.fillMaxWidth(), colors = colors) {
        Text(
            text = if (muteUntilMillis != null) {
                stringResource(
                    R.string.status_muted_until,
                    TargetTime.formatClock(muteUntilMillis, use24Hour),
                )
            } else {
                stringResource(R.string.status_not_muted)
            },
            style = if (muted) {
                MaterialTheme.typography.headlineMedium
            } else {
                MaterialTheme.typography.titleLarge
            },
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp),
        )
    }
}
