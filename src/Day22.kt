import Direction.*
import java.util.Deque
import java.util.LinkedList

private const val DEBUG = false
private val cubeModel: List<List<Triple<Int, Direction, Boolean>?>> = listOf(
    listOf(
        Triple(4, DOWN, true),
        Triple(2, DOWN, false),
        Triple(1, DOWN, false),
        Triple(3, DOWN, true),
    ), // 0
    listOf(
        Triple(0, RIGHT, false),
        Triple(5, RIGHT, true),
        Triple(4, LEFT, false),
        Triple(2, RIGHT, false),
    ), // 1
    listOf(
        Triple(0, UP, false),
        Triple(5, DOWN, false),
        Triple(1, LEFT, false),
        Triple(3, RIGHT, false),
    ), // 2
    listOf(
        Triple(0, LEFT, true),
        Triple(5, LEFT, false),
        Triple(2, LEFT, false),
        Triple(4, RIGHT, false),
    ), // 3
    listOf(
        Triple(0, DOWN, true),
        Triple(5, UP, true),
        Triple(3, LEFT, false),
        Triple(1, RIGHT, false),
    ), // 4
    listOf(
        Triple(2, UP, false),
        Triple(4, UP, true),
        Triple(1, UP, true),
        Triple(3, UP, false),
    ) // 5
)


private val testEdgeConnection1: List<List<Triple<Int, Direction, Boolean>?>> = listOf(
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

private fun List<List<Tile>>.rotateRight(): List<List<Tile>> {
    return MutableList(this.size) { y ->
        MutableList(this.size) { x ->
            this[this.size - x - 1][y]
        }
    }
}

private fun List<List<Tile>>.rotateLeft() = rotateRight().rotateRight().rotateRight()

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
        println("final pos $position")
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
        // map input squares to cube model with horizontal-cross shape
        var zeroPos: Pos? = null
        outer@ for (y in cubeMap.indices) {
            for (x in cubeMap[y].indices) {
                if (cubeMap[y][x] == 0) {
                    zeroPos = Pos(y, x)
                    break@outer
                }
            }
        }
        val startPos = zeroPos ?: error("Cant find zero pos in cube")
        val horizontalCrossSquares: MutableMap<Int, List<List<Tile>>> = mutableMapOf()
        horizontalCrossSquares[0] = squares[0] ?: error("Zero square not present") // Zero is well oriented
        val queue: Deque<Triple<Pos, Int, Int>> = LinkedList()
        queue.add(Triple(startPos, 0, 0))
        val visited = mutableSetOf<Int>()
        val inputFacesToCrossFaces = mutableMapOf<Int, Int>()
        inputFacesToCrossFaces[0] = 0
        while (queue.isNotEmpty()) {
            val (current, expectedSquareId, prevRotated) = queue.pop()
            val inputSquareId = cubeMap[current.y][current.x] ?: error("Cube face not found")
            visited.add(inputSquareId)
            for ((directionId, moveDirection) in Direction.values().withIndex()) {
                val neighbourPos = current + moveDirection.delta
                if (!cubeMap.hasPos(neighbourPos)) {
                    continue
                }
                val neighbourSquareId = cubeMap[neighbourPos.y][neighbourPos.x] ?: continue
                if (neighbourSquareId in visited) {
                    continue
                }
                var squareDirection = moveDirection

                var withPrevRot = Direction.values()[directionId]

                var horizontalCrossSquare = squares[neighbourSquareId] ?: error("Square Not found")

                for (i in 0 until  prevRotated) {
                    squareDirection = squareDirection.left()
                    withPrevRot = withPrevRot.right()
                    horizontalCrossSquare = horizontalCrossSquare.rotateLeft()
                }
                val dirIdWithPrevRot = Direction.values().indexOf(withPrevRot)

                val expected = cubeModel[expectedSquareId][dirIdWithPrevRot] ?: error("connection not found in cube model")
                println("Going from $expectedSquareId ($inputSquareId) $withPrevRot ($prevRotated) and expecting ${expected.first} but found $neighbourSquareId")
                var rotatedTimes = prevRotated
                while (expected.second != squareDirection) {
                    squareDirection = squareDirection.right()
                    horizontalCrossSquare = horizontalCrossSquare.rotateRight()
//                        println("rotated $inputSquareId right")
                    rotatedTimes += 1
                }
                if (expected.first in horizontalCrossSquares) {
                    error("square ${expected.first} already writen")
                }
                horizontalCrossSquares[expected.first] = horizontalCrossSquare
                println("$neighbourSquareId is ${expected.first} rotated $rotatedTimes times")
                inputFacesToCrossFaces[expected.first] = neighbourSquareId
                queue.add(Triple(neighbourPos, expected.first, rotatedTimes))
            }
        }
        if (testData) {
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
        if (DEBUG) println("horizontal cross has ${horizontalCrossSquares.size} sides")

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
//            val x = face.flatten().map { it.x }
//            val y = face.flatten().map { it.y }
//            val minX = x.min()
//            val maxX = x.max()
//            val minY = y.min()
//            val maxY = y.max()
//            val left = face.flatten().filter { it.x == minX }
//            val right = face.flatten().filter { it.x == maxX }
//            val top = face.flatten().filter { it.y == minY }
//            val down = face.flatten().filter { it.y == maxY }
            return when (edge) {
                UP -> {
                    face.first()
                }

                DOWN -> {
                    face.last()
                }

                LEFT -> face.map { it.first() }
                RIGHT -> face.map { it.last() }
            }
        }

        val edgeConnection = cubeModel


        for ((fromNode, listOfEdges) in edgeConnection.withIndex()) {
            for ((edge, outDir) in listOfEdges.zip(Direction.values())) {
                if (edge == null) {
                    continue
                }
                val (inNode, inDir, reverse) = edge
                val toBeConnectedFrom = getEdges(fromNode, outDir)
                val toBeConnectedTo = getEdges(inNode, inDir.opposite()).let {
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
                    if (outTile.neighbours[outDir] != null){
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

        if (DEBUG) println(cubeMap.joinToString("\n") { line -> line.joinToString("") { it?.toString() ?: " " } })

        val parsedMoves = parseMoves(moves)
        return Triple(tiles, parsedMoves, start)
    }

    fun part2(input: List<String>, testData: Boolean = false, debug: Boolean = false): Int {
        val (tiles, parsedMoves, start) = input.parseForPart2(testData = testData)
        println("start pos $start")
        return walk(start, parsedMoves, tiles, debug)
    }

    val testInput = readInput("Day22_test")

    val input = readInput("Day22")
//    assert(part1(testInput), 6032)
//    println(part1(input))
    assert(part2(testInput, testData = true, debug = DEBUG), 5031)
    println(part2(input))
}
// Time: 05:43