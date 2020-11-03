/*
 * Copyright 2016-2020 JetBrains s.r.o.
 * Use of this source code is governed by the Apache 2.0 License that can be found in the LICENSE.txt file.
 */

package kotlinx.collections.immutable.implementations.altImmutableList

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.implementations.immutableList.AbstractPersistentList
import kotlin.random.Random

private object EmptyIterator: Iterator<Nothing> {
    override fun hasNext(): Boolean = false
    override fun next(): Nothing = throw NoSuchElementException()
}

class ImplicitKeyTreap<E> internal constructor(
        private val root: TreapNode<E>?,
        val randomSource: Random = Random.Default
): AbstractPersistentList<E>() {
    internal fun copy(root: TreapNode<E>? = this.root, randomSource: Random = this.randomSource) =
            ImplicitKeyTreap(root, randomSource)

    internal fun TreapNode(value: E, left: TreapNode<E>? = null, right: TreapNode<E>? = null) =
            TreapNode(left, value, right, randomSource)

    override fun add(element: E): PersistentList<E> =
            copy(root + TreapNode(element))

    override fun removeAll(predicate: (E) -> Boolean): PersistentList<E> {
        return copy(root?.removeAll(predicate))
    }

    override fun set(index: Int, element: E): PersistentList<E> {
        requireNotNull(root)
        val (l, r, _) = root.splitAt(index)
        return copy(l + TreapNode(element) + r)
    }

    override fun add(index: Int, element: E): PersistentList<E> {
        if (size == 0 && index == 0) return ImplicitKeyTreap(TreapNode(element))
        requireNotNull(root)
        val (l, r, e) = root.splitAt(index)
        return copy(l + TreapNode(element) + TreapNode(e) + r)
    }

    override fun addAll(elements: Collection<E>): PersistentList<E> {
        return when (elements) {
            is ImplicitKeyTreap -> {
                copy(root + elements.root)
            }
            else -> super.addAll(elements)
        }
    }

    override fun addAll(index: Int, c: Collection<E>): PersistentList<E> {
        return when (c) {
            is ImplicitKeyTreap -> {
                if (index == 0 && size == 0) return c
                requireNotNull(root)
                val (l, r, e) = root.splitAt(index)
                copy(l + c.root + TreapNode(e) + r)
            }
            else -> super.addAll(index, c)
        }
    }

    override fun removeAt(index: Int): PersistentList<E> {
        requireNotNull(root)
        val (l, r, _) = root.splitAt(index)
        return copy(l + r)
    }

    override fun builder(): PersistentList.Builder<E> {
        TODO("Not yet implemented")
    }

    override val size: Int
        get() = root.size

    override fun get(index: Int): E {
        requireNotNull(root)
        return root.get(index)
    }

    override fun iterator(): Iterator<E> = root?.iterator() ?: EmptyIterator

    companion object {
        val EMPTY: ImplicitKeyTreap<Nothing> = ImplicitKeyTreap(null)
    }

}