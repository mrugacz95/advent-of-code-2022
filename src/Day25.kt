import java.math.BigInteger

fun snafuToDecimal(snafu: String): BigInteger {
    var base = 1.toBigInteger()
    var sum = 0.toBigInteger()
    for (digit in snafu.reversed()) {
        val number = when (digit) {
            '=' -> -2
            '-' -> -1
            '1' -> 1
            '0' -> 0
            '2' -> 2
            else -> error("Unknown number")
        }.toBigInteger()
        sum += (number * base)
        base *= 5.toBigInteger()
    }
    return sum
}

fun decimalToSNAFU(decimalNumber: BigInteger): String {
    var number = decimalNumber
    val base5 = mutableListOf<Int>()
    while (number != 0.toBigInteger()) {
        val digit = number % 5.toBigInteger()
        number /= 5.toBigInteger()
        base5.add(digit.toInt())
    }
    val snafu = (base5 + listOf(0)).toMutableList()
    for (i in base5.indices) {
        while (snafu[i].toString().toInt() > 2) {
            snafu[i + 1] += 1
            snafu[i] -= 5
        }
        while (snafu[i].toString().toInt() < -2) {
            snafu[i + 1] -= 1
            snafu[i] += 5
        }
    }

    val result = snafu.reversed().map {
        when (it) {
            1 -> '1'
            2 -> '2'
            0 -> '0'
            -1 -> '-'
            -2 -> '='
            else -> error("unknown digit $it in $decimalNumber")

        }
    }.joinToString("").let {
        if (it.first() == '0') {
            it.drop(1)
        } else {
            it
        }
    }
    return result
}

fun main() {
    val decimalNumbers = listOf(
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 2022, 12345, 314159265,
        1747, 906, 198
    ).map { it.toBigInteger() }
    val snafuNumbers = listOf(
        "1", "2", "1=", "1-", "10", "11", "12", "2=", "2-", "20", "1=0", "1-0", "1=11-2", "1-0---0", "1121-1110-1=0",
        "1=-0-2", "12111", "2=0="
    )
    for ((dec, snafu) in decimalNumbers.zip(snafuNumbers)) {
        assert(decimalToSNAFU(dec) == snafu)
        assert(snafuToDecimal(snafu) == dec)
    }

    fun part1(input: List<String>): String {
        return input.map { snafuToDecimal(it) }.reduce { acc, it -> acc + it }.let {
            decimalToSNAFU(it)
        }
    }

    val testInput = readInput("Day25_test")

    val input = readInput("Day25")
    assert(part1(testInput), "2=-1=0")
    println(part1(input))
}
// Time: 01:00