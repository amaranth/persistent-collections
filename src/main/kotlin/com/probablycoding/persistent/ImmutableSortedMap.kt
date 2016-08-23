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

interface ImmutableSortedMap<K, V> : ImmutableMap<K, V>, SortedMap<K, V> {
    override val entries: ImmutableSortedSet<Map.Entry<K, V>>
    override val keys: ImmutableSortedSet<K>

    override fun clear(): ImmutableSortedMap<K, V>
    override fun put(key: K, value: V): ImmutableSortedMap<K, V>
    override fun putAll(from: Map<out K, V>): ImmutableSortedMap<K, V>
    override fun remove(key: K): ImmutableSortedMap<K, V>

    override fun headMap(toKey: K): ImmutableSortedMap<K, V>
    override fun subMap(fromKey: K, toKey: K): ImmutableSortedMap<K, V>
    override fun tailMap(fromKey: K): ImmutableSortedMap<K, V>
}
