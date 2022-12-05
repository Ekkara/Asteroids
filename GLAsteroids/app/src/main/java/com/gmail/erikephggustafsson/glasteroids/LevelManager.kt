package com.gmail.erikephggustafsson.glasteroids

import android.content.Context
import android.content.res.AssetManager
import android.graphics.PointF
import android.util.Log
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private const val LEVEL_FOLDER = "levels"
const val SCORE_PER_ASTEROID = 1
const val SCORE_PER_ENEMY = 5

class LevelManager(val appContext: Context) {
    var _amountOfLevels = 0
    var currentLevel = 0

    val _am: AssetManager = engine.context.getAssets()
    val _files = _am.list(LEVEL_FOLDER)

    //HUD Elements:
    val _scoreTextIndex = engine._hud.addTextElement(5f, 12f)
    val _levelTextIndex = engine._hud.addTextElement(METERS_TO_SHOW_X - 30f, 8f)
    val _goalTextIndex = engine._hud.addTextElement(METERS_TO_SHOW_X - 30f, 12f)

    init {
        _amountOfLevels = _files!!.size
        loadNextLevel()
    }

    val _asteroidWaitingToSpawn = ArrayList<Asteroid>()
    val _spawnedAsteroids = ArrayList<Asteroid>()

    val _enemiesWaitingToSpawn = ArrayList<Enemy>()
    val _spawnedEnemies = ArrayList<Enemy>()
    var _lastAtemptToSpawnEnemy = 0f
    lateinit var _gameRules: LevelData
    var _readyToLoadLevel = false

    fun loadNextLevel() {
        _destroyedThisLevel = 0

        val directory = LEVEL_FOLDER + "/" + currentLevel.toString() + ".txt"
        val fileText: String = appContext.assets.open(directory).bufferedReader().use {
            it.readText()
        }
        _gameRules = LevelData(
            findValue("_asteroidToDestroy", fileText).toInt(),
            findValue("_asteroidTargetAmount", fileText).toInt(),
            findValue("_tryToSpawnEnemy", fileText).toFloat(),
            findValue("_chanceOfSpawningEnemy", fileText).toFloat(),
            findValue("_maxAmountOfEnemies", fileText).toInt())

        _lastAtemptToSpawnEnemy = 0f
        engine._player.clearBullets()

        engine._hud.updateDisplayText(_scoreTextIndex,
            "SCORE: " + _score.toString())
        engine._hud.updateDisplayText(_levelTextIndex,
            "LEVEL: " + currentLevel.toString())
        engine._hud.updateDisplayText(_goalTextIndex,
            "DESTROY: 0/" + _gameRules._asteroidToDestroy.toString())

        currentLevel++
        currentLevel = currentLevel % _amountOfLevels
    }

    fun update(dt: Float) {
        //calculate amount of missing asteroids
        val missingAsteroids = _gameRules._asteroidTargetAmount - _spawnedAsteroids.size
        if (missingAsteroids > 0 && _destroyedThisLevel < _gameRules._asteroidToDestroy) {
            //spawn missing asteroid
            for (i in 0 until missingAsteroids) {
                var minHeight = 0f
                var maxHeight = 0f

                //spawn above or below the player
                val type = Random.nextInt(1, 3)
                when (type) {
                    1 -> {
                        minHeight = -LARGE_DIAMETER + 10f
                        maxHeight = -LARGE_DIAMETER + 10f
                    }
                    2 -> {
                        minHeight = WORLD_HEIGHT
                        maxHeight = WORLD_HEIGHT
                    }
                }

                val asteroid = Asteroid(
                    Utils.randomFloatInRange(0f, WORLD_WIDTH),
                    Utils.randomFloatInRange(minHeight, maxHeight),
                    Asteroid.getRandomSize())

                if (asteroid.canSpawnHere(_spawnedAsteroids) &&
                    asteroid.canSpawnHere(_asteroidWaitingToSpawn)
                ) {
                    _asteroidWaitingToSpawn.add(asteroid)
                }
            }
        }

        //see if it is time to spawn a enemy
        if (_spawnedEnemies.size < _gameRules._maxAmountOfEnemies) {
            _lastAtemptToSpawnEnemy += dt
            if (_lastAtemptToSpawnEnemy >= _gameRules._tryToSpawnEnemy) {
                val randomPrecentage = Utils.randomFloatInRange(0f, 100f)

                if (randomPrecentage <= _gameRules._chanceOfSpawning) {
                    val enemy = Enemy(
                        WORLD_WIDTH + 7f,
                        Utils.randomFloatInRange(0f, WORLD_HEIGHT))
                    _enemiesWaitingToSpawn.add(enemy)
                }
                _lastAtemptToSpawnEnemy = 0f
            }
        }

        //move asteroids
        for (asteroid in _spawnedAsteroids) {
            asteroid.update(dt)
        }
        for (enemy in _spawnedEnemies) {
            enemy.update(dt)
        }


        checkIfReadyToStartNextWave()
    }

