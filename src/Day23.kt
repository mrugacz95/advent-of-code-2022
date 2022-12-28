private val neighbours = listOf(
    Pos(-1, -1),
    Pos(-1, 0),
    Pos(-1, 1),
    Pos(0, 1),
    Pos(1, 1),
    Pos(1, 0),
    Pos(1, -1),
    Pos(0, -1),
)


private val directionNeighbours = mapOf( // direction to move in neighbour occupied
    Direction.UP to listOf(
        Pos(-1, -1),
        Pos(-1, 0),
        Pos(-1, 1),
    ),
    Direction.DOWN to listOf(
        Pos(1, 1),
        Pos(1, 0),
        Pos(1, -1),

        ),
    Direction.LEFT to listOf(
        Pos(-1, -1),
        Pos(0, -1),
        Pos(1, -1),
    ),
    Direction.RIGHT to listOf(
        Pos(-1, 1),
        Pos(0, 1),
        Pos(1, 1),
    ),
)

private data class Elf(
    val id: Int,
    var proposedMove : Direction? = null,
    var pos : Pos,
    val moves: MutableList<Direction> = Direction.values().toMutableList()) {
    override fun toString(): String {
        return "Elf(id=$id, pos=$pos, proposedMove=$proposedMove, moves=$moves)"
    }
}

private fun List<Elf>.print()  {
    val x = this.map { it.pos.x }
    val y = this.map { it.pos.y }
    val minX = x.min() - 1
    val maxX = x.max() + 1
    val minY = y.min() - 1
    val maxY = y.max() + 1
    val result = List(maxY - minY + 1) {
        MutableList(maxX - minX + 1){
            "."
        }
    }
    map { result[it.pos.y - minY][it.pos.x - minX] = "#" }
    println(result.joinToString("\n") {it.joinToString("") } + "\n")
}
fun main() {
    fun List<String>.parse(): MutableSet<Pos> {
        val elves = mutableSetOf<Pos>()
        mapIndexed { y, line ->
            line.mapIndexed { x, c ->
                if (c == '#') {
                    elves.add(Pos(y, x))
                }
            }
        }
        return elves
    }



    fun simulate(parsedElvesPositions: MutableSet<Pos>, stopCondition : (round: Int, moves: Boolean) -> Boolean): Pair<Int, Int> {
        val elves = mutableListOf<Elf>()
        for(parsed in parsedElvesPositions) { elves.add(Elf(elves.size, pos = parsed)) }
        var round = 1
        while (true) {
//            elves.print()
            val elvesList = elves.toList()
            val elvesPositions = elves.map { it.pos }.toSet()
            val proposedOccupiedPositions = mutableMapOf<Pos, Int>()
            var anyMovement = false
            for (elf in elvesList) {
                var willMove = false
                for (dir in neighbours) {
                    if ((elf.pos + dir) in elvesPositions) {
                        willMove = true
                        break
                    }
                }
                if (willMove) {
                    for (dir in elf.moves) {
                        val neighbours = directionNeighbours[dir] ?: error("unknown direction")
                        if (neighbours.none { (elf.pos + it) in elvesPositions }) {
                            elf.proposedMove = dir
                            proposedOccupiedPositions[elf.pos + dir.delta] = (proposedOccupiedPositions[elf.pos + dir.delta] ?: 0) + 1
                            break
                        }
                    }
                }
            }

            // use proposed moves
            for (elf in elves){
                val move = elf.proposedMove
                if(move != null && proposedOccupiedPositions[elf.pos + move.delta] == 1){
                    elf.pos = elf.pos + move.delta
                    anyMovement = true
                }
                val moveToRoll = elf.moves.first() // find { it == elf.proposedMove } ?: error("Cant find proposed move")
                elf.moves.remove(moveToRoll)
                elf.moves.add(moveToRoll)
            }
            elves.map { it.proposedMove = null }
            round += 1
//            elves.print()
            if (stopCondition(round, anyMovement)){
                break
            }
        }
        val x = elves.map { it.pos.x }
        val y = elves.map { it.pos.y }
        val minX = x.min()
        val maxX = x.max()
        val minY = y.min()
        val maxY = y.max()
        val emptyTiles = ((maxX - minX + 1) * (maxY - minY + 1) - elves.size)
        return round - 1 to emptyTiles
    }

    fun part1(input: List<String>): Int {
        val parsedElvesPositions = input.parse()
        val (_, emptyTiles) = simulate(parsedElvesPositions) { round, _ ->
            round > 10
        }
        return emptyTiles

    }

    fun part2(input: List<String>): Int {
        val parsedElvesPositions = input.parse()
        val (rounds, _) = simulate(parsedElvesPositions) { _, anyMovement ->
            !anyMovement
        }
        return rounds
    }

    val testInput = readInput("Day23_test")

    val input = readInput("Day23")
    assert(part1(testInput), 110)
    println(part1(input))
    assert(part2(testInput), 20)
    println(part2(input))
}
// Time: 2:00
