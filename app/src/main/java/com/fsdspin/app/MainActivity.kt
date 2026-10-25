package com.fsdspin.app

import android.app.Activity
import android.content.Context
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.random.Random

private enum class AppScreen { Wheel, Edit, Designs, Settings }

private const val PREFS_NAME = "fsd_spin_prefs"
private const val KEY_SURVEY_DONE = "survey_done"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FsdSpinApp()
        }
    }
}

@Composable
fun FsdSpinApp() {
    val context = LocalContext.current
    val activity = context as? Activity
    val prefs = remember {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Opening animation (every launch) then, only on the very first
    // launch, the one-time survey. After that, straight to the wheel.
    var showSplash by remember { mutableStateOf(true) }
    var surveyDone by remember {
        mutableStateOf(prefs.getBoolean(KEY_SURVEY_DONE, false))
    }

    val sounds = remember { WheelSounds(context) }
    DisposableEffect(Unit) { onDispose { sounds.release() } }

    // Unity Ads: initialised once. Until a real Game ID is set in Ads.kt,
    // this is a safe no-op and no ad is ever shown.
    LaunchedEffect(Unit) { UnityAdsManager.initialize(context) }

    if (showSplash) {
        SplashScreen(onFinished = { showSplash = false })
        return
    }
    if (!surveyDone) {
        SurveyScreen(
            onComplete = { source, birthYear ->
                prefs.edit()
                    .putString("survey_source", source)
                    .putInt("survey_birth_year", birthYear)
                    .putBoolean(KEY_SURVEY_DONE, true)
                    .apply()
                surveyDone = true
            }
        )
        return
    }

    MainContent(sounds = sounds, activity = activity)
}

@Composable
private fun MainContent(sounds: WheelSounds, activity: Activity?) {
    val context = LocalContext.current
    var screen by remember { mutableStateOf(AppScreen.Wheel) }
    var template by remember { mutableStateOf(TEMPLATES[0]) }
    var entries by remember { mutableStateOf(defaultEntries(template)) }
    var spinning by remember { mutableStateOf(false) }
    var resultEntry by remember { mutableStateOf<WheelEntry?>(null) }
    var spinCount by remember { mutableStateOf(0) }

    // Customisation settings (defaults: dark purple, white text).
    var soundEnabled by remember { mutableStateOf(true) }
    var hideDuringSpin by remember { mutableStateOf(true) }
    var hideAllControls by remember { mutableStateOf(false) }
    var eliminationMode by remember { mutableStateOf(false) }
    var forcedId by remember { mutableStateOf<Long?>(null) }
    var backgroundColor by remember { mutableStateOf(DARK_PURPLE_BACKGROUND) }
    var textColor by remember { mutableStateOf(DEFAULT_TEXT_COLOR) }
    // Null = use the template's own rim; a colour = custom plate colour.
    var plateColor by remember { mutableStateOf<Color?>(null) }

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
            lastSegment = -1
            rotation.animateTo(
                targetValue = target,
                animationSpec = tween(
                    durationMillis = 6000,
                    easing = FastOutSlowInEasing
                )
            )
            spinning = false
            spinCount += 1
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
        // Ads no longer show here: since v1.2 they show on the SPIN
        // button click, before the spin starts (see below).
    }

    // SPIN button flow (free app, with ads): tapping SPIN first shows
    // an interstitial ad; the wheel spins as soon as the ad closes.
    // If no ad is ready, the spin starts at once — never blocked.
    // (Tapping the wheel itself spins directly, without an ad.)
    var adShowing by remember { mutableStateOf(false) }
    fun onSpinButtonClick() {
        if (spinning || adShowing || entries.size < 2) return
        val act = activity
        if (act != null && UnityAdsManager.adsReady) {
            adShowing = true
            UnityAdsManager.showInterstitial(act) {
                adShowing = false
                spin()
            }
        } else {
            spin()
        }
    }

    fun applyTemplate(chosen: WheelTemplate) {
        template = chosen
        entries = entries.mapIndexed { index, entry ->
            entry.copy(
                colorArgb = chosen.palette[
                    index % chosen.palette.size
                ].value.toLong()
            )
        }
    }

    val controlsVisible = !hideAllControls && !(hideDuringSpin && spinning)

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFFFD700),
            background = backgroundColor,
            surface = DARK_PURPLE_SURFACE,
            onBackground = textColor,
            onSurface = textColor,
            onPrimary = Color(0xFF2D0A4E)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            when (screen) {
                AppScreen.Wheel -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (controlsVisible) {
                            Text(
                                "FSD SPIN",
                                color = textColor,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "by FSD ETC",
                                color = Color(0xFFFFD700),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
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
                        }
                        // The wheel sits in the centre of the screen:
                        // this weighted box takes all the space between
                        // the header and the bottom controls.
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                SpinWheel(
                                    entries = entries,
                                    template = template,
                                    rotationDegrees = rotation.value,
                                    plateColor = plateColor,
                                    onTap = {
                                        // One vibration on every wheel tap.
                                        vibrateOnce(context)
                                        spin()
                                    }
                                )
                                // Small brand label in the corner of the
                                // wheel, visible only while spinning.
                                // Plain conditional (not AnimatedVisibility):
                                // the animated overload does not compile
                                // inside this nested Box scope.
                                if (spinning) {
                                    Text(
                                        "FSD ETC",
                                        color = Color(0xFFFFD700),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                    )
                                }
                            }
                        }
                        if (controlsVisible) {
                            Button(
                                onClick = { onSpinButtonClick() },
                                enabled = !spinning && !adShowing &&
                                    entries.size >= 2,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFD700),
                                    contentColor = Color(0xFF2D0A4E)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    if (spinning) "Spinning…"
                                    else if (adShowing) "Loading ad…"
                                    else "SPIN",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (entries.size < 2) {
                                Text(
                                    "Add at least 2 entries to spin.",
                                    color = textColor
                                )
                            }
                        } else if (hideAllControls && !spinning) {
                            TextButton(onClick = { hideAllControls = false }) {
                                Text("Show controls")
                            }
                        }
                    }
                }
                AppScreen.Edit -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(backgroundColor)
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            OutlinedButton(onClick = { screen = AppScreen.Wheel }) {
                                Text("← Back to wheel")
                            }
                        }
                        EntryEditor(
                            entries = entries,
                            onEntriesChange = { entries = it },
                            textColor = textColor,
                            backgroundColor = backgroundColor
                        )
                    }
                }
                AppScreen.Designs -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(backgroundColor)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            OutlinedButton(onClick = { screen = AppScreen.Wheel }) {
                                Text("← Back to wheel")
                            }
                        }
                        TemplatePicker(
                            current = template,
                            onTemplateChange = { applyTemplate(it) },
                            textColor = textColor
                        )
                    }
                }
                AppScreen.Settings -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(backgroundColor)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(onClick = { screen = AppScreen.Wheel }) {
                            Text("← Back to wheel")
                        }
                        SettingRow(
                            label = "Spin sound",
                            description = "Tick while spinning, jingle on win.",
                            checked = soundEnabled,
                            onCheckedChange = { soundEnabled = it },
                            textColor = textColor
                        )
                        SettingRow(
                            label = "Hide buttons while spinning",
                            description = "Controls disappear during a spin.",
                            checked = hideDuringSpin,
                            onCheckedChange = { hideDuringSpin = it },
                            textColor = textColor
                        )
                        SettingRow(
                            label = "Hide all controls",
                            description = "Only the wheel shows. Tap the wheel to spin; use 'Show controls' to get them back.",
                            checked = hideAllControls,
                            onCheckedChange = { hideAllControls = it },
                            textColor = textColor
                        )
                        SettingRow(
                            label = "Elimination mode",
                            description = "The winner is removed after each spin, so the next spin picks someone else.",
                            checked = eliminationMode,
                            onCheckedChange = { eliminationMode = it },
                            textColor = textColor
                        )
                        ForceWinnerPicker(
                            entries = entries,
                            forcedId = forcedId,
                            onForcedChange = { forcedId = it },
                            textColor = textColor
                        )
                        Text(
                            "Customisation",
                            color = textColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        ColorPickerRow(
                            label = "Background colour",
                            colors = BACKGROUND_OPTIONS,
                            selected = backgroundColor,
                            onColorChange = { backgroundColor = it },
                            textColor = textColor
                        )
                        ColorPickerRow(
                            label = "Text colour",
                            colors = TEXT_COLOR_OPTIONS,
                            selected = textColor,
                            onColorChange = { textColor = it },
                            textColor = textColor
                        )
                        ColorPickerRow(
                            label = "Plate (rim) colour",
                            colors = PLATE_COLOR_OPTIONS,
                            selected = plateColor,
                            onColorChange = { plateColor = it },
                            textColor = textColor
                        )
                        OutlinedButton(onClick = { plateColor = null }) {
                            Text("Use template rim instead")
                        }
                        Text(
                            "Wheel design",
                            color = textColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        TemplatePicker(
                            current = template,
                            onTemplateChange = { applyTemplate(it) },
                            textColor = textColor
                        )
                        Text(
                            "FSD SPIN is free, supported by ads.",
                            color = textColor.copy(alpha = 0.7f)
                        )
                        AboutSection(textColor = textColor)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Made by FSD ETC",
                            color = textColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            // Winner result overlay.
            resultEntry?.let { winner ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = DARK_PURPLE_SURFACE
                        ),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Winner!",
                                color = textColor,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                winner.text,
                                color = textColor,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            if (eliminationMode) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Elimination mode: this entry will be removed.",
                                    color = textColor
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { dismissResult() }) { Text("OK") }
                        }
                    }
                }
            }
        }
    }
}
