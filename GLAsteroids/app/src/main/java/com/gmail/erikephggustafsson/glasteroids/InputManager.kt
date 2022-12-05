package com.gmail.erikephggustafsson.glasteroids

open class InputManager {
    var _unSynchedButtonA = false

    var _horizontalFactor = 0.0f

    //var _pressingA = false
    var _buttonA = InputVarible()
    var _pressingB = false

    fun syncronize(){
        if (!_unSynchedButtonA.equals(_buttonA._isKeyPressed)) {
            if(_unSynchedButtonA) {
                _buttonA._isJustPress = true
            }
            else{
                _buttonA._isKeyJustReleased = true
            }
        }
        else{
            _buttonA._isKeyJustReleased = false
            _buttonA._isJustPress = false
        }
        _buttonA._isKeyPressed = _unSynchedButtonA
    }

    fun onStart() {}
    fun onStop() {}
    fun onPause() {
        _pressingB = false
        _horizontalFactor = 0f
        _unSynchedButtonA = false

        //these following variables are of a custom datatype, don't worry if it does not look the same for you
        _buttonA._isJustPress = false
        _buttonA._isKeyPressed = false
        _buttonA._isKeyJustReleased= false
    }
    fun onResume() {}
}