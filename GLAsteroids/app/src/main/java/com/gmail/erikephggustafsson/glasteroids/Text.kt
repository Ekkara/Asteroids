package com.gmail.erikephggustafsson.glasteroids

import android.graphics.Color
import android.opengl.Matrix
import com.gmail.erikephggustafsson.glasteroids.GLManager.draw

enum class ProjectOn{
    World,
    Screen
}
class Text (s: String, x: Float, y: Float, val projectOn: ProjectOn = ProjectOn.Screen, val defaultX: Float = x, val defaultY:Float = y) : GLEntity() {
    var _meshes = ArrayList<Mesh>()
    private var _spacing = GLYPH_SPACING //spacing between characters
    private var _glyphWidth = GLYPH_WIDTH.toFloat()
    private var _glyphHeight = GLYPH_HEIGHT.toFloat()
    init {
        setString(s)
        _pos.x = x
        _pos.y = y
        //we can't use setWidthHeight, because normalization will break
        //the layout of the pixel-font. So we resort to simply scaling the text-entity
        setScale(0.28f); //TO DO: magic value. scaling to 75%
        _color[0] = 255f
        _color[1] = 255f
        _color[2] = 255f
        _color[3] = 0f

    }

    fun setString(s: String) {
        _meshes = GLPixelFont.getString(s)
    }

    override fun render(viewportMatrix: FloatArray) {
        if(projectOn.equals(ProjectOn.Screen)){
            _pos.x = defaultX + Camera.cameraOffset.x
            _pos.y = defaultY + Camera.cameraOffset.y
        }
        for (i in _meshes.indices) {
            if (_meshes[i] == BLANK_SPACE) {
                continue
            }
            Matrix.setIdentityM(modelMatrix, OFFSET) //reset model matrix
            Matrix.translateM(modelMatrix, OFFSET, _pos.x + (_glyphWidth + _spacing) * i, _pos.y, _depth)
            Matrix.scaleM(modelMatrix, OFFSET, _scale, _scale, 1f)
            Matrix.multiplyMM(
                viewportModelMatrix,
                OFFSET,
                viewportMatrix,
                OFFSET,
                modelMatrix,
                OFFSET
            )
            draw(_meshes[i], viewportModelMatrix, _color)
        }
    }

    fun setScale(factor: Float) {
        _scale = factor
        _spacing = GLYPH_SPACING * _scale
        _glyphWidth = GLYPH_WIDTH * _scale
        _glyphHeight = GLYPH_HEIGHT * _scale
        _height = _glyphHeight
        _width = (_glyphWidth + _spacing) * _meshes.size
    }
}