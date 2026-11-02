package cat.carlinapalius.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cat.carlinapalius.data.FamilyPrefs

/** Menú principal. Cada funcionalitat nova de l'app hi afegeix una entrada. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    memberName: String,
    familyCode: String,
    versionName: String,
    onCheckUpdates: () -> Unit,
    onOpenTodos: () -> Unit,
    onOpenPantry: () -> Unit,
    onShareCode: () -> Unit,
    onLeaveFamily: () -> Unit,
) {
    var showFamily by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Carlina Palius")
                        Text(
                            "versió $versionName",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onCheckUpdates) {
                        Icon(Icons.Default.SystemUpdate, contentDescription = "Busca actualitzacions")
                    }
                    IconButton(onClick = { showFamily = true }) {
                        Icon(Icons.Default.Group, contentDescription = "Família")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Hola, $memberName!",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            MenuEntry(
                icon = Icons.Default.Checklist,
                title = "Llista de tasques",
                subtitle = "Compartida amb tota la família",
                onClick = onOpenTodos,
            )
            MenuEntry(
                icon = Icons.Default.Kitchen,
                title = "Rebost",
                subtitle = "Què tenim a casa i quant en queda",
                onClick = onOpenPantry,
            )
        }
    }

    if (showFamily) {
        AlertDialog(
            onDismissRequest = { showFamily = false },
            title = { Text("La teva família") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Passa aquest codi als altres perquè s'hi uneixin:")
                    Text(
                        FamilyPrefs.pretty(familyCode),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    TextButton(onClick = { showFamily = false; onLeaveFamily() }) {
                        Text("Canvia de família")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFamily = false; onShareCode() }) { Text("Comparteix el codi") }
            },
            dismissButton = {
                TextButton(onClick = { showFamily = false }) { Text("Tanca") }
            },
        )
    }
}

@Composable
private fun MenuEntry(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}
