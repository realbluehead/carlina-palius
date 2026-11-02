package cat.carlinapalius.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cat.carlinapalius.data.PantryItem
import cat.carlinapalius.data.PantryRepository
import kotlinx.coroutines.flow.catch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryScreen(
    repository: PantryRepository,
    onBack: () -> Unit,
) {
    var items by remember { mutableStateOf<List<PantryItem>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var newName by remember { mutableStateOf("") }
    var toDelete by remember { mutableStateOf<PantryItem?>(null) }

    LaunchedEffect(repository) {
        repository.observe()
            .catch { error = it.localizedMessage ?: "No s'ha pogut connectar" }
            .collect { items = it; error = null }
    }

    fun submit() {
        val name = newName.trim()
        if (name.isNotEmpty()) {
            repository.add(name)
            newName = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rebost") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Enrere")
                    }
                },
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        placeholder = { Text("Afegeix un producte…") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = { submit() }),
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { submit() }, enabled = newName.isNotBlank()) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Afegeix")
                    }
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            val list = items
            when {
                error != null && list == null -> PantryMessage("Error: $error")
                list == null -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                list.isEmpty() -> PantryMessage("El rebost és buit. Afegeix el primer producte!")
                else -> LazyColumn(Modifier.fillMaxSize()) {
                    items(list, key = { it.id }) { item ->
                        PantryRow(
                            item = item,
                            onMinus = { repository.change(item, -1) },
                            onPlus = { repository.change(item, +1) },
                            onDelete = { toDelete = item },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    toDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Esborrar «${item.name}»?") },
            text = { Text("Desapareixerà del rebost de tota la família.") },
            confirmButton = {
                TextButton(onClick = { repository.delete(item); toDelete = null }) { Text("Esborra") }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) { Text("Cancel·la") }
            },
        )
    }
}

@Composable
private fun PantryRow(item: PantryItem, onMinus: () -> Unit, onPlus: () -> Unit, onDelete: () -> Unit) {
    val empty = item.quantity == 0L
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(item.name, style = MaterialTheme.typography.bodyLarge)
            if (empty) {
                Text(
                    "S'ha acabat",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        FilledTonalIconButton(onClick = onMinus, enabled = !empty) {
            Icon(Icons.Default.Remove, contentDescription = "Treu-ne un")
        }
        Text(
            item.quantity.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = if (empty) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.widthIn(min = 36.dp),
        )
        FilledTonalIconButton(onClick = onPlus) {
            Icon(Icons.Default.Add, contentDescription = "Afegeix-ne un")
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Close, contentDescription = "Esborra")
        }
    }
}

@Composable
private fun PantryMessage(text: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
    }
}
