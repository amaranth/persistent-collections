/*
 * Copyright (C) 2016 - Travis Watkins <amaranth@probablycoding.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("unused")
@file:JvmName("ImmutableSequences")

package com.probablycoding.persistent

import com.probablycoding.persistent.impl.PersistentHashMap
import com.probablycoding.persistent.impl.PersistentHashSet
import com.probablycoding.persistent.impl.PersistentTreeSet
import com.probablycoding.persistent.impl.PersistentVector

@Suppress("UNCHECKED_CAST")
inline fun <T, K, V, M : ImmutableMap<in K, in V>> Sequence<T>.associateImmutableTo(destination: M, transform: (T) -> Pair<K, V>): M {
    // TODO: Is there a way to speed this up without a public transient type?
    return fold(destination) { map, element ->
        val pair = transform(element)
        map.put(pair.first, pair.second) as M
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <T, K, M : ImmutableMap<in K, in T>> Sequence<T>.associateByTo(destination: M, keySelector: (T) -> K): M {
    // TODO: Is there a way to speed this up without a public transient type?
    return fold(destination) { map, element -> map.put(keySelector(element), element) as M }
}

@Suppress("UNCHECKED_CAST")
inline fun <T, K, V, M : ImmutableMap<in K, in V>> Sequence<T>.associateByTo(destination: M, keySelector: (T) -> K, valueTransform: (T) -> V): M {
    // TODO: Is there a way to speed this up without a public transient type?
    return fold(destination) { map, element -> map.put(keySelector(element), valueTransform(element)) as M }
}

fun <T, K, V> Sequence<T>.associateImmutable(transform: (T) -> Pair<K, V>): ImmutableMap<K, V> {
    return PersistentHashMap.fromSequence(this, transform)
}

fun <T, K> Sequence<T>.associateImmutableBy(keySelector: (T) -> K): ImmutableMap<K, T> {
    return PersistentHashMap.fromSequence(this) { element -> Pair(keySelector(element), element) }
}

fun <T, K, V> Sequence<T>.associateImmutableBy(keySelector: (T) -> K, valueTransform: (T) -> V): ImmutableMap<K, V> {
    return PersistentHashMap.fromSequence(this, keySelector, valueTransform)
}

fun <T> Sequence<T>.toImmutableList(): ImmutableList<T> {
    return PersistentVector.fromSequence(this)
}

fun <T> Sequence<T>.toImmutableSet(): ImmutableSet<T> {
    return PersistentHashSet.fromSequence(this)
}

fun <T : Comparable<T>> Sequence<T>.toImmutableSortedSet(): ImmutableSortedSet<T> {
    return PersistentTreeSet.fromSequence(this)
}
