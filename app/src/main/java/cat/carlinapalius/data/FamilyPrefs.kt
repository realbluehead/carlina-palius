package cat.carlinapalius.data

import android.content.Context
import kotlin.random.Random

/** Guarda al mòbil a quina família pertany l'usuari i com es diu. */
class FamilyPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("carlina_palius", Context.MODE_PRIVATE)

    var familyCode: String?
        get() = prefs.getString(KEY_FAMILY, null)
        set(value) = prefs.edit().putString(KEY_FAMILY, value).apply()

    var memberName: String
        get() = prefs.getString(KEY_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_NAME, value).apply()

    companion object {
        private const val KEY_FAMILY = "family_code"
        private const val KEY_NAME = "member_name"

        // Sense lletres que es confonguin (O/0, I/1, L) per dictar-lo fàcilment.
        private const val ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"
        const val CODE_LENGTH = 8

        fun newCode(): String =
            (1..CODE_LENGTH).map { ALPHABET[Random.nextInt(ALPHABET.length)] }.joinToString("")

        fun normalize(input: String): String =
            input.uppercase().filter { it.isLetterOrDigit() }

        fun pretty(code: String): String =
            if (code.length == CODE_LENGTH) code.chunked(4).joinToString("-") else code
    }
}
