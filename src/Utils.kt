import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

/**
 * Reads lines from the given input txt file.
 */
fun readInput(name: String) = File("src", "$name.txt")
    .readLines()

/**
 * Converts string to md5 hash.
 */
fun String.md5() = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray()))
    .toString(16)
    .padStart(32, '0')


fun <T> assert(answer: T, expected: T){
    if (answer == expected){
        println("\uD83D\uDFE2 $answer")
    }
    else {
        println("\uD83D\uDD34 Expected $expected but found $answer")
    }
}

data class Pos(val y: Int, val x: Int) {
    operator fun plus(other: Pos): Pos {
        return Pos(y + other.y, x + other.x)
    }

    operator fun minus(other: Pos): Pos {
        return Pos(y - other.y, x - other.x)
    }
}

enum class Direction(val delta: Pos) {
    UP(Pos(-1, 0)),
    DOWN(Pos(1, 0)),
    LEFT(Pos(0, -1)),
    RIGHT(Pos(0, 1));

    fun right() = when (this) {
        UP -> RIGHT
        DOWN -> LEFT
        LEFT -> UP
        RIGHT -> DOWN
    }

    fun left() = when (this) {
        UP -> LEFT
        DOWN -> RIGHT
        LEFT -> DOWN
        RIGHT -> UP
    }

    fun opposite() = when (this) {
        UP -> RIGHT
        DOWN -> UP
        LEFT -> RIGHT
        RIGHT -> LEFT
    }
}