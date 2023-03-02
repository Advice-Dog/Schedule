package com.advice.glitch

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import kotlin.math.min
import kotlin.random.Random

class ColorChannelShift : GlitchEffect {

    enum class ColorChannel {
        RED,
        GREEN,
        BLUE
    }

    companion object {
        private val XFE_ADD = PorterDuffXfermode(PorterDuff.Mode.ADD)

        private const val WWIDTH: Int = 100
        private const val WHEIGHT: Int = 100
        private const val SMCOUNT: Int = (WWIDTH + 1) * (WHEIGHT + 1)

        // Tick Counter
        private var offset = 0
    }

    override fun apply(canvas: Canvas, bitmap: Bitmap, isGlitch: Boolean) {
        canvas.save()

        canvas.drawColor(Color.TRANSPARENT)

        val width = bitmap.width
        val height = bitmap.height

        canvas.translate(canvas.width / 2f - width / 2f, canvas.height / 2f - height / 2f)

        canvas.drawBitmap(modify(ColorChannel.RED, isGlitch), redPaint)
        canvas.drawBitmap(modify(ColorChannel.GREEN, isGlitch), greenPaint)
        canvas.drawBitmap(modify(ColorChannel.BLUE, isGlitch), bluePaint)

        offset++

        try {
            canvas.restore()
        } catch (ex: Exception) {

        }
    }

    private val redPaint = getPaint(ColorChannel.RED)
    private val greenPaint = getPaint(ColorChannel.GREEN)
    private val bluePaint = getPaint(ColorChannel.BLUE)

    private val bitmap: Bitmap
    private val matrixOriginal = FloatArray(SMCOUNT * 2)

    constructor(bitmap: Bitmap) {
        this.bitmap = bitmap
        init()
    }

    private val maxHorizontalOffset: Float
        get() = min(100f, 20f)

    private val maxVerticalOffset: Float
        get() = bitmap.height * 0.00f

    fun init() {
        initMatrix()
    }

    private fun initMatrix() {
        val width = bitmap.width
        val height = bitmap.height

        var i = 0
        for (i2 in 0..(WHEIGHT)) {
            val f = ((height * i2).div(WHEIGHT)).toFloat()
            for (i3 in 0..(WWIDTH)) {
                val f2 = ((width * i3).div(WWIDTH)).toFloat()
                matrixOriginal[i * 2] = f2
                matrixOriginal[i * 2 + 1] = f
                i += 1
            }
        }
    }

    private fun modify(channel: ColorChannel, isGlitch: Boolean): FloatArray {
        val random = Random(offset)

        val isNormal = !isGlitch || random.nextBoolean()

        val matrix = matrixOriginal.clone()

        if (isNormal) {
            return matrix
        }

        val areas = listOf(
            IntRange(0, 5000) to random.nextInt(3) - 1,
            IntRange(5000, 10000) to random.nextInt(3) - 1,
            IntRange(10000, 12000) to random.nextInt(3) - 1,
            IntRange(12000, 18000) to random.nextInt(3) - 1
        )

        val xOffset = random.nextFloat() * maxHorizontalOffset
        val yOffset = random.nextFloat() * maxVerticalOffset

        synchronized(this) {
            for (index in 0 until (SMCOUNT * 2) step 2) {
                when (channel) {
                    ColorChannel.RED -> {
                        // shift left
                        matrix[index] = matrix[index] - xOffset
                        matrix[index + 1] = matrix[index + 1] + yOffset
                    }
                    ColorChannel.GREEN -> {
                        // do nothing
                    }
                    ColorChannel.BLUE -> {
                        // shift right
                        matrix[index] = matrix[index] + xOffset
                        matrix[index + 1] = matrix[index + 1] + yOffset
                    }
                }

                val shift = areas.find { index in it.first }?.second ?: 0

                if (shift != 0) {
                    if (shift > 0) {
                        matrix[index] = matrix[index] + Random(offset).nextInt(50)
                    } else if (shift < 0) {
                        matrix[index] = matrix[index] - Random(offset).nextInt(50)
                    }
                }
            }

        }
        return matrix
    }

    private fun Canvas.drawBitmap(matrix: FloatArray, paint: Paint) {
        drawBitmapMesh(bitmap, WWIDTH, WHEIGHT, matrix, 0, null, 0, paint)
    }

    private fun getPaint(channel: ColorChannel): Paint {
        return Paint().apply {
            isFilterBitmap = true
            xfermode = XFE_ADD
            colorFilter = ColorMatrixColorFilter(ColorMatrix().apply {
                set(getMatrix(channel))
            })
        }
    }

    private fun getMatrix(channel: ColorChannel): FloatArray {
        val matrix = Array(20) { 0.0f }
        val index = when (channel) {
            ColorChannel.RED -> 0
            ColorChannel.GREEN -> 6
            ColorChannel.BLUE -> 12
        }
        matrix[index] = 1.0f
        matrix[18] = 1.0f
        return matrix.toFloatArray()
    }

}