fun main() {
    fun part1(input: List<String>): Int {
        return input.size
    }

    fun part2(input: List<String>): Int {
        return input.size
    }

    val testInput = readInput("Day0X_test")

    val input = readInput("Day0X")
    assert(part1(testInput), 1)
    println(part1(input))
    assert(part2(testInput), 1)
    println(part2(input))
}
