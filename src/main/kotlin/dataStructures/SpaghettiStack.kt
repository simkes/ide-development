package dataStructures

class SpaghettiStack <T> {

    private var currentNode: Node<T>? = null

    private class Node<T>(
        val value: T,
        val parent: Node<T>?
    )

    fun addNode(data: T) {
        currentNode = Node(data, currentNode)
    }

    fun exitNode() {
        if (currentNode != null) {
            currentNode = currentNode!!.parent
        }
    }

    fun currentValue(): T? = currentNode?.value

    fun lookUpInParentChain(predicate: (T) -> Boolean): T? {
        var scope = currentNode
        while (scope != null) {
            if (predicate(scope.value)) {
                return scope.value
            }
            scope = scope.parent
        }
        return null
    }
}