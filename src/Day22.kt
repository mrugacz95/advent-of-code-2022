import Direction.*
import java.util.Deque
import java.util.LinkedList

private const val DEBUG = false

/*
    Model describes all edges in cube:
         +---+
         | 0 |
     +---+---+---+---+
     | 1 | 2 | 3 | 4 |
     +---+---+---+---+
         | 5 |
         +---+
    List contains definition for every cube face. Each definition
    contains 4 edges up, down, left, right. Edge is defined with 3
    constants: connected face id, direction on connected face after
    crossing edge, information if edge is connected in reverse order.

    For example when going from face 0 in direction up we obtain edge
    with cubeMap[0][Direction.values().indexOf(Direction.UP)] which is
    Triple(4, DOWN, true),. That means that face 0 and 3 are connected in
    such way:

    +---+---+
    | 0 | m||
    +---+---+
    (3 is rotated left)

    or as connected from 3 perspective:

    +---+
    ||0 |
    +---+
    | 3 |
    +---+
    (0 is rotated right)
 */
private val cubeModel: List<Map<Direction, Triple<Int, Direction, Boolean>?>> = listOf(
    mapOf(
        UP to Triple(4, DOWN, true),
        DOWN to Triple(2, DOWN, false),
        LEFT to Triple(1, DOWN, false),
        RIGHT to Triple(3, DOWN, true),
    ), // 0
    mapOf(
        UP to Triple(0, RIGHT, false),
        DOWN to Triple(5, RIGHT, true),
        LEFT to Triple(4, LEFT, false),
        RIGHT to Triple(2, RIGHT, false),
    ), // 1
    mapOf(
        UP to Triple(0, UP, false),
        DOWN to Triple(5, DOWN, false),
        LEFT to Triple(1, LEFT, false),
        RIGHT to Triple(3, RIGHT, false),
    ), // 2
    mapOf(
        UP to Triple(0, LEFT, true),
        DOWN to Triple(5, LEFT, false),
        LEFT to Triple(2, LEFT, false),
        RIGHT to Triple(4, RIGHT, false),
    ), // 3
    mapOf(
        UP to Triple(0, DOWN, true),
        DOWN to Triple(5, UP, true),
        LEFT to Triple(3, LEFT, false),
        RIGHT to Triple(1, RIGHT, false),
    ), // 4
    mapOf(
        UP to Triple(2, UP, false),
        DOWN to Triple(4, UP, true),
        LEFT to Triple(1, UP, true),
        RIGHT to Triple(3, UP, false),
    ) // 5
)

