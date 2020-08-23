/*
 * Copyright 2016-2019 JetBrains s.r.o.
 * Use of this source code is governed by the Apache 2.0 License that can be found in the LICENSE.txt file.
 */

package benchmarks.immutableMap

import benchmarks.*
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.benchmark.*
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.*

@State(Scope.Benchmark)
open class PutAll {


    @Param(BM_10, BM_100, BM_1000, BM_10000, BM_100000, BM_1000000, BM_10000000)
    var size: Int = 0

    @Param(HASH_IMPL, ORDERED_IMPL)
    var implementation = ""

    @Param(ASCENDING_HASH_CODE, RANDOM_HASH_CODE, COLLISION_HASH_CODE)
    var hashCodeType = ""

    private var map = persistentMapOf<IntWrapper, String>()
    private var mapCopy = persistentMapOf<IntWrapper, String>()
    private var otherMap = persistentMapOf<IntWrapper, String>()

    @Setup
    fun prepare() {
        val keys = generateKeys(hashCodeType, size)
        map = persistentMapPut(implementation, keys.filterIndexed { ix, e -> ix < keys.size / 2 })
        mapCopy = persistentMapPut(implementation, keys.filterIndexed { ix, e -> ix < keys.size / 2 })

        otherMap = persistentMapPut(implementation, keys.filterIndexed { ix, e -> ix >= keys.size / 2 })
    }

    @Benchmark
    fun addAll(): ImmutableMap<IntWrapper, String> {
        return map.plus(otherMap)
    }

    @Benchmark
    fun addAllWorstCase(): ImmutableMap<IntWrapper, String> {
        return map.plus(mapCopy)
    }

    @Benchmark
    fun addAllOld(): ImmutableMap<IntWrapper, String> {
        return map.mutate { it.putAll(otherMap) }
    }

    @Benchmark
    fun addAllOldWorstCase(): ImmutableMap<IntWrapper, String> {
        return map.mutate { it.putAll(mapCopy) }
    }
}