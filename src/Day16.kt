import kotlin.math.max
import kotlin.math.min

private const val START = "AA"
private const val DEBUG = false

private data class Valve(val id: Int, val name: String, val rate: Int, val neighbours: List<String>, var open: Boolean = false) {
    fun canOpen(): Boolean = name != START && !open && rate > 0

    val neighboursVisited = BooleanArray(neighbours.size)
    fun allVisited() = neighboursVisited.all { true }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return this.name == (other as? Valve)?.name
    }
}

private fun Collection<Valve>.indexOf(key: String): Int {
    return first { it.name == key }.id
}

private fun floydWarshall(graph: Map<String, Valve>): List<MutableList<Int>> {

    val dist = List(graph.size) { MutableList(graph.size) { 100000 } }
    for (v in graph.values) {
        for (n in v.neighbours) {
            dist[v.id][graph[n]!!.id] = 1
        }
        dist[v.id][v.id] = 0
    }
    for (v1 in graph.values) {
        for (v2 in graph.values) {
            for (v3 in graph.values) {
//                        if (dist[v1.id][v3.id] != Int.MAX_VALUE && dist[v3.id][v2.id] != Int.MAX_VALUE) {
                dist[v2.id][v3.id] = min(dist[v2.id][v3.id], dist[v2.id][v1.id] + dist[v1.id][v3.id])
//                            dist[v2.id][v3.id] = min(dist[v1.id][v2.id], dist[v1.id][v3.id] + dist[v3.id][v2.id])
//                        }
            }
        }
    }
    return dist
}

