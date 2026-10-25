package com.fsdspin.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.random.Random

private enum class AppScreen { Wheel, Edit, Designs, Settings }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                FsdSpinApp()
            }
        }
    }
}

@Composable
fun FsdSpinApp() {
    val context = LocalContext.current
    val sounds = remember { WheelSounds(context) }
    DisposableEffect(Unit) { onDispose { sounds.release() } }

    var screen by remember { mutableStateOf(AppScreen.Wheel) }
    var template by remember { mutableStateOf(TEMPLATES[0]) }
    var entries by remember { mutableStateOf(defaultEntries(template)) }
    var spinning by remember { mutableStateOf(false) }
    var resultEntry by remember { mutableStateOf<WheelEntry?>(null) }

    // Customisation settings.
    var soundEnabled by remember { mutableStateOf(true) }
    var hideDuringSpin by remember { mutableStateOf(true) }
    var hideAllControls by remember { mutableStateOf(false) }
    var eliminationMode by remember { mutableStateOf(false) }
    var forcedId by remember { mutableStateOf<Long?>(null) }

    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    sounds.enabled = soundEnabled

    // Tick sound: play once every time the pointer crosses a slice edge.
    var lastSegment by remember { mutableStateOf(-1) }
    LaunchedEffect(spinning) {
        if (!spinning) return@LaunchedEffect
        snapshotFlow { rotation.value }.collect { value ->
            val segment = pointerSegmentIndex(value, entries.size)
            if (segment != lastSegment) {
                lastSegment = segment
                sounds.playTick()
            }
        }
    }

    fun spin() {
        if (spinning || entries.size < 2) return
        scope.launch {
            val winnerIndex = forcedId?.let { id ->
                entries.indexOfFirst { it.id == id }.takeIf { it >= 0 }
            } ?: Random.nextInt(entries.size)
            val target = targetRotationForWinner(
                currentRotation = rotation.value,
                winnerIndex = winnerIndex,
                entryCount = entries.size,
                fullTurns = 6
            )
            spinning = true
            resultEntry = null
            rotation.animateTo(
                targetValue = target,
                animationSpec = tween(
                    durationMillis = 6000,
                    easing = FastOutSlowInEasing
                )
            )
            spinning = false
            resultEntry = entries[winnerIndex]
            sounds.playWin()
        }
    }

    fun dismissResult() {
        val winner = resultEntry
        resultEntry = null
        if (eliminationMode && winner != null && entries.size > 2) {
            entries = entries.filter { it.id != winner.id }
            if (forcedId == winner.id) forcedId = null
        }
    }

    val controlsVisible = !hideAllControls && !(hideDuringSpin && spinning)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(template.background)
    ) {
        when (screen) {
            AppScreen.Wheel -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    if (controlsVisible) {
                        Text(
                            "FSD SPIN",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { screen = AppScreen.Edit }) {
                                Text("Edit")
                            }
                            OutlinedButton(onClick = { screen = AppScreen.Designs }) {
                                Text("Designs")
                            }
                            OutlinedButton(onClick = { screen = AppScreen.Settings }) {
                                Text("Settings")
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                    Box(contentAlignment = Alignment.Center) {
                        SpinWheel(
                            entries = entries,
                            template = template,
                            rotationDegrees = rotation.value,
                            onTap = { spin() }
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    if (controlsVisible) {
                        Button(
                            onClick = { spin() },
                            enabled = !spinning && entries.size >= 2,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (spinning) "Spinning…" else "SPIN")
                        }
                        if (entries.size < 2) {
                            Text("Add at least 2 entries to spin.")
                        }
                    } else if (hideAllControls && !spinning) {
                        TextButton(onClick = { hideAllControls = false }) {
                            Text("Show controls")
                        }
                    }
                }
            }
            AppScreen.Edit -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        OutlinedButton(onClick = { screen = AppScreen.Wheel }) {
                            Text("← Back to wheel")
                        }
                    }
                    EntryEditor(entries = entries, onEntriesChange = { entries = it })
                }
            }
            AppScreen.Designs -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        OutlinedButton(onClick = { screen = AppScreen.Wheel }) {
                            Text("← Back to wheel")
                        }
                    }
                    TemplatePicker(
                        current = template,
                        onTemplateChange = { chosen ->
                            template = chosen
                            entries = entries.mapIndexed { index, entry ->
                                entry.copy(
                                    colorArgb = chosen.palette[
                                        index % chosen.palette.size
                                    ].value.toLong()
                                )
                            }
                        }
                    )
                }
            }
            AppScreen.Settings -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = { screen = AppScreen.Wheel }) {
                        Text("← Back to wheel")
                    }
                    SettingRow(
                        label = "Spin sound",
                        description = "Tick while spinning, jingle on win.",
                        checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it }
                    )
                    SettingRow(
                        label = "Hide buttons while spinning",
                        description = "Controls disappear during a spin.",
                        checked = hideDuringSpin,
                        onCheckedChange = { hideDuringSpin = it }
                    )
                    SettingRow(
                        label = "Hide all controls",
                        description = "Only the wheel shows. Tap the wheel to spin; use 'Show controls' to get them back.",
                        checked = hideAllControls,
                        onCheckedChange = { hideAllControls = it }
                    )
                    SettingRow(
                        label = "Elimination mode",
                        description = "The winner is removed after each spin, so the next spin picks someone else.",
                        checked = eliminationMode,
                        onCheckedChange = { eliminationMode = it }
                    )
                    ForceWinnerPicker(
                        entries = entries,
                        forcedId = forcedId,
                        onForcedChange = { forcedId = it }
                    )
                }
            }
        }

        // Winner result overlay.
        resultEntry?.let { winner ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(modifier = Modifier.padding(32.dp)) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Winner!", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            winner.text,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        if (eliminationMode) {
                            Spacer(Modifier.height(8.dp))
                            Text("Elimination mode: this entry will be removed.")
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { dismissResult() }) { Text("OK") }
                    }
                }
            }
        }
    }
}
