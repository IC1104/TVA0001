package com.example.tva0001

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp

/** The space-themed icon set for the home screen tiles. */
enum class SpaceIcon { Rocket, Planet, Crescent, RetroTv, Comet, Sparkle, Constellation, GearOrbit }

private val Cream = Color(0xFFF1EFE8)
private val NeonPink = Color(0xFFFF3D8A)
private val StarCyan = Color(0xFF7FE0F0)

@Composable
fun SpaceIconView(icon: SpaceIcon, size: Dp) {
    // Offscreen so shapes can be cut out (the planet's ring, the moon's curve).
    Canvas(Modifier.size(size).graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }) {
        val w = this.size.width
        val line = Stroke(width = w * 0.065f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (icon) {
            SpaceIcon.Rocket -> rotate(40f) {
                val body = Path().apply {
                    moveTo(w * 0.5f, w * 0.08f)
                    cubicTo(w * 0.68f, w * 0.24f, w * 0.66f, w * 0.52f, w * 0.61f, w * 0.68f)
                    lineTo(w * 0.39f, w * 0.68f)
                    cubicTo(w * 0.34f, w * 0.52f, w * 0.32f, w * 0.24f, w * 0.5f, w * 0.08f)
                    close()
                }
                val flame = Path().apply {
                    moveTo(w * 0.42f, w * 0.72f)
                    quadraticTo(w * 0.5f, w * 0.98f, w * 0.58f, w * 0.72f)
                    close()
                }
                drawPath(flame, NeonPink)
                drawPath(body, Cream, style = line)
                drawLine(Cream, Offset(w * 0.38f, w * 0.52f), Offset(w * 0.24f, w * 0.72f), w * 0.065f, StrokeCap.Round)
                drawLine(Cream, Offset(w * 0.62f, w * 0.52f), Offset(w * 0.76f, w * 0.72f), w * 0.065f, StrokeCap.Round)
                drawCircle(StarCyan, radius = w * 0.07f, center = Offset(w * 0.5f, w * 0.36f), style = Stroke(w * 0.05f))
            }
            SpaceIcon.Planet -> {
                val ring = Rect(Offset(w * 0.06f, w * 0.38f), Size(w * 0.88f, w * 0.24f))
                rotate(-20f) { drawOval(NeonPink, ring.topLeft, ring.size, style = line) }
                // The planet hides the back of its ring, then the front of the ring crosses it.
                drawCircle(Color.Black, radius = w * 0.25f, blendMode = BlendMode.Clear)
                drawCircle(Cream, radius = w * 0.22f, style = line)
                rotate(-20f) { drawArc(NeonPink, 0f, 180f, false, ring.topLeft, ring.size, style = line) }
                drawCircle(StarCyan, radius = w * 0.035f, center = Offset(w * 0.42f, w * 0.42f))
            }
            SpaceIcon.Crescent -> {
                drawCircle(Cream, radius = w * 0.3f, center = Offset(w * 0.44f, w * 0.54f))
                drawCircle(Color.Black, radius = w * 0.26f, center = Offset(w * 0.6f, w * 0.42f), blendMode = BlendMode.Clear)
                drawSparkle(Offset(w * 0.78f, w * 0.22f), w * 0.1f, StarCyan)
                drawCircle(NeonPink, radius = w * 0.04f, center = Offset(w * 0.84f, w * 0.6f))
            }
            SpaceIcon.RetroTv -> {
                drawLine(Cream, Offset(w * 0.5f, w * 0.32f), Offset(w * 0.3f, w * 0.1f), w * 0.05f, StrokeCap.Round)
                drawLine(Cream, Offset(w * 0.5f, w * 0.32f), Offset(w * 0.72f, w * 0.12f), w * 0.05f, StrokeCap.Round)
                drawCircle(NeonPink, radius = w * 0.045f, center = Offset(w * 0.3f, w * 0.1f))
                drawCircle(StarCyan, radius = w * 0.045f, center = Offset(w * 0.72f, w * 0.12f))
                drawRoundRect(
                    Cream,
                    topLeft = Offset(w * 0.12f, w * 0.32f),
                    size = Size(w * 0.76f, w * 0.54f),
                    cornerRadius = CornerRadius(w * 0.12f),
                    style = line,
                )
                val play = Path().apply {
                    moveTo(w * 0.43f, w * 0.47f)
                    lineTo(w * 0.62f, w * 0.59f)
                    lineTo(w * 0.43f, w * 0.71f)
                    close()
                }
                drawPath(play, NeonPink)
            }
            SpaceIcon.Comet -> {
                val head = Offset(w * 0.7f, w * 0.3f)
                fun tail(from: Offset, to: Offset, width: Float, tint: Color) = drawLine(
                    Brush.linearGradient(listOf(tint, tint.copy(alpha = 0f)), from, to),
                    from, to, width, StrokeCap.Round,
                )
                tail(Offset(w * 0.62f, w * 0.38f), Offset(w * 0.14f, w * 0.86f), w * 0.07f, Cream)
                tail(Offset(w * 0.7f, w * 0.44f), Offset(w * 0.34f, w * 0.88f), w * 0.05f, NeonPink)
                tail(Offset(w * 0.56f, w * 0.32f), Offset(w * 0.12f, w * 0.62f), w * 0.05f, StarCyan)
                drawCircle(Cream, radius = w * 0.12f, center = head)
            }
            SpaceIcon.Sparkle -> {
                drawSparkle(Offset(w * 0.44f, w * 0.54f), w * 0.36f, Cream)
                drawSparkle(Offset(w * 0.8f, w * 0.2f), w * 0.12f, StarCyan)
                drawCircle(NeonPink, radius = w * 0.045f, center = Offset(w * 0.8f, w * 0.78f))
            }
            SpaceIcon.Constellation -> {
                val stars = listOf(
                    Offset(w * 0.14f, w * 0.72f), Offset(w * 0.34f, w * 0.42f), Offset(w * 0.56f, w * 0.58f),
                    Offset(w * 0.72f, w * 0.24f), Offset(w * 0.88f, w * 0.64f),
                )
                stars.zipWithNext { a, b -> drawLine(Cream.copy(alpha = 0.6f), a, b, w * 0.035f, StrokeCap.Round) }
                stars.forEachIndexed { i, star ->
                    val tint = when (i) { 3 -> NeonPink; 1 -> StarCyan; else -> Cream }
                    drawCircle(tint, radius = if (i == 3) w * 0.08f else w * 0.06f, center = star)
                }
            }
            SpaceIcon.GearOrbit -> {
                scale(0.72f) { drawGear(Cream) }
                val orbit = Rect(Offset(w * 0.04f, w * 0.36f), Size(w * 0.92f, w * 0.28f))
                rotate(-25f) {
                    drawOval(StarCyan.copy(alpha = 0.8f), orbit.topLeft, orbit.size, style = Stroke(w * 0.035f))
                    drawCircle(NeonPink, radius = w * 0.06f, center = Offset(orbit.right - w * 0.02f, orbit.center.y))
                }
            }
        }
    }
}

/** A four-pointed twinkle centered on [center]. */
private fun DrawScope.drawSparkle(center: Offset, radius: Float, tint: Color) {
    val pinch = radius * 0.18f
    val star = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticTo(center.x + pinch, center.y - pinch, center.x + radius, center.y)
        quadraticTo(center.x + pinch, center.y + pinch, center.x, center.y + radius)
        quadraticTo(center.x - pinch, center.y + pinch, center.x - radius, center.y)
        quadraticTo(center.x - pinch, center.y - pinch, center.x, center.y - radius)
        close()
    }
    drawPath(star, tint)
}

/** A solid gear with a hole in the middle, filling the canvas. */
private fun DrawScope.drawGear(tint: Color) {
    val w = size.width
    val tooth = Size(w * 0.16f, w * 0.2f)
    repeat(8) { i ->
        rotate(i * 45f) {
            drawRoundRect(
                tint,
                topLeft = Offset(center.x - tooth.width / 2, w * 0.06f),
                size = tooth,
                cornerRadius = CornerRadius(w * 0.03f),
            )
        }
    }
    drawCircle(tint, radius = w * 0.3f)
    drawCircle(Color.Black, radius = w * 0.12f, blendMode = BlendMode.Clear)
}
