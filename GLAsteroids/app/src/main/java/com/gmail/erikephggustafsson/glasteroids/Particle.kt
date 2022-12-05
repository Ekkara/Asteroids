package com.gmail.erikephggustafsson.glasteroids

import android.graphics.PointF
import android.opengl.GLES20
import android.util.Log

private const val MIN_LIFE_TIME = 0.8f
private const val MAX_LIFE_TIME = 1.2f
private const val MIN_SPEED = 12f
private const val MAX_SPEED = 17f

class Particle:GLEntity() {
    var _ttl = 0f
init {
    //we don't want any particle on start
    setColors(0.98f,0.98f,0.98f,1f)
    _ttl = -1f
    _width = 1.2f
    _height = 1.2f
    _mesh = Mesh(
        generatePolygon(5, _width / 2,false),
        GLES20.GL_TRIANGLES
    )
    _mesh.setWidthHeight(_width,_height)

}


    fun ActivateParticle(pointF: PointF){

        _pos = pointF
        _ttl = Utils.randomFloatInRange(MIN_LIFE_TIME, MAX_LIFE_TIME)
        val speed = Utils.randomFloatInRange(MIN_SPEED, MAX_SPEED)
        _vel = PointF(Utils.randomFloatInRange(-1f,1f),
            Utils.randomFloatInRange(-1f,1f))
       _vel = Utils.setLengthOfVector2(_vel, speed)
    }
    override fun onOutSideOfBounds() {
        forceDeath()
    }
    fun forceDeath(){
        _ttl = -1f
    }

    override fun isDead(): Boolean {
        return _ttl < 0f
    }

    override fun onCollision(that: GLEntity) {

    }

    override fun update(dt: Float) {
        if(isDead()) return
        super.update(dt)
        _ttl -= dt
    }
    override fun render(viewportMatrix: FloatArray) {
        if(isDead()) return
        super.render(viewportMatrix)
    }
}