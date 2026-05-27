package com.ex.myapplication

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF

class Boss(
    private val bitmap: Bitmap,
    screenWidth: Int
) {
    val width: Float = bitmap.width.toFloat()
    val height: Float = bitmap.height.toFloat()

    var x: Float = (screenWidth - width) / 2f
    val y: Float = 60f
    private var dx: Float = 5f
    private val rect: RectF = RectF(x, y, x + width, y + height)

    var spawnTimer: Int = 0
    private val spawnInterval: Int = 90

    fun update(screenWidth: Int) {
        x += dx
        if (x <= 0f || x + width >= screenWidth) {
            dx = -dx
            x = x.coerceIn(0f, screenWidth - width)
        }
        rect.set(x, y, x + width, y + height)
    }

    fun shouldSpawn(): Boolean {
        spawnTimer++
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0
            return true
        }
        return false
    }

    fun getCenterX(): Float = x + width / 2f
    fun getCenterY(): Float = y + height / 2f

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, null, rect, null)
    }

    fun getRect(): RectF = rect
}
