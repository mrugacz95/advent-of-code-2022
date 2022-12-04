fun main() {
    fun part1(input: List<String>): Int {
        return input.sumOf {
            val (left, right) = it.chunked(it.length / 2)
            val common = left.toSet().intersect(right.toSet()).first()
            if (common.isLowerCase()) {
                common - 'a' + 1
            } else {
                common - 'A' + 27
            }
        }
    }

    fun part2(input: List<String>): Int {
        return input.chunked(3).sumOf {
            val commonChars = it.map { it.toHashSet() }.foldRight(HashSet<Char>()){ item, acc ->
                if (acc.isEmpty())
                    item
                else
                    acc.intersect(item).toHashSet()
            }
            val common = commonChars.first()
            if (common.isLowerCase()) {
                common - 'a' + 1
            } else {
                common - 'A' + 27
            }
        }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day03_test")
    println(part1(testInput))
    println(part2(testInput))

    val input = readInput("Day03")
    println(part1(input))
    println(part2(input))
}
