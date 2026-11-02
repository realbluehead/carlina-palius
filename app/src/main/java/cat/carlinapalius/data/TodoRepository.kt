package cat.carlinapalius.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class Todo(
    val id: String,
    val text: String,
    val done: Boolean,
    val addedBy: String,
    val createdAt: Long,
)

/**
 * La llista compartida viu a Firestore a `families/{codi}/todos`.
 * Firestore manté una còpia local, així que funciona sense connexió i
 * sincronitza sola quan torna la xarxa.
 */
class TodoRepository(familyCode: String) {
    private val todos = FirebaseFirestore.getInstance()
        .collection("families").document(familyCode)
        .collection("todos")

    fun observe(): Flow<List<Todo>> = callbackFlow {
        ensureSignedIn()
        val registration = todos.orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents.orEmpty().map { doc ->
                    Todo(
                        id = doc.id,
                        text = doc.getString("text").orEmpty(),
                        done = doc.getBoolean("done") ?: false,
                        addedBy = doc.getString("addedBy").orEmpty(),
                        createdAt = doc.getLong("createdAt") ?: 0L,
                    )
                }
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    fun add(text: String, addedBy: String) {
        todos.add(
            mapOf(
                "text" to text,
                "done" to false,
                "addedBy" to addedBy,
                "createdAt" to System.currentTimeMillis(),
            )
        )
    }

    fun setDone(todo: Todo, done: Boolean) {
        todos.document(todo.id).update("done", done)
    }

    fun delete(todo: Todo) {
        todos.document(todo.id).delete()
    }

    fun clearDone(items: List<Todo>) {
        val batch = FirebaseFirestore.getInstance().batch()
        items.filter { it.done }.forEach { batch.delete(todos.document(it.id)) }
        batch.commit()
    }

    companion object {
        /** Les regles de Firestore demanen un usuari; n'hi ha prou amb un d'anònim. */
        suspend fun ensureSignedIn() {
            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser == null) auth.signInAnonymously().await()
        }
    }
}
