/*
 * Copyright 2016-2019 JetBrains s.r.o.
 * Use of this source code is governed by the Apache 2.0 License that can be found in the LICENSE.txt file.
 */

package benchmarks.immutableList

import benchmarks.AMT_IMPL
import benchmarks.TREAP_IMPL
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.implementations.altImmutableList.ImplicitKeyTreap
import kotlinx.collections.immutable.implementations.immutableList.persistentVectorOf
import kotlinx.collections.immutable.persistentListOf

fun persistentListAdd(implementation: String, size: Int): PersistentList<String> {
    var list: PersistentList<String> = emptyPersistentList(implementation)
    repeat(times = size) {
        list = list.add("some element")
    }
    return list
}

fun <E> emptyPersistentList(implementation: String): PersistentList<E> {
    return when (implementation) {
        AMT_IMPL -> persistentVectorOf()
        TREAP_IMPL -> ImplicitKeyTreap.emptyOf()
        else -> TODO()
    }
}