    var _destroyedThisLevel = 0
    var _score = 0
    fun increaseScore(amount: Int) {
        //increase score
        _destroyedThisLevel++
        _score += amount
        engine._hud.updateDisplayText(_scoreTextIndex,
            "SCORE: " + _score.toString())

        if (_destroyedThisLevel >= _gameRules._asteroidToDestroy) {
            _readyToLoadLevel = true
            engine._hud.updateDisplayText(_goalTextIndex,
                "DESTROY REMAINING")
        } else {
            engine._hud.updateDisplayText(_goalTextIndex,
                "DESTROY: " + _destroyedThisLevel.toString() + "/" + _gameRules._asteroidToDestroy.toString())

        }
    }


    fun addAndRemoveEntities() {
        //region add and remove asteroids
        val asteroidCount = _spawnedAsteroids.size
        for (i in asteroidCount - 1 downTo 0) {
            if (_spawnedAsteroids[i].isDead()) {
                _spawnedAsteroids.removeAt(i)
            }
        }

        for (i in 0 until _asteroidWaitingToSpawn.size) {
            if (_asteroidWaitingToSpawn[i].canSpawnHere(_spawnedAsteroids)) {
                _spawnedAsteroids.add(_asteroidWaitingToSpawn[i])
            }
        }
        _asteroidWaitingToSpawn.clear()
        //endregion

        //region add and remove enemies
        val enemyCount = _spawnedEnemies.size
        for (i in enemyCount - 1 downTo 0) {
            if (_spawnedEnemies[i].isDead() &&
                _spawnedEnemies[i].areAllBulletsDead()
            ) {
                _spawnedEnemies.removeAt(i)
            }
        }

        for (i in 0 until _enemiesWaitingToSpawn.size) {
            _spawnedEnemies.add(_enemiesWaitingToSpawn[i])
        }
        _enemiesWaitingToSpawn.clear()
        //endregion
    }


    fun checkIfReadyToStartNextWave() {
        if (!(_readyToLoadLevel && _spawnedAsteroids.size.equals(0))) return
        _asteroidWaitingToSpawn.clear()
        _readyToLoadLevel = false
        loadNextLevel()
    }

    fun gameOver() {
        currentLevel = 0
        _readyToLoadLevel = true
        _spawnedAsteroids.clear()
        _spawnedEnemies.clear()
        _enemiesWaitingToSpawn.clear()
        _score = 0
        engine._player.clearBullets()
    }

