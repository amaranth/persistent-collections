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
import java.math.BigDecimal
import java.util.NoSuchElementException
import kotlin.test.*

abstract class AbstractListTest : AbstractCollectionTest() {
    abstract override fun <E> build(vararg elements: E): ImmutableList<E>

    fun size(level: Int) = BigDecimal(2).pow(level * 5).intValueExact()

    fun sizes(): Collection<Int> {
        val max = 3
        val sizes = (1..max).mapTo(arrayListOf<Int>()) { size(it) }
        sizes.add(size(max) + 2)
        return sizes
    }

    @Test
    fun insertOrderPreserved() {
        assertEquals(listOf(1, 3, 2), build(1, 3, 2).asSequence().toList())
    }

    @Test
    fun duplicatesPreserved() {
        assertEquals(listOf(1, 2, 2, 1), build(1, 2, 2, 1).asSequence().toList())
    }

    @Test
    fun indexOf() {
        val list = build(1, 2, 2, 1)
        assertEquals(1, list.indexOf(2))
        assertEquals(-1, list.indexOf(3))
    }

    @Test
    fun subList() {
        assertEquals(build(1, 2, 3, 4), build(1, 2, 3, 4).subList(0, 4))
        assertEquals(build(2, 3), build(1, 2, 3, 4).subList(1, 3))
        assertEquals(build(2), build(1, 2, 3, 4).subList(1, 2))
        assertEquals(build(3), build(1, 2, 3, 4).subList(2, 3))
        assertEquals(build<Int>(), build(1, 2, 3, 4).subList(2, 2))
    }

    @Test
    fun lastIndexOf() {
        val list = build(1, 2, 2, 1)
        assertEquals(2, list.lastIndexOf(2))
        assertEquals(-1, list.lastIndexOf(3))
    }

    @Test
    fun indexOfWithNulls() {
        val list = build(1, null, null, 1)
        assertEquals(1, list.indexOf(null))
        assertEquals(-1, list.indexOf(3))
    }

    @Test
    fun lastIndexOfWithNulls() {
        val list = build(1, null, null, 1)
        assertEquals(2, list.lastIndexOf(null))
        assertEquals(-1, list.lastIndexOf(3))
    }

    @Test
    fun notEqualsWithDifferentLengths() {
        assertNotEquals(build(1, 1, 1), build(1, 1))
        assertNotEquals(build(1, 1), build(1, 1, 1))
    }

    @Test
    fun getOutOfBounds() {
        assertFailsWith<IndexOutOfBoundsException> { sequence(10)[11] }
    }

    @Test
    fun setOutOfBounds() {
        assertFailsWith<IndexOutOfBoundsException> { sequence(10)[11] = 100 }
    }

    @Test
    fun get() {
        val list = sequence(10)
        for (i in 0 until list.size) {
            assertEquals(i, list[i])
        }
        for (i in (0 until list.size).reversed()) {
            assertEquals(i, list[i])
        }
    }

    @Test
    fun set() {
        var list = sequence(10)
        for (i in 0 until list.size) {
            list = list.set(i, i + 10)
        }

        for (i in 0 until list.size) {
            assertEquals(i + 10, list[i])
        }
    }

    @Test
    fun setSameValueTwice() {
        var list = build(1, 2, 3)
        list = list.set(0, 3)
        list = list.set(0, 3)
        assertEquals(listOf(3, 2, 3), list.asSequence().toList())
    }

    @Test
    fun emptyGet() {
        assertFailsWith<IndexOutOfBoundsException> { build<Int>()[0] }
    }

    @Test
    fun emptySet() {
        assertFailsWith<IndexOutOfBoundsException> { build<Int>()[0] = 100 }
    }

    @Test
    fun emptyTake() {
        assertEquals(build<Int>(), build<Int>().take(1))
    }

    @Test
    fun emptyDrop() {
        assertEquals(build<Int>(), build<Int>().drop(1))
    }

    @Test
    fun take() {
        assertSequence(sequence(10).take(5), 0, 5)
    }

    @Test
    fun takeNone() {
        val list = sequence(10)
        assertSequence(list.take(0), 0, 0)
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
        val list = sequence(10)
        assertSequence(list.drop(0), 0, 10)
    }

    @Test
    fun dropAll() {
        assertSequence(sequence(10).drop(100), 0, 0)
    }

    @Test
    fun first() {
        assertEquals(0, sequence(10).first())
        assertFailsWith<NoSuchElementException> { build<Int>().first() }
    }

    @Test
    fun last() {
        assertEquals(9, sequence(10).last())
        assertFailsWith<NoSuchElementException> { build<Int>().last() }
    }

    @Test
    fun addGet() {
        for (size in sizes()) {
            val list = sequence(size)
            assertEquals(size, list.size)
            assertSequence(list, 0, size)
        }
    }

    @Test
    fun update() {
        for (size in sizes()) {
            var list = sequence(size)
            for (i in 0..size - 1) {
                list = list.set(i, i + 1)
            }
            for (i in 0..size - 1) {
                assertEquals(i + 1, list[i])
            }
        }
    }

    @Test
    fun string() {
        assertEquals("[1, 2, 3]", build(1, 2, 3).toString())
        assertEquals("[1, 2, null]", build(1, 2, null).toString())
    }

    fun assertSequence(list: List<Int>, from: Int, length: Int) {
        var from_ = from
        assertEquals(length, list.size)
        for (integer in list) {
            assertEquals(from_++, integer)
        }
    }

    fun sequence(size: Int): ImmutableList<Int> {
        val array = Array(size) { it }
        return build(*array)
    }
}
