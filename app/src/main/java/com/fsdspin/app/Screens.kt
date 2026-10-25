package com.fsdspin.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Palette offered when the user recolours a single slice. */
private val PICKER_COLORS = listOf(
    Color(0xFFE53935), Color(0xFFFB8C00), Color(0xFFFFEB3B), Color(0xFF43A047),
    Color(0xFF1E88E5), Color(0xFF8E24AA), Color(0xFFEC407A), Color(0xFF00ACC1),
    Color(0xFF212121), Color(0xFFFFFFFF), Color(0xFFFFD700), Color(0xFF00E5FF)
)

/**
 * Edit the wheel entries: rename, recolour, delete, add.
 * Every change is live — the wheel on the main screen updates at once.
 */
@Composable
fun EntryEditor(
    entries: List<WheelEntry>,
    onEntriesChange: (List<WheelEntry>) -> Unit,
    textColor: Color = DEFAULT_TEXT_COLOR,
    backgroundColor: Color = DARK_PURPLE_BACKGROUND
) {
    var newText by remember { mutableStateOf("") }
    LazyColumn(
        modifier = Modifier
            .background(backgroundColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                "Wheel entries — tap a colour dot to change that slice colour.",
                color = textColor
            )
            Spacer(Modifier.height(8.dp))
        }
        items(entries, key = { it.id }) { entry ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = entry.text,
                    onValueChange = { new ->
                        onEntriesChange(entries.map {
                            if (it.id == entry.id) it.copy(text = new) else it
                        })
                    },
                    label = { Text("Entry") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                // Colour dot: cycles through the picker palette on tap.
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(entry.color)
                        .border(1.dp, Color.Gray, CircleShape)
                        .clickable {
                            val current = PICKER_COLORS.indexOfFirst {
                                it.value == entry.color.value
                            }
                            val next = PICKER_COLORS[
                                (current + 1).mod(PICKER_COLORS.size)
                            ]
                            onEntriesChange(entries.map {
                                if (it.id == entry.id) {
                                    it.copy(colorArgb = next.value.toLong())
                                } else it
                            })
                        }
                )
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                    onClick = {
                        if (entries.size > 2) {
                            onEntriesChange(entries.filter { it.id != entry.id })
                        }
                    }
                ) { Text("✕") }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newText,
                    onValueChange = { newText = it },
                    label = { Text("Add new entry") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        val trimmed = newText.trim()
                        if (trimmed.isNotEmpty()) {
                            val nextId = (entries.maxOfOrNull { it.id } ?: 0) + 1
                            val colour = PICKER_COLORS[
                                entries.size % PICKER_COLORS.size
                            ]
                            onEntriesChange(
                                entries + WheelEntry(
                                    id = nextId,
                                    text = trimmed,
                                    colorArgb = colour.value.toLong()
                                )
                            )
                            newText = ""
                        }
                    }
                ) { Text("Add") }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Tip: a wheel needs at least 2 entries to spin.",
                color = textColor
            )
        }
    }
}

/** Template picker: each template recolours the whole wheel at once. */
@Composable
fun TemplatePicker(
    current: WheelTemplate,
    onTemplateChange: (WheelTemplate) -> Unit,
    textColor: Color = DEFAULT_TEXT_COLOR
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Wheel designs — pick a template, including the premium " +
                "Gold Plate style with a thick gold rim.",
            color = textColor
        )
        TEMPLATES.forEach { template ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTemplateChange(template) }
                    .padding(8.dp)
            ) {
                template.palette.take(5).forEach { colour ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(colour)
                            .border(1.dp, Color.Gray, CircleShape)
                    )
                    Spacer(Modifier.width(4.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    template.name +
                        if (template.name == current.name) "  ✓" else "",
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun SettingRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    textColor: Color = DEFAULT_TEXT_COLOR
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = textColor)
            Text(description, color = textColor.copy(alpha = 0.7f))
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/**
 * A row of colour dots for Settings customisation. [selected] may be
 * null (meaning "use the template default"); in that case no dot is
 * marked as selected.
 */
@Composable
fun ColorPickerRow(
    label: String,
    colors: List<Color>,
    selected: Color?,
    onColorChange: (Color) -> Unit,
    textColor: Color = DEFAULT_TEXT_COLOR
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, color = textColor, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            colors.forEach { colour ->
                val isSelected = selected != null && colour.value == selected.value
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colour)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) Color.White else Color.Gray,
                            shape = CircleShape
                        )
                        .clickable { onColorChange(colour) }
                )
            }
        }
    }
}

