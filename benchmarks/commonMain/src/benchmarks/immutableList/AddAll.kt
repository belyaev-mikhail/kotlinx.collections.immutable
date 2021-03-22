/*
 * Copyright 2016-2019 JetBrains s.r.o.
 * Use of this source code is governed by the Apache 2.0 License that can be found in the LICENSE.txt file.
 */

package benchmarks.immutableList

import benchmarks.*
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.benchmark.*

@State(Scope.Benchmark)
open class AddAll {
    @Param(BM_1, BM_10, BM_100, BM_1000, BM_10000, BM_100000, BM_1000000, BM_10000000)
    var size: Int = 0

    @Param(AMT_IMPL, TREAP_IMPL)
    var implementation: String = ""

    private var listToAdd = persistentListOf<String>()
    private var half = persistentListOf<String>()
    private var otherHalf = persistentListOf<String>()
    private var third = persistentListOf<String>()
    private var twoThirds = persistentListOf<String>()

    @Setup
    fun prepare() {
        listToAdd = persistentListAdd(implementation, size)
        half = persistentListAdd(implementation, size / 2)
        otherHalf = persistentListAdd(implementation, size - size / 2)
        third = persistentListAdd(implementation, size / 3)
        twoThirds = persistentListAdd(implementation, size - size / 3)
    }

    // Results of the following benchmarks do not indicate memory or time spent per operation,
    // however regressions there do indicate changes.
    //
    // the benchmarks measure mean time and memory spent per added element.
    //
    // Expected time: nearly constant.
    // Expected memory: nearly constant.

    /**
     * Adds [size] elements to an empty persistent list using `addAll` operation.
     */
    @Benchmark
    fun addAllLast(): ImmutableList<String> {
        return persistentListAdd(implementation, 0).addAll(listToAdd)
    }

    /**
     * Adds `size / 2` elements to an empty persistent list
     * and then adds `size - size / 2` elements using `addAll` operation.
     */
    @Benchmark
    fun addAllLast_Half(): ImmutableList<String> {
        return half.addAll(otherHalf)
    }

    /**
     * Adds `size - size / 3` elements to an empty persistent list
     * and then adds `size / 3` elements using `addAll` operation.
     */
    @Benchmark
    fun addAllLast_OneThird(): ImmutableList<String> {
        return twoThirds.addAll(third)
    }

    /**
     * Adds `size / 2` elements to an empty persistent list
     * and then inserts `size - size / 2` elements at the beginning using `addAll` operation.
     */
    @Benchmark
    fun addAllFirst_Half(): ImmutableList<String> {
        return half.addAll(0, otherHalf)
    }

    /**
     * Adds `size - size / 3` elements to an empty persistent list
     * and then inserts `size / 3` elements at the beginning using `addAll` operation.
     */
    @Benchmark
    fun addAllFirst_OneThird(): ImmutableList<String> {
        return twoThirds.addAll(0, third)
    }

    /**
     * Adds `size / 2` elements to an empty persistent list
     * and then inserts `size - size / 2` elements at the middle using `addAll` operation.
     */
    @Benchmark
    fun addAllMiddle_Half(): ImmutableList<String> {
        return half.addAll(half.size / 2, otherHalf)
    }

    /**
     * Adds `size - size / 3` elements to an empty persistent list builder
     * and then inserts `size / 3` elements at the middle using `addAll` operation.
     */
    @Benchmark
    fun addAllMiddle_OneThird(): ImmutableList<String> {
        return twoThirds.addAll(twoThirds.size / 2, third)
    }
}