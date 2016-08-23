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
package com.probablycoding.persistent

import java.util.Comparator

interface SortedMap<K, out V> : Map<K, V> {
    override val entries: SortedSet<Map.Entry<K, @UnsafeVariance V>>
    override val keys: SortedSet<K>

    fun comparator(): Comparator<in K>?

    fun firstEntry(): Map.Entry<K, V>

    fun firstKey(): K

    fun firstValue(): V

    fun headMap(toKey: K): SortedMap<K, V>

    fun lastEntry(): Map.Entry<K, V>

    fun lastKey(): K

    fun lastValue(): V

    fun subMap(fromKey: K, toKey: K): SortedMap<K, V>

    fun tailMap(fromKey: K): SortedMap<K, V>
}
