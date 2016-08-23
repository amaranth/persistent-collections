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
import java.util.Comparator
import java.util.NoSuchElementException
import kotlin.test.*

abstract class AbstractSortedMapTest(supportsNullValues: Boolean = true) : AbstractMapTest(supportsNullValues) {
    abstract fun <K, V> buildMap(comparator: Comparator<K>, vararg entries: Pair<K, V>): ImmutableSortedMap<K, V>
    override abstract fun <K, V> buildMap(vararg entries: Pair<K, V>): ImmutableSortedMap<K, V>

    @Test
    fun sortedForEach() {
        val map = buildMap(1 to "A", 3 to "C", 2 to "B", 5 to "E", 4 to "D")
        val actual = arrayListOf<Int>()
        map.forEach { key, value -> actual.add(key) }
        assertEquals(listOf(1, 2, 3, 4, 5), actual)
    }

    @Test
    fun subMap() {
        val map = buildMap(1 to "A", 2 to "B", 3 to "C")
        assertEquals(buildMap(1 to "A", 2 to "B", 3 to "C"), map.subMap(1, 4))
        assertEquals(buildMap(2 to "B", 3 to "C"), map.subMap(2, 4))
        assertEquals(buildMap(1 to "A", 2 to "B"), map.subMap(1, 3))
        assertEquals(buildMap(2 to "B"), map.subMap(2, 3))
        assertEquals(buildMap(1 to "A"), map.subMap(1, 2))
        assertEquals(buildMap<Int, String>(), map.subMap(1, 1))
        assertFailsWith<IllegalArgumentException> { map.subMap(4, 1) }
    }

    @Test
    fun tailMap() {
        val map = buildMap(1 to "A", 2 to "B", 3 to "C")
        assertEquals(buildMap(1 to "A", 2 to "B", 3 to "C"), map.tailMap(1))
        assertEquals(buildMap(2 to "B", 3 to "C"), map.tailMap(2))
        assertEquals(buildMap(3 to "C"), map.tailMap(3))
        assertEquals(buildMap<Int, String>(), map.tailMap(4))
    }

    @Test
    fun headMap() {
        val map = buildMap(1 to "A", 2 to "B", 3 to "C")
        assertEquals(buildMap<Int, String>(), map.headMap(0))
        assertEquals(buildMap(1 to "A"), map.headMap(2))
        assertEquals(buildMap(1 to "A", 2 to "B"), map.headMap(3))
        assertEquals(buildMap(1 to "A", 2 to "B", 3 to "C"), map.headMap(4))
    }

    @Test
    fun firstSortedMap() {
        val map = buildMap(1 to "A", 3 to "C", 2 to "B", 5 to "E", 4 to "D")
        assertEquals(Pair(1, "A"), map.first().toPair())
    }

    @Test
    fun firstSortedMapEmpty() {
        assertFailsWith<NoSuchElementException> { buildMap<Int, Int>().first() }
    }

    @Test
    fun lastSortedMap() {
        val map = buildMap(1 to "A", 3 to "C", 2 to "B", 5 to "E", 4 to "D")
        assertEquals(Pair(5, "E"), map.last().toPair())
    }

    @Test
    fun lastSortedMapEmpty() {
        assertFailsWith<NoSuchElementException> { buildMap<Int, Int>().last() }
    }

    @Test
    fun sortedWithCustomComparator() {
        val comparator = Comparator<Int> { o1, o2 -> o1.compareTo(o2) * -1 }
        val map = buildMap(comparator, 2 to 20, 1 to 10, 3 to 30, 7 to 70, 4 to 40)
        val actual = arrayListOf<Int>()

        for ((key) in map) {
            actual.add(key)
        }

        assertEquals(listOf(7, 4, 3, 2, 1), actual)
        assertEquals(comparator, map.comparator())
    }

    @Test
    fun compartorIsNullWhenNotSupplied() {
        val map = buildMap<Int, Int>()
        assertNull(map.comparator())
    }

    @Test
    fun take() {
        assertSequence(sequence(10).headMap(5), 0, 5)
    }

    @Test
    fun takeNone() {
        val map = sequence(10)
        assertSequence(map.headMap(0), 0, 0)
    }

    @Test
    fun headMapLoop() {
        val max = 100
        val map = sequence(max)
        for (i in 1..max) {
            assertSequence(map.headMap(i), 0, i)
        }
    }

    @Test
    fun tailMapLoop() {
        val max = 100
        val map = sequence(max)
        for (i in 1..max) {
            assertSequence(map.tailMap(i), i, max - i)
        }
    }

    @Test
    fun takeAll() {
        assertSequence(sequence(10).headMap(100), 0, 10)
    }

    @Test
    fun drop() {
        assertSequence(sequence(10).tailMap(5), 5, 5)
    }

    @Test
    fun dropNone() {
        val map = sequence(10)
        assertSequence(map.tailMap(0), 0, 10)
    }

    @Test
    fun dropAll() {
        assertSequence(sequence(10).tailMap(100), 0, 0)
    }

    @Test
    fun asSortedMap() {
        assertEquals(sortedMapOf(1 to "A", 2 to "B"), buildMap(2 to "B", 1 to "A").toSortedMap())
    }

    fun sequence(size: Int): SortedMap<Int, Int> {
        val array = Array(size) { Pair(it, it) }
        return buildMap(*array)
    }

    fun assertSequence(map: SortedMap<Int, Int>, from: Int, length: Int) {
        var from_ = from
        assertEquals(length, map.size)
        for (pair in map) {
            val expected = from_++
            val actual = pair.component1()
            assertEquals(expected, actual)
        }
    }
}
