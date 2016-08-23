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

class PersistentHashSet<E> private constructor(private val map: PersistentHashMap<E, Int>) : AbstractSet<E>() {
    override val size = map.size

    override operator fun contains(element: E): Boolean {
        return map.containsKey(element)
    }

    override fun add(element: E): PersistentHashSet<E> {
        val newMap = map.put(element, 0)
        if (newMap === map) {
            return this
        } else {
            return PersistentHashSet(newMap)
        }
    }

    override fun addAll(elements: Collection<E>): PersistentHashSet<E> {
        return elements.fold(asTransient()) { set, element -> set.add(element) }.asPersistent()
    }

    override fun remove(element: E): PersistentHashSet<E> {
        val newMap = map.remove(element)
        if (newMap === map) {
            return this
        } else {
            return PersistentHashSet(newMap)
        }
    }

    override fun removeAll(elements: Collection<E>): PersistentHashSet<E> {
        return elements.fold(asTransient()) { set, element -> set.remove(element) }.asPersistent()
    }

    override fun clear(): PersistentHashSet<E> {
        return empty()
    }

    override fun iterator(): Iterator<E> {
        return map.keys.iterator()
    }

    private fun asPersistent(): PersistentHashSet<E> {
        return PersistentHashSet(map.asPersistent())
    }

    private fun asTransient(): PersistentHashSet<E> {
        return PersistentHashSet(map.asTransient())
    }

    companion object {
        private val EMPTY = PersistentHashSet<Any?>(PersistentHashMap.empty())

        @Suppress("UNCHECKED_CAST")
        fun <E> empty(): PersistentHashSet<E> {
            return EMPTY as PersistentHashSet<E>
        }

        fun <E> fromSequence(sequence: Sequence<E>): PersistentHashSet<E> {
            return sequence.fold(empty<E>().asTransient()) { set, element -> set.add(element) }.asPersistent()
        }

        fun <E> of(vararg elements: E): PersistentHashSet<E> {
            return fromSequence(elements.asSequence())
        }
    }
}
