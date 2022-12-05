package com.gmail.erikephggustafsson.glasteroids

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.core.view.WindowCompat

class MainActivity : Activity(){
    private lateinit var game: Game
    val TAG = "MAIN_A"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val controls = TouchController(findViewById(R.id.gamepad))
        game = findViewById<Game>(R.id.game)
        game._player.setControls(controls)
        Log.w(TAG, "create")
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    override fun onResume() {
        Log.w(TAG, "resume")
        super.onResume()
        game.onResume()
    }
    override fun onStart() {
        super.onStart()
        Log.w(TAG, "start")
    }

    override fun onPause() {
        Log.w(TAG, "pause")
        game.onPause()
        super.onPause()
    }

    override fun onStop() {
        Log.w(TAG, "stop")
        super.onStop()
    }
    override fun onDestroy(){
        Log.w(TAG, "destroy")
        super.onDestroy()
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) { //handle older SDKs, using the deprecated systemUiVisbility API
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    // Set the content to appear under the system bars so that the
                    // content doesn't resize when the system bars hide and show.
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    // Hide the nav bar and status bar
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        } else {
            // Tell the Window that our app is going to responsible for fitting for any system windows.
            // This is similar to: view.setSystemUiVisibility(LAYOUT_STABLE | LAYOUT_FULLSCREEN)
            WindowCompat.setDecorFitsSystemWindows(window, false)
            // the decor view is the true root of the Window's view hierarchy. It contains both the "decor" (i.e. the window's title (action bar) and also contains the app-supplied content view.
            //we need the root view so we can get its insetController
            val insetsController = window.decorView.windowInsetsController ?: return
            //Hide the keyboard (IME = "input method editor")
            insetsController.hide(WindowInsets.Type.ime())
            // set the modern equivalent to Sticky Immersive Mode:
            insetsController.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            // hide both the system bars (eg. the status and navigation bars):
            insetsController.hide(WindowInsets.Type.systemBars())
        }
    }
}

