import java.lang.RuntimeException
import kotlin.math.abs

fun main() {
    fun List<String>.parse(): List<Int?> {
        val addPattern = "addx (?<num>-?\\d+)".toRegex()
        return map {
            val addx = addPattern.matchEntire(it)?.groups?.get("num")?.value
            when {
                it == "noop" -> {
                    null
                }
                addx != null -> {
                    addx.toInt()
                }

                else -> {
                    throw RuntimeException("Unknown pattern $it")
                }
            }
        }
    }

    fun part1(input: List<String>): Int {
        var sum = 0
        var registerX = 1
        val commands = input.parse()
        var ip = 0
        var cycle = 1
        var firstAddXCycle = true
        while (ip < commands.size) {
            val current = commands[ip]
            if ((cycle - 20) % 40 == 0) {
                sum += cycle * registerX
            }
            when (current) {
                is Int -> {
                    if (firstAddXCycle) {
                        firstAddXCycle = false
                    } else {
                        firstAddXCycle = true
                        registerX += current
                        ip += 1
                    }
                }
                else -> {
                    ip += 1
                }
            }

            cycle += 1
        }
        return sum
    }

    fun part2(input: List<String>): String {
        val crt = MutableList(40 * 6) { 'x' }
        var sum = 0
        var registerX = 1
        val commands = input.parse()
        var ip = 0
        var cycle = 1
        var firstAddXCycle = true
        while (ip < commands.size) {
            val current = commands[ip]

            crt[cycle - 1] = if (abs((cycle - 1) % 40 - registerX) <= 1) {
                '#'
            } else {
                '.'
            }
            if ((cycle - 20) % 40 == 0) {
                sum += cycle * registerX
            }
            when (current) {
                is Int -> {
                    if (firstAddXCycle) {
                        firstAddXCycle = false
                    } else {
                        firstAddXCycle = true
                        registerX += current
                        ip += 1
                    }
                }
                else -> {
                    ip += 1
                }
            }
            cycle += 1
        }
        return crt.chunked(40).joinToString("\n") { it.joinToString("") }
    }

    val testInput = readInput("Day10_test")

    val input = readInput("Day10")
    assert(part1(testInput), 13140)
    println(part1(input))

    val expected = """##..##..##..##..##..##..##..##..##..##..
                     |###...###...###...###...###...###...###.
                     |####....####....####....####....####....
                     |#####.....#####.....#####.....#####.....
                     |######......######......######......####
                     |#######.......#######.......#######.....""".trimMargin()
    assert(part2(testInput), expected)
    println()
    println(part2(input))
}
// Time: 09:36