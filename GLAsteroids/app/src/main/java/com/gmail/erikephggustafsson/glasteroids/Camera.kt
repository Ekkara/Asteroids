package com.gmail.erikephggustafsson.glasteroids

import android.graphics.PointF
import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log

const val WORLD_WIDTH = 300f //all dimensions are in meters
const val WORLD_HEIGHT = 150f
const val METERS_TO_SHOW_X = 160f
const val METERS_TO_SHOW_Y = 90f
class Camera {

    fun Center() = PointF(WORLD_WIDTH/2, WORLD_HEIGHT/2)
    // Create the projection Matrix. This is used to project the scene onto a 2D viewport.



    fun draw(){
        //setup a projection matrix by passing in the range of the game world that will be mapped by OpenGL to the screen.
        val offset = 0
        val left = 0f
        val right = METERS_TO_SHOW_X
        val bottom = METERS_TO_SHOW_Y  //bottom is at METERS_TO_SHOW
        val top = 0f //top is at 0, just like we're used to!
        val near = 0f
        val far = 1f
        Matrix.orthoM(_viewportMatrix, offset, left+ cameraOffset.x, right+ cameraOffset.x, bottom+ cameraOffset.y, top+ cameraOffset.y, near, far);
    }
    fun changeViewport(width: Int, height: Int){
        GLES20.glViewport(0, 0, width, height)
    }

    companion object{
        val _viewportMatrix = FloatArray(4 * 4) //In essence, it is our our Camera
        var cameraOffset = PointF(0f,0f)
        private set
        fun isEntityOnScreen(entity: GLEntity):Boolean{
            val x = entity._pos.x
            val y = entity._pos.y
            val radius = entity.radius()
            return (x+radius > cameraOffset.x &&
                    x+radius < cameraOffset.x+METERS_TO_SHOW_X &&
                    y+radius > cameraOffset.y &&
                    +radius < cameraOffset.y + METERS_TO_SHOW_Y)
        }
        fun isEntityInWorld(entity: GLEntity):Boolean{
            return   (entity.left() > WORLD_WIDTH
                    || entity.right() < 0f ||
                    entity.top() > WORLD_HEIGHT ||
                    entity.bottom() < 0f)
        }
        fun moveCameraTowards(pointF: PointF){
            var point = Utils.lerpPoint(pointF, cameraOffset, 1f)


            cameraOffset = PointF(point.x - (METERS_TO_SHOW_X/2), point.y - (METERS_TO_SHOW_Y/2))

            if (0 > cameraOffset.y){
                cameraOffset.y = 0f
            }
            if(WORLD_HEIGHT - METERS_TO_SHOW_Y < cameraOffset.y  ){
                cameraOffset.y = WORLD_HEIGHT - METERS_TO_SHOW_Y
            }
            if (0 > cameraOffset.x){
                cameraOffset.x = 0f
            }
            if(WORLD_WIDTH - METERS_TO_SHOW_X < cameraOffset.x){
                cameraOffset.x = WORLD_WIDTH - METERS_TO_SHOW_X
            }
        }
    }
}