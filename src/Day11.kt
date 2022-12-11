private data class Monkey(
    val id: Int,
    var items: MutableList<Int>,
    var part2Items: MutableList<Item>,
    val operation: (Int) -> Int,
    val testDivisibleBy: Int,
    val testTrueMonkey: Int,
    val testFalseMonkey: Int
)

private object Patterns {
    val monkey = "Monkey (?<id>\\d+):".toRegex()
    val startingItems = " +Starting items: ".toRegex()
    val operation = " +Operation: new = old (?<sign>[+*]) (?<id>\\d+|old)".toRegex()
    val divisible = " +Test: divisible by (?<divider>\\d+)".toRegex()
    val testTrue = " +If true: throw to monkey (?<id>\\d+)".toRegex()
    val testFalse = " +If false: throw to monkey (?<id>\\d+)".toRegex()
}

private fun String.getValue(pattern: Regex, id: String): String {
    val match = pattern.matchEntire(this)
    val groups = match?.groups
    groups ?: throw Exception("Cant parse `$this` with `$pattern`")
    return groups[id]?.value ?: throw Exception("Id not found in regex")
}

private fun lowerWorryLevel(worry: Int): Int {
    return worry / 3
}

data class Item(
    var monkeysValues: MutableList<Int>
) {
     constructor(value: Int, monkeys: Int) : this(MutableList<Int>(monkeys) {value} )
}


private fun makeOperationsForAllMonkeys(monkeys: List<Monkey>, item: Item, operation: (Int) -> Int) : Item{
    val newMonkeyValues = monkeys.map { it.testDivisibleBy }.zip(item.monkeysValues) {
        divider, monkeyValue ->
        operation(monkeyValue) % divider
    }.toMutableList()
    return Item(
        newMonkeyValues
    )
}
fun main() {
    fun List<String>.parse(): List<Monkey> {
        val allMonkeys = joinToString("\n").split("\n\n")
        return allMonkeys.map { monkeyString ->
            val monkeyDesc = monkeyString.split("\n")
            val id = monkeyDesc[0].getValue(Patterns.monkey, "id").toInt()
            val items = monkeyDesc[1].replace(Patterns.startingItems, "")
                .split(", ")
                .map { it.toInt() }
                .toMutableList()
            val part2Items = items.map { Item(it, allMonkeys.size) }.toMutableList()
            val operationSign = monkeyDesc[2].getValue(Patterns.operation, "sign")
            val operationItem = monkeyDesc[2].getValue(Patterns.operation, "id")
            val operation: (Int) -> Int = if (operationItem == "old") {
                when (operationSign) {
                    "+" -> { v -> v + v }
                    "*" -> { v -> v * v }
                    else -> throw Exception("Unknown operation")
                }
            } else {
                val operationId = operationItem.toInt()
                when (operationSign) {
                    "+" -> { v -> v + operationId }
                    "*" -> { v -> v * operationId }
                    else -> throw Exception("Unknown operation")
                }
            }

            val divisible = monkeyDesc[3].getValue(Patterns.divisible, "divider").toInt()
            val testTrue = monkeyDesc[4].getValue(Patterns.testTrue, "id").toInt()
            val testFalse = monkeyDesc[5].getValue(Patterns.testFalse, "id").toInt()
            Monkey(
                id = id,
                items = items,
                operation = operation,
                testDivisibleBy = divisible,
                testTrueMonkey = testTrue,
                testFalseMonkey = testFalse,
                part2Items = part2Items
            )
        }
    }

    fun part1(input: List<String>): Int {
        val monkeys = input.parse()
        var round = 0
        val monkeyBusiness = MutableList(monkeys.size) { 0 }
        var monkeyId = 0
        while (round != 20) {
            val monkey = monkeys[monkeyId]
            for (item in monkey.items) {
                monkeyBusiness[monkey.id] += 1
                var newWorryLvl = monkey.operation(item)
                newWorryLvl = lowerWorryLevel(newWorryLvl)
                val nextMonkeyId = when (newWorryLvl % monkey.testDivisibleBy == 0) {
                    true -> monkey.testTrueMonkey
                    false -> monkey.testFalseMonkey
                }
                monkeys[nextMonkeyId].items.add(newWorryLvl)
            }
            monkey.items = mutableListOf()
            if (monkeyId + 1 == monkeys.size) {
                round += 1
            }
            monkeyId = (monkeyId + 1) % monkeys.size
        }
        return monkeyBusiness.sorted().reversed().slice(0..1).reduce { acc, it -> acc * it }
    }

    fun part2(input: List<String>): Long {
        val monkeys = input.parse()
        var round = 0
        val monkeyBusiness = MutableList(monkeys.size) { 0L }
        var monkeyId = 0
        while (round != 10000) {
            val monkey = monkeys[monkeyId]
            for (item in monkey.part2Items) {
                monkeyBusiness[monkey.id] = monkeyBusiness[monkey.id] + 1
                val newWorryLvl = makeOperationsForAllMonkeys(monkeys, item, monkey.operation)
                val nextMonkeyId = when (newWorryLvl.monkeysValues[monkeyId] % monkey.testDivisibleBy == 0) {
                    true -> monkey.testTrueMonkey
                    false -> monkey.testFalseMonkey
                }
                monkeys[nextMonkeyId].part2Items.add(newWorryLvl)
            }
            monkey.part2Items = mutableListOf()
            if (monkeyId + 1 == monkeys.size) {
                round += 1
            }
            monkeyId = (monkeyId + 1) % monkeys.size
        }
        return monkeyBusiness.sorted().reversed().slice(0..1).reduce { acc, it -> acc * it }
    }

    val testInput = readInput("Day11_test")

    val input = readInput("Day11")
    assert(part1(testInput), 10605)
    println(part1(input))
    assert(part2(testInput), 2713310158)
    println(part2(input))
}
// Time: 01:36
