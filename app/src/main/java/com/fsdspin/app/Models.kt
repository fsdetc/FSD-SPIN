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

/** A full visual style for the wheel: rim, hub, segment palette, background. */
data class WheelTemplate(
    val name: String,
    val rim: Color,
    val hub: Color,
    val pointer: Color,
    val background: Color,
    val palette: List<Color>
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
