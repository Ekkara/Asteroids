package com.gmail.erikephggustafsson.glasteroids

import android.graphics.PointF
import android.opengl.GLES20
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

const val TO_RADIANS = PI.toFloat() / 180.0f
const val THRUST = 8f
const val DRAG = 0.99f

const val TIME_BETWEEN_SHOTS = 0.2f //seconds. TO DO: game play setting!
const val BULLET_COUNT = (BULLET_TIME_TO_LIVE / TIME_BETWEEN_SHOTS).toInt() + 1

private const val ROTATION_VELOCITY = 360f //TODO: game play values!
private const val MAX_SPEED = 50f
private const val START_HEALTH = 3

object PlayerShipShape {
    val verts = floatArrayOf( // in counterclockwise order:
        0.0f, 0.5f, 0.0f, // top
        -0.5f, -0.5f, 0.0f, // bottom left
        0.0f, -0.3f, 0.0f, //back (up)
        0.0f, -0.3f, 0.0f, //back (up)
        0.5f, -0.5f, 0.0f, // bottom right
        0.0f, 0.5f, 0.0f // top
    )
    val mesh = Mesh(verts, GLES20.GL_TRIANGLES)
}

class Player(val startPos: PointF) : GLEntity() {
    private val TAG = "Player"
    var _bullets = ArrayList<Bullet>(BULLET_COUNT)
    var _inputs = InputManager()

    private var _bulletCooldown = 0f

    private val flame = GLEntity()

    var remainingHealth = START_HEALTH
    val healthTextIndex = engine._hud.addTextElement(5f, 8f)

    fun setControls(input: InputManager) {
        _inputs.onPause()
        _inputs.onStop()
        _inputs = input
        _inputs.onResume()
        _inputs.onStart()
    }

    init {
        _pos.x = startPos.x
        _pos.y = startPos.y
        _width = 8f; //todo: gameplay values! move to configs
        _height = 12f;
        _mesh = Mesh(PlayerShipShape.verts.clone(), GLES20.GL_TRIANGLES)
        _mesh.flipY()
        _rotation = 90f
        _mesh.setWidthHeight(_width, _height)



        flame._mesh = Mesh(Triangle.verts.clone(), GLES20.GL_TRIANGLES)
        flame._mesh.setWidthHeight(4.8f, 8f)//numbers generated by trial and error, is it better to hardcode the shape instead of scaling it?
        flame.setColors(0.2f, 0.7f, 1f, 1f) //todo: gameplay values

        updateHealthText()
        for (i in 0 until BULLET_COUNT) {
            _bullets.add(Bullet(0.2f, 0.7f, 1f))
        }
    }

    override fun update(dt: Float) {
        _inputs.syncronize()
        _rotation += dt * ROTATION_VELOCITY * _inputs._horizontalFactor
        if(_inputs._pressingB){
            val theta = _rotation * TO_RADIANS
            _vel.x += sin(theta) * THRUST
            _vel.y -= cos(theta) * THRUST
        }

        _bulletCooldown -= dt;
        if (_inputs._buttonA._isJustPress && _bulletCooldown <= 0f) {//fire
            if (Bullet.maybeFireBullet(this, _bullets)) {
                _bulletCooldown = TIME_BETWEEN_SHOTS;
                engine.onGameEvent(GameEvent.Fire)

            }
        }
        _vel.x *= DRAG
        _vel.y *= DRAG

        if (_vel.length() > MAX_SPEED) {
            _vel = Utils.setLengthOfVector2(_vel, MAX_SPEED)
        }
        super.update(dt)
        Camera.moveCameraTowards(_pos)
        for(b in _bullets){
            b.update(dt)
        }

    }

    override fun isColliding(that: GLEntity): Boolean {
        if (!areBoundingSpheresOverlapping(this, that)) {
            return false
        }
        val shipHull = getPointList()
        val thatPoints = that.getPointList()
        if (polygonVsPolygon(shipHull, thatPoints)) {
            return true
        }
        return polygonVsPoint(thatPoints,
            _pos.x,
            _pos.y) //finally, check if we're inside the asteroid
    }

    fun updateHealthText(){
        var hp = ""
        for(i in 0 until remainingHealth){
            hp += "<3"
        }
        engine._hud.updateDisplayText(healthTextIndex,"HP:" + hp)
    }
    override fun onCollision(that: GLEntity) {
        super.onCollision(that)

        if(that is Asteroid||
                that is Enemy||
                that is Bullet){//since i have nothing else, remove?
            remainingHealth--
            updateHealthText()
            engine.onGameEvent(GameEvent.TakeDamge)
            if(remainingHealth <= 0){
                engine.isGameOver=true
            }
        }
    }
    fun gameOver(){
        remainingHealth = START_HEALTH
        updateHealthText()
        _pos.x = startPos.x
        _pos.y = startPos.y
        _vel.x = 0f
        _vel.y = 0f
        _rotation = 90f
    }

    var renderFlame = 0
    override fun render(viewportMatrix: FloatArray) {
        //todo: maybe separate update logic with draw logic.
        if (_inputs._pressingB) {
            renderFlame++
            if (renderFlame > 10) {
                //place flame mesh behind the player
                val forwardVector = Utils.normilizeVector(PointF(
                    sin(_rotation * TO_RADIANS),
                    -cos(_rotation * TO_RADIANS)))

                flame._pos = PointF(_pos.x - 5f * forwardVector.x,
                    _pos.y - 5f * forwardVector.y)

                flame._rotation = _rotation
                //render the flame before the player to hide parts of the flame
                //could define a similar reverted shape as the player to not require this
                //but this is easier and gives the same end result
                flame.render(viewportMatrix)
                if (renderFlame > 20) {
                    renderFlame = 0
                    engine.onGameEvent(GameEvent.Boost)
                }
            }
        }
        for (b in _bullets) {
            if (!b.isDead()) b.render(Camera._viewportMatrix)
        }
        super.render(viewportMatrix)
    }
    fun clearBullets(){
        for(bullet in _bullets){
            bullet.killBullet()
        }
    }
}