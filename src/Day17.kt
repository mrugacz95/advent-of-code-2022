private sealed class Block(val shape: List<String>) {
    val height = shape.size
}

private object HorizontlBlock : Block(listOf("####"))
private object PlusBlock : Block(
    listOf(
        ".#.",
        "###",
        ".#."
    )
)

private object LBlock : Block(
    listOf(
        "..#",
        "..#",
        "###"
    )
)

private object VerticalBlock : Block(listOf("#", "#", "#", "#"))
private object SquareBlock : Block(
    listOf(
        "##",
        "##"
    )
)

private val blocks = listOf(HorizontlBlock, PlusBlock, LBlock, VerticalBlock, SquareBlock)

enum class CollideType { FLOOR, WALL, CORNER, TOWER, NONE }

private data class Position(val y: Int, val x: Int)

private fun List<List<Char>>.print(block: Block, pos: Position) {
    val board = toMutableList().map { it.toMutableList() }

    for ((y, line) in block.shape.withIndex()) {
        for ((x, c) in line.withIndex()) {
            board[y + pos.y][x + pos.x] = c
        }
    }

    println(board.joinToString("\n") { it.joinToString("") })
    println()
}

private const val WIDTH = 7 + 2
private const val HEIGHT = 4 + 1
fun main() {
    fun List<String>.parse(): List<Char> {
        return first().toList()
    }

    fun createWellLine(width: Int = WIDTH): MutableList<Char> {
        return MutableList(width) { x ->
            if (x == 0 || x == width - 1) '|' else '.'
        }
    }

    fun createWell(): MutableList<MutableList<Char>> {
        val well = MutableList(HEIGHT) { y ->
            if (y == HEIGHT - 1) {
                MutableList(WIDTH) { x ->
                    if (x == 0 || x == WIDTH - 1) '+' else '-'
                }
            } else {
                createWellLine()
            }
        }
        return well
    }

    fun collide(pos: Position, block: Block, well: MutableList<MutableList<Char>>): CollideType {
        for ((y, line) in block.shape.withIndex()) {
            for ((x, c) in line.withIndex()) {
                if (c == '.') continue
                if (y + pos.y >= well.size) error("increase margin")
                when (well[y + pos.y][x + pos.x]) {
                    '.' -> continue
                    '-' -> return CollideType.FLOOR
                    '+' -> return CollideType.CORNER
                    '|' -> return CollideType.WALL
                    '#' -> return CollideType.TOWER
                }
            }
        }
        return CollideType.NONE
    }

    fun moveToDelta(c: Char): Int {
        return when (c) {
            '>' -> 1
            '<' -> -1
            else -> error("Incorrect move")
        }
    }

    fun calcTowerHeight(well: MutableList<MutableList<Char>>): Int {
        return well.size - well.indexOfFirst { line -> line.any { it == '#' } } - 1
    }

    fun simulate(input: List<String>, numberOfRocks: Long): Int {
        val well = createWell()
        val moves = input.parse()
        var moveId = 0
        var blockId = 0
        while (true) {
            var pos = Position(0, 3) // spawn
            val block = blocks[blockId % blocks.size]
            while (true) {
                // resolve horizontal movement
                val nextMove = moves[moveId % moves.size]
                moveId += 1
                val possibleX = pos.x + moveToDelta(nextMove)
                val movementOccurs = collide(Position(pos.y, possibleX), block, well) == CollideType.NONE
                val nextX = if (movementOccurs) possibleX else pos.x
                // resolve vertical movement
                val possibleY = pos.y + 1
                val fallOccurs = collide(Position(possibleY, nextX), block, well) == CollideType.NONE
                val nextY = if (fallOccurs) possibleY else pos.y
                pos = Position(nextY, nextX)
                if (!fallOccurs) {
                    break
                }
            }
            // place block
            for ((y, line) in block.shape.withIndex()) {
                for ((x, c) in line.withIndex()) {
                    if (c == '.') {
                        continue
                    } else {
                        well[y + pos.y][x + pos.x] = '#'
                    }
                }
            }
//                well.print(block, pos)
            val nextBlock = blocks[(blockId + 1) % blocks.size]
            val towerHeight = calcTowerHeight(well)
            while (towerHeight + 3 + 1 + nextBlock.height != well.size) {
                if (towerHeight + 3 + 1 + nextBlock.height > well.size)
                    well.add(0, createWellLine())
                else {
                    well.removeAt(0)
                }
            }
            blockId += 1
            if (blockId.toLong() == numberOfRocks) {
                break
            }
        }
        return calcTowerHeight(well)
    }

    fun simulateBetter(input: List<String>, numberOfBlocks: Long): Long {
        val well = createWell()
        val moves = input.parse()
        var moveId = 0
        var blockId = 0L
        var droppedLines = 0L
        val margin = 24000
        var jumpedToFuture = false
        val blockTravels = mutableListOf<Pair<Position, Int>>()
        while (true) {
            var pos = Position(0, 3) // spawn
            val block = blocks[(blockId % blocks.size).toInt()]
            while (true) {
                // resolve horizontal movement
                val nextMove = moves[moveId % moves.size]
                moveId += 1
                val possibleX = pos.x + moveToDelta(nextMove)
                val movementOccurs = collide(Position(pos.y, possibleX), block, well) == CollideType.NONE
                val nextX = if (movementOccurs) possibleX else pos.x
                // resolve vertical movement
                val possibleY = pos.y + 1
                val fallOccurs = collide(Position(possibleY, nextX), block, well) == CollideType.NONE
                val nextY = if (fallOccurs) possibleY else pos.y
                pos = Position(nextY, nextX)
                if (!fallOccurs) {
                    break
                }
            }
            // place block
            for ((y, line) in block.shape.withIndex()) {
                for ((x, c) in line.withIndex()) {
                    if (c == '.') {
                        continue
                    } else {
                        well[y + pos.y][x + pos.x] = '#'
                    }
                }
            }
            blockTravels.add( pos to calcTowerHeight(well))
//                well.print(block, pos)
            val nextBlock = blocks[((blockId + 1) % blocks.size).toInt()]
            val towerHeight = calcTowerHeight(well)
            val expectedLines = towerHeight + 3 + 1 + nextBlock.height
            while (expectedLines != well.size) {
                if (expectedLines > well.size)
                    well.add(0, createWellLine())
                else {
                    well.removeAt(0)
                }
            }
            while (well.size > margin) {
                well.removeLast()
                droppedLines += 1
            }
            blockId += 1
            if (blockId.toLong() == numberOfBlocks) {
                break
            }
            if (droppedLines >= 1 && !jumpedToFuture && blockTravels.size > moves.size) {
                val somehash = blockTravels.slice(blockTravels.size-moves.size until  blockTravels.size).map { it.first }.hashCode()
                for (interval in 1..moves.size) {
                    val otherhash = blockTravels.slice(blockTravels.size-moves.size - interval until blockTravels.size - interval).map { it.first }.hashCode()
                    if (otherhash == somehash) {
                        val firstOccurence = blockTravels[blockTravels.size-moves.size - interval]
                        val lastOccurence =  blockTravels[blockTravels.size-moves.size]
                        val intervalTowerHeight = lastOccurence.second - firstOccurence.second
                        val intervalAddedBlocks = interval
                        val lackingIntervals = (numberOfBlocks - blockId ) / intervalAddedBlocks
                        blockId += lackingIntervals * interval
                        droppedLines += intervalTowerHeight * lackingIntervals
                        jumpedToFuture = true
                        println("jumped to $blockId")
                        break
                    }
                }
            }
        }
        println()
        return calcTowerHeight(well) + droppedLines
    }

    fun part1(input: List<String>): Int {
        return simulate(input, numberOfRocks = 2022)
    }

    fun part2(input: List<String>): Long {
        return simulateBetter(input, numberOfBlocks = 1000000000000)
    }

    val testInput = readInput("Day17_test")

    val input = readInput("Day17")
    assert(part1(testInput), 3068)
    println(part1(input))
    assert(part2(testInput), 1514285714288)
    println(part2(input))
}
// Time: 00:XX