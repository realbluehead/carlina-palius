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
import cat.carlinapalius.data.FamilyPrefs
import cat.carlinapalius.data.TodoRepository
import cat.carlinapalius.ui.CarlinaTheme
import cat.carlinapalius.ui.FirebaseMissingScreen
import cat.carlinapalius.ui.MenuScreen
import cat.carlinapalius.ui.SetupScreen
import cat.carlinapalius.ui.TodoScreen
import com.google.firebase.FirebaseApp

private enum class Screen { Menu, Todos }

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

                    else -> MenuScreen(
                        memberName = memberName,
                        familyCode = code,
                        onOpenTodos = { screen = Screen.Todos },
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

    private fun shareCode(code: String) {
        val text = "Uneix-te a la nostra família a Carlina Palius amb el codi ${FamilyPrefs.pretty(code)}"
        val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(send, "Comparteix el codi"))
    }
}
