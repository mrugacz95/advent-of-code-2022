private data class Tree(
    val height: Int,
    var left: Boolean? = null,
    var top: Boolean? = null,
    var right: Boolean? = null,
    var bottom: Boolean? = null,
    val x: Int,
    val y: Int,
) {
    fun visible() = top == true ||
            left == true ||
            right == true ||
            bottom == true
}

fun main() {
    fun List<String>.parse(): List<List<Tree>> {
        return mapIndexed { y, line ->
            line.mapIndexed { x, tree ->
                Tree(tree.toString().toInt(), x = x, y = y)
            }
        }
    }

    fun part1(input: List<String>): Int {
        val forest = input.parse()
        for (y in forest.indices) {
            var highest = -1
            for (x in forest[y].indices) {
                val tree = forest[y][x]
                if (tree.height <= highest) {
                    tree.left = false
                } else {
                    tree.left = true
                    highest = tree.height
                }
            }
        }
        for (y in forest.indices) {
            var highest = -1
            for (x in forest[y].indices.reversed()) {
                val tree = forest[y][x]
                if (tree.height <= highest) {
                    tree.right = false
                } else {
                    tree.right = true
                    highest = tree.height
                }
            }
        }
        for (x in forest.first().indices) {
            var highest = -1
            for (y in forest.indices) {
                val tree = forest[y][x]
                if (tree.height <= highest) {
                    tree.top = false
                } else {
                    tree.top = true
                    highest = tree.height
                }
            }
        }
        for (x in forest.first().indices) {
            var highest = -1
            for (y in forest.indices.reversed()) {
                val tree = forest[y][x]
                if (tree.height <= highest) {
                    tree.bottom = false
                } else {
                    tree.bottom = true
                    highest = tree.height
                }
            }
        }
        return forest.flatten().count {
            it.visible()
        }
    }

    fun part2(input: List<String>): Int {
        val forest = input.parse()
        return forest.flatten().map { tree ->
            var left = 0
            var right = 0
            var top = 0
            var bottom = 0
            for (x in tree.x - 1 downTo 0) {
                left += 1
                if (forest[tree.y][x].height >= tree.height) {
                    break
                }
            }
            for (x in tree.x + 1 until forest.size) {
                right += 1
                if (forest[tree.y][x].height >= tree.height) {
                    break
                }
            }
            for (y in tree.y - 1 downTo 0) {
                top += 1
                if (forest[y][tree.x].height >= tree.height) {
                    break
                }
            }
            for (y in tree.y + 1 until forest.size) {
                bottom += 1
                if (forest[y][tree.x].height >= tree.height) {
                    break
                }
            }
            left * top * right * bottom
        }.maxOf { it }
    }

    val testInput = readInput("Day08_test")

    val input = readInput("Day08")
    assert(part1(testInput), 21)
    println(part1(input))
    assert(part2(testInput), 8)
    println(part2(input))
}
// Time: 01:00