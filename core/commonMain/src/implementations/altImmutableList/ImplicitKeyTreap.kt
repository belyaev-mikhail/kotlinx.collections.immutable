/*
 * Copyright 2016-2020 JetBrains s.r.o.
 * Use of this source code is governed by the Apache 2.0 License that can be found in the LICENSE.txt file.
 */

package kotlinx.collections.immutable.implementations.altImmutableList

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.implementations.immutableList.AbstractPersistentList
import kotlinx.collections.immutable.internal.ListImplementation
import kotlin.random.Random

private object EmptyIterator: ListIterator<Nothing> {
    override fun hasNext(): Boolean = false
    override fun next(): Nothing = throw NoSuchElementException()
    override fun hasPrevious(): Boolean = false
    override fun nextIndex(): Int = 0
    override fun previous(): Nothing = throw NoSuchElementException()
    override fun previousIndex(): Int = -1
}

class ImplicitKeyTreap<E> internal constructor(
        private val root: TreapNode<E>?,
        val randomSource: Random = Random.Default
): AbstractPersistentList<E>() {
    internal fun copyOrSelf(root: TreapNode<E>? = this.root, randomSource: Random = this.randomSource) =
        when {
            root === this.root && randomSource === this.randomSource -> this
            else -> ImplicitKeyTreap(root, randomSource)
        }

    internal fun TreapNode(value: E, left: TreapNode<E>? = null, right: TreapNode<E>? = null) =
            TreapNode(left, value, right, randomSource)

    override fun add(element: E): ImplicitKeyTreap<E> =
            copyOrSelf(root + TreapNode(element))

    override fun removeAll(predicate: (E) -> Boolean): PersistentList<E> =
            copyOrSelf(root?.removeAll(predicate))

    override fun set(index: Int, element: E): PersistentList<E> {
        ListImplementation.checkElementIndex(index, size)
        checkNotNull(root)
        val (l, r, _) = root.splitAt(index)
        return copyOrSelf(l + TreapNode(element) + r)
    }

    override fun add(index: Int, element: E): ImplicitKeyTreap<E> {
        ListImplementation.checkPositionIndex(index, size)
        if (size == 0 && index == 0) return ImplicitKeyTreap(TreapNode(element))
        checkNotNull(root)
        val (l, r, e) = root.splitAt(index)
        checkNotNull(e)
        return copyOrSelf(l + TreapNode(element) + TreapNode(e) + r)
    }

    override fun addAll(elements: Collection<E>): ImplicitKeyTreap<E> {
        return when (elements) {
            is ImplicitKeyTreap -> {
                copyOrSelf(root + elements.root)
            }
            else -> {
                var result = this
                for (e in elements) result = result.add(e)
                result
            }
        }
    }

    override fun addAll(index: Int, c: Collection<E>): ImplicitKeyTreap<E> {
        ListImplementation.checkPositionIndex(index, size)
        if (index == size) return addAll(c)
        return when (c) {
            is ImplicitKeyTreap -> {
                if (index == 0 && size == 0) return c
                requireNotNull(root)
                val (l, r, e) = root.splitAt(index)
                checkNotNull(e)
                copyOrSelf(l + c.root + TreapNode(e) + r)
            }
            else -> {
                requireNotNull(root)
                val (l, r, e) = root.splitAt(index)
                checkNotNull(e)
                var left = l
                for (adding in c) left += TreapNode(adding)
                copyOrSelf(left + TreapNode(e) + r)
            }
        }
    }

    override fun removeAt(index: Int): PersistentList<E> {
        ListImplementation.checkElementIndex(index, size)
        requireNotNull(root)
        val (l, r, _) = root.splitAt(index)
        return copyOrSelf(l + r)
    }

    override fun builder(): PersistentList.Builder<E> {
        return DefaultListBuilder(this)
    }

    override val size: Int
        get() = root.size

    override fun get(index: Int): E {
        ListImplementation.checkElementIndex(index, size)
        root!!
        return root.get(index)
    }

    //override fun iterator(): Iterator<E> = root?.iterator() ?: EmptyIterator

    override fun iterator(): Iterator<E> = listIterator()

    override fun listIterator(): ListIterator<E> = listIterator(0)
    override fun listIterator(index: Int): ListIterator<E> {
        ListImplementation.checkPositionIndex(index, size)
        return when(root) {
            null -> EmptyIterator
            else -> TreapNodeIterator(root, index, size)
        }
    }
    companion object {
        private val EMPTY: ImplicitKeyTreap<Nothing> = ImplicitKeyTreap(null)

        fun <E> emptyOf(): ImplicitKeyTreap<E> =
                @Suppress("UNCHECKED_CAST") (EMPTY as ImplicitKeyTreap<E>)
    }

}