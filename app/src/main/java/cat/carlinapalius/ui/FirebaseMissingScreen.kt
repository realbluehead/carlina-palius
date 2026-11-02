package cat.carlinapalius.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Es mostra si l'APK s'ha compilat sense `app/google-services.json`. */
@Composable
fun FirebaseMissingScreen() {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Falta configurar la sincronització", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Aquesta versió s'ha compilat sense el fitxer google-services.json de Firebase. " +
                    "Segueix les instruccions del README, posa el fitxer a app/ i torna a compilar.",
            )
        }
    }
}
