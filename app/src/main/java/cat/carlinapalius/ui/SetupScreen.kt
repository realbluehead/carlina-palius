package cat.carlinapalius.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import cat.carlinapalius.data.FamilyPrefs

/** Primera pantalla: el teu nom i a quina família t'uneixes. */
@Composable
fun SetupScreen(
    initialName: String,
    onDone: (name: String, familyCode: String) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var code by remember { mutableStateOf("") }
    val normalizedCode = FamilyPrefs.normalize(code)
    val nameOk = name.isNotBlank()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(24.dp))
            Text("Carlina Palius", style = MaterialTheme.typography.headlineLarge)
            Text(
                "Les coses de casa, compartides amb tota la família.",
                style = MaterialTheme.typography.bodyLarge,
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Com et dius?") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth(),
            )

            Text("Ets el primer de la família?", style = MaterialTheme.typography.titleMedium)
            Button(
                onClick = { onDone(name.trim(), FamilyPrefs.newCode()) },
                enabled = nameOk,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Crea una família nova") }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            Text("Algú ja t'ha passat un codi?", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Codi de família") },
                placeholder = { Text("ABCD-EFGH") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedButton(
                onClick = { onDone(name.trim(), normalizedCode) },
                enabled = nameOk && normalizedCode.length == FamilyPrefs.CODE_LENGTH,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Uneix-me a la família") }
        }
    }
}
