package model

import korlibs.image.vector.*
import korlibs.image.vector.format.*
import korlibs.io.file.std.*

sealed class GameState {
    abstract val player: GameObject
    abstract val frame: Int
    abstract val enemyList: List<GameObject>
    abstract val score: Int

    data object Start : GameState() {
        override val player: GameObject = uzimaru1
        override val frame: Int = 0
        override val enemyList: List<GameObject> = emptyList()
        override val score: Int = 0
    }

    sealed class Playing : GameState() {
        abstract fun calculateNextState(): GameState

        protected fun calculateNextEnemies(): List<GameObject> {
            if (frame % listOf(100, 200).random() == 0) {
                return enemyList.asSequence()
                    .map {
                        it.copy(x = it.x + 8)
                    }.filter {
                        it.x < canvasWidth
                    }.plus(enemy)
                    .toList()
            } else {
                return enemyList.asSequence()
                    .map {
                        it.copy(x = it.x + 8)
                    }.filter {
                        it.x < canvasWidth
                    }.toList()
            }
        }

        protected fun calculateGameEnd(): End? {
            if (enemyList.any { it.checkCollision(player) }) {
                return End.GameOver(player, enemyList, frame, score)
            }
            if (score >= clearScore) {
                return End.GameClear(player.copy(y = groundY), enemyList, frame, score)
            }
            return null
        }



        data class PlayerJumping(
            override val player: GameObject,
            override val enemyList: List<GameObject>,
            private val t: Int,
            override val frame: Int,
            override val score: Int
        ) : Playing() {

            override fun calculateNextState(): GameState {
                calculateGameEnd()?.let {
                    return it
                }
                val gravity = 0.4
                val vy = 13
                val y = 0.5 * gravity * t * t - vy * t + (groundY)
                val playerY = if (y <= groundY) {
                    y
                } else {
                    groundY
                }
                val nextPlayer = if (score < evolutionScore) {
                    uzimaru1.copy(y = playerY.toInt())
                } else {
                    uzimaru2.copy(y = playerY.toInt())
                }
                return if (y <= groundY) {
                    PlayerJumping(nextPlayer, calculateNextEnemies(), t + 1, frame + 1, score + 1)
                } else {
                    PlayerRunning(nextPlayer, calculateNextEnemies(), frame + 1, score + 1)
                }
            }
        }

        data class PlayerRunning(
            override val player: GameObject,
            override val enemyList: List<GameObject>,
            override val frame: Int,
            override val score: Int
        ) : Playing() {
            fun jumpStart(): PlayerJumping {
                return PlayerJumping(
                    player,
                    enemyList,
                    0,
                    frame,
                    score
                )
            }

            override fun calculateNextState(): GameState {
                calculateGameEnd()?.let {
                    return it
                }
                val nextPlayer = if (score < evolutionScore) {
                    uzimaru1
                } else {
                    uzimaru2
                }
                return this.copy(player = nextPlayer, enemyList = calculateNextEnemies(), frame = frame + 1, score = score + 1)
            }
        }
    }

    sealed class End : GameState() {
        data class GameClear(
            override val player: GameObject,
            override val enemyList: List<GameObject>,
            override val frame: Int,
            override val score: Int
        ) : End()

        data class GameOver(
            override val player: GameObject,
            override val enemyList: List<GameObject>,
            override val frame: Int,
            override val score: Int
        ) : End()
    }

    companion object {
        const val canvasWidth = 800
        const val canvasHeight = 600
        const val groundY = 400
        const val evolutionScore = 1000
        const val clearScore = 2000
        const val playerX = 600
        const val playerWidth = 100
        const val playerHeight = 100
        const val enemyWidth = 100
        const val enemyHeight = 100
        const val chickenWidth = 200
        const val chickenHeight = 200

        lateinit var uzimaru1: GameObject
            private set
        lateinit var uzimaru2: GameObject
            private set
        lateinit var enemy: GameObject
            private set
        lateinit var chicken: GameObject
            private set

        suspend fun init() {
            uzimaru1 = GameObject(
                playerX,
                groundY,
                playerWidth,
                playerHeight,
                resourcesVfs["images/v2.svg"].readSVG().render(),
            )
            uzimaru2 = GameObject(
                playerX,
                groundY,
                playerWidth,
                playerHeight,
                resourcesVfs["images/v3.svg"].readSVG().render()
            )
            enemy = GameObject(
                0,
                groundY,
                enemyWidth,
                enemyHeight,
                resourcesVfs["images/v1.svg"].readSVG().render()
            )
            chicken = GameObject(
                canvasWidth / 2 - chickenWidth / 2,
                canvasHeight / 2,
                chickenWidth,
                chickenHeight,
                resourcesVfs["images/v4.svg"].readSVG().render(),
            )
        }

        fun newGame(): GameState {
            return Playing.PlayerRunning(
                uzimaru1,
                emptyList(),
                0,
                0
            )
        }

    }
}
