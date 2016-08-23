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

import com.probablycoding.persistent.impl.add
import com.probablycoding.persistent.impl.addAll
import com.probablycoding.persistent.impl.descendingIterator
import com.probablycoding.persistent.impl.removeAt
import org.junit.Assert.*
import org.junit.Test
import java.util.NoSuchElementException
import kotlin.test.*

class Arrays2Test {
    @Test
    fun descendingIterator() {
        val empty = emptyArray<Int>()
        assertFalse(empty.descendingIterator().hasNext())
        assertFailsWith<NoSuchElementException> { empty.descendingIterator().next() }

        val iterator = arrayOf(1, 2, 3).descendingIterator()
        assertTrue(iterator.hasNext())
        iterator.next()
        iterator.next()
        iterator.next()
        assertFalse(iterator.hasNext())
        assertFailsWith<NoSuchElementException> { iterator.next() }

        val array = arrayOf(1, 2, 3, 4, 5, 6)
        val reversed = array.reversedArray()
        array.descendingIterator().asSequence().zip(reversed.asSequence()).forEach {
            assertEquals(it.second, it.first)
        }
    }

    @Test
    fun add() {
        val array = arrayOf(1, 2, 4).add(2, 3)
        assertArrayEquals(arrayOf(1, 2, 3, 4), array)
    }

    @Test
    fun addAll() {
        val array = arrayOf(1, 2, 3)
        val begin = array.addAll(0, -2, -1, 0)
        val end = array.addAll(3, 4, 5, 6)
        assertArrayEquals(arrayOf(-2, -1, 0, 1, 2, 3), begin)
        assertArrayEquals(arrayOf(1, 2, 3, 4, 5, 6), end)
        assertFailsWith<IndexOutOfBoundsException> { array.addAll(-1, 1, 2, 3) }
        assertFailsWith<IndexOutOfBoundsException> { array.addAll(4, 1, 2, 3) }
    }

    @Test
    fun removeAt() {
        val array = arrayOf(1, 2, 3, 4, 5, 6)
        assertArrayEquals(arrayOf(2, 3, 4, 5, 6), array.removeAt(0))
        assertArrayEquals(arrayOf(1, 3, 4, 5, 6), array.removeAt(1))
        assertArrayEquals(arrayOf(1, 2, 3, 4, 5), array.removeAt(5))
        assertFailsWith<IndexOutOfBoundsException> { array.removeAt(-1) }
        assertFailsWith<IndexOutOfBoundsException> { array.removeAt(6) }

        assertArrayEquals(arrayOf(1, 3, 5), array.removeAt(1, 3, 5))
        assertArrayEquals(arrayOf(3, 4, 5, 6), array.removeAt(0, 1))
        assertArrayEquals(emptyArray(), array.removeAt(0, 1, 2, 3, 4, 5))
    }
}
