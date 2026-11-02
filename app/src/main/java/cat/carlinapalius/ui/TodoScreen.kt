package cat.carlinapalius.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import cat.carlinapalius.data.Todo
import cat.carlinapalius.data.TodoRepository
import kotlinx.coroutines.flow.catch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    repository: TodoRepository,
    memberName: String,
    onBack: () -> Unit,
) {
    var todos by remember { mutableStateOf<List<Todo>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var newText by remember { mutableStateOf("") }

    LaunchedEffect(repository) {
        repository.observe()
            .catch { error = it.localizedMessage ?: "No s'ha pogut connectar" }
            .collect { todos = it; error = null }
    }

    fun submit() {
        val text = newText.trim()
        if (text.isNotEmpty()) {
            repository.add(text, memberName)
            newText = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Llista de tasques") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Enrere")
                    }
                },
                actions = {
                    if (todos.orEmpty().any { it.done }) {
                        IconButton(onClick = { repository.clearDone(todos.orEmpty()) }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Esborra les fetes")
                        }
                    }
                },
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = newText,
                        onValueChange = { newText = it },
                        placeholder = { Text("Afegeix una tasca…") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = { submit() }),
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { submit() }, enabled = newText.isNotBlank()) {
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
            val items = todos
            when {
                error != null && items == null -> CenteredText("Error: $error")
                items == null -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                items.isEmpty() -> CenteredText("No hi ha res pendent. Afegeix la primera tasca!")
                else -> LazyColumn(Modifier.fillMaxSize()) {
                    // Primer les pendents, després les fetes.
                    items(items.sortedBy { it.done }, key = { it.id }) { todo ->
                        TodoRow(
                            todo = todo,
                            onToggle = { repository.setDone(todo, it) },
                            onDelete = { repository.delete(todo) },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun TodoRow(todo: Todo, onToggle: (Boolean) -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = todo.done, onCheckedChange = onToggle)
        Column(Modifier.weight(1f)) {
            Text(
                todo.text,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (todo.done) TextDecoration.LineThrough else null,
                color = if (todo.done) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
            )
            if (todo.addedBy.isNotEmpty()) {
                Text(
                    todo.addedBy,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Close, contentDescription = "Esborra")
        }
    }
}

@Composable
private fun CenteredText(text: String) {
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
