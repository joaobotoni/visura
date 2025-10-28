package com.visura.ui.presenter.elements.animations

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlin.math.sqrt
import kotlin.random.Random

private const val GRID_SIZE_DP = 50f
private const val GRID_ANIMATION_DURATION = 40000
private const val PIN_ANIMATION_DURATION = 2500
private const val PIN_ANIMATION_DELAY_1 = 800
private const val PIN_ANIMATION_DELAY_2 = 1600
private const val RADAR_ANIMATION_DURATION = 4000
private const val PIN_SCALE_MIN = 1f
private const val PIN_SCALE_MAX = 1.4f
private const val RADAR_SCALE_MIN = 0f
private const val RADAR_SCALE_MAX = 4f
private const val RADAR_ALPHA_MIN = 0.5f
private const val RADAR_ALPHA_MAX = 0f
private const val SAFE_MARGIN_TOP = 0.50f
private const val SAFE_MARGIN_BOTTOM = 0.20f
private const val SAFE_MARGIN_HORIZONTAL = 0.15f
private const val MIN_DISTANCE_RATIO = 0.25f
private const val MAX_POSITION_ATTEMPTS = 50

@Composable
fun AnimatedMinimalistBackground() {
    val seed by rememberLifecycleAwareSeed()
    val animations = rememberBackgroundAnimations()
    val colors = BackgroundColors(
        surfaceVariant = MaterialTheme.colorScheme.surfaceVariant,
        primary = MaterialTheme.colorScheme.primary
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawAnimatedGrid(
            offset = animations.gridOffset,
            color = colors.surfaceVariant
        )

        drawRadarPulse(
            scale = animations.radarScale,
            alpha = animations.radarAlpha,
            color = colors.primary
        )

        val pins = generatePinPositions(seed, size.width, size.height)

        drawParticles(colors.primary)
        drawAnimatedPins(
            pins = pins,
            scales = animations.pinScales,
            color = colors.primary
        )
    }
}

@Composable
private fun rememberLifecycleAwareSeed(): State<Int> {
    var seed by remember { mutableIntStateOf(Random.nextInt()) }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        seed = Random.nextInt()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP || event == Lifecycle.Event.ON_START) {
                seed = Random.nextInt()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return remember { derivedStateOf { seed } }
}

@Composable
private fun rememberBackgroundAnimations(): BackgroundAnimations {
    val infiniteTransition = rememberInfiniteTransition(label = "background")

    val pinScales = listOf(
        infiniteTransition.animatePinScale(delay = 0, label = "pin1"),
        infiniteTransition.animatePinScale(delay = PIN_ANIMATION_DELAY_1, label = "pin2"),
        infiniteTransition.animatePinScale(delay = PIN_ANIMATION_DELAY_2, label = "pin3")
    )

    return BackgroundAnimations(
        gridOffset = infiniteTransition.animateGridOffset().value,
        pinScales = pinScales.map { it.value },
        radarScale = infiniteTransition.animateRadarScale().value,
        radarAlpha = infiniteTransition.animateRadarAlpha().value
    )
}

