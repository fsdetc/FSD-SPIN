package com.fsdspin.app

import androidx.compose.ui.graphics.Color

/**
 * One slice of the wheel. [colorArgb] is stored as a Long so the
 * entry list survives recomposition and can be saved later.
 */
data class WheelEntry(
    val id: Long,
    val text: String,
    val colorArgb: Long
) {
    val color: Color get() = Color(colorArgb)
}

/** A full visual style for the wheel: rim (plate), hub, segment palette, background. */
data class WheelTemplate(
    val name: String,
    val rim: Color,
    val hub: Color,
    val pointer: Color,
    val background: Color,
    val palette: List<Color>,
    /** Thickness of the rim/plate as a fraction of the radius. Gold Plate is thicker. */
    val rimFraction: Float = 0.06f
)

/** Default dark-purple app background (dark purple, never plain black). */
val DARK_PURPLE_BACKGROUND = Color(0xFF2D0A4E)
val DARK_PURPLE_SURFACE = Color(0xFF1E0736)
val DEFAULT_TEXT_COLOR = Color(0xFFFFFFFF)

/** Background colours offered in Settings (dark purples first). */
val BACKGROUND_OPTIONS = listOf(
    Color(0xFF2D0A4E),
    Color(0xFF1E0736),
    Color(0xFF3B0F66),
    Color(0xFF4A148C),
    Color(0xFF12081F),
    Color(0xFF0F2027),
    Color(0xFF1A1A1A),
    Color(0xFF2E2E2E)
)

/** Text colours offered in Settings. */
val TEXT_COLOR_OPTIONS = listOf(
    Color(0xFFFFFFFF),
    Color(0xFFFFD700),
    Color(0xFF00E5FF),
    Color(0xFFFF80AB),
    Color(0xFFB2FF59),
    Color(0xFFE0E0E0)
)

/** Plate (rim) colours offered in Settings — overrides the template rim. */
val PLATE_COLOR_OPTIONS = listOf(
    Color(0xFFFFD700),
    Color(0xFFC0C0C0),
    Color(0xFF8E24AA),
    Color(0xFF212121),
    Color(0xFF00BCD4),
    Color(0xFFEC407A),
    Color(0xFF2E7D32),
    Color(0xFFFFFFFF)
)

