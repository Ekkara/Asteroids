package com.gmail.erikephggustafsson.glasteroids

import android.graphics.PointF
import android.opengl.GLES20
import kotlin.math.cos
import kotlin.math.sin

const val SPEED = 120f //TO DO: game play settings
const val BULLET_TIME_TO_LIVE = 3.0f //seconds

class Bullet(color_r:Float,color_g:Float, color_b:Float,val bulletSpeed:Float = SPEED) : GLEntity() {
    var _ttl = -1f

    init {
        setColors(color_r,color_g,color_b, 1f)
        _width = 2f
        _height = 2f
        //_mesh = BULLET_MESH //all bullets use the exact same mesh
        _mesh = Mesh(
            generatePolygon(5, _width / 2, false),
            GLES20.GL_TRIANGLES
        )

        _mesh.setWidthHeight(_width, _height)
    }

    fun fireFrom(source: GLEntity, direction: PointF?) {
        var theta = source._rotation * TO_RADIANS
        _pos.x = source._pos.x + sin(theta) * (source._width * 0.5f)
        _pos.y = source._pos.y - cos(theta) * (source._height * 0.5f)

        if(direction == null){
            _vel.x = sin(theta) * bulletSpeed
            _vel.y = -cos(theta) * bulletSpeed
        }
        else{
            val velocity = Utils.setLengthOfVector2(direction, bulletSpeed)
            _vel.x = velocity.x
            _vel.y = velocity.y
        }
        _ttl = BULLET_TIME_TO_LIVE
    }

    override fun onOutSideOfBounds() {
        _ttl = -1f
    }

    override fun isDead(): Boolean {
        return _ttl < 0
    }

    override fun update(dt: Float) {
        if (isDead()) return
        _ttl -= dt
        super.update(dt)
    }

    override fun render(viewportMatrix: FloatArray) {
        if (isDead()) return
        super.render(viewportMatrix)
    }

    override fun isColliding(that: GLEntity): Boolean {
        if (!areBoundingSpheresOverlapping(this, that)) { //quick rejection
            return false
        }
        val asteroidVerts: ArrayList<PointF> = that.getPointList()
        return polygonVsPoint(asteroidVerts, _pos.x, _pos.y)
    }

    override fun onCollision(that: GLEntity) {
        killBullet()
    }
    fun killBullet(){
        _ttl = -1f
    }

    companion object {
        fun maybeFireBullet(source: GLEntity, arrayList: ArrayList<Bullet>, direction: PointF? = null): Boolean {
            for (b in arrayList) {
                if (b.isDead()) {
                    b.fireFrom(source,direction)
                    return true
                }
            }
            return false
        }
    }
}