package com.example.tva0001

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RadialGradient
import android.graphics.Shader
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

/**
 * The synthwave sunset wallpaper, drawn in code.
 *
 * The TV draws app screens at 1920x1080 and stretches them to the 4K panel.
 * A SurfaceView is its own layer, so we can ask for a full 3840x2160 buffer
 * and the sunset stays sharp while the tiles and text sit on top.
 */
class SunsetWallpaperView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    init {
        holder.setFormat(PixelFormat.RGBA_8888)
        holder.setFixedSize(WIDTH, HEIGHT)
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) = Unit

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // A still picture, so drawing it once is enough.
        val canvas = holder.lockCanvas() ?: return
        try {
            drawSunset(canvas, width.toFloat(), height.toFloat())
        } finally {
            holder.unlockCanvasAndPost(canvas)
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) = Unit

    companion object {
        private const val WIDTH = 3840
        private const val HEIGHT = 2160
    }
}

private fun color(hex: Long) = hex.toInt()

private fun drawSunset(canvas: Canvas, w: Float, h: Float) {
    val horizon = h * 0.63f
    val sunX = w * 0.5f
    val sunY = h * 0.45f
    val sunR = h * 0.23f
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Sky: deep blue-purple at the top, warming to magenta at the horizon.
    paint.shader = LinearGradient(
        0f, 0f, 0f, horizon,
        intArrayOf(color(0xFF262040), color(0xFF4E2650), color(0xFF7E2A5E), color(0xFF9A2C62)),
        floatArrayOf(0f, 0.4f, 0.8f, 1f),
        Shader.TileMode.CLAMP,
    )
    canvas.drawRect(0f, 0f, w, horizon, paint)

    // Wide soft glow around the sun.
    paint.shader = RadialGradient(
        sunX, sunY, sunR * 2.6f,
        intArrayOf(color(0x99F0407F), color(0x40C02E6A), color(0x00C02E6A)),
        floatArrayOf(0f, 0.5f, 1f),
        Shader.TileMode.CLAMP,
    )
    canvas.drawCircle(sunX, sunY, sunR * 2.6f, paint)

    // Hot rim just outside the sun's edge.
    paint.shader = null
    paint.color = color(0xFFF2387E)
    paint.maskFilter = BlurMaskFilter(sunR * 0.12f, BlurMaskFilter.Blur.NORMAL)
    canvas.drawCircle(sunX, sunY, sunR * 1.04f, paint)
    paint.maskFilter = null

    // The sun: pale pink in the middle, deeper pink at the edge.
    paint.shader = RadialGradient(
        sunX, sunY + sunR * 0.25f, sunR * 1.1f,
        intArrayOf(color(0xFFF4A6D8), color(0xFFF378B4), color(0xFFF05294)),
        floatArrayOf(0f, 0.55f, 1f),
        Shader.TileMode.CLAMP,
    )
    canvas.drawCircle(sunX, sunY, sunR, paint)
    paint.shader = null

    drawGround(canvas, paint, w, h, horizon)
    drawSunReflection(canvas, paint, sunX, horizon, w, h)
    drawWindingPath(canvas, paint, w, h, horizon)

    // Mountains on each side, with the valley opening onto the sun.
    drawMountain(canvas, paint, w, horizon, fromX = -0.02f, toX = 0.46f, peakX = 0.27f, peakY = 0.335f, seed = 7)
    drawMountain(canvas, paint, w, horizon, fromX = 0.54f, toX = 1.02f, peakX = 0.75f, peakY = 0.345f, seed = 11)

    // Darken the corners a little so the eye goes to the middle.
    paint.shader = RadialGradient(
        w / 2, h / 2, w * 0.65f,
        intArrayOf(color(0x00000000), color(0x00000000), color(0x66000000)),
        floatArrayOf(0f, 0.6f, 1f),
        Shader.TileMode.CLAMP,
    )
    canvas.drawRect(0f, 0f, w, h, paint)
    paint.shader = null
}

private fun drawGround(canvas: Canvas, paint: Paint, w: Float, h: Float, horizon: Float) {
    paint.shader = LinearGradient(
        0f, horizon, 0f, h,
        intArrayOf(color(0xFF3E1A3E), color(0xFF2A1030), color(0xFF1A0A1C)),
        floatArrayOf(0f, 0.4f, 1f),
        Shader.TileMode.CLAMP,
    )
    canvas.drawRect(0f, horizon, w, h, paint)
    paint.shader = null

    // Gently rolling contour lines, closer together near the horizon for depth.
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 2.5f
    val lines = 46
    for (i in 1..lines) {
        val t = i / lines.toFloat()
        val y = horizon + (h - horizon) * t.pow(1.8f)
        paint.color = color(0xFF7C4BB0)
        paint.alpha = (60 + 110 * t).toInt()
        val path = Path()
        var x = 0f
        while (x <= w) {
            val wave = sin(x / w * 2 * PI * 1.3 + i * 0.35).toFloat() * (6f + 26f * t) +
                sin(x / w * 2 * PI * 3.1 + i).toFloat() * (3f + 10f * t)
            if (x == 0f) path.moveTo(x, y + wave) else path.lineTo(x, y + wave)
            x += 24f
        }
        canvas.drawPath(path, paint)
    }
    paint.style = Paint.Style.FILL
    paint.alpha = 255
}

