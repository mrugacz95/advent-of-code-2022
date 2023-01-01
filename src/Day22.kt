import Direction.*

private const val DEBUG = false

private val testEdgeConnection: List<List<Triple<Int, Direction, Boolean>?>> = listOf(
    listOf(
        Triple(1, DOWN, true),
        Triple(3, DOWN, false),
        Triple(2, DOWN, false),
        Triple(5, LEFT, false),
    ), // 0
    listOf(
        Triple(0, DOWN, true),
        Triple(4, UP, false),
        Triple(5, UP, false),
        Triple(2, RIGHT, false)
    ), // 1
    listOf(
        Triple(0, RIGHT, false),
        Triple(4, RIGHT, true),
        Triple(1, LEFT, false),
        Triple(3, RIGHT, false),
    ), // 2
    listOf(
        Triple(0, UP, false),
        Triple(4, DOWN, false),
        Triple(2, LEFT, false),
        Triple(5, DOWN, true),
    ), // 3
    listOf(
        Triple(3, UP, false),
        Triple(1, UP, true),
        Triple(2, UP, true),
        Triple(5, RIGHT, false),
    ), // 4
    listOf(
        Triple(3, LEFT, true),
        Triple(1, RIGHT, false),
        Triple(4, LEFT, false),
        Triple(0, RIGHT, true),
    ) // 5
)

// up, down, left, right
private val problemEdgeConnection: List<List<Triple<Int, Direction, Boolean>?>> = listOf(
    listOf(
        Triple(5, RIGHT, false),
        null,
        Triple(3, RIGHT, true),
        null,
    ), // 0
    listOf(
        Triple(5, UP, false),
        Triple(2, LEFT, false),
        null,
        Triple(4, LEFT, true),
    ), // 1
    listOf(
        null,
        null,
        Triple(3, DOWN, false),
        Triple(1, UP, false),
    ), // 2
    listOf(
        Triple(2, RIGHT, false),
        null,
        Triple(0, RIGHT, true),
        null
    ), // 3
    listOf(
        null,
        Triple(5, LEFT, false),
        null,
        Triple(1, LEFT, true),
    ), // 4
    listOf(
        null,
        Triple(1, DOWN, false),
        Triple(0, DOWN, false),
        Triple(4, UP, false),
    ), // 5
)

private data class Tile(
    val y: Int, val x: Int,
    val wall: Boolean,
    val neighbours: MutableMap<Direction, Tile> = mutableMapOf(),
    val neighboursOrientation: MutableMap<Direction, Direction> = mutableMapOf()
) {
    override fun toString(): String {
        return "Tile(y=$y, x=$x, neighbours=${neighbours.size})"
    }

    val pos = Pos(y, x)
}

private fun Direction.value() = when (this) {
    UP -> 3
    DOWN -> 1
    LEFT -> 2
    RIGHT -> 0
}

private fun parseTiles(map: String): Pair<MutableMap<Pos, Tile>, Tile> {
    val tiles = mutableMapOf<Pos, Tile>()
    var start: Tile? = null
    for ((y, line) in map.split("\n").withIndex()) {
        for ((x, c) in line.withIndex()) {
            if (c != ' ') {
                val tile = Tile(y = y, x = x, wall = c == '#')
                tiles[Pos(y, x)] = tile
                if (start == null) {
                    start = tile
                }
            }
        }
    }
    start ?: error("start not found")
    return tiles to start
}

fun parseMoves(moves: String): List<Pair<Char, Int>> =
    "([RL]|\\d+)".toRegex().findAll(moves).map { it.value }.map {
        if (it.first().isDigit()) {
            'F' to it.toInt()
        } else {
            it.first() to 0
        }
    }.toList()

