import java.math.BigInteger

private const val DEBUG = true

private sealed class TreeNode(open val name: String?)
private data class Terminal(
    val value: BigInteger,
    override val name: String?
) : TreeNode(name) {
    override fun toString(): String {
        return "$value"
    }
}

private data class Production(
    var lhs: TreeNode,
    var rhs: TreeNode,
    var symbol: Char,
    override val name: String?
) : TreeNode(name) {
    override fun toString(): String {
        return "($lhs $symbol $rhs)"
    }
}

private data class Human(
    override val name: String
) : TreeNode(name) {
    override fun toString(): String {
        return "x"
    }
}

private const val ROOT = "root"
private const val HUMAN = "humn"

private fun oppositeSymbol(symbol: Char) = when (symbol) {
    '+' -> '-'
    '-' -> '+'
    '*' -> '/'
    '/' -> '*'
    else -> error("Unknown symbol")
}

fun main() {
    fun List<String>.parse(replaceHuman: Boolean = false): TreeNode {

        val tree = this.associate {
            val key = it.slice(0..3)
            val value = it.slice(6 until it.length)
            key to value
        }

        fun parseSubTree(nodeName: String): TreeNode {
            if (replaceHuman && nodeName == HUMAN) {
                return Human(HUMAN)
            }
            val value = tree[nodeName] ?: error("Node not found")
            return if (!value.first().isDigit()) {
                val lhsNodeName = value.slice(0..3)
                val rhsNodeName = value.slice(7..10)
                val lhs = parseSubTree(lhsNodeName)
                val rhs = parseSubTree(rhsNodeName)

                val symbol = if (replaceHuman && nodeName == ROOT) {
                    '='
                } else {
                    value[5]
                }
                Production(lhs, rhs, symbol, nodeName)
            } else {
                Terminal(value.toBigInteger(), nodeName)
            }
        }

        return parseSubTree(ROOT)
    }

    fun evaluateNode(tree: TreeNode): BigInteger {
        when (tree) {
            is Terminal -> return tree.value
            is Production -> {
                val operation: (BigInteger, BigInteger) -> BigInteger = when (tree.symbol) {
                    '+' -> { a, b -> a + b }
                    '*' -> { a, b -> a * b }
                    '/' -> { a, b -> a / b }
                    '-' -> { a, b -> a - b }
                    '=' -> {
                        if (tree.lhs is Human) {
                            return evaluateNode(tree.rhs)
                        } else {
                            return evaluateNode(tree.lhs)
                        }
                    }

                    else -> error("Unknown operation")
                }
                val ev1 = evaluateNode(tree.lhs)
                val ev2 = evaluateNode(tree.rhs)
                return operation(ev1, ev2)
            }

            is Human -> error("Unknown value of $HUMAN")
            else -> {
                error("Unknown type")
            }
        }
    }

    fun TreeNode.hasUnknown(): Boolean {
        return when (this) {
            is Human -> true
            is Production -> lhs.hasUnknown() || rhs.hasUnknown()
            is Terminal -> false
        }
    }

    fun TreeNode.simplify(): TreeNode {
        return if (hasUnknown()) {
            this
        } else {
            when (this) {
                is Production -> Terminal(value = evaluateNode(this), name = null)
                is Terminal -> this
                is Human -> error("this node is unknown")
            }
        }
    }

    fun part1(input: List<String>): BigInteger {
        val tree = input.parse()
        return evaluateNode(tree)
    }

    fun part2(input: List<String>, debug : Boolean = false): BigInteger {
        val tree = input.parse(replaceHuman = true) as? Production ?: error("Root is not production")
        if (debug) println(tree)
        tree.lhs = tree.lhs.simplify()
        tree.rhs = tree.rhs.simplify()
        if (tree.rhs.hasUnknown()) {
            tree.lhs = tree.rhs.also { tree.rhs = tree.lhs }
        }
        if (debug) println(tree)
        while (tree.lhs !is Human) {
            val rhsTree = tree.rhs
            val lhsTree = tree.lhs as? Production ?: error("lhs is not production")
            val subLeft = (tree.lhs as Production).lhs
            val subRight = (tree.lhs as Production).rhs
            if (lhsTree.symbol in listOf('-', '/') && subRight.hasUnknown()) { // handle sub and div with swap
                tree.lhs = subRight
                tree.rhs = Production(subLeft, rhsTree, lhsTree.symbol, name = null)
            }
            else {
                if (subLeft.hasUnknown()) {
                    tree.lhs = subLeft
                    tree.rhs = Production(rhsTree, subRight, oppositeSymbol(lhsTree.symbol), name = null)
                } else {
                    tree.lhs = subRight
                    tree.rhs = Production(rhsTree, subLeft, oppositeSymbol(lhsTree.symbol), name = null)
                }
            }
            tree.rhs = tree.rhs.simplify()
            if (debug) println(tree)
        }
        return evaluateNode(tree)
    }

    val testInput = readInput("Day21_test")

    val input = readInput("Day21")
    assert(part1(testInput).toInt(), 152)
    println(part1(input))
    assert(part2(testInput, debug = DEBUG).toInt(), 301)
    println(part2(input))
}
// Part 1 time: 01:20
// Part 2 time: 01:50