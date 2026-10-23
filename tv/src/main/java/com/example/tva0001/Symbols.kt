package com.example.tva0001

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp

/** The simple white symbols for tiles without an app logo, and for the weather. */
enum class Symbol { Play, Hdmi, Apps, Settings, Sun, Moon, Cloud, Rain, Snow, Storm, Fog }

@Composable
fun SymbolIcon(symbol: Symbol, size: Dp, color: Color) {
    // Offscreen so the gear's hole and the moon's curve can be cut out.
    Canvas(Modifier.size(size).graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }) {
        val w = this.size.width
        val line = Stroke(width = w * 0.07f, join = StrokeJoin.Round)
        when (symbol) {
            Symbol.Play -> {
                val triangle = Path().apply {
                    moveTo(w * 0.32f, w * 0.2f)
                    lineTo(w * 0.82f, w * 0.5f)
                    lineTo(w * 0.32f, w * 0.8f)
                    close()
                }
                drawPath(triangle, color, style = Stroke(width = w * 0.08f, join = StrokeJoin.Round))
                drawPath(triangle, color)
            }
            Symbol.Hdmi -> {
                // The outline of an HDMI plug, with its inner contact strip.
                val plug = Path().apply {
                    moveTo(w * 0.08f, w * 0.3f)
                    lineTo(w * 0.92f, w * 0.3f)
                    lineTo(w * 0.92f, w * 0.55f)
                    lineTo(w * 0.76f, w * 0.72f)
                    lineTo(w * 0.24f, w * 0.72f)
                    lineTo(w * 0.08f, w * 0.55f)
                    close()
                }
                drawPath(plug, color, style = line)
                drawLine(color, Offset(w * 0.28f, w * 0.46f), Offset(w * 0.72f, w * 0.46f), strokeWidth = w * 0.07f)
            }
            Symbol.Apps -> {
                val dot = w * 0.16f
                for (row in 0..2) for (col in 0..2) {
                    drawRoundRect(
                        color,
                        topLeft = Offset(w * (0.14f + col * 0.29f), w * (0.14f + row * 0.29f)),
                        size = Size(dot, dot),
                        cornerRadius = CornerRadius(dot * 0.3f),
                    )
                }
            }
            Symbol.Settings -> {
                val tooth = Size(w * 0.16f, w * 0.2f)
                repeat(8) { i ->
                    rotate(i * 45f) {
                        drawRoundRect(
                            color,
                            topLeft = Offset(center.x - tooth.width / 2, w * 0.06f),
                            size = tooth,
                            cornerRadius = CornerRadius(w * 0.03f),
                        )
                    }
                }
                drawCircle(color, radius = w * 0.3f)
                drawCircle(Color.Black, radius = w * 0.12f, blendMode = BlendMode.Clear)
            }
            Symbol.Sun -> {
                drawCircle(color, radius = w * 0.2f)
                repeat(8) { i ->
                    rotate(i * 45f) {
                        drawLine(
                            color,
                            Offset(center.x, w * 0.06f),
                            Offset(center.x, w * 0.16f),
                            strokeWidth = w * 0.07f,
                            cap = StrokeCap.Round,
                        )
                    }
                }
            }
            Symbol.Moon -> {
                drawCircle(color, radius = w * 0.32f)
                drawCircle(
                    Color.Black,
                    radius = w * 0.28f,
                    center = Offset(w * 0.66f, w * 0.36f),
                    blendMode = BlendMode.Clear,
                )
            }
            Symbol.Cloud -> drawCloud(color, lift = 0f)
            Symbol.Rain -> {
                drawCloud(color, lift = 0.14f)
                for (x in listOf(0.35f, 0.52f, 0.69f)) {
                    drawLine(
                        color,
                        Offset(w * x, w * 0.68f),
                        Offset(w * (x - 0.06f), w * 0.84f),
                        strokeWidth = w * 0.06f,
                        cap = StrokeCap.Round,
                    )
                }
            }
            Symbol.Snow -> {
                drawCloud(color, lift = 0.14f)
                for (x in listOf(0.35f, 0.52f, 0.69f)) {
                    drawCircle(color, radius = w * 0.045f, center = Offset(w * x, w * 0.76f))
                }
            }
            Symbol.Storm -> {
                drawCloud(color, lift = 0.14f)
                val bolt = Path().apply {
                    moveTo(w * 0.56f, w * 0.62f)
                    lineTo(w * 0.42f, w * 0.8f)
                    lineTo(w * 0.54f, w * 0.8f)
                    lineTo(w * 0.46f, w * 0.96f)
                    lineTo(w * 0.66f, w * 0.74f)
                    lineTo(w * 0.54f, w * 0.74f)
                    close()
                }
                drawPath(bolt, color)
            }
            Symbol.Fog -> {
                for (y in listOf(0.34f, 0.5f, 0.66f)) {
                    drawLine(
                        color,
                        Offset(w * 0.14f, w * y),
                        Offset(w * 0.86f, w * y),
                        strokeWidth = w * 0.07f,
                        cap = StrokeCap.Round,
                    )
                }
            }
        }
    }
}

/** A puffy cloud; [lift] moves it up to make room for rain or snow underneath. */
private fun DrawScope.drawCloud(color: Color, lift: Float) {
    val w = size.width
    val up = w * lift
    drawCircle(color, radius = w * 0.16f, center = Offset(w * 0.34f, w * 0.56f - up))
    drawCircle(color, radius = w * 0.22f, center = Offset(w * 0.54f, w * 0.46f - up))
    drawCircle(color, radius = w * 0.14f, center = Offset(w * 0.72f, w * 0.58f - up))
    drawRoundRect(
        color,
        topLeft = Offset(w * 0.18f, w * 0.56f - up),
        size = Size(w * 0.68f, w * 0.16f),
        cornerRadius = CornerRadius(w * 0.08f),
    )
}
