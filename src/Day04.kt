private fun IntRange.contains(other: IntRange): Boolean {
    return first <= other.first && last >= other.last
}

private fun IntRange.overlap(other: IntRange): Boolean {
    return first <= other.last && other.first <= last
}

fun main() {
    fun parse(line: String): List<IntRange> {
        return line.split(',').map { segment ->
            val (a,b) = segment.split('-').map { it.toInt() }
            a..b
        }
    }

    fun part1(input: List<List<IntRange>>): Int {
        return input.filter { (a,b) ->
            a.contains(b) || b.contains(a)
        }.size
    }

    fun part2(input: List<List<IntRange>>): Int {
        return input.filter { (a,b) ->
            a.overlap(b)
        }.size
    }

    val testInput = readInput("Day04_test").map { parse(it) }
    assert(part1(testInput), 2)
    assert(part2(testInput), 4)

    val input = readInput("Day04").map { parse(it) }
    println(part1(input))
    println(part2(input))
}