private data class Tile(
    val y: Int, val x: Int,
    val wall: Boolean,
    val neighbours: MutableMap<Direction, Tile> = mutableMapOf(),
    val neighboursOrientation: MutableMap<Direction, Direction> = mutableMapOf(),
    var endRotation: Int = 0, // some tiles are rotated during bending, and has to be rotated again to get final direction
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

private fun List<List<Tile>>.rotateRight(): List<List<Tile>> =
    MutableList(this.size) { y ->
        MutableList(this.size) { x ->
            this[this.size - x - 1][y]
        }
    }

private fun <T> List<List<T>>.hasPos(pos: Pos): Boolean {
    return pos.y in indices && pos.x in this[pos.y].indices
}

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

    fun walk(start: Tile, moves: List<Pair<Char, Int>>, tiles: MutableMap<Pos, Tile>, debug: Boolean = false): Int {
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
        if (DEBUG) println("final pos $position with direction $direction")
        return (position.y + 1) * 1000 + (position.x + 1) * 4 + direction.left(position.endRotation).value()
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
        val cubeMap = mutableMapOf<Pos, Int>()
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
                    val pos = Pos(y / edgeLength, x / edgeLength)
                    cubeMap[pos] = squareId
                }
            }
        }
        if (DEBUG) {
            for ((squareId, square) in squares) {
                println("Square: $squareId")
                println(square.joinToString("\n") { line ->
                    line.joinToString("") {
                        if (it.wall)
                            "#"
                        else
                            "."
                    }
                })
            }
        }
        // map input cube to cube model with horizontal-cross shape
        val startPos = cubeMap.keys.filter { it.y == 0 }.minByOrNull { it.x } ?: error("Cant find zero pos in cube")
        val horizontalCrossSquares: MutableMap<Int, List<List<Tile>>> = mutableMapOf()
        horizontalCrossSquares[0] = squares[0] ?: error("Start square not present") // Start is well oriented
        // Queue contains: position in input cubeMap, expected square id in horizontal-cross model, previous square times of rotation
        val queue: Deque<Triple<Pos, Int, Int>> = LinkedList()
        queue.add(Triple(startPos, 0, 0))
        val visited = mutableSetOf<Int>()
        while (queue.isNotEmpty()) {
            val (current, expectedSquareId, prevRotated) = queue.pop()
            val inputSquareId = cubeMap[current] ?: error("Cube face not found")
            visited.add(inputSquareId)
            for (moveDirection in Direction.values()) { // go in every direction
                val neighbourPos = current + moveDirection.delta
                if (neighbourPos !in cubeMap) {
                    continue
                }
                val neighbourSquareId = cubeMap[neighbourPos] ?: continue
                if (neighbourSquareId in visited) {
                    continue
                }
                var squareDirection = moveDirection
                var horizontalCrossSquare = squares[neighbourSquareId] ?: error("Square Not found")

                val directionWithPrevRot = moveDirection.right(prevRotated)

                // we also look at horizontal-cross cube model and see which square is expected to occur
                val expected = cubeModel[expectedSquareId][directionWithPrevRot] ?: error("connection not found in cube model")

                if (expected.first in horizontalCrossSquares) { // we should visit every expected square once
                    error("square ${expected.first} already writen")
                }

                if (DEBUG) {
                    println(
                        "Going from $expectedSquareId ($inputSquareId) $directionWithPrevRot ($prevRotated) " +
                                "and expecting ${expected.first} but found $neighbourSquareId"
                    )
                }
                var rotatedTimes = 0
                while (expected.second != squareDirection) { // rotate input square to match h-cross model
                    squareDirection = squareDirection.right()
                    horizontalCrossSquare = horizontalCrossSquare.rotateRight()
                    rotatedTimes += 1
                }

                // save squares rotation to adjust final direction
                horizontalCrossSquare.flatten().map { it.endRotation = rotatedTimes }

                if (DEBUG) println("$neighbourSquareId is ${expected.first} rotated $rotatedTimes times")
                horizontalCrossSquares[expected.first] = horizontalCrossSquare // save mapped square
                queue.add(Triple(neighbourPos, expected.first, rotatedTimes))
            }
        }
        if (testData && DEBUG) {
            for (y in 0 until 4) {
                for (x in 0 until 4)
                    print(" ")
                for (x in 0 until 4)
                    print(if (horizontalCrossSquares[0]!![y][x].wall) "#" else ".")
                println()
            }
            println()
            for (y in 0 until 4) {
                for (tileId in 1..4) {
                    for (x in 0 until 4)
                        print(if (horizontalCrossSquares[tileId]!![y][x].wall) "#" else ".")
                    print(" ")
                }
                println()
            }
            println()
            for (y in 0 until 4) {
                for (x in 0 until 4)
                    print(" ")
                for (x in 0 until 4)
                    print(if (horizontalCrossSquares[5]!![y][x].wall) "#" else ".")
                println()
            }
        }

        // connect adjacent in square
        for (square in horizontalCrossSquares.values) {
            for (y in square.indices) {
                for (x in square[y].indices) {
                    val pos = Pos(y, x)
                    val tile = square[y][x]
                    for (dir in Direction.values()) {
                        val neighbourPos = pos + dir.delta
                        if (!square.hasPos(neighbourPos)) {
                            continue
                        }
                        square[neighbourPos.y][neighbourPos.x].let { neighbour ->
                            tile.neighbours[dir] = neighbour
                            tile.neighboursOrientation[dir] = dir
                        }
                    }
                }
            }
        }

        fun getEdges(squareId: Int, edge: Direction): List<Tile> {
            val face = horizontalCrossSquares[squareId] ?: error("Face not present")
            return when (edge) {
                UP -> face.first()
                DOWN -> face.last()
                LEFT -> face.map { it.first() }
                RIGHT -> face.map { it.last() }
            }
        }

        // connect edges according to h-cross cube model
        for ((fromNode, listOfEdges) in cubeModel.withIndex()) {
            for ((outDir, edge) in listOfEdges) {
                if (edge == null) {
                    continue
                }
                val (inNode, inDir, reverse) = edge
                val toBeConnectedFrom = getEdges(fromNode, outDir)
                val toBeConnectedTo : List<Tile> = getEdges(inNode, inDir.opposite()).toMutableList().also {
                    if (reverse) {
                        it.reverse()
                    }
                }
                for ((outTile, inTile) in toBeConnectedFrom.zip(toBeConnectedTo)) {
                    if (DEBUG) {
                        println("connecting $outTile $outDir -> $inTile $inDir")
                    }
                    if (outTile.neighbours[outDir] != null) {
                        error("tiles already connected")
                    }
                    outTile.neighbours[outDir] = inTile
                    outTile.neighboursOrientation[outDir] = inDir
                }
            }
        }

        // check if connection was correct
        var err = false
        for (tile in tiles.values) {
            for (dir in Direction.values()) {
                if (tile.neighbours[dir] == null) {
                    err = true
                    println("Unconnected tile $tile with dir $dir")
                }
            }
        }
        if (err) error("Unconnected tiles found")

        val parsedMoves = parseMoves(moves)
        return Triple(tiles, parsedMoves, start)
    }

    fun part2(input: List<String>, testData: Boolean = false, debug: Boolean = false): Int {
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