
private typealias Group = MutableList<List<Int>>
fun main() {
    fun part1(input: Group): Int {
        return input.maxOfOrNull {
                it.sum()
            } ?: throw Exception("No solution found")
    }

    fun part2(input: Group): Int {
        return input
            .map { it.sum() }
            .sorted()
            .takeLast(3)
            .sum()
    }

    var group = mutableListOf<Int>()
    val input = readInput("Day01")
        .map { it.toIntOrNull() }
        .foldRight(mutableListOf<List<Int>>()) { item, acc ->
            if (item == null) {
                acc.add(group)
                group = mutableListOf()
            } else {
                group.add(item)
            }
            acc
        }
    println(part1(input))
    println(part2(input))
}
