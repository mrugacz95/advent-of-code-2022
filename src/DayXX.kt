fun main() {
    fun List<String>.parse() : List<String> {
        return this
    }

    fun part1(input: List<String>): Int {
        return input.parse().size
    }

    fun part2(input: List<String>): Int {
        return input.parse().size
    }

    val testInput = readInput("Day0X_test")

    val input = readInput("Day0X")
    assert(part1(testInput), 1)
    println(part1(input))
    assert(part2(testInput), 1)
    println(part2(input))
}
// Time: 00:XX