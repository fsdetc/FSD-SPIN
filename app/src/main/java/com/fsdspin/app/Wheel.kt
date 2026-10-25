package com.fsdspin.app

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * The spinning wheel itself. Drawn on Canvas: coloured slices, entry
 * text along each slice, a rim, a centre hub and a pointer at the top.
 * Tapping the wheel also spins it.
 */
@Composable
fun SpinWheel(
    entries: List<WheelEntry>,
    template: WheelTemplate,
    rotationDegrees: Float,
    size: Dp = 320.dp,
    onTap: () -> Unit
) {
    Canvas(
        modifier = Modifier
            .size(size)
            .pointerInput(Unit) {
                detectTapGestures { onTap() }
            }
    ) {
        if (entries.isEmpty()) return@Canvas
        val radius = min(size.width, size.height) / 2f
        val centre = Offset(size.width / 2f, size.height / 2f)
        val segment = 360f / entries.size

        // Rim.
        drawCircle(color = template.rim, radius = radius, center = centre)
        val innerRadius = radius * 0.94f

        entries.forEachIndexed { index, entry ->
            val start = rotationDegrees + index * segment
            // Slice.
            drawArc(
                color = entry.color,
                startAngle = start,
                sweepAngle = segment,
                useCenter = true,
                topLeft = Offset(centre.x - innerRadius, centre.y - innerRadius),
                size = Size(innerRadius * 2f, innerRadius * 2f)
            )
            // Slice divider line.
            val edge = Math.toRadians(start.toDouble())
            drawLine(
                color = template.rim,
                start = centre,
                end = Offset(
                    centre.x + (innerRadius * cos(edge)).toFloat(),
                    centre.y + (innerRadius * sin(edge)).toFloat()
                ),
                strokeWidth = 3f
            )
            // Entry text, rotated to sit along the slice.
            drawSliceText(
                text = entry.text,
                centre = centre,
                radius = innerRadius,
                middleAngleDegrees = start + segment / 2f
            )
        }

        // Centre hub.
        drawCircle(color = template.hub, radius = radius * 0.16f, center = centre)
        drawCircle(
            color = template.rim,
            radius = radius * 0.16f,
            center = centre,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
        )

        // Pointer at the top.
        val pointerPath = Path().apply {
            moveTo(centre.x - radius * 0.09f, centre.y - radius - radius * 0.02f)
            lineTo(centre.x + radius * 0.09f, centre.y - radius - radius * 0.02f)
            lineTo(centre.x, centre.y - radius + radius * 0.14f)
            close()
        }
        drawPath(path = pointerPath, color = template.pointer)
    }
}

private fun DrawScope.drawSliceText(
    text: String,
    centre: Offset,
    radius: Float,
    middleAngleDegrees: Float
) {
    val paint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = radius * 0.13f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
        setShadowLayer(4f, 0f, 2f, android.graphics.Color.BLACK)
    }
    val angleRad = Math.toRadians(middleAngleDegrees.toDouble())
    // Keep text readable: flip it when it would be upside down.
    var drawAngle = middleAngleDegrees
    if (drawAngle > 90f && drawAngle < 270f) drawAngle += 180f
    val textRadius = radius * 0.62f
    val x = centre.x + (textRadius * cos(angleRad)).toFloat()
    val y = centre.y + (textRadius * sin(angleRad)).toFloat()
    drawContext.canvas.nativeCanvas.save()
    drawContext.canvas.nativeCanvas.rotate(drawAngle, x, y)
    // Shorten very long names so they stay inside the slice.
    val shown = if (text.length > 12) text.take(11) + "…" else text
    drawContext.canvas.nativeCanvas.drawText(shown, x, y, paint)
    drawContext.canvas.nativeCanvas.restore()
}

/** Small helper so callers can rotate a point for layout math if needed. */
fun rotatePoint(angleDegrees: Float, radius: Float): Offset {
    val rad = Math.toRadians(angleDegrees.toDouble())
    return Offset((radius * cos(rad)).toFloat(), (radius * sin(rad)).toFloat())
}
