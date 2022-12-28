import java.util.PriorityQueue

private const val DEBUG = true

private data class Blizzard(val id: Int, val direction: Direction, val position: Pos)

private data class Valley(val blizzards: List<Blizzard>, val height: Int, val width: Int) {

    override fun toString(): String {
        return toString(null)
    }

    fun toString(playerPos: Pos? = null): String {
        val valley = MutableList(height) { y ->
            MutableList(width) { x ->
                if (y == entrance.y && x == entrance.x) {
                    '.'
                } else if (y == exit.y && x == exit.x) {
                    ','
                } else if (y == 0 || x == 0 || y == height - 1 || x == width - 1) {
                    '#'
                } else {
                    '.'
                }
            }
        }
        val groups = blizzards.groupBy { it.position }
        for ((position, blizzards) in groups) {
            valley[position.y][position.x] = if (blizzards.size == 1) {
                val blizzard = blizzards.first()
                when (blizzard.direction) {
                    Direction.UP -> '^'
                    Direction.DOWN -> 'v'
                    Direction.LEFT -> '<'
                    Direction.RIGHT -> '>'
                }
            } else {
                blizzards.size.toString().first()
            }
        }
        if (playerPos != null) {
            valley[playerPos.y][playerPos.x] = 'E'
        }
        return valley.joinToString("\n") { it.joinToString("") }
    }

    val occupied = blizzards.map { it.position }.toSet()

    val entrance = Pos(y = 0, x = 1)
    val exit = Pos(y = height - 1, x = width - 2)
}

private val neighbours = Direction.values()

private fun getNextState(valley: Valley): Valley {
    val result = mutableListOf<Blizzard>()
    for (blizzard in valley.blizzards) {
        var newPosition = blizzard.position + blizzard.direction.delta
        if (newPosition.x == 0) {
            newPosition = newPosition.copy(x = valley.width - 2)
        } else if (newPosition.x == valley.width - 1) {
            newPosition = newPosition.copy(x = 1)
        }
        if (newPosition.y == 0) {
            newPosition = newPosition.copy(y = valley.height - 2)
        } else if (newPosition.y == valley.height - 1) {
            newPosition = newPosition.copy(y = 1)
        }
        result.add(blizzard.copy(position = newPosition))
    }
    return valley.copy(blizzards = result)
}

private typealias DijkstraNode = Triple<Int, Pos, Int>

fun main() {
    fun List<String>.parse(): Valley {
        val blizzards = mutableListOf<Blizzard>()
        for ((y, line) in this.withIndex()) {
            for ((x, c) in line.withIndex()) {
                if (c in listOf('<', '>', 'v', '^')) {
                    val direction = when (c) {
                        '<' -> Direction.LEFT
                        '>' -> Direction.RIGHT
                        'v' -> Direction.DOWN
                        '^' -> Direction.UP
                        else -> error("Unknown character")
                    }
                    blizzards.add(
                        Blizzard(
                            id = blizzards.size,
                            direction = direction,
                            position = Pos(y, x)
                        )
                    )
                }
            }
        }
        return Valley(blizzards, height = this.size, width = this.first().length)
    }

    fun simulate(valley: Valley, entrance: Pos, exit: Pos, startTime: Int, debug: Boolean = false): Int {
        val queue = PriorityQueue<DijkstraNode> { o1, o2 -> // dist, pos, time
            o1.third - o2.third
        }
        val timeDimension = mutableListOf(valley)

        fun isOccupied(pos: Pos, time: Int): Boolean {
            while (timeDimension.getOrNull(time) == null) {
                timeDimension.add(getNextState(timeDimension.last()))
            }
            val valleyAtTime = timeDimension[time]
            if (pos == entrance) { // entrance
                return false
            }
            if (pos == exit) { // exit
                return false
            }
            if (pos.x <= 0 || pos.x >= valleyAtTime.width - 1 || pos.y <= 0 || pos.y >= valleyAtTime.height - 1) { // frame
                return true
            }
            return pos in valleyAtTime.occupied
        }

        val startNode = Triple(0, entrance, startTime)
        queue.add(startNode)
        val prev = mutableMapOf<DijkstraNode, DijkstraNode?>()
        val visited = mutableSetOf<Pair<Int, Pos>>()
        var last: DijkstraNode? = null
        while (!queue.isEmpty()) {
            val node = queue.poll()
            val (dist, pos, time) = node
            if (Pair(time, pos) in visited) {
                continue
            }
            visited.add(Pair(time, pos))
            if (pos == exit) {
                last = node
                break
            }
            for (neighbour in neighbours) {
                if (!isOccupied(pos + neighbour.delta, time + 1)) {
                    val next = Triple(dist + 1, pos + neighbour.delta, time + 1)
                    queue.add(next)
                    prev[next] = node
                }
            }
            if (!isOccupied(pos, time + 1)) {
                val next = Triple(dist, pos, time + 1)
                queue.add(next)
                prev[next] = node
            }
        }
        var pathNode: DijkstraNode = last ?: error("Cant walk to exit")
        if (debug) {

            val path = mutableListOf<DijkstraNode>()
            while (true) {
                path.add(pathNode)
                pathNode = prev[pathNode] ?: break
            }
            for (node in path.reversed()) {
                println("Minute ${node.third + 1}")
                println(timeDimension[node.third].toString(node.second))
                println()
            }
        }
        return last.third
    }

    fun part1(input: List<String>, debug: Boolean = false): Int {
        val parsed = input.parse()
        return simulate(parsed, parsed.entrance, parsed.exit, 0, debug = debug)
    }

    fun part2(input: List<String>): Int {
        val parsed = input.parse()
        val time1 = simulate(parsed, parsed.entrance, parsed.exit, 0)
        val time2 = simulate(parsed, parsed.exit, parsed.entrance, time1)
        val time3 = simulate(parsed, parsed.entrance, parsed.exit, time2)
        if (DEBUG) println("Intervals: $time1, ${time2 - time1}, ${time3 - time2}")
        return time3
    }

    val testInput = readInput("Day24_test")

    val input = readInput("Day24")
    assert(part1(testInput, debug = DEBUG), 18)
    println(part1(input))
    assert(part2(testInput), 54)
    println(part2(input))
}
// Time: 02:30