package com.github.dhakarpd.animeera.presentation.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color


/**
 * With time in every ~16ms the animation clock ticks and each pixel of
 * particular composable gets re-drawn
 *
 * Q)- In layout inspector it can be observed that the composable over whose modifier
 * you used this .shimmer extension is getting recomposed every ~16ms
 *
 * A)- Layout Inspector shows recomposition because animation state updates every frame. However,
 * Compose optimizes this by skipping layout and composition work and only re-running the draw phase.
 * This is expected for animation-driven modifiers like shimmer and does not indicate a
 * performance issue.
 *
 * | Observation                   | Reality |
 * | ----------------------------- | ------- |
 * | Inspector shows recomposition | True    |
 * | Function fully re-runs        | ❌ No    |
 * | Layout recalculated           | ❌ No    |
 * | Draw phase updated            | ✅ Yes   |
 * | Performance issue             | ❌ No    |
 *
 * Why Layout Inspector shows “Recomposition”
 *
 * Layout Inspector is conservative:
 *
 * It reports any invalidation of a recomposition scope
 * It does NOT differentiate between:
 * full recomposition and recomposition due to a draw-only invalidation
 *
 * Your shimmer composable is NOT:
 *
 * Re-measuring
 *
 * Re-laying out
 *
 * Re-creating child composables
 *
 * Re-running expensive logic
 * **/
fun Modifier.shimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")

    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            )
        ),
        label = "shimmerTranslate"
    )

    val colors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f)
    )

    background(
        brush = Brush.linearGradient(
            colors = colors,
            start = Offset(translateAnim, translateAnim),
            end = Offset(translateAnim + 300f, translateAnim + 300f)
        )
    )
}
