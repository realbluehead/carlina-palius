package cat.carlinapalius.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class PantryItem(
    val id: String,
    val name: String,
    val quantity: Long,
)

/** El rebost viu a Firestore a `families/{codi}/pantry`, un document per producte. */
class PantryRepository(familyCode: String) {
    private val pantry = FirebaseFirestore.getInstance()
        .collection("families").document(familyCode)
        .collection("pantry")

    fun observe(): Flow<List<PantryItem>> = callbackFlow {
        TodoRepository.ensureSignedIn()
        val registration = pantry.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val items = snapshot?.documents.orEmpty().map { doc ->
                PantryItem(
                    id = doc.id,
                    name = doc.getString("name").orEmpty(),
                    quantity = doc.getLong("quantity") ?: 0L,
                )
            }
            trySend(items.sortedBy { it.name.lowercase() })
        }
        awaitClose { registration.remove() }
    }

    fun add(name: String) {
        pantry.add(mapOf("name" to name, "quantity" to 1L))
    }

    /**
     * Suma o resta amb un increment atòmic: si dues persones toquen el botó alhora,
     * es compten tots dos tocs.
     */
    fun change(item: PantryItem, delta: Long) {
        if (item.quantity + delta < 0) return
        pantry.document(item.id).update("quantity", FieldValue.increment(delta))
    }

    fun delete(item: PantryItem) {
        pantry.document(item.id).delete()
    }
}
