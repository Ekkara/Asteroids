package com.gmail.erikephggustafsson.glasteroids

import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.graphics.plus

private const val MAX_AMOUNT_OF_PARTICLE_SYSTEM = 4
private const val PARTICLES_PER_EFFECT = 50
private const val AMOUNT_OF_PARTICLES = MAX_AMOUNT_OF_PARTICLE_SYSTEM * PARTICLES_PER_EFFECT

class ParticleManager {
    private val _particles = ArrayList<Particle>(AMOUNT_OF_PARTICLES)
    private var _isSystemAvailable = ArrayList<Boolean>(MAX_AMOUNT_OF_PARTICLE_SYSTEM)

    init {
        for (particles in 0 until AMOUNT_OF_PARTICLES) {
            _particles.add(Particle())
        }
        for (indexAvailable in 0 until MAX_AMOUNT_OF_PARTICLE_SYSTEM) {
            _isSystemAvailable.add(true)
        }
    }


    fun generateExplosionAt(x: Float, y: Float) {
        for (indexAvailable in 0 until MAX_AMOUNT_OF_PARTICLE_SYSTEM) {
            if (_isSystemAvailable[indexAvailable]) {
                for (index in 0 until PARTICLES_PER_EFFECT) {
                    _particles[PARTICLES_PER_EFFECT * indexAvailable + index].ActivateParticle(
                        PointF(x, y))
                }
                _isSystemAvailable[indexAvailable] = false
                break
            }
        }
        //no particle found :(
    }

    fun killParticleSystem(target: Int) {
        for (index in 0 until PARTICLES_PER_EFFECT) {
            _particles[PARTICLES_PER_EFFECT * target + index].forceDeath()
        }
        _isSystemAvailable[target] = true
    }
    fun killAllParticleSystems(){
        for(index in 0 until MAX_AMOUNT_OF_PARTICLE_SYSTEM){
            killParticleSystem(index)
        }
    }

    fun update(dt: Float) {
        for (i in 0 until MAX_AMOUNT_OF_PARTICLE_SYSTEM) {
            if (!_isSystemAvailable[i]) {
                var done = true
                for (j in i * PARTICLES_PER_EFFECT until (i + 1) * PARTICLES_PER_EFFECT) {
                    _particles[j].update(dt)

                    if (!_particles[j].isDead()) {
                        done = false
                    }
                }
                if (done) {
                    _isSystemAvailable[i] = true
                }
            }
        }
    }

    fun render(viewportMatrix: FloatArray) {
        for (i in 0 until MAX_AMOUNT_OF_PARTICLE_SYSTEM) {
            if (!_isSystemAvailable[i]) {
                for (j in i * PARTICLES_PER_EFFECT until (i + 1) * PARTICLES_PER_EFFECT) {
                    _particles[j].render(viewportMatrix)
                }
            }
        }
    }
}