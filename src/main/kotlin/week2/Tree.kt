package week2

sealed class Tree<out T> {
    override fun toString(): String = when (this) {
        is Leaf -> value.toString()
        is Node -> "($left, $right)"
    }
}

data class Leaf<out T>(val value: T) : Tree<T>()
data class Node<out T>(val left: Tree<T>, val right: Tree<T>) : Tree<T>()

interface TreeVisitor<in T, R> {
    fun visitLeaf(value: T): R
    fun beforeVisitNode(): TreeVisitor<T, R> = this
    fun duringVisitNode(left: R): TreeVisitor<T, R>? = this
    fun afterVisitNode(left: R, right: R): R
}

@OptIn(ExperimentalStdlibApi::class)
fun <T, R> Tree<T>.visit(
    visitor: TreeVisitor<T, R>,
): R = DeepRecursiveFunction<Pair<Tree<T>, TreeVisitor<T, R>>, R> { (tree, treeVisitor) ->
    when (tree) {
        is Leaf -> treeVisitor.visitLeaf(tree.value)
        is Node -> {
            val leftVisitor = treeVisitor.beforeVisitNode()
            val left = callRecursive(tree.left to leftVisitor)
            val rightVisitor = treeVisitor.duringVisitNode(left) ?: return@DeepRecursiveFunction left
            val right = callRecursive(tree.right to rightVisitor)
            treeVisitor.afterVisitNode(left, right)
        }
    }
}(this to visitor)

private object TreeCountVisitor : TreeVisitor<Any?, Int> {
    override fun visitLeaf(value: Any?): Int = 1
    override fun afterVisitNode(left: Int, right: Int): Int = left + right
}
fun Tree<*>.count(): Int = visit(TreeCountVisitor)

private object TreeCountAllVisitor : TreeVisitor<Any?, Int> {
    override fun visitLeaf(value: Any?): Int = 1
    override fun afterVisitNode(left: Int, right: Int): Int = 1 + left + right
}
fun Tree<*>.countAll(): Int = visit(TreeCountAllVisitor)

private object TreeHeightVisitor : TreeVisitor<Any?, Int> {
    override fun visitLeaf(value: Any?): Int = 1
    override fun afterVisitNode(left: Int, right: Int): Int = 1 + maxOf(left, right)
}
fun Tree<*>.height(): Int = visit(TreeHeightVisitor)

private class TreeMaximumVisitor<T>(private val comparator: Comparator<T>) : TreeVisitor<T, T> {
    override fun visitLeaf(value: T): T = value
    override fun afterVisitNode(left: T, right: T): T = maxOf(left, right, comparator)
}
fun <T : Comparable<T>> Tree<T>.biggest(): T = visit(TreeMaximumVisitor(naturalOrder()))

private object TreeSumVisitor : TreeVisitor<Int, Int> {
    override fun visitLeaf(value: Int): Int = value
    override fun afterVisitNode(left: Int, right: Int): Int = left + right
}
fun Tree<Int>.sum(): Int = visit(TreeSumVisitor)

private class TreeContainsVisitor<T>(private val element: T) : TreeVisitor<T, Boolean> {
    override fun visitLeaf(value: T): Boolean = element == value
    override fun duringVisitNode(left: Boolean): TreeVisitor<T, Boolean>? =
        if (left) null else super.duringVisitNode(left)
    override fun afterVisitNode(left: Boolean, right: Boolean): Boolean = left || right
}
operator fun <T> Tree<T>.contains(element: T): Boolean = visit(TreeContainsVisitor(element))

operator fun <T> Tree<T>.plus(other: Tree<T>): Node<T> = Node(this, other)

private class TreeAddToVisitor<T>(private val dest: MutableCollection<T>) : TreeVisitor<T, Unit> {
    override fun visitLeaf(value: T) {
        dest.add(value)
    }
    override fun afterVisitNode(left: Unit, right: Unit) {
    }
}
fun <T> Tree<T>.toList(): List<T> = buildList {
    visit(TreeAddToVisitor(this))
}