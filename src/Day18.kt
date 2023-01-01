import java.util.LinkedList

private val neighbours = listOf(
    Vec3(1, 0, 0),
    Vec3(-1, 0, 0),
    Vec3(0, 1, 0),
    Vec3(0, -1, 0),
    Vec3(0, 0, 1),
    Vec3(0, 0, -1),
)

private fun vectorInRange(vector: Vec3, space: List<List<List<Boolean>>>): Boolean {
    return vector.z in space.indices &&
            vector.y in space[vector.z].indices &&
            vector.x in space[vector.z][vector.y].indices
}

fun main() {
    fun List<String>.parse(): List<Vec3> {
        return map { line ->
            val (z, y, x) = line.split(",").map { it.toInt() }
            Vec3(x, y, z)
        }.sorted()
    }

    fun part1(input: List<String>): Int {
        val vectors = input.parse()
        var visibleSides = 0
        for (vec in vectors) {
            var sides = 6
            for (other in vectors) {
                if (vec.dist(other) == 1) {
                    sides -= 1
                }
            }
            visibleSides += sides
        }
        return visibleSides
    }

    fun BFS3d(space: List<List<List<Boolean>>>): Set<Vec3> {
        val start = Vec3(0, 0, 0)
        val queue = LinkedList<Vec3>()
        queue.add(start)
        val visited = mutableSetOf<Vec3>()
        while (!queue.isEmpty()) {
            val current = queue.pop()
            if (current in visited) continue
            visited.add(current)
            for (neighbour in neighbours) {
                val n = current + neighbour
                if (vectorInRange(n, space) &&
                    !space[n.z][n.y][n.x]
                ) {
                    queue.add(n)
                }
            }
        }
        return visited
    }

    fun printSpace(space: List<List<List<Boolean>>>) {

        println()
        for (z in space.indices) {
            print("  ")
            for (x in space.first().first().indices) {
                print(x)
            }
            println()
            for (y in space[z].indices) {
                print("$y ")
                for (x in space[z][y].indices) {
                    print(if (space[z][y][x]) "#" else ".")
                }
                println()
            }
            println()
        }
        println()
    }

    fun part2(input: List<String>, debug: Boolean = false): Int {
        val vectors = input.parse()
        val maxX = vectors.maxBy { it.x }.x + 1
        val maxY = vectors.maxBy { it.y }.y + 1
        val maxZ = vectors.maxBy { it.z }.z + 1
        val space = List(maxZ) {
            List(maxY) {
                MutableList(maxX) { false }
            }
        }
        for (v in vectors) {
            space[v.z][v.y][v.x] = true
        }
        if (debug) printSpace(space)
        val visited = BFS3d(space)
        for (z in space.indices) {
            for (y in space.first().indices) {
                for (x in space.first().first().indices) {
                    space[z][y][x] = Vec3(z, y, x) !in visited
                }
            }
        }
        if (debug) printSpace(space)
        var visibleSides = 0
        for (z in space.indices) {
            for (y in space.first().indices) {
                for (x in space.first().first().indices) {
                    for (n in neighbours) {
                        if (space[z][y][x]) {
                            val other = Vec3(z + n.z, y + n.y, x + n.x)
                            if (vectorInRange(other, space)) {
                                if (!space[other.z][other.y][other.x]) {
                                    visibleSides += 1
                                }
                            } else {
                                visibleSides += 1
                            }
                        }
                    }
                }
            }
        }
        return visibleSides
    }

    val testInput = readInput("Day18_test")

    val input = readInput("Day18")
    assert(part1(testInput), 64)
    println(part1(input))
    assert(part2(testInput, debug = true), 58)
    println(part2(input))
}
// Time: 01:18