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

import java.util.Comparator

class PersistentTreeSet<E> private constructor(private val map: PersistentTreeMap<E, Int>) : AbstractSortedSet<E>() {
    override val comparator: Comparator<in E> = map.comparator
    override val size = map.size

    override fun contains(element: E): Boolean {
        return map.containsKey(element)
    }

    override fun add(element: E): PersistentTreeSet<E> {
        return PersistentTreeSet(map.put(element, 0))
    }

    override fun clear(): PersistentTreeSet<E> {
        return empty(map.comparator)
    }

    override fun comparator(): Comparator<in E>? {
        return map.comparator()
    }

    override fun first(): E {
        return map.firstKey()
    }

    override fun last(): E {
        return map.lastKey()
    }

    override fun subSet(fromElement: E, toElement: E): PersistentTreeSet<E> {
        return PersistentTreeSet(map.subMap(fromElement, toElement))
    }

    override fun tailSet(fromElement: E): PersistentTreeSet<E> {
        return PersistentTreeSet(map.tailMap(fromElement))
    }

    override fun iterator(): Iterator<E> {
        return object : Iterator<E> {
            private val iterator = map.iterator()

            override operator fun hasNext(): Boolean {
                return iterator.hasNext()
            }

            override operator fun next(): E {
                return iterator.next().key
            }
        }
    }

    companion object {
        private val EMPTY = PersistentTreeSet(PersistentTreeMap.empty<Comparable<Any?>, Int>())

        @Suppress("UNCHECKED_CAST")
        fun <E : Comparable<E>> empty(): PersistentTreeSet<E> {
            return EMPTY as PersistentTreeSet<E>
        }

        fun <E> empty(comparator: Comparator<in E>): PersistentTreeSet<E> {
            return PersistentTreeSet(PersistentTreeMap.empty<E, Int>(comparator))
        }

        fun <E : Comparable<E>> fromSequence(sequence: Sequence<E>): PersistentTreeSet<E> {
            return sequence.fold(empty(), PersistentTreeSet<E>::add)
        }

        fun <E> fromSequence(comparator: Comparator<in E>, sequence: Sequence<E>): PersistentTreeSet<E> {
            return sequence.fold(empty(comparator), PersistentTreeSet<E>::add)
        }

        fun <E : Comparable<E>> of(vararg elements: E): PersistentTreeSet<E> {
            return fromSequence(elements.asSequence())
        }

        fun <E> of(comparator: Comparator<in E>, vararg elements: E): PersistentTreeSet<E> {
            return fromSequence(comparator, elements.asSequence())
        }
    }
}