/**
 * Host-control dropdown for the optional forced winner.
 * Labelled honestly in the UI as a host control, so nobody mistakes
 * a forced result for a random one when the host uses it.
 */
@Composable
fun ForceWinnerPicker(
    entries: List<WheelEntry>,
    forcedId: Long?,
    onForcedChange: (Long?) -> Unit,
    textColor: Color = DEFAULT_TEXT_COLOR
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = entries.firstOrNull { it.id == forcedId }?.text ?: "Random (no force)"
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Host control — force winner (optional)", color = textColor)
        Text(
            "When set, the wheel always lands on this entry. " +
                "Leave it on Random for a fair spin.",
            color = textColor.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(4.dp))
        OutlinedButton(onClick = { expanded = true }) { Text(selected) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Random (no force)") },
                onClick = { onForcedChange(null); expanded = false }
            )
            entries.forEach { entry ->
                DropdownMenuItem(
                    text = { Text(entry.text) },
                    onClick = { onForcedChange(entry.id); expanded = false }
                )
            }
        }
    }
}

/**
 * About FSD ETC, taken from the official site fsdetc.web.app.
 * FSD SPIN will also be published on that site.
 */
@Composable
fun AboutSection(textColor: Color = DEFAULT_TEXT_COLOR) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("About FSD ETC", color = textColor, fontWeight = FontWeight.Bold)
        Text(
            "FSD ETC makes Android apps. One app is live today — FSD Studio, " +
                "a Minecraft Skin Studio for Android: find any player's skin, " +
                "pose it, render it, make it yours. More apps are on the way, " +
                "and FSD SPIN will also be published on fsdetc.web.app.",
            color = textColor
        )
        Text(
            "Our channel & community",
            color = textColor,
            fontWeight = FontWeight.Bold
        )
        Text(
            "• 315+ YouTube subscribers\n" +
                "• 80+ Discord community members\n" +
                "• Support for every FSD ETC app runs through our Discord " +
                "server — join via fsdetc.web.app.",
            color = textColor
        )
        Text(
            "FSD Studio — our live app",
            color = textColor,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Minecraft Skin Studio for Android: search any player's skin by " +
                "username or upload a skin file, create pose, studio and mob " +
                "renders, edit skins, and download your creations in high " +
                "quality. Version 2.2.1 · Android 7.0+ · about 5.7 MB · " +
                "free to start, no card needed.",
            color = textColor
        )
        Text(
            "Official site: fsdetc.web.app",
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Winner celebration as a DEDICATED FULL SCREEN — not a popup.
 * It replaces the whole screen, so no button behind it (Settings,
 * colour pickers, anything) can be tapped while it is shown.
 * The winner is celebrated with animation, confetti and vibration,
 * and leaves only through the Continue button.
 */
@Composable
fun WinnerCelebrationScreen(
    winner: WheelEntry,
    eliminationMode: Boolean,
    textColor: Color = DEFAULT_TEXT_COLOR,
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        shown = true
        vibrateOnce(context, 150L)
    }
    val scale by animateFloatAsState(
        targetValue = if (shown) 1f else 0.6f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "winnerScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(600),
        label = "winnerAlpha"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DARK_PURPLE_BACKGROUND),
        contentAlignment = Alignment.Center
    ) {
        ConfettiDots()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                "Winner!",
                color = Color(0xFFFFD700),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(alpha)
            )
            Text(
                winner.text,
                color = textColor,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .scale(scale)
                    .alpha(alpha)
            )
            Text(
                "Celebrating the spin — this entry takes the win!",
                color = textColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alpha)
            )
            if (eliminationMode) {
                Text(
                    "Elimination mode: this entry will be removed.",
                    color = textColor.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(alpha)
                )
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    vibrateOnce(context, 60L)
                    onContinue()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD700),
                    contentColor = Color(0xFF2D0A4E)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(alpha)
            ) {
                Text("Continue", fontWeight = FontWeight.Bold)
            }
        }
    }
}