val TEMPLATES: List<WheelTemplate> = listOf(
    WheelTemplate(
        name = "Classic",
        rim = Color(0xFF212121),
        hub = Color(0xFFFFFFFF),
        pointer = Color(0xFFFFC107),
        background = Color(0xFFF5F5F5),
        palette = listOf(
            Color(0xFFE53935), Color(0xFFFB8C00), Color(0xFFFFEB3B),
            Color(0xFF43A047), Color(0xFF1E88E5), Color(0xFF8E24AA),
            Color(0xFFEC407A), Color(0xFF00ACC1)
        )
    ),
    WheelTemplate(
        name = "Gold",
        rim = Color(0xFF8D6E00),
        hub = Color(0xFFFFF8E1),
        pointer = Color(0xFFFFD700),
        background = Color(0xFF1A1A1A),
        palette = listOf(
            Color(0xFFFFD700), Color(0xFFB8860B), Color(0xFFFFEC8B),
            Color(0xFFDAA520), Color(0xFFFFF2B2), Color(0xFF8C6D00),
            Color(0xFFFFC107), Color(0xFF6D4C00)
        )
    ),
    // Premium gold plate / rim style: thick gold rim, dark hub,
    // deep gold-and-black slices — the premium showpiece design.
    WheelTemplate(
        name = "Gold Plate",
        rim = Color(0xFFFFD700),
        rimFraction = 0.11f,
        hub = Color(0xFF1A1A1A),
        pointer = Color(0xFFFFEA00),
        background = Color(0xFF2D0A4E),
        palette = listOf(
            Color(0xFFFFD700), Color(0xFF1A1A1A), Color(0xFFFFEC8B),
            Color(0xFF3D3200), Color(0xFFFFC107), Color(0xFF2D2D2D),
            Color(0xFFFFE082), Color(0xFF5C4A00)
        )
    ),
    WheelTemplate(
        name = "Royal Purple",
        rim = Color(0xFFFFD700),
        hub = Color(0xFFFFFFFF),
        pointer = Color(0xFFE1BEE7),
        background = Color(0xFF2D0A4E),
        palette = listOf(
            Color(0xFF7B1FA2), Color(0xFFAB47BC), Color(0xFFCE93D8),
            Color(0xFF4A148C), Color(0xFFBA68C8), Color(0xFF6A1B9A),
            Color(0xFFE1BEE7), Color(0xFF9C27B0)
        )
    ),
    WheelTemplate(
        name = "Diamond",
        rim = Color(0xFF90A4AE),
        hub = Color(0xFFFFFFFF),
        pointer = Color(0xFF00E5FF),
        background = Color(0xFFE3F2FD),
        palette = listOf(
            Color(0xFFB2EBF2), Color(0xFFE1F5FE), Color(0xFF80DEEA),
            Color(0xFFFFFFFF), Color(0xFF4DD0E1), Color(0xFFB3E5FC),
            Color(0xFF00BCD4), Color(0xFFE0F7FA)
        )
    ),
    WheelTemplate(
        name = "Neon",
        rim = Color(0xFF12002E),
        hub = Color(0xFF1A0033),
        pointer = Color(0xFF00FFEA),
        background = Color(0xFF0D001A),
        palette = listOf(
            Color(0xFFFF00E5), Color(0xFF00E5FF), Color(0xFF7C4DFF),
            Color(0xFF00FF88), Color(0xFFFFEA00), Color(0xFFFF3D00),
            Color(0xFF2979FF), Color(0xFF76FF03)
        )
    ),
    WheelTemplate(
        name = "Candy",
        rim = Color(0xFFAD1457),
        hub = Color(0xFFFFFFFF),
        pointer = Color(0xFFFF80AB),
        background = Color(0xFFFCE4EC),
        palette = listOf(
            Color(0xFFF48FB1), Color(0xFFB2DFDB), Color(0xFFFFCCBC),
            Color(0xFFC5E1A5), Color(0xFFFFF59D), Color(0xFFB39DDB),
            Color(0xFF90CAF9), Color(0xFFFFAB91)
        )
    ),
    WheelTemplate(
        name = "Forest",
        rim = Color(0xFF1B3A1B),
        hub = Color(0xFFF1F8E9),
        pointer = Color(0xFFFFD54F),
        background = Color(0xFFE8F5E9),
        palette = listOf(
            Color(0xFF2E7D32), Color(0xFF66BB6A), Color(0xFFA5D6A7),
            Color(0xFF33691E), Color(0xFF81C784), Color(0xFF1B5E20),
            Color(0xFFC0CA33), Color(0xFF9CCC65)
        )
    )
)

/** Default entries shown on first launch. The user can edit them all. */
fun defaultEntries(template: WheelTemplate): List<WheelEntry> {
    val names = listOf(
        "Rahul", "Priya", "Amit", "Sneha",
        "Vikram", "Ananya", "Rohan", "Kavya"
    )
    return names.mapIndexed { index, name ->
        WheelEntry(
            id = index.toLong() + 1,
            text = name,
            colorArgb = template.palette[index % template.palette.size].value.toLong()
        )
    }
}

/** Which slice is under the top pointer for a given wheel rotation. */
fun pointerSegmentIndex(rotationDegrees: Float, entryCount: Int): Int {
    if (entryCount == 0) return 0
    val segment = 360f / entryCount
    // Pointer sits at the top of the wheel (-90 degrees in canvas terms).
    val normalized = ((270f - rotationDegrees) % 360f + 360f) % 360f
    return (normalized / segment).toInt().coerceIn(0, entryCount - 1)
}

/**
 * Final rotation so that [winnerIndex]'s slice centre lands exactly on the
 * top pointer, after [fullTurns] complete spins.
 */
fun targetRotationForWinner(
    currentRotation: Float,
    winnerIndex: Int,
    entryCount: Int,
    fullTurns: Int = 6
): Float {
    if (entryCount == 0) return currentRotation
    val segment = 360f / entryCount
    val winnerCentre = winnerIndex * segment + segment / 2f
    // Centre must equal 270 degrees (top) at rest.
    val wanted = ((270f - winnerCentre) % 360f + 360f) % 360f
    val current = ((currentRotation % 360f) + 360f) % 360f
    var delta = wanted - current
    if (delta < 0) delta += 360f
    return currentRotation + fullTurns * 360f + delta
}
