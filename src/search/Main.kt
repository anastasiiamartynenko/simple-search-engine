package search

import java.io.File

fun searchByQuery(
    query: String,
    map: MutableMap<String, MutableList<Int>>,
    entries: MutableList<String>,
    strategy: String = "all"
): MutableList<String> {
    val foundEntries = mutableListOf<String>()
    val indices: MutableList<Int> = mutableListOf()
    when (strategy) {
        "all" ->  {
            map[query]?.let { indices.addAll(it) }
        }
        "any" -> {
            val words = query.split(" ")
            for (word in words) {
                map[word]?.let { indices.addAll(it) }
            }
        }
        "none" -> {
            val blockedIndices = mutableListOf<Int>()

            val words = query.split(" ")
            for (word in words) {
                map[word]?.let { blockedIndices.addAll(it) }
            }

            for (i in 0 until entries.size) {
                if (!blockedIndices.contains(i)) {
                    indices.add(i)
                }
            }
        }
        else -> { println("Bad request.") }
    }

    for (index in indices) {
        foundEntries.add(entries[index])
    }

    return foundEntries
}

fun buildInvertedIndex(entries: MutableList<String>): MutableMap<String, MutableList<Int>> {
    val invertedIndex = mutableMapOf<String, MutableList<Int>>()

    for ((index, entry) in entries.withIndex()) {
        val wordsInEntry = entry.split(" ")

        for (word in wordsInEntry) {
            if (invertedIndex.containsKey(word.lowercase())) {
                invertedIndex[word.lowercase()]?.add(index)
            } else {
                invertedIndex[word.lowercase()] = mutableListOf(index)
            }
        }
    }

    return invertedIndex
}

fun main(args: Array<String>) {
    val fileName = args[1]
    val file = File(fileName)
    val entries = file.readLines().toMutableList()

    val map = buildInvertedIndex(entries)

    var keepReading = true
    while (keepReading) {
        println("""
            === Menu ===
            1. Search information.
            2. Print all data.
            0. Exit.
        """.trimIndent())

        when (readln()) {
            "1" -> {
                println("Select a matching strategy: ALL, ANY, NONE")
                val strategy = readln().lowercase()
                if (strategy != "all" && strategy != "any" && strategy != "none") {
                    println("Bad request")
                    return
                }
                println("Enter a name or email to search all matching people.")
                val query = readln()
                val foundEntries = searchByQuery(query.lowercase(), map, entries, strategy = strategy)
                println(
                    if (foundEntries.isNotEmpty()) foundEntries.joinToString().replace(", ", "\n")
                    else "No matching people found."
                )
            }
            "2" -> println("\n=== List of people ===\n${entries.joinToString().replace(", ", "\n")}")
            "0" -> {
                keepReading = false
            }
            else -> println("Incorrect option! Try again.")
        }
    }
}
