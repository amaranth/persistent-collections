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

interface ImmutableMap<K, V> : Map<K, V>, Iterable<Map.Entry<K, V>> {
    override val entries: ImmutableSet<Map.Entry<K, V>>
    override val keys: ImmutableSet<K>
    override val values: ImmutableCollection<V>

    fun clear(): ImmutableMap<K, V>
    fun put(key: K, value: V): ImmutableMap<K, V>
    fun putAll(from: Map<out K, V>): ImmutableMap<K, V>
    fun remove(key: K): ImmutableMap<K, V>
}
