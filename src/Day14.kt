import kotlin.math.max
import kotlin.math.min

private fun List<List<Char>>.print() {
    println(joinToString("\n") { it.joinToString("") })
}

private data class Pos(var y: Int, var x: Int)

fun main() {
    fun List<String>.parse(): Triple<MutableList<MutableList<Char>>, Pair<Int, Int>, Pair<Int, Int>> {
        val lines = map {
            it.split(" -> ").map { line ->
                val (x, y) = line.split(",")
                y.toInt() to x.toInt()
            }
        }
        var xPos = Pair(Int.MAX_VALUE, -Int.MAX_VALUE)
        var yPos = Pair(Int.MAX_VALUE, -Int.MAX_VALUE)
        lines.flatten().map {
            yPos = Pair(min(it.first, yPos.first), max(it.first, yPos.second))
            xPos = Pair(min(it.second, xPos.first), max(it.second, xPos.second))
        }
        yPos = Pair(min(0, yPos.first), yPos.second + 1)
        xPos = Pair(0, 1000)
        val cave = MutableList(yPos.second - yPos.first + 1) {
            MutableList(xPos.second - xPos.first + 1) { ' ' }
        }
        for (line in lines) {
            for ((start, end) in line.zipWithNext()) {
                for (y in min(start.first, end.first)..max(start.first, end.first)) {
                    for (x in min(start.second, end.second)..max(start.second, end.second)) {
                        cave[y - yPos.first][x - xPos.first] = 'x'
                    }
                }
            }
        }
        return Triple(cave, yPos, xPos)
    }

    fun simulate(
        cave: MutableList<MutableList<Char>>,
        xPos: Pair<Int, Int>,
        yPos: Pair<Int, Int>,
        finishCondition: (Pos) -> Boolean
    ): Int {
        var units = 0
        while (true) {
            val sand = Pos(0, 500)
            units += 1
            while (true) {
                if (finishCondition(sand)) {
                    return units - 1
                }
                if (sand.y == yPos.second) {
                    break
                }
                if (cave[sand.y + 1 - yPos.first][sand.x - xPos.first] == ' ') {
                    sand.y += 1
                    continue
                }
                if (cave[sand.y + 1 - yPos.first][sand.x - 1 - xPos.first] == ' ') {
                    sand.y += 1
                    sand.x -= 1
                    continue
                }
                if (cave[sand.y + 1 - yPos.first][sand.x + 1 - xPos.first] == ' ') {
                    sand.y += 1
                    sand.x += 1
                    continue
                }
                break
            }
            cave[sand.y - yPos.first][sand.x - xPos.first] = 'o'
//            cave.print()
        }
    }

    fun part1(input: List<String>): Int {
        val (cave, yPos, xPos) = input.parse()
        return simulate(cave, xPos, yPos) { sand ->
            sand.y >= yPos.second
        }
    }

    fun part2(input: List<String>): Int {
        val (cave, yPos, xPos) = input.parse()
        return simulate(cave, xPos, yPos) { sand ->
            cave[0 - yPos.first][500 - xPos.first] == 'o'
        }
    }

    val testInput = readInput("Day14_test")

    val input = readInput("Day14")
    assert(part1(testInput), 24)
    println(part1(input))
    assert(part2(testInput), 93)
    println(part2(input))
}
// Time: 01:00