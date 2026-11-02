package ru.itmo.tuman

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ru.itmo.tuman.core.navigation.TumanNavHost
import ru.itmo.tuman.core.uikit.theme.TumanTheme

/** Единственная Activity приложения: внутри — тема и граф навигации Compose. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TumanTheme {
                TumanNavHost()
            }
        }
    }
}
