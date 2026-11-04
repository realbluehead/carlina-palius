package cat.carlinapalius.data

import java.time.DayOfWeek
import java.time.LocalDate

enum class Waste(val label: String, val color: Long) {
    Paper("Paper i cartró", 0xFF4A9BD1),
    Packaging("Envasos", 0xFFF2C500),
    Rest("Rebuig", 0xFFA59D90),
}

/** Calendari de recollida porta a porta del Sector Nord. */
object Recycling {
    private val sectorNord = mapOf(
        DayOfWeek.MONDAY to Waste.Paper,
        DayOfWeek.WEDNESDAY to Waste.Packaging,
        DayOfWeek.THURSDAY to Waste.Rest,
        DayOfWeek.SATURDAY to Waste.Packaging,
    )

    /** El residu que toca el [date], o null si aquell dia no hi ha recollida. */
    fun wasteOn(date: LocalDate): Waste? = sectorNord[date.dayOfWeek]
}