@Composable
private fun InfiniteTransition.animateGridOffset() = animateFloat(
    initialValue = 0f,
    targetValue = GRID_SIZE_DP,
    animationSpec = infiniteRepeatable(
        animation = tween(GRID_ANIMATION_DURATION, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
    ),
    label = "grid"
)

@Composable
private fun InfiniteTransition.animatePinScale(delay: Int, label: String) = animateFloat(
    initialValue = PIN_SCALE_MIN,
    targetValue = PIN_SCALE_MAX,
    animationSpec = infiniteRepeatable(
        animation = tween(PIN_ANIMATION_DURATION, delayMillis = delay, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse
    ),
    label = label
)

@Composable
private fun InfiniteTransition.animateRadarScale() = animateFloat(
    initialValue = RADAR_SCALE_MIN,
    targetValue = RADAR_SCALE_MAX,
    animationSpec = infiniteRepeatable(
        animation = tween(RADAR_ANIMATION_DURATION, easing = LinearOutSlowInEasing),
        repeatMode = RepeatMode.Restart
    ),
    label = "radar"
)

@Composable
private fun InfiniteTransition.animateRadarAlpha() = animateFloat(
    initialValue = RADAR_ALPHA_MIN,
    targetValue = RADAR_ALPHA_MAX,
    animationSpec = infiniteRepeatable(
        animation = tween(RADAR_ANIMATION_DURATION, easing = FastOutLinearInEasing),
        repeatMode = RepeatMode.Restart
    ),
    label = "radarAlpha"
)

private fun DrawScope.drawAnimatedGrid(offset: Float, color: Color) {
    val gridSize = GRID_SIZE_DP.dp.toPx()
    val offsetX = offset % gridSize
    val offsetY = offset % gridSize

    drawGridLines(
        gridSize = gridSize,
        offset = offsetX,
        count = (size.width / gridSize).toInt() + 2,
        color = color,
        isVertical = true
    )

    drawGridLines(
        gridSize = gridSize,
        offset = offsetY,
        count = (size.height / gridSize).toInt() + 2,
        color = color,
        isVertical = false
    )
}

private fun DrawScope.drawGridLines(
    gridSize: Float,
    offset: Float,
    count: Int,
    color: Color,
    isVertical: Boolean
) {
    repeat(count) { i ->
        val position = i * gridSize - offset
        drawLine(
            color = color.copy(alpha = 0.3f),
            start = if (isVertical) Offset(position, 0f) else Offset(0f, position),
            end = if (isVertical) Offset(position, size.height) else Offset(size.width, position),
            strokeWidth = 1f
        )
    }
}

private fun DrawScope.drawRadarPulse(scale: Float, alpha: Float, color: Color) {
    drawCircle(
        color = color.copy(alpha = alpha * 0.3f),
        radius = 40.dp.toPx() * (1f + scale),
        center = center,
        style = Stroke(width = 2f)
    )
}

private fun generatePinPositions(seed: Int, width: Float, height: Float): List<Offset> {
    val random = Random(seed)
    val availableHeight = 1f - SAFE_MARGIN_TOP - SAFE_MARGIN_BOTTOM
    val minDistance = width * MIN_DISTANCE_RATIO
    val positions = mutableListOf<Offset>()

    repeat(3) {
        positions.add(
            generateSeparatedPosition(
                random = random,
                width = width,
                height = height,
                availableHeight = availableHeight,
                minDistance = minDistance,
                existingPositions = positions
            )
        )
    }

    return positions
}

private fun generateSeparatedPosition(
    random: Random,
    width: Float,
    height: Float,
    availableHeight: Float,
    minDistance: Float,
    existingPositions: List<Offset>
): Offset {
    var attempts = 0
    var newPosition: Offset

    do {
        newPosition = Offset(
            width * (SAFE_MARGIN_HORIZONTAL + random.nextFloat() * (1f - 2 * SAFE_MARGIN_HORIZONTAL)),
            height * (SAFE_MARGIN_TOP + random.nextFloat() * availableHeight)
        )
        attempts++
    } while (attempts < MAX_POSITION_ATTEMPTS && !isPositionValid(newPosition, existingPositions, minDistance))

    return newPosition
}

private fun isPositionValid(position: Offset, existingPositions: List<Offset>, minDistance: Float): Boolean {
    return existingPositions.none { existingPos ->
        val dx = position.x - existingPos.x
        val dy = position.y - existingPos.y
        sqrt(dx * dx + dy * dy) < minDistance
    }
}

private fun DrawScope.drawParticles(color: Color) {
    val particles = listOf(
        Offset(size.width * 0.2f, size.height * 0.3f),
        Offset(size.width * 0.8f, size.height * 0.5f),
        Offset(size.width * 0.5f, size.height * 0.8f)
    )

    particles.forEach { position ->
        drawCircle(
            color = color.copy(alpha = 0.4f),
            radius = 3.5.dp.toPx(),
            center = position
        )
    }
}

private fun DrawScope.drawAnimatedPins(pins: List<Offset>, scales: List<Float>, color: Color) {
    pins.forEachIndexed { index, position ->
        val scale = scales[index]

        drawCircle(
            color = color.copy(alpha = 0.08f),
            radius = 16.dp.toPx() * scale,
            center = position
        )

        drawCircle(
            color = color.copy(alpha = 0.4f),
            radius = 8.dp.toPx() * scale,
            center = position,
            style = Stroke(width = 1f)
        )

        drawCircle(
            color = color.copy(alpha = 0.8f),
            radius = 3.dp.toPx(),
            center = position
        )
    }
}

private data class BackgroundAnimations(
    val gridOffset: Float,
    val pinScales: List<Float>,
    val radarScale: Float,
    val radarAlpha: Float
)

private data class BackgroundColors(
    val surfaceVariant: Color,
    val primary: Color
)