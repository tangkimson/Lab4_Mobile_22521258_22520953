package com.ex.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import kotlin.random.Random

class GameManager(private val context: Context) {

    private val minionBitmap: Bitmap by lazy {
        val source = BitmapFactory.decodeResource(context.resources, R.drawable.alian)
        Bitmap.createScaledBitmap(source, 80, 80, false)
    }

    private val bossBitmap: Bitmap by lazy {
        val source = BitmapFactory.decodeResource(context.resources, R.drawable.alian)
        Bitmap.createScaledBitmap(source, 200, 200, false)
    }

    private val opponentBitmaps: List<Bitmap> by lazy {
        listOf(
            R.drawable.rocket,
            R.drawable.rocket_2,
            R.drawable.alian,
            R.drawable.token_red_emovebg
        ).mapNotNull { resId ->
            BitmapFactory.decodeResource(context.resources, resId)?.let { bmp ->
                Bitmap.createScaledBitmap(bmp, 100, 100, false)
            }
        }
    }

    fun createOpponent(speed: Float, screenWidth: Int): Opponent {
        val laneIndex = Random.nextInt(Opponent.LANE_COUNT)
        val laneWidth = screenWidth.toFloat() / Opponent.LANE_COUNT
        val x = laneIndex * laneWidth + laneWidth / 2f - 50f
        val bitmap = opponentBitmaps.random()
        return Opponent(x, -80f, speed, bitmap, laneIndex, screenWidth)
    }

    fun createMinion(x: Float, y: Float, speed: Float, screenWidth: Int): Opponent {
        val laneIndex = (x / (screenWidth.toFloat() / Opponent.LANE_COUNT))
            .toInt()
            .coerceIn(0, Opponent.LANE_COUNT - 1)
        return Opponent(x - 40f, y, speed, minionBitmap, laneIndex, screenWidth)
    }

    fun createBoss(screenWidth: Int): Boss {
        return Boss(bossBitmap, screenWidth)
    }

    fun createPlayerShipBitmap(): Bitmap {
        val source = BitmapFactory.decodeResource(context.resources, R.drawable.rocket_2)
        val scaled = Bitmap.createScaledBitmap(source, 90, 90, false)
        val matrix = Matrix().apply { postRotate(180f) }
        return Bitmap.createBitmap(scaled, 0, 0, scaled.width, scaled.height, matrix, true)
    }
}
