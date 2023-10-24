package model

import korlibs.image.bitmap.*
import kotlin.math.*

data class GameObject(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val image: Bitmap,
) {

    fun checkCollision(other: GameObject): Boolean {
        val centerX1 = this.x + this.width / 2.0
        val centerX2 = other.x + other.width / 2.0
        val centerY1 = this.y + this.height / 2.0
        val centerY2 = other.y + other.height / 2.0
        return abs(centerX1 - centerX2) < (this.width / 2.0 + other.width / 2.0) * 0.7 &&
            abs(centerY1 - centerY2) < (this.height / 2.0 + other.width / 2.0) * 0.7
    }
}
