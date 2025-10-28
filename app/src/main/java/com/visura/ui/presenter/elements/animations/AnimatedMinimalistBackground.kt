package com.visura.ui.presenter.elements.animations

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedMinimalistBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "background")

    val gridOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "grid"
    )

    val pinScale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pin1"
    )

    val pinScale2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, delayMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pin2"
    )

    val pinScale3 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, delayMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pin3"
    )

    val radarScale by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar"
    )

    val radarAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarAlpha"
    )

    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val primary = MaterialTheme.colorScheme.primary

    Canvas(modifier = Modifier.fillMaxSize()) {
        val gridSize = 50.dp.toPx()

        val offsetX = gridOffset % gridSize
        val offsetY = gridOffset % gridSize

        val verticalLines = (size.width / gridSize).toInt() + 2
        for (i in 0 until verticalLines) {
            val x = i * gridSize - offsetX
            drawLine(
                color = surfaceVariant.copy(alpha = 0.3f),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f
            )
        }

        val horizontalLines = (size.height / gridSize).toInt() + 2
        for (i in 0 until horizontalLines) {
            val y = i * gridSize - offsetY
            drawLine(
                color = surfaceVariant.copy(alpha = 0.3f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
        }

        val centerX = size.width / 2
        val centerY = size.height / 2

        drawCircle(
            color = primary.copy(alpha = radarAlpha * 0.3f),
            radius = 40.dp.toPx() * (1f + radarScale),
            center = Offset(centerX, centerY),
            style = Stroke(width = 2f)
        )

        val pins = listOf(
            Offset(size.width * 0.3f, size.height * 0.25f) to pinScale1,
            Offset(size.width * 0.65f, size.height * 0.4f) to pinScale2,
            Offset(size.width * 0.45f, size.height * 0.65f) to pinScale3
        )

        drawLine(
            color = primary.copy(alpha = 0.35f),
            start = pins[0].first,
            end = pins[1].first,
            strokeWidth = 2.5f
        )

        drawLine(
            color = primary.copy(alpha = 0.35f),
            start = pins[1].first,
            end = pins[2].first,
            strokeWidth = 2.5f
        )

        drawLine(
            color = primary.copy(alpha = 0.35f),
            start = pins[2].first,
            end = pins[0].first,
            strokeWidth = 2.5f
        )

        val particles = listOf(
            Offset(size.width * 0.2f, size.height * 0.3f),
            Offset(size.width * 0.8f, size.height * 0.5f),
            Offset(size.width * 0.5f, size.height * 0.8f)
        )

        particles.forEach { position ->
            drawCircle(
                color = primary.copy(alpha = 0.4f),
                radius = 3.5.dp.toPx(),
                center = position
            )
        }

        pins.forEach { (position, scale) ->
            drawCircle(
                color = primary.copy(alpha = 0.35f),
                radius = 14.dp.toPx() * scale,
                center = position
            )

            drawCircle(
                color = primary.copy(alpha = 0.7f),
                radius = 8.dp.toPx() * scale,
                center = position,
                style = Stroke(width = 2f)
            )

            drawCircle(
                color = primary.copy(alpha = 1f),
                radius = 5.dp.toPx(),
                center = position
            )
        }
    }
}