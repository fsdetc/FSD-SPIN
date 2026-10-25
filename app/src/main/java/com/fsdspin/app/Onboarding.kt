package com.fsdspin.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private enum class OnboardingStep { Welcome, Details, Verifying, Celebration }

/**
 * First-launch onboarding, shown only once (the first time the app opens).
 * Flow, all animated and smooth: welcome -> age selection (+ where the
 * user found the app) -> button click with vibration -> verifying
 * animation (age verified, then onboarding verified) -> full-screen
 * congratulations celebration -> enter the app. No popup dialog anywhere.
 *
 * Everything stays on the device: answers are stored in SharedPreferences
 * only, never sent anywhere — this is an offline app.
 */
@Composable
fun OnboardingScreen(onComplete: (source: String, birthYear: Int) -> Unit) {
    var step by remember { mutableStateOf(OnboardingStep.Welcome) }
    var source by remember { mutableStateOf<String?>(null) }
    var otherSource by remember { mutableStateOf("") }
    var birthYearText by remember { mutableStateOf("") }
    val year = birthYearText.toIntOrNull()
    val yearValid = year != null && year in 1900..2026

    AnimatedContent(
        targetState = step,
        transitionSpec = {
            (fadeIn(animationSpec = tween(450)) +
                scaleIn(initialScale = 0.96f)) togetherWith
                fadeOut(animationSpec = tween(300))
        },
        label = "onboarding"
    ) { current ->
        when (current) {
            OnboardingStep.Welcome -> OnboardingWelcome(
                onGetStarted = { step = OnboardingStep.Details }
            )
            OnboardingStep.Details -> OnboardingDetails(
                source = source,
                onSourceChange = { source = it },
                otherSource = otherSource,
                onOtherSourceChange = { otherSource = it },
                birthYearText = birthYearText,
                onBirthYearChange = { new ->
                    if (new.length <= 4 && new.all { it.isDigit() }) {
                        birthYearText = new
                    }
                },
                yearValid = yearValid,
                onContinue = { step = OnboardingStep.Verifying }
            )
            OnboardingStep.Verifying -> OnboardingVerifying(
                onVerified = { step = OnboardingStep.Celebration }
            )
            OnboardingStep.Celebration -> OnboardingCelebration(
                onEnter = {
                    val finalSource = when {
                        source == "Other" -> otherSource.trim()
                            .ifEmpty { "Other" }
                        source != null -> source!!
                        else -> "Not shared"
                    }
                    onComplete(finalSource, year!!)
                }
            )
        }
    }
}

