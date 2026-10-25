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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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
    onEntriesChange: (List<WheelEntry>) -> Unit
) {
    var newText by remember { mutableStateOf("") }
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("Wheel entries — tap a colour dot to change that slice colour.")
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
            Text("Tip: a wheel needs at least 2 entries to spin.")
        }
    }
}

/** Template picker: each template recolours the whole wheel at once. */
@Composable
fun TemplatePicker(
    current: WheelTemplate,
    onTemplateChange: (WheelTemplate) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Wheel designs — pick a template. More designs (Gold, Diamond and more) live here.")
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
                        if (template.name == current.name) "  ✓" else ""
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
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label)
            Text(description, color = Color.Gray)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
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
    onForcedChange: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = entries.firstOrNull { it.id == forcedId }?.text ?: "Random (no force)"
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Host control — force winner (optional)")
        Text(
            "When set, the wheel always lands on this entry. " +
                "Leave it on Random for a fair spin.",
            color = Color.Gray
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
