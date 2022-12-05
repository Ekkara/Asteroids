package com.gmail.erikephggustafsson.glasteroids

import android.graphics.PointF
import android.opengl.GLES20
import android.util.Log
import kotlin.math.atan2

private const val VEL = 20f
private const val BULLET_RECHARGE_TIME = 2.0f
private const val AMOUNT_OF_BULLETS = (BULLET_TIME_TO_LIVE / BULLET_RECHARGE_TIME).toInt() + 1

object EnemyShape {
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

class Enemy(x: Float, y: Float) : GLEntity() {
    var _bullets = ArrayList<Bullet>(AMOUNT_OF_BULLETS)
    init {
        Log.d("SSS",x.toString() +"," +y.toString())
        _pos.x = x
        _pos.y = y

        _vel.x = Utils.randomFloatInRange(-1f, -0.1f)
        _vel.y = Utils.randomFloatInRange(-1f, 1f)
        _vel = Utils.setLengthOfVector2(
            Utils.normilizeVector(_vel),
            VEL)

        _mesh = Mesh(EnemyShape.verts.clone(), GLES20.GL_TRIANGLES)
        _rotation = atan2(_vel.y, _vel.x) * Utils.TO_DEG + 270 //rotate towards the direction it travels
        _width = 8f;
        _height = 8f
        _mesh.setWidthHeight(_width, _height)
        setColors(1f, 0.4f, 0.4f, 1f)

        for (i in 0 until AMOUNT_OF_BULLETS) {
            _bullets.add(Bullet(1f,0f,0f,20f))
        }
    }

    var _bulletCooldown = BULLET_RECHARGE_TIME
    override fun update(dt: Float) {
        for(bullet in _bullets){
            bullet.update(dt)
        }

        if(isDead())return
        super.update(dt)
        if(Camera.isEntityOnScreen(this)){//avoid surprise attacks
            _bulletCooldown-= dt
            if(_bulletCooldown < 0f){
                val targetPos = engine._player._pos
                val directionOfFire = PointF(targetPos.x - _pos.x, targetPos.y - _pos.y)

                if(Bullet.maybeFireBullet(this, _bullets, directionOfFire)){
                    _bulletCooldown = BULLET_RECHARGE_TIME
                    engine.onGameEvent(GameEvent.Fire)
                }
            }
        }
        else{
            _bulletCooldown = BULLET_RECHARGE_TIME
        }
    }
    override fun isColliding(that: GLEntity): Boolean {
        if (!areBoundingSpheresOverlapping(this, that)) {
            return false
        }
        val shipHull = getPointList()
        val collidingPoints = that.getPointList()
        if (polygonVsPolygon(shipHull, collidingPoints)) {
            return true
        }
        return polygonVsPoint(collidingPoints,
            _pos.x,
            _pos.y) //finally, check if we're inside the asteroid
    }
    override fun onCollision(that: GLEntity) {
        _isAlive = false
        engine._particleManager.generateExplosionAt(_pos.x, _pos.y)
        engine._levelManager.increaseScore(SCORE_PER_ENEMY)
    }

    override fun render(viewportMatrix: FloatArray) {
        for(bullet in _bullets){
            bullet.render(viewportMatrix)
        }
        if(isDead())return
        super.render(viewportMatrix)
    }

    fun areAllBulletsDead(): Boolean {
        for(bullet in _bullets){
            if(!bullet.isDead()) return false
        }
        return true
    }
}