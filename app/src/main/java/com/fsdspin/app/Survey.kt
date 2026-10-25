package com.fsdspin.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * First-launch survey, shown only once (the first time the app opens).
 * Everything stays on the device: answers are stored in SharedPreferences
 * only, never sent anywhere — this is an offline app.
 *
 * Flow: form -> Continue -> spinning loading animation (as if processing)
 * -> popup with a vibration: setup complete, app ready to use.
 */
@Composable
fun SurveyScreen(onComplete: (source: String, birthYear: Int) -> Unit) {
    val context = LocalContext.current
    var source by remember { mutableStateOf<String?>(null) }
    var otherSource by remember { mutableStateOf("") }
    var birthYearText by remember { mutableStateOf("") }
    var processing by remember { mutableStateOf(false) }
    var showDonePopup by remember { mutableStateOf(false) }
    // Captured when Continue is tapped, so edits during the 2-second
    // processing animation cannot change (or invalidate) the answers.
    var submittedSource by remember { mutableStateOf("") }
    var submittedYear by remember { mutableStateOf(0) }

    val year = birthYearText.toIntOrNull()
    val yearValid = year != null && year in 1900..2026
    val sourceValid = source != null &&
        (source != "Other" || otherSource.trim().isNotEmpty())
    val canContinue = sourceValid && yearValid

    // Spinning "processing" animation, then the done popup (+ vibration).
    LaunchedEffect(processing) {
        if (processing) {
            delay(2000L)
            processing = false
            showDonePopup = true
            vibrateOnce(context, 120L)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DARK_PURPLE_BACKGROUND)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Welcome to FSD SPIN!",
                color = DEFAULT_TEXT_COLOR,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Just two quick questions so we can improve your experience. " +
                    "Your answers stay on this device only.",
                color = DEFAULT_TEXT_COLOR.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Where did you find this app?",
                color = DEFAULT_TEXT_COLOR,
                fontWeight = FontWeight.Bold
            )
            listOf("YouTube", "Discord", "AI", "Instagram", "Friend", "Other")
                .forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { source = option }
                    ) {
                        RadioButton(
                            selected = source == option,
                            onClick = { source = option }
                        )
                        Text(option, color = DEFAULT_TEXT_COLOR)
                    }
                }
            if (source == "Other") {
                OutlinedTextField(
                    value = otherSource,
                    onValueChange = { otherSource = it },
                    label = { Text("Tell us where") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Birth year only",
                color = DEFAULT_TEXT_COLOR,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = birthYearText,
                onValueChange = { new ->
                    if (new.length <= 4 && new.all { it.isDigit() }) {
                        birthYearText = new
                    }
                },
                label = { Text("e.g. 2021") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            if (birthYearText.isNotEmpty() && !yearValid) {
                Text(
                    "Please enter a valid 4-digit birth year.",
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    submittedSource =
                        if (source == "Other") otherSource.trim() else source!!
                    submittedYear = year!!
                    processing = true
                },
                enabled = canContinue && !processing,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue")
            }
        }

        if (processing) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp),
                        color = DEFAULT_TEXT_COLOR
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Setting things up…", color = DEFAULT_TEXT_COLOR)
                }
            }
        }
    }

    if (showDonePopup) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Setup complete!") },
            text = {
                Text("You're all set — FSD SPIN is ready to use. Enjoy spinning!")
            },
            confirmButton = {
                TextButton(
                    onClick = { onComplete(submittedSource, submittedYear) }
                ) { Text("Let's go!") }
            }
        )
    }
}
