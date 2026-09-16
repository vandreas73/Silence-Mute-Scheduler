package hu.vandreas73.muteuntil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import hu.vandreas73.muteuntil.ui.MuteUntilScreen
import hu.vandreas73.muteuntil.ui.theme.MuteUntilTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MuteUntilTheme {
                MuteUntilScreen()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // The user can swipe the muted notification away. Opening the app brings
        // the Unmute now action back.
        AppGraph.controller(this).refreshNotification()
    }
}
