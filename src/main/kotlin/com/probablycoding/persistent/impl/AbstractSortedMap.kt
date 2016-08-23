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
package com.probablycoding.persistent.impl

import com.probablycoding.persistent.ImmutableSortedMap
import com.probablycoding.persistent.ImmutableSortedSet
import java.util.Comparator

abstract class AbstractSortedMap<K, V> : AbstractMap<K, V>(), ImmutableSortedMap<K, V> {
    abstract val comparator: Comparator<in K>
    override val entries: ImmutableSortedSet<Map.Entry<K, V>>
        get() = ImmutableSortedEntrySet(this)
    override val keys: ImmutableSortedSet<K>
        get() = ImmutableSortedKeySet(this)

    override fun firstKey(): K {
        return firstEntry().key
    }

    override fun firstValue(): V {
        return firstEntry().value
    }

    override fun headMap(toKey: K): ImmutableSortedMap<K, V> {
        if (comparator.compare(firstKey(), toKey) >= 0) {
            return clear()
        } else {
            return subMap(firstKey(), toKey)
        }
    }

    override fun lastKey(): K {
        return lastEntry().key
    }

    override fun lastValue(): V {
        return lastEntry().value
    }

    private class ImmutableSortedEntrySet<K, V>(private val parent: AbstractSortedMap<K, V>) : AbstractSortedSet<Map.Entry<K, V>>() {
        override val comparator = Comparator<Map.Entry<K, V>> { first, second -> parent.comparator.compare(first.key, second.key) }
        override val size = parent.size

        override operator fun contains(element: Map.Entry<K, V>): Boolean {
            return parent.containsKey(element.key) && parent[element.key] == element.value
        }

        override operator fun iterator(): Iterator<Map.Entry<K, V>> {
            return parent.iterator()
        }

        override fun comparator(): Comparator<in Map.Entry<K, V>>? {
            val comparator = parent.comparator()
            if (comparator == null) {
                return null
            } else {
                return this.comparator
            }
        }

        override fun first(): Map.Entry<K, V> {
            val key = parent.firstKey()
            return Entry(key, parent[key] as V)
        }

        override fun last(): Map.Entry<K, V> {
            val key = parent.lastKey()
            return Entry(key, parent[key] as V)
        }

        override fun subSet(fromElement: Map.Entry<K, V>, toElement: Map.Entry<K, V>): ImmutableSortedSet<Map.Entry<K, V>> {
            return parent.subMap(fromElement.key, toElement.key).entries
        }

        override fun tailSet(fromElement: Map.Entry<K, V>): ImmutableSortedSet<Map.Entry<K, V>> {
            return parent.tailMap(fromElement.key).entries
        }

        override fun add(element: Map.Entry<K, V>): ImmutableSortedSet<Map.Entry<K, V>> {
            return parent.put(element.key, element.value).entries
        }

        override fun clear(): ImmutableSortedSet<Map.Entry<K, V>> {
            return parent.clear().entries
        }
    }

    private class ImmutableSortedKeySet<K>(private val parent: AbstractSortedMap<K, *>) : AbstractSortedSet<K>() {
        override val comparator = parent.comparator
        override val size = parent.size

        override operator fun contains(element: K): Boolean {
            return parent.containsKey(element)
        }

        override operator fun iterator(): Iterator<K> {
            return object : Iterator<K> {
                private val iterator = parent.iterator()

                override fun hasNext(): Boolean {
                    return iterator.hasNext()
                }

                override fun next(): K {
                    return iterator.next().key
                }
            }
        }

        override fun comparator(): Comparator<in K>? {
            return parent.comparator()
        }

        override fun first(): K {
            return parent.firstKey()
        }

        override fun last(): K {
            return parent.lastKey()
        }

        override fun subSet(fromElement: K, toElement: K): ImmutableSortedSet<K> {
            return parent.subMap(fromElement, toElement).keys
        }

        override fun tailSet(fromElement: K): ImmutableSortedSet<K> {
            return parent.tailMap(fromElement).keys
        }

        override fun add(element: K): ImmutableSortedSet<K> {
            throw UnsupportedOperationException()
        }

        override fun clear(): ImmutableSortedSet<K> {
            return parent.clear().keys
        }
    }
}
