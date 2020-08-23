/*
 * Copyright 2016-2019 JetBrains s.r.o.
 * Use of this source code is governed by the Apache 2.0 License that can be found in the LICENSE.txt file.
 */

package benchmarks.immutableSet

import benchmarks.*
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.benchmark.*
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.*
import kotlin.random.Random

@State(Scope.Benchmark)
open class RetainAll {
    @Param(BM_10, BM_100, BM_1000, BM_10000, BM_100000, BM_1000000, BM_10000000)
    var size: Int = 0

    @Param(HASH_IMPL, ORDERED_IMPL)
    var implementation = ""

    @Param(ASCENDING_HASH_CODE, RANDOM_HASH_CODE, COLLISION_HASH_CODE)
    var hashCodeType = ""

    private var elements = listOf<IntWrapper>()

    private var set = persistentSetOf<IntWrapper>()
    private var setCopy = persistentSetOf<IntWrapper>()
    private var setMixed = persistentSetOf<IntWrapper>()
    private var setDisjoint = persistentSetOf<IntWrapper>()

    @Setup
    fun prepare() {
        elements = generateElements(hashCodeType, size).shuffled(Random(20))
        val lists = elements.chunked(elements.size / 4)

        set = persistentSetAdd(implementation, lists[0] + lists[1])
        setCopy = persistentSetAdd(implementation, lists[0] + lists[1])

        setMixed = persistentSetAdd(implementation, lists[1] + lists[2])
        setDisjoint = persistentSetAdd(implementation, lists[2] + lists[3])
    }

    @Benchmark
    fun retainAllDisjoint(): ImmutableSet<IntWrapper> {
        return set intersect setDisjoint
    }

    @Benchmark
    fun retainAllMixed(): ImmutableSet<IntWrapper> {
        return set intersect setMixed
    }

    @Benchmark
    fun retainAllCopy(): ImmutableSet<IntWrapper> {
        return set intersect setCopy
    }

    @Benchmark
    fun retainAllDisjointOld(): ImmutableSet<IntWrapper> {
        return set.mutate { it.removeAll { it !in setDisjoint } }
    }

    @Benchmark
    fun retainAllMixedOld(): ImmutableSet<IntWrapper> {
        return set.mutate { it.removeAll { it !in setMixed } }
    }

    @Benchmark
    fun retainAllCopyOld(): ImmutableSet<IntWrapper> {
        return set.mutate { it.removeAll { it !in setCopy } }
    }
}