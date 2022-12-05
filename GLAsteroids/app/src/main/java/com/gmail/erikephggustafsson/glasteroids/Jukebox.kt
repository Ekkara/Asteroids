package com.gmail.erikephggustafsson.glasteroids

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import java.io.IOException

enum class GameEvent {
    AstroidsCollide, AstroydDestroyed, Boost, Fire, TakeDamge
}

private const val MAX_STREAMS = 5
private const val DEFAULT_SFX_VOLUME = 12f
private const val DEFAULT_MUSIC_VOLUME = 12f

class Jukebox(val engine: Game) {
    val TAG = "Jukebox"
    private val mSoundsMap = HashMap<GameEvent, Int>()
    private var mSoundPool: SoundPool? = null
    private var mBgPlayer: MediaPlayer? = null
    private var mSoundEnabled = true //TODO: make prefs!
    private var mMusicEnabled = true //TODO: make prefs

    init {
        engine.getActivity().volumeControlStream = AudioManager.STREAM_MUSIC;
        loadIfNeeded()
        resumeBgMusic()
    }

    private fun loadIfNeeded() {
        if (mSoundEnabled) {
            loadSounds()
        }
        if (mMusicEnabled) {
            loadMusic()
        }
    }

    private fun loadSounds() {
        val attr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        mSoundPool = SoundPool.Builder()
            .setAudioAttributes(attr)
            .setMaxStreams(MAX_STREAMS)
            .build()

        mSoundsMap.clear()
        //sound effects made on https://sfxr.me/
        loadEventSound(GameEvent.AstroidsCollide, "sfx/AstroidsCollide.wav")
        loadEventSound(GameEvent.Boost, "sfx/Boost.wav")
        loadEventSound(GameEvent.AstroydDestroyed, "sfx/DestroyAsteroid.wav")
        loadEventSound(GameEvent.Fire, "sfx/LaserShoot.wav")
        loadEventSound(GameEvent.TakeDamge, "sfx/TakeDamage.wav")
    }

    private fun loadEventSound(event: GameEvent, fileName: String) {
        try {
            val afd = engine.getAssets().openFd(fileName)
            val soundId = mSoundPool!!.load(afd, 1)
            mSoundsMap[event] = soundId
        } catch (e: IOException) {
            Log.e(TAG, "Error loading sound $e")
        }
    }

    private fun unloadSounds() {
        if (mSoundPool == null) {
            return
        }
        mSoundPool!!.release()
        mSoundPool = null
        mSoundsMap.clear()
    }

    var eventsToPlay = arrayListOf<GameEvent>()

    fun addEventToQueue(event: GameEvent) {
        if (!mSoundEnabled) {
            return
        }
        if (!eventsToPlay.contains(event) &&
            eventsToPlay.size <= MAX_STREAMS) {
            eventsToPlay.add(event)
        }
    }

    fun playSoundsInQueue() {
        if (!mSoundEnabled) {
            return
        }
        for (event in eventsToPlay) {
            val leftVolume = DEFAULT_SFX_VOLUME
            val rightVolume = DEFAULT_SFX_VOLUME
            val priority = 1
            val loop = 0 //-1 loop forever, 0 play once
            val rate = 1.0f
            val soundID = mSoundsMap[event]
            if (soundID == null) {
                Log.e(TAG, "Attempting to play non-existent event sound: {event}")
                return
            }
            if (soundID > 0) { //if soundID is 0, the file failed to load. Make sure you catch this in the loading routine.
                mSoundPool!!.play(soundID, leftVolume, rightVolume, priority, loop, rate)
            }
        }
        eventsToPlay.clear()
    }

    private fun loadMusic() {
        try {
            mBgPlayer = MediaPlayer()
            val afd = engine.getAssets().openFd("bgm/SongAFriendMade.mp3")
            mBgPlayer!!.setDataSource(
                afd.fileDescriptor,
                afd.startOffset,
                afd.length
            )
            mBgPlayer!!.isLooping = true
            mBgPlayer!!.setVolume(DEFAULT_MUSIC_VOLUME, DEFAULT_MUSIC_VOLUME)
            mBgPlayer!!.prepare()
        } catch (e: IOException) {
            Log.e(TAG, "Unable to create MediaPlayer.", e)
        }
    }

    fun pauseBgMusic() {//TODO:add enum classes and if statement to prevent unauthorized pause calls
        if (!mMusicEnabled) {
            return
        }
        mBgPlayer!!.pause()
    }

    fun resumeBgMusic() {
        if (!mMusicEnabled) {
            return
        }
        mBgPlayer!!.start()
    }

    private fun unloadMusic() {
        if (mBgPlayer == null) {
            return
        }
        mBgPlayer!!.stop()
        mBgPlayer!!.release()
    }
}