fun main() {
    fun part1(input: List<String>): Int {
        return input.first().windowed(4).indexOfFirst { it.toSet().size == 4 } + 4
    }

    fun part2(input: List<String>): Int {
        return input.first().windowed(14).indexOfFirst { it.toSet().size == 14 } + 14
    }

    val testInput = readInput("Day06_test")
    val input = readInput("Day06")

    assert(part1(testInput), 7)
    println(part1(input))
    assert(part2(testInput), 19)
    println(part2(input))
}
// Time: 00:07
