import java.util.PriorityQueue
import kotlin.math.min

private data class Vertex(
    var height: Char,
    var steps: Int = Int.MAX_VALUE,
    val x: Int,
    val y: Int,
) : Comparable<Vertex> {
    var prev: Vertex? = null
    val neighbours: MutableList<Vertex> = mutableListOf()
    override fun compareTo(other: Vertex): Int {
        return this.steps - other.steps
    }

    override fun toString(): String {
        return "Vertex(x=$x, y=$y, height=$height, steps=$steps)"
    }
}

private data class Graph(val vertices: List<List<Vertex>>, val start: List<Vertex>, val end: Vertex)

private val COLORS = (231 downTo 226).toList() +
        (190 downTo 46 step 36).toList() +
        (47..51).toList() +
        (87..195 step 36).toList() +
        (194 downTo 191).toList() +
        listOf(155, 119)

private fun Graph.print() {
    val result = Array(vertices.size) {
        Array(vertices[it].size) { "." }
    }
    var vertex = end
    while (true) {
        val prev = vertex.prev ?: break
        val c = when {
            prev.x - 1 == vertex.x -> "<"
            prev.x + 1 == vertex.x -> ">"
            prev.y - 1 == vertex.y -> "^"
            prev.y + 1 == vertex.y -> "V"
            else -> throw Exception("Unknown")
        }
        result[prev.y][prev.x] = "\u001b[38;5;${167}m\u001B[1m$c\u001b[0m"
        vertex = prev
    }

    result[end.y][end.x] = "\u001b[38;5;${167}m" + "E"
    vertices.flatten().map {
        val h = it.height - 'a'

        result[it.y][it.x] = "\u001b[48;5;${COLORS[h]}m" + result[it.y][it.x] + "\u001b[0m"
    }
    println(result.joinToString("\n") { it.joinToString("") })
}

private fun Graph.dijkstra() {
    val queue = PriorityQueue<Vertex>()
    val visited = HashSet<Vertex>()
    queue.addAll(this.start)
    this.start.map { it.steps = 0 }
    while (queue.isNotEmpty()) {
        val current = queue.poll()
        if (current in visited) continue
        visited.add(current)
        for (neighbour in current.neighbours) {
            if (neighbour in visited) continue
            neighbour.steps = min(neighbour.steps, current.steps + 1)
            neighbour.prev = current
            queue.add(neighbour)
        }
    }
}

fun main() {
    fun List<String>.parse(): Graph {
        val neighbours = listOf(Pair(-1, 0), Pair(1, 0), Pair(0, 1), Pair(0, -1))
        val vertices = mapIndexed { y, line -> line.mapIndexed { x, c -> Vertex(c, x = x, y = y) } }
        val start: Vertex = vertices.flatten().firstOrNull { it.height == 'S' } ?: throw Exception("No start node found")
        val end: Vertex = vertices.flatten().firstOrNull { it.height == 'E' } ?: throw Exception("No start node found")

        start.height = 'a'
        end.height = 'z'

        for (y in this.indices) {
            val line = get(y)
            for (x in line.indices) {
                val v = vertices[y][x]
                for ((ny, nx) in neighbours) {
                    if (ny + y >= 0 &&
                        ny + y < this.size &&
                        nx + x >= 0 &&
                        nx + x < line.length &&
                        v.height >= vertices[y + ny][x + nx].height - 1
                    ) {
                        v.neighbours.add(vertices[y + ny][x + nx])
                    }
                }
            }
        }
        return Graph(vertices, listOf(start), end)
    }

    fun part1(input: List<String>): Int {
        val graph = input.parse()
        graph.dijkstra()
        graph.print()
        return graph.end.steps
    }

    fun part2(input: List<String>): Int {
        val graph = input.parse().let {
            val aVertices = it.vertices.flatten().filter { v -> v.height == 'a' }
            it.copy(start = aVertices)
        }
        graph.dijkstra()
        return graph.end.steps
    }

    val testInput = readInput("Day12_test")

    val input = readInput("Day12")
    assert(part1(testInput), 31)
    println(part1(input))
    assert(part2(testInput), 29)
    println(part2(input))
}
// Time: 01:35