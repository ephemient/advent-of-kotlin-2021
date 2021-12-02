package week1

fun generateParenthesisCombinations(num: Int): List<String> {
    val cache = ArrayList<List<String>>(num)
    cache.add(listOf(""))
    (1..num).mapTo(cache) { i ->
        (0 until i).flatMap { j ->
            cache[j].flatMap { head ->
                cache[i - j - 1].map { tail ->
                    "($head)$tail"
                }
            }
        }
    }
    return cache.last()
}
