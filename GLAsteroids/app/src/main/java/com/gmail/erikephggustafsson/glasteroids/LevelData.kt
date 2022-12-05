package com.gmail.erikephggustafsson.glasteroids

data class LevelData(
    val _asteroidToDestroy:Int,
    val _asteroidTargetAmount:Int,//won't spawn asteroids over this number, but they can still be
    //made by destroying other existing asteroids
    val _tryToSpawnEnemy:Float,//how often (seconds) an attempt to spawn an enemy is made
    val _chanceOfSpawning:Float,//range from 0-100%
    val _maxAmountOfEnemies:Int
)
