private sealed class TreeNode(open val name: String)
private data class Terminal(
    val value: Int,
    override val name: String
) : TreeNode(name)

private data class Production(
    var lhs: TreeNode,
    var rhs: TreeNode,
    val symbol: Char,
    override val name: String
) : TreeNode(name)

private const val ROOT = "root"

fun main() {
    fun List<String>.parse(): TreeNode {

        val tree = this.associate {
            val key = it.slice(0..3)
            val value = it.slice(6 until it.length)
            key to value
        }

        fun parseSubTree(nodeName: String): TreeNode {
            val value = tree[nodeName] ?: error("Node not found")
            return if (!value.first().isDigit()) {
                val lhsNodeName = value.slice(0..3)
                val rhsNodeName = value.slice(7..10)
                val lhs = parseSubTree(lhsNodeName)
                val rhs = parseSubTree(rhsNodeName)
                val symbol = value[5]
                Production(lhs, rhs, symbol, nodeName)
            } else {
                Terminal(value.toInt(), nodeName)
            }
        }


        return parseSubTree(ROOT)
    }

    fun evaluateNode(tree: TreeNode): Long {
        when (tree) {
            is Terminal -> return tree.value.toLong()
            is Production -> {
                val operation: (Long, Long) -> Long = when (tree.symbol) {
                    '+' -> { a, b -> a + b }
                    '*' -> { a, b -> a * b }
                    '/' -> { a, b -> a / b }
                    '-' -> { a, b -> a - b }
                    else -> error("Unknown operation")
                }
                val ev1 = evaluateNode(tree.lhs)
                val ev2 = evaluateNode(tree.rhs)
                return operation(ev1, ev2)
            }

            else -> {
                error("Unknown type")
            }
        }
    }

    fun part1(input: List<String>): Long {
        val tree = input.parse()
        return evaluateNode(tree)
    }

    fun part2(input: List<String>): Int {
        val tree = input.parse()
        return 0
    }

    val testInput = readInput("Day21_test")

    val input = readInput("Day21")
    assert(part1(testInput), 152L)
    println(part1(input))
    assert(part2(testInput), 301)
    println(part2(input))
}
// Part 1 time: 01:20