@Composable
private fun OnboardingWelcome(onGetStarted: () -> Unit) {
    val context = LocalContext.current
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val scale by animateFloatAsState(
        targetValue = if (shown) 1f else 0.7f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "welcomeScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(700),
        label = "welcomeAlpha"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DARK_PURPLE_BACKGROUND)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "FSD SPIN",
                color = Color(0xFFFFD700),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .scale(scale)
                    .alpha(alpha)
            )
            Text(
                "by FSD ETC",
                color = DEFAULT_TEXT_COLOR,
                fontSize = 16.sp,
                modifier = Modifier.alpha(alpha)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Spin the wheel. Win the moment.",
                color = DEFAULT_TEXT_COLOR,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alpha)
            )
            Text(
                "Custom entries, premium designs, elimination mode — " +
                    "all in one smooth spin.",
                color = DEFAULT_TEXT_COLOR.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alpha)
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    vibrateOnce(context, 60L)
                    onGetStarted()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD700),
                    contentColor = Color(0xFF2D0A4E)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(alpha)
            ) {
                Text("Get Started", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun OnboardingDetails(
    source: String?,
    onSourceChange: (String) -> Unit,
    otherSource: String,
    onOtherSourceChange: (String) -> Unit,
    birthYearText: String,
    onBirthYearChange: (String) -> Unit,
    yearValid: Boolean,
    onContinue: () -> Unit
) {
    val context = LocalContext.current
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
                "Almost there!",
                color = DEFAULT_TEXT_COLOR,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Just your birth year, so the experience fits you. " +
                    "Everything stays on this device only.",
                color = DEFAULT_TEXT_COLOR.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Select your birth year",
                color = DEFAULT_TEXT_COLOR,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = birthYearText,
                onValueChange = onBirthYearChange,
                label = { Text("Birth year, e.g. 2021") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )
            if (birthYearText.isNotEmpty() && !yearValid) {
                Text(
                    "Please enter a valid 4-digit birth year.",
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Where did you find this app? (optional)",
                color = DEFAULT_TEXT_COLOR,
                fontWeight = FontWeight.Bold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("YouTube", "Discord", "AI").forEach { option ->
                    FilterChip(
                        selected = source == option,
                        onClick = { onSourceChange(option) },
                        label = { Text(option) }
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("Instagram", "Friend", "Other").forEach { option ->
                    FilterChip(
                        selected = source == option,
                        onClick = { onSourceChange(option) },
                        label = { Text(option) }
                    )
                }
            }
            if (source == "Other") {
                OutlinedTextField(
                    value = otherSource,
                    onValueChange = onOtherSourceChange,
                    label = { Text("Tell us where") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    // Vibration on button click, as required.
                    vibrateOnce(context, 80L)
                    onContinue()
                },
                enabled = yearValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD700),
                    contentColor = Color(0xFF2D0A4E)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun OnboardingVerifying(onVerified: () -> Unit) {
    var stage by remember { mutableStateOf(0) }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        delay(900L)
        stage = 1 // Age verified.
        vibrateOnce(context, 40L)
        delay(900L)
        stage = 2 // Onboarding verified.
        vibrateOnce(context, 40L)
        delay(700L)
        onVerified()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DARK_PURPLE_BACKGROUND),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = Color(0xFFFFD700)
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Verifying your age… " + if (stage >= 1) "✓" else "",
                color = DEFAULT_TEXT_COLOR,
                fontSize = 18.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Completing onboarding… " + if (stage >= 2) "✓" else "",
                color = DEFAULT_TEXT_COLOR.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun OnboardingCelebration(onEnter: () -> Unit) {
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
        label = "celebrationScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(600),
        label = "celebrationAlpha"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DARK_PURPLE_BACKGROUND),
        contentAlignment = Alignment.Center
    ) {
        // Fast party animation across the whole screen.
        ConfettiDots()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                "Congratulations!",
                color = Color(0xFFFFD700),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .scale(scale)
                    .alpha(alpha)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Your onboarding is complete — FSD SPIN is ready!",
                color = DEFAULT_TEXT_COLOR,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alpha)
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    vibrateOnce(context, 60L)
                    onEnter()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD700),
                    contentColor = Color(0xFF2D0A4E)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(alpha)
            ) {
                Text("Enter FSD SPIN", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private data class ConfettiDot(
    val xFraction: Float,
    val phase: Float,
    val sizeDp: Int,
    val color: Color,
    val speed: Float
)

/**
 * Fast, full-screen party dots: one looping fall animation drives
 * every dot, each with its own position, phase, size and colour.
 */
@Composable
fun ConfettiDots() {
    val dots = remember {
        listOf(
            ConfettiDot(0.08f, 0.00f, 14, Color(0xFFFFD700), 1.0f),
            ConfettiDot(0.20f, 0.30f, 10, Color(0xFF00E5FF), 1.2f),
            ConfettiDot(0.32f, 0.55f, 16, Color(0xFFFF80AB), 0.9f),
            ConfettiDot(0.45f, 0.15f, 11, Color(0xFFB2FF59), 1.1f),
            ConfettiDot(0.58f, 0.70f, 14, Color(0xFFFFD700), 1.0f),
            ConfettiDot(0.70f, 0.40f, 10, Color(0xFF00E5FF), 1.3f),
            ConfettiDot(0.82f, 0.05f, 16, Color(0xFFFF80AB), 0.9f),
            ConfettiDot(0.92f, 0.50f, 12, Color(0xFFFFFFFF), 1.1f),
            ConfettiDot(0.12f, 0.80f, 11, Color(0xFFB2FF59), 1.2f),
            ConfettiDot(0.65f, 0.90f, 13, Color(0xFFFFD700), 1.0f),
            ConfettiDot(0.38f, 0.25f, 10, Color(0xFFFFFFFF), 1.2f),
            ConfettiDot(0.76f, 0.65f, 15, Color(0xFFFF80AB), 0.9f)
        )
    }
    // Simple looping fall animation (Animatable loop) — avoids the
    // InfiniteTransition API that failed to compile in this setup.
    val fallAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            fallAnim.animateTo(
                1f,
                animationSpec = tween(4200, easing = LinearEasing)
            )
            fallAnim.snapTo(0f)
        }
    }
    val fall = fallAnim.value
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val width = maxWidth
        val height = maxHeight
        dots.forEach { dot ->
            val progress = ((fall * dot.speed + dot.phase) % 1f)
            Box(
                modifier = Modifier
                    .offset(
                        x = width * dot.xFraction,
                        y = height * progress
                    )
                    .size(dot.sizeDp.dp)
                    .clip(CircleShape)
                    .background(dot.color)
            )
        }
    }
}
