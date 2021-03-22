/*
 * Copyright 2016-2021 JetBrains s.r.o.
 * Use of this source code is governed by the Apache 2.0 License that can be found in the LICENSE.txt file.
 */

package kotlinx.collections.immutable.implementations.altImmutableList

internal abstract class NodeIteratorBase<E>(protected val size: Int,
                                            protected var index: Int): ListIterator<E> {
    protected abstract val currentElement: E
    protected abstract fun forward()
    protected abstract fun backward()

    override fun nextIndex(): Int = index
    override fun previousIndex(): Int = index - 1

    override fun hasNext(): Boolean = nextIndex() < size
    override fun hasPrevious(): Boolean = previousIndex() >= 0

    override fun next(): E {
        if (!hasNext()) throw NoSuchElementException()
        val result = currentElement
        if (index + 1 != size && index != -1) forward() // do not move past the end or we'll lose the node
        ++index
        return result
    }

    override fun previous(): E {
        if (!hasPrevious()) throw NoSuchElementException()
        if (index != 0 && index != size) backward()
        --index
        return currentElement
    }

}

internal class TreapNodeIterator<E>: NodeIteratorBase<E> {
    private val backStack = mutableListOf<TreapNode<E>>()

    constructor(root: TreapNode<E>, startIndex: Int, size: Int): super(size = size, index = startIndex) {
        fun construct(node: TreapNode<E>, index: Int) {
            val nodeIndex = node.left.size
            when {
                index == nodeIndex -> push(node)
                index < nodeIndex -> {
                    push(node)
                    construct(requireNotNull(node.left), index)
                }
                index > nodeIndex -> {
                    push(node)
                    if (node.right == null) return
                    construct(node.right, index - 1 - nodeIndex)
                }
            }
        }
        construct(root, startIndex)
    }

    private val currentNode: TreapNode<E>
        get() = backStack.last()
    private val currentParent: TreapNode<E>?
        get() = backStack.getOrNull(backStack.lastIndex - 1)

    override val currentElement: E
        get() = currentNode.element

    private fun push(node: TreapNode<E>) {
        backStack.add(node)
    }
    private fun pop() {
        backStack.removeLastOrNull()
    }

    private fun goLeftAllTheWay() {
        while (true) {
            when (val left = currentNode.left) {
                null -> return
                else -> push(left)
            }
        }
    }
    private fun goRightAllTheWay() {
        while (true) {
            when (val right = currentNode.right) {
                null -> return
                else -> push(right)
            }
        }
    }

    override fun forward() {
        when (val right = currentNode.right) {
            null -> {
                if (currentNode === currentParent?.left) pop()
                else {
                    do {
                        pop()
                    } while (currentParent !== null && currentNode !== currentParent?.left)
                    pop()
                }
            }
            else -> {
                push(right)
                goLeftAllTheWay()
            }
        }
    }

    override fun backward() {
        when (val left = currentNode.left) {
            null -> {
                if (currentNode === currentParent?.right) pop()
                else {
                    do {
                        pop()
                    } while (currentParent !== null && currentNode !== currentParent?.right)
                    pop()
                }
            }
            else -> {
                push(left)
                goRightAllTheWay()
            }
        }
    }
}