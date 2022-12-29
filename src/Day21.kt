private sealed class ParseTree
private data class Terminal(val value: Int) : ParseTree()
private data class Production(var job1: String, var job2: String, val symbol: Char) : ParseTree()

private const val ROOT = "root"

fun main() {
    fun List<String>.parse(): Map<String, ParseTree> {
        val tree = mutableMapOf<String, ParseTree>()
        for (line in this) {
            val key = line.slice(0..3)
            val value = line.slice(6 until line.length)
            val operation = if (" " in value) {
                val job1 = line.slice(6..9)
                val job2 = line.slice(13..16)
                val symbol = line[11]
                Production(job1, job2, symbol)
            } else {
                Terminal(value.toInt())
            }
            tree[key] = operation
        }
        return tree
    }

    fun evaluateNode(name: String, tree: Map<String, ParseTree>): Long {
        val node = tree[name]
        when (node) {
            is Terminal -> return node.value.toLong()
            is Production -> {
                val operation: (Long, Long) -> Long = when (node.symbol) {
                    '+' -> { a, b -> a + b }
                    '*' -> { a, b -> a * b }
                    '/' -> { a, b -> a / b }
                    '-' -> { a, b -> a - b }
                    else -> error("Unknown operation")
                }
                val ev1 = evaluateNode(node.job1, tree)
                val ev2 = evaluateNode(node.job2, tree)
                return operation(ev1, ev2)
            }

            else -> {
                error("Unknown type")
            }
        }
    }

    fun part1(input: List<String>): Long {
        val tree = input.parse()
        return evaluateNode(ROOT, tree)
    }

    fun part2(input: List<String>): Int {
        val tree = input.parse().toMutableMap()
        return tree.size
    }

    val testInput = readInput("Day21_test")

    val input = readInput("Day21")
    assert(part1(testInput), 152L)
    println(part1(input))
    assert(part2(testInput), 301)
    println(part2(input))
}
// Part 1 time: 01:20