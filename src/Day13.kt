import java.util.Stack
import kotlin.math.max

private const val DEBUG = false
private sealed class InnerItem
private data class InnerNumber(val value: Int) : InnerItem() {
    override fun toString(): String {
        return value.toString()
    }
}

private data class InnerList(val list: List<InnerItem>) : InnerItem() {
    override fun toString(): String {
        return list.toString()
    }
}

private data class InnerChar(val c: Char) : InnerItem()

fun main() {
    fun String.parseLine(): InnerList {
        val stack = Stack<InnerItem>()
        var i = 0
        while (i < length) {
            when {
                this[i] == '[' -> stack.push(InnerChar('['))
                this[i] == ']' -> {
                    val list = mutableListOf<InnerItem>()
                    while ((stack.peek() as? InnerChar)?.c != '[') {
                        list.add(stack.pop())
                    }
                    stack.pop()
                    stack.add(InnerList(list.reversed()))
                }
                this[i].isDigit() -> {
                    var j = i
                    while (this[j + 1].isDigit()) {
                        j++
                    }
                    stack.add(InnerNumber(this.slice(i..j).toInt()))
                    i = j
                }
            }
            i += 1
        }
        return stack.pop() as? InnerList ?: throw Exception("Parsing not complete")
    }

    fun List<String>.parse(): List<Pair<InnerList, InnerList>> {
        return this.joinToString("\n").split("\n\n").map { pair ->
            val (first, second) = pair.split("\n")
            first.parseLine() to second.parseLine()
        }
    }

    fun compare(lhs: InnerItem, rhs: InnerItem): Boolean? {
        if (lhs is InnerNumber && rhs is InnerNumber) {
            if (DEBUG)println("Compare $lhs vs $rhs")
            val result = if (lhs.value < rhs.value) {
                true
            } else if (lhs.value > rhs.value) {
                false
            } else {
                null
            }
            if (DEBUG) {
                if (result == true) {
                    println("Left side is smaller, so inputs are in the right order")
                } else if (result == false) {
                    println("Right side is smaller, so inputs are not in the right order")
                }
            }
            return result
        } else if (lhs is InnerList && rhs is InnerList) {
            if (DEBUG) println("Compare $lhs vs $rhs")
            for (i in 0 until max(lhs.list.size, rhs.list.size)) {
                if (i >= rhs.list.size) {
                    return false
                }
                if (i >= lhs.list.size) {
                    return true
                }
                val left = lhs.list[i]
                val right = rhs.list[i]
                val cmp = compare(left, right)
                if (cmp != null) {
                    return cmp
                }
            }
            return null
        } else if ((lhs is InnerNumber).xor(rhs is InnerNumber)) {
            if (DEBUG) print("Mixed types; ")
            return if (lhs is InnerNumber) {
                if (DEBUG) println(" convert left to ${InnerList(listOf(lhs))} and retry comparison")
                compare(InnerList(listOf(lhs)), rhs)
            } else if (rhs is InnerNumber) {
                if (DEBUG)  println(" convert right to ${InnerList(listOf(rhs))} and retry comparison")
                compare(lhs, InnerList(listOf(rhs)))
            } else {
                throw Exception("Unexpected")
            }
        }
        throw Exception("Unexpected")
    }

    fun part1(input: List<String>): Int {
        return input.parse().mapIndexed { idx, (first, second) ->
            if (DEBUG)  println("== Pair ${idx + 1} ==")
            val result = compare(first, second)
            if (DEBUG) println()
            result
        }
            .mapIndexed { idx, item -> idx + 1 to item }
            .filter { (_, item) -> item == true }
            .sumOf { (idx, _) -> idx }
    }

    fun part2(input: List<String>): Int {
        val parsed = input.parse()
        val additionalPackets = listOf(
            InnerList(mutableListOf(InnerList(mutableListOf(InnerNumber(2))))),
            InnerList(mutableListOf(InnerList(mutableListOf(InnerNumber(6)))))
        )
        val sorted = (parsed.flatMap { listOf(it.first, it.second) } + additionalPackets)
            .sortedWith { lhs, rhs ->
            when (compare(lhs, rhs)) {
                true -> 1
                false -> -1
                else -> 0
            }
        }.reversed()
        return additionalPackets.map { sorted.indexOf(it) + 1 }.reduce { acc, it -> acc * it }
    }

    val testInput = readInput("Day13_test")

    val input = readInput("Day13")
    assert(part1(testInput), 13)
    println(part1(input))
    assert(part2(testInput), 140)
    println(part2(input))
}
// Time: 01:30