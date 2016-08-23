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

import org.junit.Assert.*
import org.junit.Test

class EntryTest {
    @Test
    fun basics() {
        val a = Entry(0, "Hello")
        assertEquals(0, a.key)
        assertEquals("Hello", a.value)

        val (aKey, aValue) = a
        assertEquals(0, aKey)
        assertEquals("Hello", aValue)

        val b = Entry(1, "World!")
        assertEquals(1, b.key)
        assertEquals("World!", b.value)

        val (bKey, bValue) = b
        assertEquals(1, bKey)
        assertEquals("World!", bValue)
    }

    @Test
    fun nulls() {
        val a = Entry<Int, String?>(0, null)
        assertEquals(0, a.key)
        assertEquals(null, a.value)

        val (aKey, aValue) = a
        assertEquals(0, aKey)
        assertEquals(null, aValue)

        val b = Entry<String?, Int>(null, 1)
        assertEquals(null, b.key)
        assertEquals(1, b.value)

        val (bKey, bValue) = b
        assertEquals(null, bKey)
        assertEquals(1, bValue)
    }

    @Test
    fun string() {
        val a = Entry(0, "Hello")
        val b = Entry(1, "World!")
        val c = Entry(2, null)
        val d = Entry(null, 3)

        assertEquals("0=Hello", a.toString())
        assertEquals("1=World!", b.toString())
        assertEquals("2=null", c.toString())
        assertEquals("null=3", d.toString())
    }

    @Test
    fun equalsAndHashCode() {
        val a = Entry(0, "Hello")
        val b = Entry(0, "World!")
        val c = Entry(1, "Hello")
        val d = Entry(2, "Hello")
        val e = Entry(3, null)
        val f = Entry(4, null)
        val g = Entry(null, 5)
        val h = Entry(null, 6)

        assertEquals(a, a)
        assertEquals(b, b)
        assertEquals(c, c)
        assertEquals(d, d)
        assertEquals(e, e)
        assertEquals(f, f)
        assertEquals(g, g)
        assertEquals(h, h)

        assertNotEquals(a, b)
        assertNotEquals(a, c)
        assertNotEquals(a, d)
        assertNotEquals(a, e)
        assertNotEquals(a, f)
        assertNotEquals(a, g)
        assertNotEquals(a, h)
        assertNotEquals(b, c)
        assertNotEquals(b, d)
        assertNotEquals(b, e)
        assertNotEquals(b, f)
        assertNotEquals(b, g)
        assertNotEquals(b, h)
        assertNotEquals(c, d)
        assertNotEquals(c, e)
        assertNotEquals(c, f)
        assertNotEquals(c, g)
        assertNotEquals(c, h)
        assertNotEquals(d, e)
        assertNotEquals(d, f)
        assertNotEquals(d, g)
        assertNotEquals(d, h)
        assertNotEquals(e, f)
        assertNotEquals(e, g)
        assertNotEquals(e, h)
        assertNotEquals(f, g)
        assertNotEquals(f, h)
        assertNotEquals(g, h)

        assertEquals(a.hashCode(), a.hashCode())
        assertEquals(b.hashCode(), b.hashCode())
        assertEquals(c.hashCode(), c.hashCode())
        assertEquals(d.hashCode(), d.hashCode())
        assertEquals(e.hashCode(), e.hashCode())
        assertEquals(f.hashCode(), f.hashCode())
        assertEquals(g.hashCode(), g.hashCode())
        assertEquals(h.hashCode(), h.hashCode())

        assertNotEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a.hashCode(), c.hashCode())
        assertNotEquals(a.hashCode(), d.hashCode())
        assertNotEquals(a.hashCode(), e.hashCode())
        assertNotEquals(a.hashCode(), f.hashCode())
        assertNotEquals(a.hashCode(), g.hashCode())
        assertNotEquals(a.hashCode(), h.hashCode())
        assertNotEquals(b.hashCode(), c.hashCode())
        assertNotEquals(b.hashCode(), d.hashCode())
        assertNotEquals(b.hashCode(), e.hashCode())
        assertNotEquals(b.hashCode(), f.hashCode())
        assertNotEquals(b.hashCode(), g.hashCode())
        assertNotEquals(b.hashCode(), h.hashCode())
        assertNotEquals(c.hashCode(), d.hashCode())
        assertNotEquals(c.hashCode(), e.hashCode())
        assertNotEquals(c.hashCode(), f.hashCode())
        assertNotEquals(c.hashCode(), g.hashCode())
        assertNotEquals(c.hashCode(), h.hashCode())
        assertNotEquals(d.hashCode(), e.hashCode())
        assertNotEquals(d.hashCode(), f.hashCode())
        assertNotEquals(d.hashCode(), g.hashCode())
        assertNotEquals(d.hashCode(), h.hashCode())
        assertNotEquals(e.hashCode(), f.hashCode())
        assertNotEquals(e.hashCode(), g.hashCode())
        assertNotEquals(e.hashCode(), h.hashCode())
        assertNotEquals(f.hashCode(), g.hashCode())
        assertNotEquals(f.hashCode(), h.hashCode())
        assertNotEquals(g.hashCode(), h.hashCode())
    }
}
