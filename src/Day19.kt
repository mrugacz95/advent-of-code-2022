import kotlin.math.max

private data class Blueprint(
    val id: Int,
    val oreNeededOre: Int,
    val clayNeededOre: Int,
    val obsidianNeededOre: Int,
    val obsidianNeededClay: Int,
    val geodeNeededOre: Int,
    val geodeNeededObsidian: Int
) {
    val maxOreNeeded = listOf(oreNeededOre, clayNeededOre, obsidianNeededOre, geodeNeededOre).max()
    fun possibleRobots(robots: Type, materials: Type): List<Type> {
        val result = mutableListOf<Type>()
        if (materials.ore >= geodeNeededOre &&
            materials.obsidian >= geodeNeededObsidian) {
            result.add(Type(geode = 1))
        } else {
            if (materials.ore >= obsidianNeededOre &&
                robots.obsidian <= geodeNeededObsidian &&
                materials.clay >= obsidianNeededClay
            ) {
                result.add(Type(obsidian = 1))
            }
            if (robots.geode == 0 &&
                robots.clay <= obsidianNeededClay &&
                materials.ore >= clayNeededOre
            ) {
                result.add(Type(clay = 1))
            }
            if (robots.obsidian == 0 &&
                robots.ore <= maxOreNeeded &&
                materials.ore >= oreNeededOre
            ) {
                result.add(Type(ore = 1))
            }
        }
        return result
    }

    fun costOfRobot(robot: Type): Type {
        return Type(
            ore = oreNeededOre * robot.ore + clayNeededOre * robot.clay + obsidianNeededOre * robot.obsidian + geodeNeededOre * robot.geode,
            clay = obsidianNeededClay * robot.obsidian,
            obsidian = geodeNeededObsidian * robot.geode,
            geode = 0
        )
    }
}

data class Type(val ore: Int = 0, val clay: Int = 0, val obsidian: Int = 0, val geode: Int = 0) {
    operator fun plus(other: Type): Type {
        return Type(ore + other.ore, clay + other.clay, obsidian + other.obsidian, geode + other.geode)
    }

    operator fun minus(other: Type): Type {
        return Type(ore - other.ore, clay - other.clay, obsidian - other.obsidian, geode - other.geode)
    }
}

fun main() {
    fun List<String>.parse(): List<Blueprint> {
        val pattern = """Blueprint (?<id>\d+): 
            |Each ore robot costs (?<oreNeededOre>\d+) ore. 
            |Each clay robot costs (?<clayNeededOre>\d+) ore. 
            |Each obsidian robot costs (?<obsidianNeededOre>\d+) ore and (?<obsidianNeededClay>\d+) clay. 
            |Each geode robot costs (?<geodeNeededOre>\d+) ore and (?<geodeNeededObsidian>\d+) obsidian.""".trimMargin()
            .replace("\n", "").toRegex()
        return joinToString("\n").replace("\n  ", " ").replace("\n\n", "\n").split("\n").map { line ->
            val groups = pattern.matchEntire(line)?.groups ?: error("Cant parse")
            Blueprint(
                id = groups["id"]?.value?.toInt() ?: error("Cant parse"),
                oreNeededOre = groups["oreNeededOre"]?.value?.toInt() ?: error("Cant parse"),
                clayNeededOre = groups["clayNeededOre"]?.value?.toInt() ?: error("Cant parse"),
                obsidianNeededOre = groups["obsidianNeededOre"]?.value?.toInt() ?: error("Cant parse"),
                obsidianNeededClay = groups["obsidianNeededClay"]?.value?.toInt() ?: error("Cant parse"),
                geodeNeededOre = groups["geodeNeededOre"]?.value?.toInt() ?: error("Cant parse"),
                geodeNeededObsidian = groups["geodeNeededObsidian"]?.value?.toInt() ?: error("Cant parse"),
            )
        }
    }

    fun calcMaxGeodes(blueprint: Blueprint, maxTime: Int): Int {
        fun dfs(robots: Type, materials: Type, time: Int, baned: List<Type>): Int {
            if (time > maxTime) {
                return materials.geode
            }
            var best = -Int.MAX_VALUE
            val possibleRobots = blueprint.possibleRobots(robots, materials).toList()
            for (builtRobot in possibleRobots) {
                if (builtRobot !in baned) {
                    val newRobots = builtRobot + robots
                    val newMaterials = (materials - blueprint.costOfRobot(builtRobot)) + robots
                    val newBest = dfs(newRobots, newMaterials, time + 1, emptyList())
                    best = max(best, newBest)
                }
            }
            best = max(best, dfs(robots, materials + robots, time + 1, possibleRobots)) // wait
            return best
        }
        return dfs(Type(ore = 1), Type(), 1, emptyList())
    }

    fun part1(input: List<String>): Int {
        return input.parse().map{ calcMaxGeodes(it, maxTime = 24) }
            .mapIndexed { i, numberOfGeodes ->
                (i + 1) * numberOfGeodes
            }
            .reduce { acc, i -> acc + i }
    }

    fun part2(input: List<String>): Int {
        return input.parse().take(3).map{ calcMaxGeodes(it, maxTime = 32) }.reduce { acc, it -> acc * it }
    }

    val testInput = readInput("Day19_test")

    val input = readInput("Day19")
    assert(part1(testInput), 33)
    println(part1(input))
    assert(part2(testInput), 62 * 56)
    println(part2(input))
}
// Time: 01:40