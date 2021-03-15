/*
 * Copyright 2016-2020 JetBrains s.r.o.
 * Use of this source code is governed by the Apache 2.0 License that can be found in the LICENSE.txt file.
 */

package kotlinx.collections.immutable.implementations.altImmutableList

import kotlinx.collections.immutable.internal.ListImplementation
import kotlin.random.Random

internal val <E> TreapNode<E>?.size: Int get() = this?.size ?: 0
internal val <E> TreapNode<E>?.rank: Int get() = this?.rank ?: 0

internal class TreapNode<E>(val left: TreapNode<E>?, val element: E, val right: TreapNode<E>?, val rank: Int) {

    constructor(left: TreapNode<E>?, element: E, right: TreapNode<E>?, randomSource: Random) :
            this(left, element, right, randomSource.nextInt())

    data class Builder<E>(
            var left: TreapNode<E>? = null,
            var right: TreapNode<E>? = null,
            var element: E? = null
    )

    internal fun copy(
            left: TreapNode<E>? = this.left,
            element: E = this.element,
            right: TreapNode<E>? = this.right,
            rank: Int = this.rank
    ) = TreapNode(left, element, right, rank)

    internal operator fun component1(): TreapNode<E>? = left
    internal operator fun component2(): TreapNode<E>? = right
    internal operator fun component3(): E = element

    val size: Int = left.size + right.size + 1
    private val centerIndex: Int get() = left.size

    fun removeAll(predicate: (E) -> Boolean): TreapNode<E>? {
        val lefty = left?.removeAll(predicate)
        val righty = right?.removeAll(predicate)
        return when {
            predicate(element) -> copy(left = lefty, right = righty)
            else -> lefty + righty
        }
    }

    fun get(index: Int): E {
        ListImplementation.checkElementIndex(index, size)
        return when {
            index == centerIndex -> element
            index < centerIndex -> left?.get(index) ?: throw IndexOutOfBoundsException()
            /* index > centerIndex */
            else -> right?.get(centerIndex + index) ?: throw IndexOutOfBoundsException()
        }
    }

    fun splitAt(index: Int, mutableNode: Builder<E> = Builder()): Builder<E> {
        ListImplementation.checkElementIndex(index, size)
        return when {
            index == centerIndex -> {
                mutableNode.left = left
                mutableNode.right = right
                mutableNode.element = element
                mutableNode
            }
            index < centerIndex -> {
                val lefty = requireNotNull(left).splitAt(index, mutableNode)
                lefty.right = lefty.right + right
                lefty
            }
            /* index > centerIndex */
            else -> {
                val righty = requireNotNull(right).splitAt(index - centerIndex, mutableNode)
                righty.left = left + righty.left
                righty
            }
        }
    }

    private suspend fun SequenceScope<E>.walk(body: suspend SequenceScope<E>.(E) -> Unit) {
        left?.let { walk(body) }
        body(element)
        right?.let { walk(body) }
    }

    operator fun iterator(): Iterator<E> = iterator {
        walk { yield(it) }
    }
}

internal operator fun <E> TreapNode<E>?.plus(that: TreapNode<E>?): TreapNode<E>? {
    if (this === null) return that
    if (that === null) return right

    return when {
        this.rank > that.rank -> this.copy(left = this.left, right = this.right + that)
        else -> that.copy(left = this + that.left, right = that.right)
    }
}
