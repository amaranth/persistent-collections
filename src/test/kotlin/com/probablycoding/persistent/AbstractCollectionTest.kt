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

import org.junit.Test
import kotlin.test.*

abstract class AbstractCollectionTest {
    abstract fun <E> build(vararg elements: E): ImmutableCollection<E>

    @Test
    fun add() {
        assertEquals(build(1), build<Int>().add(1))
        assertEquals(build(1, 2), build(1).add(2))
        assertEquals(build(1, 2, 3), build(1, 2).add(3))
    }

    @Test
    fun contains() {
        val collection = build(1, 2, 3)
        assertTrue(collection.contains(1))
        assertTrue(collection.contains(2))
        assertFalse(collection.contains(0))

        val other = build(1, 2)
        assertTrue(collection.containsAll(other))
        assertFalse(other.containsAll(collection))
    }

    @Test
    fun isEmpty() {
        val empty = build<Int>()
        assertTrue(empty.isEmpty())
        assertFalse(empty.isNotEmpty())

        val collection = build(1, 2, 3)
        assertTrue(collection.isNotEmpty())
        assertFalse(collection.isEmpty())
    }

    @Test
    fun iterator() {
        val empty = build<Int>()
        assertFalse(empty.iterator().hasNext())
        assertFails { empty.iterator().next() }

        val iterator = build(1, 2, 3).iterator()
        assertTrue(iterator.hasNext())
        iterator.next()
        iterator.next()
        iterator.next()
        assertFalse(iterator.hasNext())
        assertFails { iterator.next() }
    }

    @Test
    fun equalsAndHashCode() {
        assertEquals(build(1, 2, 3), build(1, 2, 3))
        assertNotEquals(build(1, 2, 3), build(2, 3, 4))
        assertNotEquals(build(1, 2, 3), build(1, 2))
        assertNotEquals<Any>(build(1, 2, 3), "")

        assertEquals(build(1, null, 2, null), build(1, null, 2, null))
        assertEquals(build(1, 2), build(1, 2))
        assertEquals(build(null, 2), build(null, 2))
        assertEquals(build(1, null), build(1, null))
        assertNotEquals<Collection<Any?>>(build(1), build(null))
        assertNotEquals<Collection<Any?>>(build(null), build(1))
        assertNotEquals<Collection<Any?>>(build(1, 2, 3), build(null, null, null))

        assertEquals(build(1, 2, 3).hashCode(), build(1, 2, 3).hashCode())
        assertNotEquals(build(1, 2, 3).hashCode(), build(2, 3, 4).hashCode())

        assertEquals(build(1, null, 2, null).hashCode(), build(1, null, 2, null).hashCode())
    }
}
