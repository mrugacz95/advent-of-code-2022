fun main() {
    fun List<String>.parse(): List<Int> {
        return map { it.toInt() }
    }

    fun <T> List<T>.circulateIndex(index: Int): Int {
        return ((index + 10 * (size - 1)) % (size - 1))
    }

    fun <T> List<T>.circulateIndex(index: Long): Int {
        return (((index % (size - 1)).toInt()) + 10 * (size - 1)) % (size - 1)
    }

    fun mixNumbers(list: List<Int>, debug: Boolean = false): List<Int> {
        val numbers = list.toMutableList()
        val indices = numbers.indices.toMutableList()
        for (i in 0 until numbers.size) {
            val indexInIndices = indices.indexOf(i)
            val number = numbers[indices[indexInIndices]]
            var newIndex = indexInIndices + number
            newIndex = numbers.circulateIndex(newIndex)
            indices.removeAt(indexInIndices)
            indices.add(newIndex, i)
            if (debug) println("$number moves")
            if (debug) println(indices.joinToString { numbers[it].toString() })
        }
        return indices.map { numbers[it] }
    }

    fun mixNumbersManyTimes(list: List<Long>, times: Int = 10, debug: Boolean = false): List<Long> {
        val numbers = list.toMutableList()
        val indices = numbers.indices.toMutableList()
        for (round in 1..times) {
            for (i in 0 until numbers.size) {
                val indexInIndices = indices.indexOf(i)
                val number = numbers[indices[indexInIndices]]
                val newIndex = indexInIndices + number
                val newIndexCirculated = numbers.circulateIndex(newIndex)
                indices.removeAt(indexInIndices)
                indices.add(newIndexCirculated, i)
            }

            if (debug) println("after $round round of mixing")
            if (debug) println(indices.joinToString { numbers[it].toString() })
        }
        return indices.map { numbers[it] }
    }

    fun part1(input: List<String>, debug: Boolean = false): Int {
        val mixed = mixNumbers(input.parse(), debug)
        val zeroIndex = mixed.indexOf(0)
        val numbers = (1000..3000 step 1000).map {
            mixed[(it + zeroIndex) % mixed.size]
        }
        return numbers.reduce { acc, it -> acc + it }
    }

    fun List<Long>.getGroveCoordinates(): Long {
        val zeroIndex = indexOf(0)
        val numbers = (1000..3000 step 1000).map {
            this[(it + zeroIndex) % size]
        }
        return numbers.reduce { acc, it -> acc + it }
    }

    fun part2(input: List<String>, debug: Boolean = false): Long {
        return input.parse()
            .map { it.toLong() * 811589153L }
            .let { mixNumbersManyTimes(it, debug = debug) }.getGroveCoordinates()
    }

    val testInput = readInput("Day20_test")

    val input = readInput("Day20")
    assert(part1(testInput, debug = true), 3)
    println(part1(input))
    assert(part2(testInput, debug = true), 1623178306)
    println(part2(input))
}
// Time: 01:12