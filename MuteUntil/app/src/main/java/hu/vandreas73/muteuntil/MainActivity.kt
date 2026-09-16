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
}
