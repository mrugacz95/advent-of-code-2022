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