fun main() {
    fun List<String>.parse(): Map<String, Valve> {
        val regex =
            "Valve (?<name>[A-Z]{2}) has flow rate=(?<rate>\\d+); tunnels? leads? to valves? (?<n>([A-Z]{2}(, )?)+)".toRegex()
        return mapIndexed { idx, it ->
            val groups = regex.matchEntire(it)?.groups ?: error("doesnt match '$it'")
            Valve(
                id = idx,
                name = groups["name"]?.value ?: error("no name"),
                rate = groups["rate"]?.value?.toInt() ?: error("no rate"),
                neighbours = groups["n"]?.value?.split(", ") ?: error("no neighbours"),
            )
        }.associateBy({ it.name }, { it })
    }

    fun part1(input: List<String>): Int {
        val graph = input.parse()
        val maxTime = 30
        val visited = mutableListOf<String>()
        fun dfs(valve: Valve, minute: Int, released: Int, prev: String?): Int {
            if (minute >= maxTime) {
                if (DEBUG) println("Time out: order: $visited released: $released")
                return released
            }
            if (valve.neighbours.all { it in visited }) {
                return released
            }
            var maxReleased = -Int.MAX_VALUE
            if (valve.canOpen()) {
                valve.open = true
                if (DEBUG) println("You open valve ${valve.name}")
                maxReleased = dfs(valve, minute + 1, released, prev)
                valve.open = false
            }
            if (DEBUG) println("Visit ${valve.name}")
            visited.add(valve.name)
            val releasedPressure = visited.toSet()
                .map { graph[it] ?: error("Vertex not found") }
                .filter { it.open }
                .sumOf { it.rate }
            if (DEBUG) println("Min $minute, released pressure: $releasedPressure")
            for (neighbour in valve.neighbours) {
//                if (neighbour == prev && valve.neighbours.size != 1) continue
                if (DEBUG) println("move to $neighbour min $minute")
                val nValve = graph[neighbour] ?: error("Unknown neighbour")
                val result = dfs(nValve, minute + 1, released + releasedPressure, valve.name)
                maxReleased = max(maxReleased, result)
            }
            visited.remove(valve.name)
            return maxReleased
        }

        return dfs(graph[START] ?: error("Cant start"), 1, 0, null)
    }

    fun part1SecondAttempt(input: List<String>): Int {
        val graph = input.parse()
        val keys = graph.keys

        val dist = floydWarshall(graph)
        val withRate = graph.filter { it.value.canOpen() }.map { it.value }
        val visited = BooleanArray(graph.size)
        val cache: MutableMap<String, Long> = mutableMapOf()
        fun dfs(valve: Valve, time: Int, released: Int): Int {
            if (visited[valve.id]) {
                return released
            }
            visited[valve.id] = true
            val releasedPressure = released + valve.rate * (30 - time)
            var result: Int = releasedPressure // (31 - time) * releasedPressure // released when waiting
            for (next in withRate) {
                val d = dist[valve.id][next.id]
                val t = time + 1 + d
                if (t > 30) {
                    continue
                }
                result = max(result, dfs(next, t, releasedPressure))
            }
            visited[valve.id] = false
            return result
        }
        return dfs(graph[START] ?: error("Vertex not found"), 0, 0)
    }

    fun part1ThirdAttempt(input: List<String>): Int? {
        val graph = input.parse().values
        fun floydWarshall(): List<MutableList<Int>> {

            val dist = List(graph.size) { MutableList(graph.size) { Int.MAX_VALUE } }
            for (v in graph) {
                for (n in v.neighbours) {
                    dist[v.id][graph.indexOfFirst { n == it.name }] = 1
                }
            }
            for (v1 in graph.indices)
                for (v2 in graph.indices)
                    for (v3 in graph.indices) {
                        if (dist[v1][v3] != Int.MAX_VALUE && dist[v3][v2] != Int.MAX_VALUE)
                            dist[v1][v2] = min(dist[v1][v2], dist[v1][v3] + dist[v3][v2])
                    }
            return dist
        }

        val dist = floydWarshall()
        val withRate = graph.filter { it.canOpen() }
        fun getDist(v1: String, v2: String): Int {
            return dist[graph.indexOf(v1)][graph.indexOf(v2)]
        }

        val releasedPressure = MutableList(31) {
            MutableList(graph.size) { MutableList(2) { -Int.MAX_VALUE } }
        }
        releasedPressure[0][0][0] = 0
        //        for (v in graph) {
//            val d = getDist(START, v.name) + 1
//            releasedPressure[d][graph.indexOf(v)] = v.rate
//        }
        fun printTable() {
            print("        ")
            for (v in graph) {
                print(v.name.padStart(16, ' '))
            }
            println()
            for ((lidx, line) in releasedPressure.withIndex()) {
                print("min ${lidx + 2}".padStart(8, ' '))
                for (onOff in line) {
                    print(onOff.joinToString("/") { num ->
                        val n = if (num < -Int.MAX_VALUE / 2) {
                            "-inf"
                        } else {
                            num.toString()
                        }
                        "($n)"
                    }
                        .padStart(16, ' '))
                }
                println()
            }
        }
        printTable()
        val OFF = 0
        val ON = 1
        for (minute in 1..30) {
            for (valve in graph) {
                for (n in valve.neighbours) {
                    val nid = graph.indexOf(n)
                    val possible = mutableListOf<Int>()
                    possible.add(releasedPressure[minute - 1][nid][OFF]) // came from neighbour
                    possible.add(releasedPressure[minute - 1][nid][ON]) // came from neighbour

                    releasedPressure[minute][valve.id][OFF] = max(
                        releasedPressure[minute][valve.id][OFF],
                        possible.max()
                    )

                    releasedPressure[minute][valve.id][ON] = max(
                        releasedPressure[minute][valve.id][ON],
                        possible.max()
                    )
                }

                var possible = mutableListOf<Int>()
                possible.add(releasedPressure[minute - 1][valve.id][OFF] + (30 - minute) * valve.rate) // turn on
                possible.add(releasedPressure[minute][valve.id][ON]) // stayed in node
                releasedPressure[minute][valve.id][ON] = possible.max()

                possible = mutableListOf()
                possible.add(releasedPressure[minute - 1][valve.id][OFF]) // stayed in node
                possible.add(releasedPressure[minute][valve.id][OFF])// stayed in node
                releasedPressure[minute][valve.id][OFF] = possible.max()
            }
            println("--== step: $minute ==--")
            printTable()
        }
        return releasedPressure.last().maxOfOrNull { it.max() }
    }

    fun part2(input: List<String>): Int {
        val graph = input.parse()
        val dist = floydWarshall(graph)
        return graph.size
    }

    val testInput = readInput("Day16_test")

    val input = readInput("Day16")
    assert(part1SecondAttempt(testInput), 1651)
    println(part1SecondAttempt(input))
    assert(part2(testInput), 1707)
    println(part2(input))
}
// Time: 00:XX
// 496068722226