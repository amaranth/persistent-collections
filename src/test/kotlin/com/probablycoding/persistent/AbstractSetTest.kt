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

abstract class AbstractSetTest() {
    abstract fun <E> build(vararg elements: E): ImmutableSet<E>

    @Test
    fun add() {
        var set = build<Int>()
        assertTrue(set.isEmpty())
        assertEquals(0, set.size)

        set = set.add(1)
        assertTrue(set.contains(1))
        assertFalse(set.isEmpty())
        assertEquals(1, set.size)
    }

    @Test
    fun remove() {
        var set = build(1)
        assertFalse(set.isEmpty())
        assertEquals(1, set.size)

        set = set.remove(1)
        assertTrue(set.isEmpty())
        assertEquals(0, set.size)
    }

    @Test
    fun removeNotExists() {
        val set = build(1)
        assertTrue(set.contains(1))

        val set2 = set.remove(2)
        assertEquals(set, set2)
    }

    @Test
    fun trieRemoveNotExists() {
        var set = build(1, 2)
        set = set.remove(3)
        assertEquals(build(1, 2), set)
    }

    @Test
    fun removeFromEmpty() {
        assertTrue(build<Int>().remove(1).isEmpty())
    }

    @Test
    fun removeFromSize1() {
        assertTrue(build(1).remove(1).isEmpty())
    }

    @Test
    fun updateSize1() {
        assertEquals(build(1), build(1).add(1))
    }

    @Test
    fun containsFromEmpty() {
        assertFalse(build<Int>().contains(1))
    }

    @Test
    fun putGetRemoveMultiple() {
        var set = build(1, 2, 3, 4)
        assertTrue(set.contains(1))
        assertTrue(set.contains(2))
        assertTrue(set.contains(3))
        assertTrue(set.contains(4))
        set = set.remove(1).remove(2).remove(3).remove(4)
        assertTrue(set.isEmpty())
    }

    @Test
    fun immutablity() {
        val set1 = build(1)
        val set2 = set1.add(2)

        assertEquals(build(1), set1)
        assertEquals(build(1, 2), set2)
    }

    @Test
    fun largeDataSet() {
        var ints = build<Int>()

        val range = 1..10000
        for (i in range) {
            ints = ints.add(i)
        }
        for (i in range) {
            assertTrue(ints.contains(i))
        }
        for (i in range) {
            ints = ints.remove(i)
        }

        assertTrue(ints.isEmpty())
    }

    @Test
    fun collisions() {
        val keys = setOf(CollidingKey(1, 1), CollidingKey(1, 2), CollidingKey(2, 3), CollidingKey(2, 4))
        val set = build(*keys.toTypedArray())
        for (i in set) {
            assertTrue(set.contains(i))
        }
    }

    @Test
    fun collisionsRemoved() {
        var set = build(CollidingKey(1, 1), CollidingKey(1, 2), CollidingKey(1, 3), CollidingKey(2, 4))
        assertEquals(4, set.size)

        set = set.remove(CollidingKey(1, 1))
        assertEquals(3, set.size)

        set = set.remove(CollidingKey(1, 2))
        assertEquals(2, set.size)

        set = set.remove(CollidingKey(1, 3))
        assertEquals(1, set.size)

        assertEquals(build(CollidingKey(2, 4)), set)
    }

    @Test
    fun outOfOrderInsertion() {
        assertEquals(build(3, 4, 2, 1), build(1, 2, 3, 4))
    }

    @Test
    fun duplicatesIgnored() {
        assertEquals(build(1, 2, 2, 3, 3, 3, 4, 4, 4, 4), build(1, 2, 3, 4))
    }

    @Test
    fun asSet() {
        assertEquals(setOf(1, 2, 3), build(1, 2, 3).toSet())
    }

    @Test
    fun hashCodesWithNullValues() {
        assertEquals(build(1, 2, null).hashCode(), build(1, 2, null).hashCode())
    }

    @Test
    fun equalsWithNullValues() {
        assertEquals(build(1, 2, null), build(1, 2, null))
        assertNotEquals(build<String?>(null), build<String?>("1", "2", "3"))
    }
}
