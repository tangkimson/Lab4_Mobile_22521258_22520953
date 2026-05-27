package com.ex.myapplication

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

class FiringObject(var x: Float, var y: Float, var speed: Float, var vx: Float = 0f) {
    private val paint: Paint = Paint()
    val width: Float = 20f
    val height: Float = 40f
    val rect: RectF

    init {
        paint.color = Color.YELLOW
        rect = RectF(x, y, x + width, y + height)
    }

    fun update() {
        x += vx
        y -= speed
        rect.set(x, y, x + width, y + height)
    }

    fun draw(canvas: Canvas) {
        canvas.drawRect(rect, paint)
    }

    fun isOffScreen(screenHeight: Int, screenWidth: Int = Int.MAX_VALUE): Boolean {
        return y + height < 0 || x + width < 0 || x > screenWidth
    }
}
