import kotlin.math.max
import kotlin.math.min
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

private const val START = "AA"
private const val DEBUG = true

private data class Valve(val id: Int, val name: String, val rate: Int, val neighbours: List<String>, var open: Boolean = false) {
    fun canOpen(): Boolean = name != START && !open && rate > 0

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return this.name == (other as? Valve)?.name
    }
}

private fun floydWarshall(graph: Map<String, Valve>): List<MutableList<Int>> {
    val dist = List(graph.size) { MutableList(graph.size) { Int.MAX_VALUE } }
    for (v in graph.values) {
        for (n in v.neighbours) {
            dist[v.id][graph[n]!!.id] = 1
        }
        dist[v.id][v.id] = 0
    }
    for (v1 in graph.values) {
        for (v2 in graph.values) {
            for (v3 in graph.values) {
                if (dist[v2.id][v1.id] != Int.MAX_VALUE && dist[v1.id][v3.id] != Int.MAX_VALUE) {
                    dist[v2.id][v3.id] = min(dist[v2.id][v3.id], dist[v2.id][v1.id] + dist[v1.id][v3.id])
                }
            }
        }
    }
    return dist
}

private fun solveReleasingPressure(graph: Map<String, Valve>, allowedValves: List<Valve>, dist: List<MutableList<Int>>, maxTime: Int): Int {
    val visited = BooleanArray(graph.size)
    fun dfs(valve: Valve, time: Int, released: Int): Int {
        if (visited[valve.id]) {
            return released
        }
        visited[valve.id] = true
        val releasedPressure = released + valve.rate * (maxTime - time)
        var result: Int = releasedPressure // released when waiting
        for (next in allowedValves) {
            val d = dist[valve.id][next.id]
            val t = time + 1 + d
            if (t > maxTime) {
                continue
            }
            result = max(result, dfs(next, t, releasedPressure))
        }
        visited[valve.id] = false
        return result
    }
    return dfs(graph[START] ?: error("Vertex not found"), 0, 0)
}

private fun <T> Set<T>.allSplits() = sequence<Pair<Set<T>, Set<T>>> {
    if (size > 32) error("set is too big")
    val maxBitMask = ((1 shl size + 1) - 1) / 2 + 1
    var mask = 0
    val items = toList()
    while (mask < maxBitMask) {
        var bits = mask
        val set1 = mutableSetOf<T>()
        val set2 = mutableSetOf<T>()
        for (item in items) {
            if (bits % 2 == 0) {
                set1 += item
            } else {
                set2 += item
            }
            bits = bits shr 1
        }
        mask += 1
        if (DEBUG) print("\r$mask/$maxBitMask")
        yield(set1 to set2)
    }
    println()
}

@OptIn(ExperimentalTime::class)
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
        val dist = floydWarshall(graph)
        val withRate = graph.filter { it.value.canOpen() }.map { it.value }
        val maxTime = 30
        return solveReleasingPressure(graph, withRate, dist, maxTime)
    }

    fun part2(input: List<String>): Int {
        val graph = input.parse()
        val dist = floydWarshall(graph)
        val allSplits = graph.filter { it.value.canOpen() }.map { it.value }.toSet().allSplits()
        var result = -Int.MAX_VALUE
        val maxTime = 26
        for ((set1, set2) in allSplits){
            val human = solveReleasingPressure(graph, set1.toList(), dist, maxTime)
            val elephant = solveReleasingPressure(graph, set2.toList(), dist, maxTime)
            result = max(human + elephant, result)
        }
        return result
    }

    val testInput = readInput("Day16_test")
    val time  = measureTime {
        val input = readInput("Day16")
        assert(part1(testInput), 1651)
        println(part1(input))
        assert(part2(testInput), 1707)
        println(part2(input))
    }
    println("Exec time: $time")
}
// Time: 08:00