private fun drawSunReflection(canvas: Canvas, paint: Paint, sunX: Float, horizon: Float, w: Float, h: Float) {
    // Bright glow where the sun meets the ground.
    paint.color = color(0xFFFF3D8A)
    paint.alpha = 200
    paint.maskFilter = BlurMaskFilter(h * 0.025f, BlurMaskFilter.Blur.NORMAL)
    canvas.drawOval(sunX - w * 0.07f, horizon - h * 0.012f, sunX + w * 0.07f, horizon + h * 0.03f, paint)
    paint.alpha = 255

    // The reflection, fading out towards the viewer.
    val half = w * 0.035f
    paint.shader = LinearGradient(
        0f, horizon, 0f, h,
        intArrayOf(color(0xAAFF3D8A), color(0x44D02A60), color(0x00D02A60)),
        floatArrayOf(0f, 0.4f, 1f),
        Shader.TileMode.CLAMP,
    )
    paint.maskFilter = BlurMaskFilter(half * 1.6f, BlurMaskFilter.Blur.NORMAL)
    canvas.drawRect(sunX - half, horizon, sunX + half, h, paint)
    paint.maskFilter = null
    paint.shader = null
}

private fun drawWindingPath(canvas: Canvas, paint: Paint, w: Float, h: Float, horizon: Float) {
    // A neon trail that loops across the ground.
    val path = Path().apply {
        moveTo(-w * 0.02f, h * 0.83f)
        cubicTo(w * 0.25f, h * 0.79f, w * 0.45f, h * 0.70f, w * 0.62f, h * 0.715f)
        cubicTo(w * 0.85f, h * 0.735f, w * 0.80f, h * 0.775f, w * 0.68f, h * 0.765f)
        cubicTo(w * 0.55f, h * 0.755f, w * 0.40f, h * 0.745f, w * 0.30f, h * 0.73f)
    }
    paint.style = Paint.Style.STROKE
    paint.strokeCap = Paint.Cap.ROUND
    paint.color = color(0xFFFF2E95)
    paint.strokeWidth = 14f
    paint.alpha = 110
    paint.maskFilter = BlurMaskFilter(14f, BlurMaskFilter.Blur.NORMAL)
    canvas.drawPath(path, paint)
    paint.maskFilter = null
    paint.alpha = 255
    paint.strokeWidth = 4f
    canvas.drawPath(path, paint)
    paint.style = Paint.Style.FILL
}

/** A jagged wireframe mountain between [fromX] and [toX] (fractions of the width). */
private fun drawMountain(
    canvas: Canvas, paint: Paint, w: Float, horizon: Float,
    fromX: Float, toX: Float, peakX: Float, peakY: Float, seed: Int,
) {
    val h = horizon / 0.63f
    val random = Random(seed)
    val knots = FloatArray(24) { random.nextFloat() - 0.5f }
    val steps = 240
    val peakHeight = horizon - peakY * h
    // The ridge: a broad shape that slopes down to the valley on the inner side and
    // stays up as lower hills at the screen edge, roughened with smooth bumps.
    val ridge = FloatArray(steps + 1) { i ->
        val fx = fromX + (toX - fromX) * i / steps
        val towardsSun = if (fromX < 0.5f) fx > peakX else fx < peakX
        val reach = if (fx > peakX) toX - peakX else peakX - fromX
        val d = (abs(fx - peakX) / reach).coerceIn(0f, 1f)
        val shape = if (towardsSun) (1f - d).pow(1.15f) else 0.22f + 0.78f * (1f - d).pow(1.3f)
        val u = i / steps.toFloat()
        val bumps = smoothNoise(knots, u * 9f) * 0.22f + smoothNoise(knots, u * 23f + 5f) * 0.07f
        val jitter = (random.nextFloat() - 0.5f) * 0.008f
        horizon - (peakHeight * shape * (1f + bumps) + h * jitter * shape).coerceAtLeast(0f)
    }
    fun xAt(i: Int) = w * (fromX + (toX - fromX) * i / steps)

    val outline = Path().apply {
        moveTo(xAt(0), horizon)
        for (i in 0..steps) lineTo(xAt(i), ridge[i])
        lineTo(xAt(steps), horizon)
        close()
    }
    paint.shader = LinearGradient(
        0f, peakY * h, 0f, horizon,
        intArrayOf(color(0xFF2C1538), color(0xFF1C0D27)),
        null,
        Shader.TileMode.CLAMP,
    )
    canvas.drawPath(outline, paint)
    paint.shader = null

    // Wireframe contour lines that follow the ridge down to the ground.
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 2f
    val contours = 22
    for (c in 0 until contours) {
        val t = 1f - c / contours.toFloat()
        paint.color = color(0xFF6E45A8)
        paint.alpha = (40 + 120 * t).toInt()
        val line = Path()
        for (i in 0..steps) {
            val y = horizon - (horizon - ridge[i]) * t
            if (i == 0) line.moveTo(xAt(i), y) else line.lineTo(xAt(i), y)
        }
        canvas.drawPath(line, paint)
    }
    // A brighter edge along the ridge itself.
    paint.color = color(0xFF8A5AC8)
    paint.alpha = 200
    paint.strokeWidth = 3f
    val edge = Path().apply {
        moveTo(xAt(0), ridge[0])
        for (i in 1..steps) lineTo(xAt(i), ridge[i])
    }
    canvas.drawPath(edge, paint)
    paint.style = Paint.Style.FILL
    paint.alpha = 255
}

/** Smooth random wobble between -0.5 and 0.5, the same every time for a given [knots]. */
private fun smoothNoise(knots: FloatArray, x: Float): Float {
    val i = x.toInt()
    val f = x - i
    val a = knots[i % knots.size]
    val b = knots[(i + 1) % knots.size]
    val blend = (1 - kotlin.math.cos(f * PI.toFloat())) / 2
    return a + (b - a) * blend
}
