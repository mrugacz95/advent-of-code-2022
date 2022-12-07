private interface Node {
    fun dirSize(): Int
}

private data class Dir(val name: String, val parent: Dir?) : HashMap<String, Node>(), Node {
    private var dirSize = -1
    override fun dirSize(): Int {
        if (dirSize == -1) {
            dirSize = values.sumOf { it.dirSize() }
        }
        return dirSize
    }

    fun iterate(): List<Dir> {
        val list = mutableListOf<Dir>()
        for (dir in values.filterIsInstance<Dir>()) {
            list.add(dir)
            list.addAll(dir.iterate())
        }
        return list
    }
}

private data class File(val name: String, val size: Int, val parent: Dir) : Node {
    override fun dirSize(): Int {
        return size
    }
}

fun main() {
    val changeDir = "\\$ cd (?<dir>.*)".toRegex()
    val list = "\\$ ls".toRegex()
    val file = "(?<size>\\d+) (?<name>.*)".toRegex()

    fun List<String>.parse(): Dir {
        val root = Dir(name = "/", parent = null)
        var currenLoc = root

        fun parseDir(dir: String) {
            currenLoc = when (dir) {
                "/" -> root
                ".." -> currenLoc.parent ?: throw Exception("cant go to parent of ${currenLoc.name}")
                else -> {
                    val childDir = currenLoc.values.filterIsInstance(Dir::class.java).firstOrNull { it.name == dir }
                    if (childDir == null) {
                        val newDir = Dir(dir, currenLoc)
                        currenLoc[dir] = newDir
                        newDir
                    } else {
                        childDir
                    }
                }
            }
        }

        fun parseList(index: Int) {
            var resultIndex = index + 1

            while (resultIndex < this.size && this[resultIndex].first() != '$') {
                val fileLine = this[resultIndex]
                if (fileLine.startsWith("dir")) {
                    resultIndex += 1
                    continue
                }
                val matchesFile = file.matchEntire(fileLine)
                val name = matchesFile?.groups?.get("name")?.value ?: throw Exception("Cant parse")
                val size = matchesFile.groups["size"]?.value?.toInt() ?: throw Exception("Cant parse")
                currenLoc[name] = File(name, size, currenLoc)
                resultIndex += 1
            }
        }

        for (i in indices) {
            val line = this[i]
            val matchesChangeDir = changeDir.matchEntire(line)
            val matchesList = list.matchEntire(line)
            if (matchesChangeDir?.groups != null) {
                val dir = matchesChangeDir.groups["dir"]?.value ?: throw Exception("Cant parse")
                parseDir(dir)
            } else if (matchesList != null) {
                parseList(i)
            }
        }
        return root
    }

    fun part1(input: List<String>): Int =
        input.parse().iterate().map { it.dirSize() }.filter { it <= 100000 }.sum()

    fun part2(input: List<String>): Int {
        val totalSpace = 70000000
        val neededSpace = 30000000
        val root = input.parse()
        val unusedSpace = totalSpace - root.dirSize()
        val needToFreeSpace = neededSpace - unusedSpace
        return root.iterate()
            .map { it }
            .sortedBy { it.dirSize() }
            .first { it.dirSize() > needToFreeSpace }
            .dirSize()
    }

    val testInput = readInput("Day07_test")

    val input = readInput("Day07")
    assert(part1(testInput), 95437)
    println(part1(input))
    assert(part2(testInput), 24933642)
    println(part2(input))
}
// Time: 00:40
