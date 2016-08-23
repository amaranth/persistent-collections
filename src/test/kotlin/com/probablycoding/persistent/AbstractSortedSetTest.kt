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
import java.util.NoSuchElementException
import kotlin.test.*

abstract class AbstractSortedSetTest : AbstractSetTest() {
    override abstract fun <E> build(vararg elements: E): ImmutableSortedSet<E>

    @Test
    fun subSet() {
        val set = build(1, 2, 3)
        assertEquals(build(1, 2, 3), set.subSet(0, 4))
        assertEquals(build(2, 3), set.subSet(2, 4))
        assertEquals(build(1, 2), set.subSet(1, 3))
        assertEquals(build(2), set.subSet(2, 3))
        assertEquals(build(1), set.subSet(1, 2))
        assertEquals(build<Int>(), set.subSet(0, 0))
    }

    @Test
    fun tailSet() {
        val set = build(1, 2, 3)
        assertEquals(build(1, 2, 3), set.tailSet(0))
        assertEquals(build(1, 2, 3), set.tailSet(1))
        assertEquals(build(2, 3), set.tailSet(2))
        assertEquals(build(3), set.tailSet(3))
        assertEquals(build<Int>(), set.tailSet(4))
    }

    @Test
    fun headSet() {
        val set = build(1, 2, 3)
        assertEquals(build<Int>(), set.headSet(1))
        assertEquals(build(1), set.headSet(2))
        assertEquals(build(1, 2), set.headSet(3))
        assertEquals(build(1, 2, 3), set.headSet(4))
    }

    @Test
    fun firstSortedSet() {
        val set = build(1, 3, 2, 5, 4)
        assertEquals(1, set.first())
    }

    @Test
    fun lastSortedSet() {
        val set = build(1, 3, 2, 5, 4)
        assertEquals(5, set.last())
    }

    @Test
    fun firstEmpty() {
        assertFailsWith<NoSuchElementException> { build<Int>().first() }
    }

    @Test
    fun lastEmpty() {
        assertFailsWith<NoSuchElementException> { build<Int>().last() }
    }

    @Test
    fun toSortedSet() {
        assertEquals(setOf(1, 2, 3), build(1, 2, 3).toSortedSet())
    }

    /*
    @Test
    fun take() {
        assertSequence(sequence(10).take(5), 0, 5)
    }

    @Test
    fun takeNone() {
        val set = sequence(10)
        assertSequence(set.take(0), 0, 0)
        assertSequence(set.take(-1), 0, 0)
    }

    @Test
    fun takeLoop() {
        val max = 100
        val set = sequence(max)
        for (i in 1..max) {
            assertSequence(set.take(i), 0, i)
        }
    }

    @Test
    fun dropLoop() {
        val max = 100
        val set = sequence(max)
        for (i in 1..max) {
            assertSequence(set.drop(i), i, max - i)
        }
    }

    @Test
    fun takeAll() {
        assertSequence(sequence(10).take(100), 0, 10)
    }

    @Test
    fun drop() {
        assertSequence(sequence(10).drop(5), 5, 5)
    }

    @Test
    fun dropNone() {
        val set = sequence(10)
        assertSequence(set.drop(0), 0, 10)
        assertSequence(set.drop(-1), 0, 10)
    }

    @Test
    fun dropAll() {
        assertSequence(sequence(10).drop(100), 0, 0)
    }

    fun sequence(size: Int): SortedSet<Int> {
        var set = TreeSet.empty<Int>()
        for (i in 0..size - 1) {
            set = set.add(i)
        }
        return set
    }

    fun assertSequence(set: SortedSet<Int>, from: Int, length: Int) {
        var from_ = from
        assertEquals(length, set.size())
        for (integer in set) {
            assertEquals(from_++, integer)
        }
    }
    */
}
