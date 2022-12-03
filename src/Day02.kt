private fun abcIndex(s: String) = s[0] - 'A'
private fun xyzIndex(s: String) = s[0] - 'X'

private fun xyzScore(s: String) = xyzIndex(s) + 1

private fun roundResult(opponentSymb: String, yourSymb: String): Int {
    return listOf(
        listOf(3, 6, 0),
        listOf(0, 3, 6),
        listOf(6, 0, 3),
    )[abcIndex(opponentSymb)][xyzIndex(yourSymb)] + xyzScore(yourSymb)
}

private fun whatChoose(opponentSymb: String, result: String): String {
    return listOf(
        listOf("Z", "X", "Y"),
        listOf("X", "Y", "Z"),
        listOf("Y", "Z", "X"),
    )[abcIndex(opponentSymb)][xyzIndex(result)]
}

fun main() {
    fun part1(input: List<Pair<String, String>>): Int {
        return input.sumOf {
            val (a, b) = it
            roundResult(a, b)
        }
    }

    fun part2(input: List<Pair<String, String>>): Int {
        return input.sumOf {
            val (a, b) = it
            val yourSymb = whatChoose(a, b)
            roundResult(a, yourSymb)
        }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day02_test")
        .map {
            val (a, b) = it.split(' ')
            Pair(a, b)
        }
    println(part1(testInput))
    println(part2(testInput))

    val input = readInput("Day02").map {
        val (a, b) = it.split(' ')
        Pair(a, b)
    }
    println(part1(input))
    println(part2(input))
}