    fun collision(player: Player) {
        //region asteroids with bullets
        for (b in player._bullets) {
            if (b.isDead()) {
                continue
            } //skip dead bullets
            for (a in _spawnedAsteroids) {
                if (a.isDead()) {
                    continue
                } //skip dead asteroids
                if (b.isColliding(a)) {
                    b.onCollision(a) //notify each entity so they can decide what to do
                    a.onCollision(b)
                }
            }
        }
        //endregion

        //region asteroids with player
        for (a in _spawnedAsteroids) {
            if (a.isDead()) {
                continue
            }
            if (player.isColliding(a)) {
                player.onCollision(a)
                a.onCollision(player)
            }
        }
        //endregion

        //region asteroids with asteroids
        for (firstA in 0 until _spawnedAsteroids.size) {
            if (_spawnedAsteroids[firstA].isDead()) {
                continue
            }
            for (secondA in firstA + 1 until _spawnedAsteroids.size) {
                if (_spawnedAsteroids[secondA].isDead()) {
                    continue
                }
                if (_spawnedAsteroids[firstA].isColliding(_spawnedAsteroids[secondA])) {
                    // onGameEvent(GameEvent.AstroidsCollide)

                    //todo: merge this with the "onColision" functions for clearer API, problem:
                    //todo: we need their velocity before either of them change and the code only need
                    //todo: to calculate this once, unnecessary to do it once per asteroid
                    val newVels = bounce(_spawnedAsteroids[firstA],
                        _spawnedAsteroids[firstA]._vel,
                        _spawnedAsteroids[secondA],
                        _spawnedAsteroids[secondA]._vel)
                    _spawnedAsteroids[firstA]._vel = newVels[0]
                    _spawnedAsteroids[secondA]._vel = newVels[1]
                }
            }
        }
        //endregion

        //region player with enemies
        for (enemy in _spawnedEnemies) {
            if (enemy.isDead()) {
                continue
            }
            if (player.isColliding(enemy)) {
                player.onCollision(enemy)
                enemy.onCollision(player)
            }
        }
        //endregion

        //region player with enemy bullets
        for (enemy in _spawnedEnemies) {
            for (bullet in enemy._bullets) {
                if (bullet.isDead()) {
                    continue
                }
                if (player.isColliding(bullet)) {
                    player.onCollision(bullet)
                    bullet.onCollision(player)
                }
            }
        }
        //endregion

        //region enemies with player bullets
        for (bullet in player._bullets) {
            if (bullet.isDead()) {
                continue
            } //skip dead bullets
            for (enemy in _spawnedEnemies) {
                if (enemy.isDead()) {
                    continue
                } //skip dead asteroids
                if (bullet.isColliding(enemy)) {
                    bullet.onCollision(enemy)
                    enemy.onCollision(bullet)
                }
            }
        }
        //endregion
    }

    fun render(viewportMatrix: FloatArray) {
        for (asteroid in _spawnedAsteroids) {
            asteroid.render(viewportMatrix)
        }
        for (enemy in _spawnedEnemies) {
            enemy.render(viewportMatrix)
        }
    }

    fun splitAsteroid(size: Size, position: PointF, distFromCenter: Float) {
        var newSize = Size.medium
        if (size.equals(Size.medium)) newSize = Size.small

        val amountToGenerate = Random.nextInt(3, 5)
        val step = 2.0 * PI / amountToGenerate

        for (i in 0 until amountToGenerate) {
            val theta = i * step

            val offset = Utils.setLengthOfVector2(PointF(
                cos(theta).toFloat(),
                sin(theta).toFloat()),
                distFromCenter)

            val asteroid = Asteroid(
                position.x + offset.x,
                position.y + offset.y,
                newSize)

            asteroid._vel = PointF(
                asteroid._pos.x - position.x,
                asteroid._pos.y - position.y)

            _asteroidWaitingToSpawn.add(asteroid)
        }
    }

    private fun findValue(nameOfVar: String, textFile: String): String {
        val listOfCharSrc = textFile.toCharArray()
        val start = (nameOfVar + "{").toCharArray()
        val end = '}'

        for (i in 0 until listOfCharSrc.size) {//look at all char to see if this is the beginning of the variable we search for name's
            if (listOfCharSrc[i].equals(start[0])) {//found potential match for the variable
                for (j in 0 until start.size) {
                    if (!listOfCharSrc[i + j].equals(start[j])) break //if this was a false lead, break search
                    else {
                        if (j.equals(start.size - 1)) {//found a perfect match of the var name
                            var k = i + j + 1
                            var varible = ""
                            while (!listOfCharSrc[k].equals(end)) {//generating string until the end of the variable is found
                                varible += listOfCharSrc[k]
                                k++
                                assert(k < listOfCharSrc.size) { "out of characters to find a closing brackets for " + nameOfVar}
                            }
                            return varible //return the value, always sent as a string, need to be converted to other data types later
                        }
                    }
                }
            }
        }
        assert(false) { "the variable " + nameOfVar + "was not found!" }
        return ""
    }
}


