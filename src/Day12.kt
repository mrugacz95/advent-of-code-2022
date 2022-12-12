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

private data class Graph(val vertices: List<List<Vertex>>, val start: Vertex, val end: Vertex)

private fun Graph.print() {
    for (line in vertices) {
        for (v in line) {
            if (v.prev == null) {
                print('.')
            } else {
                val c = when {
                    v.x - 1 == v.prev?.x -> '<'
                    v.x + 1 == v.prev?.x -> '>'
                    v.y - 1 == v.prev?.y -> '^'
                    v.y + 1 == v.prev?.y -> 'V'
                    else -> throw Exception("Unkonwm")
                }
                print(c)
            }
        }
        println()
    }
    println()
}

private fun Graph.flip(): Graph {
    val newVertices = vertices.map { line -> line.map { it.copy() } }
    for (line in vertices) {
        for (v in line) {
            for (n in v.neighbours) {
                newVertices[n.y][n.x].neighbours.add(newVertices[v.y][v.x])
            }
        }
    }
    return this.copy(vertices = newVertices)
}

private fun Graph.dijkstra() {
    val queue = PriorityQueue<Vertex>()
    val visited = HashSet<Vertex>()
    queue.add(this.start)
    this.start.steps = 0
    while (queue.isNotEmpty()) {
        val current = queue.poll()
        if (current in visited) continue
        visited.add(current)
        for (neighbour in current.neighbours) {
            if (neighbour in visited) continue
            neighbour.steps = current.steps + 1
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
        return Graph(vertices, start, end)
    }

    fun part1(input: List<String>): Int {
        val graph = input.parse()
        val queue = PriorityQueue<Vertex>()
        val visited = HashSet<Vertex>()
        queue.add(graph.start)
        graph.start.steps = 0
        while (queue.isNotEmpty()) {
            val current = queue.poll()
            if (current in visited) continue
            visited.add(current)
            for (neighbour in current.neighbours) {
                if (neighbour in visited) continue
                neighbour.steps = current.steps + 1
                neighbour.prev = current
                queue.add(neighbour)
            }
        }
//        graph.print()
        return graph.end.steps
    }

    fun part2(input: List<String>): Int {
        val graph = input.parse()
        var result = Int.MAX_VALUE
        for (y in graph.vertices.indices) {
            for (x in graph.vertices[y].indices) {
                if (graph.vertices[y][x].height == 'a') {
                    var currentGraph = input.parse()
                    currentGraph = currentGraph.copy(start = currentGraph.vertices[y][x])
                    currentGraph.dijkstra()
                    val steps = currentGraph.end.steps
                    result = min(steps, result)
                }
            }
        }
        return result
    }

    val testInput = readInput("Day12_test")

    val input = readInput("Day12")
    assert(part1(testInput), 31)
    println(part1(input))
    assert(part2(testInput), 29)
    println(part2(input))
}
// Time: 01:35