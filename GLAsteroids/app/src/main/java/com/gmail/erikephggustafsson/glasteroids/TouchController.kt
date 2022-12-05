package com.gmail.erikephggustafsson.glasteroids

import android.view.MotionEvent
import android.view.View
import android.widget.Button

class TouchController(view: View) : InputManager(),
    View.OnTouchListener{
    init {
        view.findViewById<Button>(R.id.keypad_left).setOnTouchListener(this)
        view.findViewById<Button>(R.id.keypad_right).setOnTouchListener(this)
        view.findViewById<Button>(R.id.keypad_a).setOnTouchListener(this)
        view.findViewById<Button>(R.id.keypad_b).setOnTouchListener(this)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val action = event.actionMasked
        val id: Int = v.getId()
        if (action == MotionEvent.ACTION_DOWN) {
            // User started pressing a key
            if (id == R.id.keypad_left) {
                _horizontalFactor -= 1f
            } else if (id == R.id.keypad_right) {
                _horizontalFactor += 1f
            }
            if (id == R.id.keypad_a) {
            _unSynchedButtonA = true
            }
            if (id == R.id.keypad_b) {
                _pressingB = true
            }
        } else if (action == MotionEvent.ACTION_UP) {
            // User released a key
            if (id == R.id.keypad_left) {
                _horizontalFactor += 1f
            } else if (id == R.id.keypad_right) {
                _horizontalFactor -= 1f
            }
            if (id == R.id.keypad_a) {
                _unSynchedButtonA = false
            }
            if (id == R.id.keypad_b) {
                _pressingB = false
            }
        }
        return false
    }

}