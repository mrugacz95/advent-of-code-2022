import kotlin.math.abs

operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>): Pair<Int, Int> {
    return Pair(this.first + other.first, this.second + other.second)
}

fun Pair<Int, Int>.adjacent(other: Pair<Int, Int>): Boolean {
    return abs(this.first - other.first) <= 1 &&
            abs(this.second - other.second) <= 1
}

fun main() {
    fun List<String>.parse(): List<Pair<Char, Int>> {
        return map {
            val (a, b) = it.split(' ')
            val dist = b.toInt()
            Pair(a.first(), dist)
        }
    }

    fun directionToPair(d: Char): Pair<Int, Int> {
        val dist = 1
        return when (d) {
            'R' -> Pair(0, dist)
            'L' -> Pair(0, -dist)
            'D' -> Pair(dist, 0)
            'U' -> Pair(-dist, 0)
            else -> throw Exception("cant parse")
        }
    }

    fun part1(input: List<String>): Int {
        var headPos = Pair(0, 0)
        var tailPos = Pair(0, 0)
        val tailPositions = mutableSetOf<Pair<Int, Int>>()
        tailPositions.add(tailPos)
        input.parse().map { (d, l) ->
            val dir = directionToPair(d)
            for (i in 0 until l) {
                headPos += dir
                if (!headPos.adjacent(tailPos)) {
                    if (headPos.first > tailPos.first) {
                        tailPos += Pair(1, 0)
                    }
                    if (headPos.second > tailPos.second) {
                        tailPos += Pair(0, 1)
                    }
                    if (headPos.first < tailPos.first) {
                        tailPos += Pair(-1, 0)
                    }
                    if (headPos.second < tailPos.second) {
                        tailPos += Pair(0, -1)
                    }
                }
                tailPositions.add(tailPos)
            }
        }
        return tailPositions.size
    }

    fun part2(input: List<String>): Int {
        val ropePos = MutableList(10) { Pair(0, 0) }
        val tailPositions = mutableSetOf<Pair<Int, Int>>()
        tailPositions.add(ropePos.last())
        input.parse().map { (d, l) ->
            val dir = directionToPair(d)
            for (i in 0 until l) {
                ropePos[0] += dir
                for (ropePartIdx in ropePos.indices) {
                    if (ropePartIdx == 0) continue
                    val prevPos = ropePos[ropePartIdx - 1]
                    val ropePart = ropePos[ropePartIdx]
                    var newRopePart = ropePart
                    if (!prevPos.adjacent(ropePart)) {
                        if (prevPos.first > ropePart.first) {
                            newRopePart += Pair(1, 0)
                        }
                        if (prevPos.second > ropePart.second) {
                            newRopePart += Pair(0, 1)
                        }
                        if (prevPos.first < ropePart.first) {
                            newRopePart += Pair(-1, 0)
                        }
                        if (prevPos.second < ropePart.second) {
                            newRopePart += Pair(0, -1)
                        }
                    }
                    ropePos[ropePartIdx] = newRopePart
                }
                tailPositions.add(ropePos.last())
            }
        }
        return tailPositions.size
    }

    val testInput1 = readInput("Day09_test1")
    val testInput2 = readInput("Day09_test2")

    val input = readInput("Day09")
    assert(part1(testInput1), 13)
    println(part1(input))
    assert(part2(testInput2), 36)
    println(part2(input))
}
// Time: 00:50