import java.util.Stack

fun main() {
    fun parse(input: List<String>): Pair<MutableList<Stack<Char>>, List<Triple<Int, Int, Int>>> {
        val crates = mutableListOf<Stack<Char>>()
        val cratesInput = input.subList(0, input.indexOf(""))
        val movesInputs = input.subList(input.indexOf("") + 1, input.size )
        for (x in cratesInput.last().indices){
            if (cratesInput.last()[x] != ' '){
                val stack = Stack<Char>()
                for (y in cratesInput.size - 2 downTo 0){
                    if (x > cratesInput[y].length){
                        break
                    }
                    if (cratesInput[y][x] != ' '){
                        stack.add(cratesInput[y][x])
                    }
                }
                crates.add(stack)
            }
        }
        val pattern = "move (?<num>\\d+) from (?<from>\\d+) to (?<to>\\d+)".toRegex()
        val parsedMoves = movesInputs.map{ line ->
            val groups = pattern.matchEntire(line)
            val num = groups?.groups?.get("num")?.value?.toInt() ?: throw Exception("Cant parse")
            val from = groups.groups["from"]?.value?.toInt() ?: throw Exception("Cant parse")
            val to = groups.groups["to"]?.value?.toInt() ?: throw Exception("Cant parse")
            Triple(num, from - 1, to - 1)
        }
        return Pair(crates, parsedMoves)
    }

    fun part1(input: List<String>): String {
        val (crates, moves) = parse(input)
        for ((num, from, to) in moves){
            for (i in 0 until num){
                val top = crates[from].pop()
                crates[to].add(top)
            }
        }
        return crates.joinToString(separator = "") { it.pop().toString() }
    }

    fun part2(input: List<String>): String {
        val (crates, moves) = parse(input)
        for ((num, from, to) in moves){
            val batch = Stack<Char>()
            for (i in 0 until num){
                batch.add(crates[from].pop())
            }
            crates[to].addAll(batch.reversed())
        }
        return crates.joinToString(separator = "") { it.pop().toString() }
    }

    val testInput = readInput("Day05_test")
    assert(part1(testInput), "CMZ")
    assert(part2(testInput), "MCD")

    val input = readInput("Day05")
    println(part1(input))
    println(part2(input))
}
