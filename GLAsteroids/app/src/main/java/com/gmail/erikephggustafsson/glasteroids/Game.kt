package com.gmail.erikephggustafsson.glasteroids

import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.random.Random
import android.graphics.*
import android.opengl.GLSurfaceView.Renderer
import android.os.SystemClock.uptimeMillis
import android.view.SurfaceView
import android.widget.TextView


private const val STAR_COUNT = 100
private const val SECOND_IN_NANOSECONDS: Long = 1000000000
private const val MILLISECOND_IN_NANOSECONDS: Long = 1000000
private const val NANOSECONDS_TO_MILLISECONDS = 1.0f / MILLISECOND_IN_NANOSECONDS
private const val NANOSECONDS_TO_SECONDS = 1.0f / SECOND_IN_NANOSECONDS


lateinit var engine: Game


class Game(ctx: Context, attrs: AttributeSet? = null) : GLSurfaceView(ctx, attrs),
    Renderer {
    val TAG = "GAME_"

    fun hexColorToFloat(hex: Int) = hex / 255f
    var isGameOver: Boolean = false
    val _hud = HUD()
    val _particleManager = ParticleManager()

    private val _stars = ArrayList<Star>()
    private val _jukebox = Jukebox(this)

    init {
        engine = this
        for (i in 0 until STAR_COUNT) {
            val x = Random.nextInt(WORLD_WIDTH.toInt()).toFloat()
            val y = Random.nextInt(WORLD_HEIGHT.toInt()).toFloat()
            _stars.add(Star(x, y))
        }
        setEGLContextClientVersion(2)
        setRenderer(this)
    }

    val camera = Camera()
    val _player = Player(PointF(10f, WORLD_HEIGHT / 2))
    val _levelManager = LevelManager(context.applicationContext)

    fun getActivity() = context as MainActivity
    fun getAssets() = context.assets

    private val _border = Border(WORLD_WIDTH / 2,
        WORLD_HEIGHT / 2,
        WORLD_WIDTH,
        WORLD_HEIGHT)

    private val bgColor = Color.rgb(0, 0, 0)

    override fun onPause() {
        _player._inputs.onPause()
        _jukebox.pauseBgMusic()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        _jukebox.resumeBgMusic()
        currentTime = System.nanoTime() * NANOSECONDS_TO_SECONDS
    }


    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        val red = hexColorToFloat(bgColor.red)
        val green = hexColorToFloat(bgColor.green)
        val blue = hexColorToFloat(bgColor.blue)
        val alpha = 1.0f
        GLES20.glClearColor(red, green, blue, alpha)
        GLManager.buildProgram()
        //resume()

    }


    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        camera.changeViewport(width, height)
    }

    override fun onDrawFrame(p0: GL10?) {
        update()
        render()
    }

    fun onGameEvent(event: GameEvent) {
        _jukebox.addEventToQueue(event)
    }

    fun gameOver() {
        _levelManager.gameOver()
        _player.gameOver()
        isGameOver = false
    }


    //trying a fixed time-step with accumulator, courtesy of
    //https://gafferongames.com/post/fix_your_timestep/Links to an external site.
    val dt = 0.01f
    var accumulator = 0.0f
    var currentTime = (System.nanoTime() * NANOSECONDS_TO_SECONDS).toFloat()
    private fun update() {
        val newTime = (System.nanoTime() * NANOSECONDS_TO_SECONDS).toFloat()
        val frameTime = newTime - currentTime
        currentTime = newTime
        accumulator += frameTime
        while (accumulator >= dt) {
            _levelManager.update(dt)
            _player.update(dt)
            _particleManager.update(dt)

            //late update
            _jukebox.playSoundsInQueue()
            _levelManager.collision(_player)
            _levelManager.addAndRemoveEntities()
            if (isGameOver) gameOver()
            accumulator -= dt
        }
    }

    private fun render() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        camera.draw()

        for (s in _stars) {
            s.render(Camera._viewportMatrix)
        }
        _levelManager.render(Camera._viewportMatrix)
        _player.render(Camera._viewportMatrix)
        _particleManager.render(Camera._viewportMatrix)
        _border.render(Camera._viewportMatrix)
        _hud.render(Camera._viewportMatrix)
    }
}