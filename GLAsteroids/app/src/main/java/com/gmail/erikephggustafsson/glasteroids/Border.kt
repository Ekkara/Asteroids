package com.gmail.erikephggustafsson.glasteroids

import android.opengl.GLES20

class Border(x:Float, y:Float, worldWidth:Float, worldHeight:Float):
    GLEntity() {
    init {
        _pos.x = x
        _pos.y = y
        _width = worldWidth-1.0f; //-1 so the border isn't obstructed by the screen edge
        _height = worldHeight-1.0f;
        setColors(1f, 1f, 1f, 1f) //WHITE for theme :)
        val borderVertices = floatArrayOf(
            // A line from point 1 to point 2
            0f, 0f, 0f,
            worldWidth, 0f, 0f,
            // Point 2 to point 3
            worldWidth, 0f, 0f,
            worldWidth, worldHeight, 0f,
            // Point 3 to point 4
            worldWidth, worldHeight, 0f,
            0f, worldHeight, 0f,
            // Point 4 to point 1
            0f, worldHeight,
            0f, 0f, 0f, 0f
        )
        _mesh = Mesh(borderVertices, GLES20.GL_LINES)
        _mesh.setWidthHeight(_width, _height) //will automatically normalize the mesh!
    }
}