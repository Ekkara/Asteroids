package com.gmail.erikephggustafsson.glasteroids

import android.graphics.PointF
import android.opengl.GLES20
import kotlin.random.Random


private const val SMALL_SPEED = 10f
private const val MEDIUM_SPEED = 8f
private const val LARGE_SPEED = 6f
private const val MIN_POINTS = 4
private const val FIRST_LIMIT = 7
private const val SECOND_LIMIT = 10
private const val THIRD_LIMIT = 13
private const val SMALL_DIAMETER = 7f
private const val MEDIUM_DIAMETER = 9f
const val LARGE_DIAMETER = 12f
private const val SMALL_MASS = 7f
private const val MEDIUM_MASS = 20f
private const val LARGE_MASS = 80f
private const val NEW_DIST = 7f

enum class Size {
    small,
    medium,
    large
}


class Asteroid(x: Float, y: Float, val size: Size, startVel: PointF? = null) : GLEntity() {
    companion object {
        fun getRandomSize(): Size {
            val rand = Random.nextInt(0, 3)
            if (rand.equals(0)) {
                return Size.large
            }
            if (rand.equals(1)) {
                return Size.medium
            }
            return Size.small
        }
    }

    init {
        if (startVel == null) {
            _vel.x = Utils.randomFloatInRange(-1f,1f)
            if(y > WORLD_HEIGHT/2){
                _vel.y = Utils.randomFloatInRange(-1f,-0.1f)
            }else{
                _vel.y = Utils.randomFloatInRange(0.1f,1f)
            }
        } else {
            _vel.x = startVel.x
            _vel.y = startVel.y

        }
        var minP = MIN_POINTS
        var maxP = FIRST_LIMIT
        var speedMagnitude = 0f
        when (size) {
            Size.small -> {
                _width = SMALL_DIAMETER
                _mass = SMALL_MASS
               speedMagnitude = SMALL_SPEED
            }
            Size.medium -> {
                _width = MEDIUM_DIAMETER
                _mass = MEDIUM_MASS
                minP = FIRST_LIMIT
                maxP = SECOND_LIMIT - 1
                speedMagnitude = MEDIUM_SPEED
            }
            Size.large -> {
                _width = LARGE_DIAMETER
                _mass = LARGE_MASS
                minP = SECOND_LIMIT
                maxP = THIRD_LIMIT - 1
                speedMagnitude = LARGE_SPEED
            }
        }
        _vel = Utils.setLengthOfVector2(
            Utils.normilizeVector(_vel),
            speedMagnitude)


        val points = Random.nextInt(minP, maxP)
        assert(points >= 3, { "requires atleast 3 points" })
        _pos.x = x
        _pos.y = y
        _height = _width


        val radius = _width * 0.5f
        _mesh = Mesh(
            generatePolygon(points, radius,true),
            GLES20.GL_LINES
        )
        _mesh.setWidthHeight(_width, _height);
    }

    override fun onOutSideOfBounds() {
        _isAlive = false
    }

    override fun isColliding(that: GLEntity): Boolean {
        if (!areBoundingSpheresOverlapping(this, that)) {
            return false
        }
        val thisAsteroidsHull = getPointList()
        val thatAsteroidsdHull = that.getPointList()
        if (polygonVsPolygon(thisAsteroidsHull, thatAsteroidsdHull)) {
            return true
        }
        return polygonVsPoint(thatAsteroidsdHull,
            _pos.x,
            _pos.y) //finally, check if we're inside the asteroid
    }

    override fun onCollision(that: GLEntity) {
        if (that is Bullet) {
            engine._particleManager.generateExplosionAt(_pos.x, _pos.y)
            engine.onGameEvent(GameEvent.AstroydDestroyed)
            if (!size.equals(Size.small)) {
                engine._levelManager.splitAsteroid(size, _pos, NEW_DIST)
            }
            engine._levelManager.increaseScore(SCORE_PER_ASTEROID)
        }
        super.onCollision(that)
    }

    fun canSpawnHere(_asteroids: ArrayList<Asteroid>): Boolean {//makes more sense to do a similar circular
        // "collision" check before spawning the entity
        for (index in 0 until _asteroids.size) {
            if (isColliding(_asteroids[index])) {
                _isAlive =
                    false // simple fix, immediate kill asteroid if it is spawned inside another asteroid
                return false
            }
        }
        return true
    }
}

fun bounce(
    e1: Asteroid, v1i: PointF,
    e2: Asteroid, v2i: PointF,
): ArrayList<PointF> {//todo: return a pair instead, makes more sense, also harder to mess up... is there any out variables in kotlin?

    /*
    v1i = initial velocity of first entity
    v1f = final velocity of first entity
    m1 = mass of first entity
    (same naming goes for the 2nd entity)
    */

    val m1 = e1._mass
    val m2 = e2._mass

    val v2fX = (v1i.x * m1 + v2i.x * m2 + v2i.x * m1 + v1i.x * m1) / (m2 + m1)
    val v2fY = (v1i.y * m1 + v2i.y * m2 + v2i.y * m1 + v1i.y * m1) / (m2 + m1)
    var v2f = PointF(v2fX, v2fY)

    val v1fX = v2i.x + v2f.x - v1i.x
    val v1fY = v2i.y + v2f.y - v1i.y
    var v1f = PointF(v1fX, v1fY)

    //now when we have these values based of real physics we are gonna ruin them to keep the game logic
    v1f = Utils.setLengthOfVector2(v1f, v1i.length())
    v2f = Utils.setLengthOfVector2(v2f, v2i.length())

    val newVelocities = ArrayList<PointF>()
    newVelocities.add(v1f)
    newVelocities.add(v2f)
    return newVelocities
    //region the math behind it
    /*

    the new velocity is calculated with two known formulas:
    1)  v1i*m1+v2i*m2=v1f*m1+v2f*m2
    2)  v1i+v1f=v2i+v2f

    respective velocity is calculated be derive one of each velocities
    v2f=(v1i*m1+v2i*m2-v1f*m1)/m2
    v1f=v2i+v2f-v1i

    by combining the two formulas one function will provide one velocity with only known variables

    v2f=((v1i*m1+v2i*m2)-(v2i+v2f-v1i)*m1)/m2
    v2f*m2 = v1i*m1+v2i*m2+v2i*m1-v2f*m1+v1i*m1
    v2f*m2+v2f*m1 = v1i*m1+v2i*m2+v2i*m1+v1i*m1
    (m2+m1)v2f/(m2+m1) = (v1i*m1+v2i*m2+v2i*m1+v1i*m1)/(m2+m1)
    v2f=(v1i*m1+v2i*m2+v2i*m1+v1i*m1)/(m2+m1)

    once one velocity is known, the other can easily be calculated as well with the formula provided above
    "v1f=v2i+v2f-v1i"

    The math was taken from real physics formulas presented by:
     https://www.khanacademy.org/science/physics/linear-momentum/elastic-and-inelastic-collisions/v/how-to-use-the-shortcut-for-solving-elastic-collisions
       */
    //endregion
}