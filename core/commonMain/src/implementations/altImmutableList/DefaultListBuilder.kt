/*
 * Copyright 2016-2020 JetBrains s.r.o.
 * Use of this source code is governed by the Apache 2.0 License that can be found in the LICENSE.txt file.
 */

package kotlinx.collections.immutable.implementations.altImmutableList

import kotlinx.collections.immutable.PersistentList

class DefaultListBuilder<E>(var list: PersistentList<E>) : PersistentList.Builder<E>, AbstractMutableList<E>() {
    override fun build(): PersistentList<E> = list

    override val size: Int
        get() = list.size

    override fun get(index: Int): E = list[index]

    override fun add(index: Int, element: E) {
        list = list.add(index, element)
    }

    override fun removeAt(index: Int): E {
        val result = list[index]
        list = list.removeAt(index)
        return result
    }

    override fun set(index: Int, element: E): E {
        val result = list[index]
        list = list.set(index, element)
        return result
    }

    override fun add(element: E): Boolean {
        list = list.add(element)
        return true
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        list = list.addAll(index, elements)
        return elements.isNotEmpty()
    }

    override fun addAll(elements: Collection<E>): Boolean {
        list = list.addAll(elements)
        return elements.isNotEmpty()
    }

    override fun clear() {
        list = list.clear()
    }

    override fun remove(element: E): Boolean {
        val oldList = list
        list = list.remove(element)
        return oldList != list
    }
}
