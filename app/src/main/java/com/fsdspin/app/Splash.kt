package com.fsdspin.app

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Opening animation, shown on EVERY launch: "Made by FSD ETC" animates
 * in (scale + fade) with one vibration, then [onFinished] opens the app.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        vibrateOnce(context, 80L)
        launch {
            scale.animateTo(
                1f,
                animationSpec = tween(900, easing = FastOutSlowInEasing)
            )
        }
        launch {
            alpha.animateTo(1f, animationSpec = tween(700))
        }
        delay(1800L)
        onFinished()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DARK_PURPLE_BACKGROUND),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Made by FSD ETC",
                color = Color(0xFFFFD700),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .scale(scale.value)
                    .alpha(alpha.value)
                    .padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "FSD SPIN",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.alpha(alpha.value)
            )
        }
    }
}
