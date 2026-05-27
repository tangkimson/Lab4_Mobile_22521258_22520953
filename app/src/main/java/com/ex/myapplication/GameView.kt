package com.ex.myapplication

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView(context: Context, attrs: AttributeSet? = null) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    private val backgroundBitmap: Bitmap =
        BitmapFactory.decodeResource(resources, R.drawable.galaxy_background)
    private var backgroundScaledBitmap: Bitmap? = null

    private val thread: GameThread
    private val gameManager: GameManager

    private val firingObjects = mutableListOf<FiringObject>()
    private val opponents = mutableListOf<Opponent>()
    private var boss: Boss? = null

    private var score: Int = 0
    private var highScore: Int = 0
    private var lives: Int = 3
    private var gameOver: Boolean = false

    private var opponentBaseSpeed = 5f
    private var firingObjectBaseSpeed = 20f
    private var bulletLevel = 1

    private var playerX = 0f
    private var playerY = 0f
    private var playerShipBitmap: Bitmap
    private val playerShipWidth: Float
        get() = playerShipBitmap.width.toFloat()
    private val playerShipHeight: Float
        get() = playerShipBitmap.height.toFloat()

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val scorePaint = Paint().apply {
        color = Color.WHITE
        textSize = 50f
    }

    private val hudPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
    }

    private val gameOverPaint = Paint().apply {
        color = Color.RED
        textSize = 100f
    }

    private val hintPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
    }

    init {
        holder.addCallback(this)
        thread = GameThread(holder, this)
        gameManager = GameManager(context)
        highScore = prefs.getInt(KEY_HIGH_SCORE, 0)
        playerShipBitmap = gameManager.createPlayerShipBitmap()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        backgroundScaledBitmap = Bitmap.createScaledBitmap(backgroundBitmap, width, height, true)
        playerX = (width - playerShipWidth) / 2f
        playerY = height - playerShipHeight - 40f
        if (boss == null && width > 0) {
            boss = gameManager.createBoss(width)
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        var retry = true
        thread.running = false
        while (retry) {
            try {
                thread.join()
                retry = false
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        thread.running = true
        thread.start()
    }

    @Synchronized
    fun update() {
        if (gameOver) return

        opponentBaseSpeed += score * 0.0001f
        firingObjectBaseSpeed += score * 0.0005f

        val maxSpeed = 15f
        if (opponentBaseSpeed > maxSpeed) opponentBaseSpeed = maxSpeed
        if (firingObjectBaseSpeed > maxSpeed) firingObjectBaseSpeed = maxSpeed

        bulletLevel = (1 + score / 50).coerceAtMost(5)

        firingObjects.forEach { it.update() }
        firingObjects.removeAll { it.isOffScreen(height, width) }

        opponents.forEach { it.update() }
        opponents.removeAll { it.isOffScreen(height) }

        val firingObjectsCopy = ArrayList(firingObjects)
        val opponentsCopy = ArrayList(opponents)

        for (firingObject in firingObjectsCopy) {
            for (opponent in opponentsCopy) {
                if (RectF.intersects(firingObject.rect, opponent.getRect())) {
                    firingObjects.remove(firingObject)
                    if (opponent.takeDamage(1)) {
                        opponents.remove(opponent)
                        score += 10
                        updateHighScore()
                    }
                    break
                }
            }
        }

        val boundaryY = height - 100f
        val reachedBoundary = opponents.filter { it.y + it.height >= boundaryY }
        for (opponent in reachedBoundary) {
            opponents.remove(opponent)
            lives--
        }
        if (lives <= 0) {
            gameOver = true
            updateHighScore()
        }

        boss?.let { currentBoss ->
            currentBoss.update(width)
            if (currentBoss.shouldSpawn()) {
                val minion = gameManager.createMinion(
                    currentBoss.getCenterX(),
                    currentBoss.getCenterY() + currentBoss.height / 2f,
                    opponentBaseSpeed,
                    width
                )
                opponents.add(minion)
            }
        }
    }

    @Synchronized
    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        drawBackground(canvas)

        boss?.draw(canvas)
        opponents.forEach { it.draw(canvas) }
        firingObjects.forEach { it.draw(canvas) }

        canvas.drawBitmap(playerShipBitmap, null, RectF(playerX, playerY, playerX + playerShipWidth, playerY + playerShipHeight), null)

        canvas.drawText("Score: $score", 50f, 80f, scorePaint)
        canvas.drawText("High Score: $highScore", 50f, 130f, hudPaint)
        canvas.drawText("Lives: $lives", 50f, 180f, hudPaint)
        canvas.drawText("Level: $bulletLevel", 50f, 230f, hudPaint)

        if (gameOver) {
            canvas.drawText("Game Over", width / 2f - 250f, height / 2f, gameOverPaint)
            canvas.drawText("Tap to restart", width / 2f - 180f, height / 2f + 80f, hintPaint)
        }
    }

    private fun drawBackground(canvas: Canvas) {
        backgroundScaledBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (gameOver) {
            if (event.action == MotionEvent.ACTION_DOWN) {
                resetGame()
                return true
            }
            return super.onTouchEvent(event)
        }

        if (event.action == MotionEvent.ACTION_DOWN) {
            playerX = (event.x - playerShipWidth / 2f).coerceIn(0f, width - playerShipWidth)
            val bulletX = playerX + playerShipWidth / 2f - 10f
            val bulletY = playerY
            fireBullets(bulletX, bulletY)
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun fireBullets(centerX: Float, centerY: Float) {
        when (bulletLevel) {
            1 -> addBullet(centerX, centerY, 0f)
            2 -> {
                addBullet(centerX, centerY, 0f)
                addBullet(centerX - 25f, centerY, -3f)
                addBullet(centerX + 25f, centerY, 3f)
            }
            3 -> {
                addBullet(centerX, centerY, 0f)
                addBullet(centerX - 30f, centerY, -5f)
                addBullet(centerX + 30f, centerY, 5f)
            }
            4 -> {
                addBullet(centerX, centerY, 0f)
                addBullet(centerX - 35f, centerY, -6f)
                addBullet(centerX + 35f, centerY, 6f)
                addBullet(centerX - 15f, centerY, -2f)
            }
            else -> {
                addBullet(centerX, centerY, 0f)
                addBullet(centerX - 40f, centerY, -7f)
                addBullet(centerX + 40f, centerY, 7f)
                addBullet(centerX - 20f, centerY, -3f)
                addBullet(centerX + 20f, centerY, 3f)
            }
        }
    }

    private fun addBullet(x: Float, y: Float, vx: Float) {
        synchronized(this) {
            firingObjects.add(FiringObject(x, y, firingObjectBaseSpeed, vx))
        }
    }

    private fun updateHighScore() {
        if (score > highScore) {
            highScore = score
            prefs.edit().putInt(KEY_HIGH_SCORE, highScore).apply()
        }
    }

    private fun resetGame() {
        score = 0
        lives = 3
        bulletLevel = 1
        opponentBaseSpeed = 5f
        firingObjectBaseSpeed = 20f
        gameOver = false
        synchronized(this) {
            firingObjects.clear()
            opponents.clear()
        }
        boss = gameManager.createBoss(width)
        playerX = (width - playerShipWidth) / 2f
        playerY = height - playerShipHeight - 40f
    }

    companion object {
        private const val PREFS_NAME = "lab4_game_prefs"
        private const val KEY_HIGH_SCORE = "high_score"
    }
}
