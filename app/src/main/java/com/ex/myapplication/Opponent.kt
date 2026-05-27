package com.ex.myapplication

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.abs

class Opponent(
    var x: Float,
    var y: Float,
    var speed: Float,
    private val bitmap: Bitmap,
    val laneIndex: Int,
    screenWidth: Int,
    laneCount: Int = LANE_COUNT
) {
    val width: Float = bitmap.width.toFloat()
    val height: Float = bitmap.height.toFloat()
    private val rect: RectF = RectF(x, y, x + width, y + height)

    val maxHealth: Int
    var currentHealth: Int

    private val laneWidth: Float = screenWidth.toFloat() / laneCount
    private val laneCenterX: Float = laneIndex * laneWidth + laneWidth / 2f - width / 2f

    private val healthBgPaint = Paint().apply { color = Color.RED }
    private val healthFgPaint = Paint().apply { color = Color.GREEN }

    init {
        maxHealth = (2..5).random()
        currentHealth = maxHealth
    }

    fun update() {
        y += speed

        val diff = laneCenterX - x
        if (abs(diff) > 1f) {
            x += diff.coerceIn(-4f, 4f)
        }

        rect.set(x, y, x + width, y + height)
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, null, rect, null)
        drawHealthBar(canvas)
    }

    private fun drawHealthBar(canvas: Canvas) {
        val barWidth = width
        val barHeight = 8f
        val left = x
        val top = y - barHeight - 4f
        val healthRatio = currentHealth.toFloat() / maxHealth.toFloat()

        canvas.drawRect(left, top, left + barWidth, top + barHeight, healthBgPaint)
        canvas.drawRect(left, top, left + barWidth * healthRatio, top + barHeight, healthFgPaint)
    }

    fun takeDamage(damage: Int): Boolean {
        currentHealth -= damage
        return currentHealth <= 0
    }

    fun isOffScreen(screenHeight: Int): Boolean {
        return y > screenHeight
    }

    fun getRect(): RectF = rect

    companion object {
        const val LANE_COUNT = 5
    }
}
