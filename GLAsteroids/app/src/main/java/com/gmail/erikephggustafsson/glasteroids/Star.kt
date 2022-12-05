package com.gmail.erikephggustafsson.glasteroids

import android.graphics.Color
import android.opengl.GLES20
import kotlin.random.Random

object Dot {
    val verts = floatArrayOf(0f, 0f, 0f)
    val mesh = Mesh(verts, GLES20.GL_POINTS)
}

object starShape {
    val verts = floatArrayOf( // in counterclockwise order:
        0.0f, 1.5f, 0.0f,  // top
        1.5f, -0.75f, 0.0f,  // bottom left
        -1.5f, -0.75f, 0.0f,  // bottom right
        0.0f, -1.5f, 0.0f,  // bot
        1.5f, 0.75f, 0.0f, // top right
        -1.5f, 0.75f, 0.0f  // top left
    )
}


class Star(x: Float, y: Float) : GLEntity() {
    init {
        _pos.x = x
        _pos.y = y
        _width = 2f
        _height = 2f
        setColors(1f,1f,1f,1f)
        _mesh = Mesh(starShape.verts.clone(), GLES20.GL_TRIANGLES)
        _mesh.setWidthHeight(_width, _height)
        val randomRot = Random.nextFloat() * 2f * Math.PI.toFloat()
        _mesh.rotateZ(randomRot)
    }
}