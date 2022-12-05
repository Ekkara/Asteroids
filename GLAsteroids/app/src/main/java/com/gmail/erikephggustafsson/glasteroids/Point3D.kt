package com.gmail.erikephggustafsson.glasteroids

import kotlin.math.abs
import kotlin.math.sqrt

class Point3D {
    var _x = 0.0f
    var _y = 0.0f
    var _z = 0.0f

    constructor() {}
    constructor(x: Float, y: Float, z: Float) {
        set(x, y, z)
    }

    constructor(p: FloatArray) {
        set(p)
    }

    operator fun set(x: Float, y: Float, z: Float) {
        _x = x
        _y = y
        _z = z
    }

    fun set(p: FloatArray) {
        assert(p.size == 3)
        _x = p[0]
        _y = p[1]
        _z = p[2]
    }

    fun distanceSquared(that: Point3D): Float {
        val dx = _x - that._x
        val dy = _y - that._y
        val dz = _z - that._z
        return dx * dx + dy * dy + dz * dz
    }

    fun distance(that: Point3D): Float {
        val dx = _x - that._x
        val dy = _y - that._y
        val dz = _z - that._z
        return sqrt((dx * dx + dy * dy + dz * dz))
    }

    fun distanceL1(that: Point3D): Float {
        return abs(_x - that._x) + abs(_y - that._y) + abs(
            _z - that._z
        )
    }
}