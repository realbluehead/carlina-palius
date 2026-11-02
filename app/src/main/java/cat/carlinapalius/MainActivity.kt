package cat.carlinapalius

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import cat.carlinapalius.data.FamilyPrefs
import cat.carlinapalius.data.PantryRepository
import cat.carlinapalius.data.TodoRepository
import cat.carlinapalius.data.Updater
import cat.carlinapalius.ui.CarlinaTheme
import cat.carlinapalius.ui.FirebaseMissingScreen
import cat.carlinapalius.ui.MenuScreen
import cat.carlinapalius.ui.PantryScreen
import cat.carlinapalius.ui.SetupScreen
import cat.carlinapalius.ui.TodoScreen
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.launch

private enum class Screen { Menu, Todos, Pantry }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = FamilyPrefs(this)
        val firebaseReady = FirebaseApp.getApps(this).isNotEmpty()

        setContent {
            CarlinaTheme {
                var familyCode by remember { mutableStateOf(prefs.familyCode) }
                var memberName by remember { mutableStateOf(prefs.memberName) }
                var screen by rememberSaveable { mutableStateOf(Screen.Menu) }
                val code = familyCode

                when {
                    !firebaseReady -> FirebaseMissingScreen()

                    code == null -> SetupScreen(initialName = memberName) { name, newCode ->
                        prefs.memberName = name
                        prefs.familyCode = newCode
                        memberName = name
                        familyCode = newCode
                    }

                    screen == Screen.Todos -> {
                        BackHandler { screen = Screen.Menu }
                        val repository = remember(code) { TodoRepository(code) }
                        TodoScreen(repository, memberName, onBack = { screen = Screen.Menu })
                    }

                    screen == Screen.Pantry -> {
                        BackHandler { screen = Screen.Menu }
                        val repository = remember(code) { PantryRepository(code) }
                        PantryScreen(repository, onBack = { screen = Screen.Menu })
                    }

                    else -> MenuScreen(
                        memberName = memberName,
                        familyCode = code,
                        versionName = Updater.installedVersionName(this),
                        onCheckUpdates = {
                            lifecycleScope.launch { Updater.checkAndInstall(this@MainActivity, manual = true) }
                        },
                        onOpenTodos = { screen = Screen.Todos },
                        onOpenPantry = { screen = Screen.Pantry },
                        onShareCode = { shareCode(code) },
                        onLeaveFamily = {
                            prefs.familyCode = null
                            familyCode = null
                        },
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Cada cop que s'obre l'app mira si hi ha una versió nova a GitHub.
        lifecycleScope.launch { Updater.checkAndInstall(this@MainActivity) }
    }

    private fun shareCode(code: String) {
        val text = "Uneix-te a la nostra família a Carlina Palius amb el codi ${FamilyPrefs.pretty(code)}"
        val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(send, "Comparteix el codi"))
    }
}
