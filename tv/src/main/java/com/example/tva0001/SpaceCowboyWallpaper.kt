package com.example.tva0001

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Typeface
import kotlin.math.pow
import kotlin.random.Random

/**
 * "See you, Space Cowboy": a silhouette filled with stars against a purple-to-red sky.
 *
 * The outline was traced from the user's wallpaper and saved at 4K in
 * assets/space_cowboy_silhouette.txt (one shape per line, "x,y" points).
 */
fun drawSpaceCowboy(context: Context, canvas: Canvas, w: Float, h: Float) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val silhouette = loadSilhouette(context, w, h)

    // The sky: violet on the left, through magenta, to red on the right.
    paint.shader = LinearGradient(
        0f, h, w * 0.75f, h * 0.2f,
        intArrayOf(color(0xFF7A1CCB), color(0xFFB12E8C), color(0xFFD72F52), color(0xFFDA2C40)),
        floatArrayOf(0f, 0.4f, 0.75f, 1f),
        Shader.TileMode.CLAMP,
    )
    canvas.drawRect(0f, 0f, w, h, paint)
    paint.shader = null

    // Everything inside the silhouette is deep space.
    canvas.save()
    canvas.clipPath(silhouette)
    drawSpace(canvas, paint, w, h)
    canvas.restore()

    // Accent: a thin neon-pink rim light around the silhouette, in TVA's colors.
    paint.style = Paint.Style.STROKE
    paint.strokeJoin = Paint.Join.ROUND
    paint.color = color(0xFFFF3D8A)
    paint.alpha = 90
    paint.strokeWidth = 14f
    paint.maskFilter = BlurMaskFilter(16f, BlurMaskFilter.Blur.NORMAL)
    canvas.drawPath(silhouette, paint)
    paint.maskFilter = null
    paint.alpha = 160
    paint.strokeWidth = 3f
    canvas.drawPath(silhouette, paint)
    paint.style = Paint.Style.FILL
    paint.alpha = 255

    drawTitle(canvas, paint, w, h)
}

private fun loadSilhouette(context: Context, w: Float, h: Float): Path {
    val scaleX = w / 3840f
    val scaleY = h / 2160f
    val path = Path()
    context.assets.open("space_cowboy_silhouette.txt").bufferedReader().useLines { lines ->
        lines.filter { it.isNotBlank() }.forEach { line ->
            val points = line.trim().split(' ').map { pair ->
                val (x, y) = pair.split(',')
                x.toFloat() * scaleX to y.toFloat() * scaleY
            }
            // Curve through the midpoints so the traced outline comes out smooth.
            fun mid(i: Int): Pair<Float, Float> {
                val a = points[i % points.size]
                val b = points[(i + 1) % points.size]
                return (a.first + b.first) / 2 to (a.second + b.second) / 2
            }
            val start = mid(0)
            path.moveTo(start.first, start.second)
            for (i in 1..points.size) {
                val control = points[i % points.size]
                val end = mid(i)
                path.quadTo(control.first, control.second, end.first, end.second)
            }
            path.close()
        }
    }
    return path
}

private fun drawSpace(canvas: Canvas, paint: Paint, w: Float, h: Float) {
    canvas.drawColor(color(0xFF04060D))

    // Faint nebula clouds so the dark isn't flat.
    fun nebula(x: Float, y: Float, r: Float, argb: Long) {
        paint.shader = RadialGradient(x, y, r, intArrayOf(color(argb), color(argb and 0x00FFFFFF)), null, Shader.TileMode.CLAMP)
        canvas.drawCircle(x, y, r, paint)
        paint.shader = null
    }
    nebula(w * 0.62f, h * 0.55f, h * 0.55f, 0x332A3C78)
    nebula(w * 0.92f, h * 0.75f, h * 0.45f, 0x2A1E5A6E)
    nebula(w * 0.35f, h * 0.08f, h * 0.35f, 0x24402A70)

    // The bright star cluster in the top right.
    val clusterX = w * 0.8f
    val clusterY = h * 0.15f
    nebula(clusterX, clusterY, h * 0.22f, 0x5552C8E0)

    val random = Random(2071)
    fun star(x: Float, y: Float, size: Float, brightness: Float) {
        paint.color = if (random.nextFloat() < 0.75f) color(0xFFBFF4FF) else color(0xFF7FE0F0)
        paint.alpha = (255 * brightness).toInt().coerceIn(30, 255)
        canvas.drawCircle(x, y, size, paint)
    }
    // A dense field of tiny stars.
    repeat(9000) {
        star(random.nextFloat() * w, random.nextFloat() * h, 0.8f + random.nextFloat().pow(3) * 2.2f, 0.25f + random.nextFloat() * 0.6f)
    }
    // Packed tighter around the cluster.
    repeat(2600) {
        val angle = random.nextFloat() * 6.2832f
        val distance = random.nextFloat().pow(1.8f) * h * 0.2f
        star(
            clusterX + kotlin.math.cos(angle) * distance * 1.4f,
            clusterY + kotlin.math.sin(angle) * distance,
            0.8f + random.nextFloat() * 1.6f,
            0.4f + random.nextFloat() * 0.6f,
        )
    }
    // A few big glowing stars.
    repeat(28) {
        val x = random.nextFloat() * w
        val y = random.nextFloat() * h
        val r = 5f + random.nextFloat() * 9f
        paint.color = color(0xFF8FEFFF)
        paint.alpha = 140
        paint.maskFilter = BlurMaskFilter(r * 2.5f, BlurMaskFilter.Blur.NORMAL)
        canvas.drawCircle(x, y, r * 2f, paint)
        paint.maskFilter = null
        paint.alpha = 255
        paint.color = color(0xFFE6FCFF)
        canvas.drawCircle(x, y, r * 0.6f, paint)
    }

    // Accent: a shooting star crossing the dark.
    val sx = w * 0.70f
    val sy = h * 0.30f
    paint.shader = LinearGradient(sx, sy, sx - w * 0.11f, sy - h * 0.06f, color(0xFFFFFFFF), color(0x00FF3D8A), Shader.TileMode.CLAMP)
    paint.strokeWidth = 4f
    paint.strokeCap = Paint.Cap.ROUND
    canvas.drawLine(sx, sy, sx - w * 0.11f, sy - h * 0.06f, paint)
    paint.shader = null
    paint.color = color(0xFFFFFFFF)
    paint.maskFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL)
    canvas.drawCircle(sx, sy, 9f, paint)
    paint.maskFilter = null
    paint.alpha = 255
}

private fun drawTitle(canvas: Canvas, paint: Paint, w: Float, h: Float) {
    val right = w * 0.94f
    val baseline = h * 0.43f

    // The title, like the end card of an episode.
    val title = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        textSize = h * 0.058f
        letterSpacing = 0.04f
        color = color(0xFFF1EFE8)
        textAlign = Paint.Align.RIGHT
        setShadowLayer(h * 0.012f, 0f, 0f, color(0x997FE0F0))
    }
    canvas.drawText("See you, Space Cowboy", right, baseline, title)

    // Accent: a pink underline and home's coordinates, Jersey City.
    val lineY = baseline + h * 0.028f
    paint.color = color(0xFFFF3D8A)
    paint.strokeWidth = 4f
    canvas.drawLine(right - w * 0.2f, lineY, right, lineY, paint)
    val caption = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        textSize = h * 0.019f
        letterSpacing = 0.18f
        color = color(0xCC9FE1EB)
        textAlign = Paint.Align.RIGHT
    }
    canvas.drawText("40.75° N  ·  74.05° W", right, lineY + h * 0.04f, caption)
}