fun main() {
    fun List<String>.parse(): Triple<MutableMap<Pos, Tile>, List<Pair<Char, Int>>, Tile> {
        val (map, moves) = joinToString("\n").split("\n\n")
        val (tiles, start) = parseTiles(map)
        val parsedMoves = parseMoves(moves)
        return Triple(tiles, parsedMoves, start)
    }

    fun joinTilesByEdge(tiles: MutableMap<Pos, Tile>) {
        val maxX = tiles.values.maxOfOrNull { it.x } ?: error("No max x found")
        val maxY = tiles.values.maxOfOrNull { it.y } ?: error("No max y found")
        val x = (0..maxX).map { x -> tiles.values.filter { it.x == x } }
        val y = (0..maxY).map { y -> tiles.values.filter { it.y == y } }
        val columnsTop = x.map { tile -> tile.minBy { it.y } }
        val columnsBottom = x.map { tile -> tile.maxBy { it.y } }
        val rowStart = y.map { tile -> tile.minBy { it.x } }
        val rowEnd = y.map { tile -> tile.maxBy { it.x } }

        for (tile in tiles.values) {
            for (dir in values()) {
                var neighbourPos = tile.pos + dir.delta
                while (neighbourPos !in tiles) {
                    neighbourPos += dir.delta
                    if (neighbourPos.y > maxY) {
                        neighbourPos = columnsTop[tile.x].pos
                    } else if (neighbourPos.y < 0) {
                        neighbourPos = columnsBottom[tile.x].pos
                    } else if (neighbourPos.x > maxX) {
                        neighbourPos = rowStart[tile.y].pos
                    } else if (neighbourPos.x < 0) {
                        neighbourPos = rowEnd[tile.y].pos
                    }
                }
                val neighbour = tiles[neighbourPos] ?: error("Couldnt find neighbour")
                tile.neighbours[dir] = neighbour
                tile.neighboursOrientation[dir] = dir
            }
        }
    }

    fun printState(tiles: MutableMap<Pos, Tile>, position: Tile, direction: Direction) {
        val (minX, maxX) = tiles.values.map { it.x }.let { x ->
            x.min() to x.max()
        }
        val (minY, maxY) = tiles.values.map { it.x }.let { y ->
            y.min() to y.max()
        }
        for (y in minY..maxY) {
            for (x in minX..maxX) {
                val tile = tiles[Pos(y, x)]
                print(
                    if (position.x == x && position.y == y) {
                        when (direction) {
                            UP -> '^'
                            DOWN -> 'v'
                            LEFT -> '<'
                            RIGHT -> '>'
                        }
                    } else if (tile == null) {
                        ' '
                    } else
                        if (tile.wall) {
                            '#'
                        } else {
                            '.'
                        }
                )
            }
            println()
        }
        println()
    }

    fun walk(start: Tile, moves: List<Pair<Char, Int>>, tiles: MutableMap<Pos, Tile>, debug : Boolean = false): Int {
        var position = start
        var direction = RIGHT
        for ((move, length) in moves) {
            when (move) {
                'F' -> {
                    for (i in 1..length) {
                        if (position.neighbours[direction]?.wall == false) {
                            position.let { oldPosition ->
                                position = oldPosition.neighbours[direction] ?: error("Unconnected tile")
                                direction = oldPosition.neighboursOrientation[direction]
                                    ?: error("Unconnected tile cant find new rotation")
                            }
                        } else {
                            break
                        }
                        if (debug) printState(tiles, position, direction)
                    }
                }

                'R' -> {
                    direction = direction.right()
                    if (debug) printState(tiles, position, direction)
                }

                'L' -> {
                    direction = direction.left()
                    if (debug) printState(tiles, position, direction)
                }
            }
        }
        return (position.y + 1) * 1000 + (position.x + 1) * 4 + direction.value()
    }

    fun part1(input: List<String>): Int {
        val (tiles, moves, start) = input.parse()
        joinTilesByEdge(tiles)
        return walk(start, moves, tiles)
    }

    fun List<String>.parseForPart2(testData: Boolean = false): Triple<MutableMap<Pos, Tile>, List<Pair<Char, Int>>, Tile> {
        val (map, moves) = joinToString("\n").split("\n\n")
        val splitMap = map.split("\n")
        val edgeLength = splitMap.minOfOrNull { line -> line.count { it != ' ' } } ?: error("Edge length not found")
        val cubeMap = List(7) { MutableList<Int?>(7) { null } }
        val squares = mutableMapOf<Int, List<List<Tile>>>()
        val (tiles, start) = parseTiles(map)
        for (y in splitMap.indices step edgeLength) {
            val line = splitMap[y]
            for (x in line.indices step edgeLength) {
                if (splitMap[y][x] != ' ') {
                    val square = splitMap.slice(y until y + edgeLength).map { it.slice(x until x + edgeLength) }
                    val squareId = squares.size
                    val tileSquare = square.mapIndexed { squareY, squareLine ->
                        squareLine.mapIndexed { squareX, _ ->
                            tiles[Pos(squareY + y, squareX + x)] ?: error("Tile not found")
                        }
                    }
                    squares[squareId] = tileSquare
                    val pos = Pos(y / edgeLength + 3, x / edgeLength + 3)
                    cubeMap[pos.y][pos.x] = squareId
                }
            }
        }

        // connect adjacent
        for ((pos, tile) in tiles) {
            for (dir in Direction.values()) {
                tiles[pos + dir.delta]?.let { neighbour ->
                    tile.neighbours[dir] = neighbour
                    tile.neighboursOrientation[dir] = dir
                }
            }
        }

        fun getEdges(squareId: Int, edge: Direction): List<Tile> {
            val face = squares[squareId] ?: error("Face not present")
            val x = face.flatten().map { it.x }
            val y = face.flatten().map { it.y }
            val minX = x.min()
            val maxX = x.max()
            val minY = y.min()
            val maxY = y.max()
            val left = face.flatten().filter { it.x == minX }
            val right = face.flatten().filter { it.x == maxX }
            val top = face.flatten().filter { it.y == minY }
            val down = face.flatten().filter { it.y == maxY }
            return when (edge) {
                UP -> top
                DOWN -> down
                LEFT -> left
                RIGHT -> right
            }
        }

        val edgeConnection = if (testData) {
            testEdgeConnection
        } else {
            problemEdgeConnection
        }

        for ((fromNode, listOfEdges) in edgeConnection.withIndex()) {
            for ((edge, outDir) in listOfEdges.zip(Direction.values())) {
                if (edge == null) {
                    continue
                }
                val (inNode, inDir, reverse) = edge
                val toBeConnectedFrom = getEdges(fromNode, outDir)
                val toBeConnectedTo = getEdges(inNode, inDir.opposite()).let { it ->
                    if (reverse) {
                        it.reversed()
                    } else {
                        it
                    }
                }
                for ((outTile, inTile) in toBeConnectedFrom.zip(toBeConnectedTo)) {
                    if (DEBUG) {
                        println("connecting $outTile $outDir -> $inTile $inDir")
                    }
                    outTile.neighbours[outDir] = inTile
                    outTile.neighboursOrientation[outDir] = inDir
                }
            }
        }

        // check if connection was correct
        if (tiles.values.any { tile -> Direction.values().any { dir -> dir !in tile.neighbours } }) {
            error("Unconnected tile")
        }

        if (DEBUG) println(cubeMap.joinToString("\n") { line -> line.joinToString("") { it?.toString() ?: " " } })

        val parsedMoves = parseMoves(moves)
        return Triple(tiles, parsedMoves, start)
    }

    fun part2(input: List<String>, testData: Boolean = false, debug : Boolean = false): Int {
        val (tiles, parsedMoves, start) = input.parseForPart2(testData = testData)
        return walk(start, parsedMoves, tiles, debug)
    }

    val testInput = readInput("Day22_test")

    val input = readInput("Day22")
    assert(part1(testInput), 6032)
    println(part1(input))
    assert(part2(testInput, testData = true, debug = DEBUG), 5031)
    println(part2(input))
}
// Time: 05:43