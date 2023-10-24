import korlibs.event.*
import korlibs.image.bitmap.*
import korlibs.time.*
import korlibs.korge.*
import korlibs.korge.scene.*
import korlibs.korge.tween.*
import korlibs.korge.view.*
import korlibs.image.color.*
import korlibs.image.format.*
import korlibs.image.text.*
import korlibs.image.vector.*
import korlibs.image.vector.format.*
import korlibs.io.file.std.*
import korlibs.korge.input.*
import korlibs.math.geom.*
import korlibs.math.interpolation.*
import kotlinx.coroutines.*
import model.*

suspend fun main() = Korge(
    windowSize = Size(GameState.canvasWidth, GameState.canvasHeight),
    backgroundColor = Colors["#3C3C3C"],
) {
	val sceneContainer = sceneContainer()
	sceneContainer.changeTo { MyScene() }
}

class MyScene : PixelatedScene(GameState.canvasWidth, GameState.canvasHeight, sceneSmoothing = true) {
    private lateinit var gameState: GameState
    private var keydown = false

    override suspend fun SContainer.sceneInit() {
        GameState.init()
        gameState = GameState.Start
    }

	override suspend fun SContainer.sceneMain() {
        mouse.onClick {
            keydown = true
        }
        keys.down(Key.SPACE) {
            keydown = true
        }
        addFixedUpdater(60.timesPerSecond) {
            updateStatus()
            drawGameState(gameState)
        }
	}

    private fun SContainer.drawGameObject(gameObject: GameObject) {
        image(gameObject.image) {
            position(gameObject.x, gameObject.y)
            size(gameObject.width, gameObject.height)
        }
    }

    private fun SContainer.drawGameState(gameState: GameState) {
        removeChildren()
        val textColor = Colors["#199861"]
        text("score: ${gameState.score}", 24, textColor) {
            position(50,50)
        }
        when (gameState) {
            is GameState.Start -> {
                val statusText = "Please Press Space Key or Click!"
                text(statusText, 32, textColor, alignment = TextAlignment.CENTER) {
                    position(GameState.canvasWidth / 2, GameState.canvasHeight / 2)
                }
            }
            is GameState.Playing -> {
                gameState.enemyList.forEach {
                    drawGameObject(it)
                }
                drawGameObject(gameState.player)
            }
            is GameState.End.GameOver -> {
                gameState.enemyList.forEach {
                    drawGameObject(it)
                }
                drawGameObject(gameState.player)
                val statusText = "Game Over!"
                text(statusText, 32, textColor, alignment = TextAlignment.CENTER) {
                    position(GameState.canvasWidth / 2, GameState.canvasHeight / 2)
                }
            }
            is GameState.End.GameClear -> {
                drawGameObject(gameState.player)
                drawGameObject(GameState.chicken)
                val statusText = "Happy Birthday uzimaru!"
                text(statusText, 32, textColor, alignment = TextAlignment.CENTER) {
                    position(GameState.canvasWidth / 2, GameState.canvasHeight / 2)
                }
            }
        }
    }

    private fun updateStatus() {
        if (keydown) {
            keydown = false
            when (val currentState = gameState) {
                is GameState.Start -> {
                    gameState = GameState.newGame()
                }
                is GameState.Playing.PlayerRunning -> {
                    gameState = currentState.jumpStart()
                }
                is GameState.Playing.PlayerJumping -> {
                    // no-op
                }
                is GameState.End -> {
                    gameState = GameState.Start
                }
            }
        }
        val currentState = gameState
        if (currentState is GameState.Playing) {
            val nextState = currentState.calculateNextState()
            if (currentState != nextState) {
                gameState = nextState
            }
        }
    }
}
