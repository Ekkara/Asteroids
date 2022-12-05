package com.gmail.erikephggustafsson.glasteroids

import android.graphics.Point
import android.graphics.PointF
import android.os.SystemClock
import android.util.Log
import java.util.*
import kotlin.math.PI
import kotlin.math.sqrt
import kotlin.random.Random

object Utils {
    const val TO_DEG = 180.0f / PI.toFloat()
    const val TO_RAD = PI.toFloat() / 180.0f

    fun lerpPoint(to: PointF, from: PointF, step: Float): PointF {
        return PointF(from.x + ((to.x - from.x) * step),
            from.y + ((to.y - from.y) * step))
    }

    fun randomFloatInRange(min: Float, max: Float): Float = min + Random.nextFloat() * (max - min)

    fun setLengthOfVector2(vector: PointF, length: Float): PointF {
        val normVector = normilizeVector(vector)
        return PointF(normVector.x * length, normVector.y * length)
    }

    fun normilizeVector(vector: PointF): PointF {
        val length = vector.length()
        return PointF(vector.x / length, vector.y / length)
    }
    fun distance(pos1:PointF, pos2:PointF):Float{
        return sqrt((pos1.x-pos2.x)*(pos1.x-pos2.x) + (pos1.y-pos2.y)*(pos1.y-pos2.y))
    }
}
