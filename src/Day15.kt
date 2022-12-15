import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class Sensor(val position: Pair<Int, Int>, val beacon: Pair<Int, Int>)

fun main() {
    fun List<String>.parse(): List<Sensor> {
        val sensorRegex =
            "Sensor at x=(?<sx>-?\\d+), y=(?<sy>-?\\d+): closest beacon is at x=(?<bx>-?\\d+), y=(?<by>-?\\d+)".toRegex()
        return map {
            val groups = sensorRegex.matchEntire(it)?.groups ?: error("Cant parse $it")
            val x = groups["sx"]?.value?.toInt() ?: error("Cant parse $it")
            val y = groups["sy"]?.value?.toInt() ?: error("Cant parse $it")
            val bx = groups["bx"]?.value?.toInt() ?: error("Cant parse $it")
            val by = groups["by"]?.value?.toInt() ?: error("Cant parse $it")
            Sensor(position = Pair(y, x), beacon = Pair(by, bx))
        }
    }

    fun manhattanDistance(pos1: Pair<Int, Int>, pos2: Pair<Int, Int>): Int {
        return abs(pos1.first - pos2.first) + abs(pos1.second - pos2.second)
    }

    fun part1(input: List<String>, row: Int): Int {
        val sensors = input.parse()
        val tunnels = HashMap<Pair<Int, Int>, Char>()
        for (sensor in sensors) {
            val dist = manhattanDistance(sensor.position, sensor.beacon)
            val height = manhattanDistance(sensor.position, Pair(row, sensor.position.second))
            val horizontal = dist - height
            if (horizontal < 0) {
                continue
            } else {
                val d = horizontal
                for (x in sensor.position.second - d..sensor.position.second + d) {
                    tunnels[Pair(row, x)] = '#'
//                    println("From ${sensor.position} put $x")
                }
            }
        }
        for (sensor in sensors) {
            tunnels.remove(sensor.beacon)
        }
        return tunnels.size
    }

    fun part2(input: List<String>, maxValue: Int): Long {
        val minValue = 0
        val sensors = input.parse()
        fun checkLackingY(): Pair<Int, Int> {
            for (y in minValue..maxValue) {
                val tunnels = mutableListOf<IntRange>()
                for (sensor in sensors) {
                    val dist = manhattanDistance(sensor.position, sensor.beacon)
                    val height = manhattanDistance(sensor.position, Pair(y, sensor.position.second))
                    val horizontal = dist - height
                    if (horizontal < 0) {
                        continue
                    } else {
                        tunnels.add(
                            max(sensor.position.second - horizontal, 0)..min(
                                sensor.position.second + horizontal,
                                maxValue
                            )
                        )
                    }
                }
                tunnels.sortBy { it.first }
                var maxX = 0
                for (tunnel1 in tunnels) {
                    if (maxX  + 1 < tunnel1.first) {
                        return y to (maxX + 1)
                    }
                    maxX = max(maxX, tunnel1.last)
                }
            }
            error("Position y not found")
        }

        val y = checkLackingY()
        return y.second * 4000000L + y.first
    }

    val testInput = readInput("Day15_test")

    val testRow = 10
    val row = 2000000

    val input = readInput("Day15")
    assert(part1(testInput, testRow), 26)
    println(part1(input, row))

    val testMaxValue = 20
    val maxValue = 4000000
    assert(part2(testInput, testMaxValue), 56000011)
    println(part2(input, maxValue))
}
// Time: 01:20