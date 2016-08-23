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
import java.util.LinkedHashSet
import java.util.Random
import kotlin.test.*

abstract class AbstractMapTest(val supportsNullValues: Boolean = true) {
    abstract fun <K, V> buildMap(vararg entries: Pair<K, V>): ImmutableMap<K, V>

    @Test
    fun putGetRemove() {
        var map = buildMap(1 to "A")
        assertEquals("A", map[1])

        map = map.remove(1)
        assertTrue(map.isEmpty())
    }

    @Test
    fun removeFromEmpty() {
        val map = buildMap<Int, Int>()
        assertTrue(map.remove(1).isEmpty())
    }

    @Test
    fun removeNotExists() {
        var map = buildMap(1 to "A")
        assertEquals("A", map[1])

        map = map.remove(2)
        assertEquals(buildMap(1 to "A"), map)
    }

    @Test
    fun removeNotExists2() {
        val map = buildMap(1 to "A", 2 to "B")
        assertEquals(buildMap(1 to "A", 2 to "B"), map.remove(3))
    }

    @Test
    fun removeSingle() {
        val map = buildMap(1 to "A")
        assertTrue(map.remove(1).isEmpty())
    }

    @Test
    fun removeUpdateSingle() {
        var map = buildMap(1 to "A")
        map = map.put(1, "A")
        assertEquals(buildMap(1 to "A"), map)
    }

    @Test
    fun getFromEmpty() {
        val map = buildMap<Int, Int>()
        assertNull(map[1])
    }

    @Test
    fun forEach() {
        val pairs = setOf(1 to "A", 2 to "B", 3 to "C", 4 to "D")
        val map = buildMap(*pairs.toTypedArray())
        val actual = hashSetOf<Pair<Int, String>>()

        map.forEach { key, value -> actual.add(Pair(key, value)) }
        assertEquals(pairs, actual)
    }

    @Test
    fun iterator() {
        val pairs = setOf(1 to "A", 2 to "B", 3 to "C", 4 to "D")
        val map = buildMap(*pairs.toTypedArray())
        val actual = hashSetOf<Pair<Int, String>>()

        map.forEach { key, value -> actual.add(Pair(key, value)) }
        assertEquals(pairs, actual)
    }

    @Test
    fun keys() {
        val pairs = setOf(1 to "A", 2 to "B", 3 to "C", 4 to "D")
        val map = buildMap(*pairs.toTypedArray())
        val actual = hashSetOf<Int>()

        map.keys.forEach { actual.add(it) }
        assertEquals(pairs.map { it.first }.toSet(), actual)
    }

    @Test
    fun values() {
        val pairs = setOf(1 to "A", 2 to "B", 3 to "C", 4 to "D")
        val map = buildMap(*pairs.toTypedArray())
        val actual = hashSetOf<String>()

        map.values.forEach { actual.add(it) }
        assertEquals(pairs.map { it.second }.toSet(), actual)
    }

    @Test
    fun forEachWithCollisions() {
        val pairs = setOf(CollidingKey(1, 1) to "A", CollidingKey(1, 2) to "B", CollidingKey(2, 3) to "C", CollidingKey(2, 4) to "D")
        val map = buildMap(*pairs.toTypedArray())
        val actual = hashSetOf<Pair<CollidingKey, String>>()

        map.forEach { key, value -> actual.add(Pair(key, value)) }
        assertEquals(pairs, actual)
    }

    @Test
    fun iteratorWithCollisions() {
        val pairs = setOf(CollidingKey(1, 1) to "A", CollidingKey(1, 2) to "B", CollidingKey(2, 3) to "C", CollidingKey(2, 4) to "D")
        val map = buildMap(*pairs.toTypedArray())
        val actual = hashSetOf<Pair<CollidingKey, String>>()

        map.forEach { key, value -> actual.add(Pair(key, value)) }
        assertEquals(pairs, actual)
    }

    @Test
    fun putRemoveCollisions() {
        var map = buildMap(CollidingKey(1, 1) to "A", CollidingKey(1, 2) to "B", CollidingKey(2, 3) to "C", CollidingKey(2, 4) to "D")
        map = map.remove(CollidingKey(1, 1)).remove(CollidingKey(1, 2)).remove(CollidingKey(2, 3)).remove(CollidingKey(2, 4))
        assertTrue(map.isEmpty())
    }

    @Test
    fun putGetRemoveMultiple() {
        var map = buildMap(1 to "A", 2 to "B", 3 to "C", 4 to "D")
        map = map.remove(1)
        map = map.remove(2)
        map = map.remove(3)
        map = map.remove(4)
        assertTrue(map.isEmpty())
    }

    @Test
    fun immutablity() {
        val map1 = buildMap(1 to "A")

        val map2 = map1.put(2, "B")
        val map3 = map2.put(3, "C")
        val map4 = map3.remove(1)

        assertEquals(buildMap(1 to "A"), map1)
        assertEquals(buildMap(1 to "A", 2 to "B"), map2)
        assertEquals(buildMap(1 to "A", 2 to "B", 3 to "C"), map3)
        assertEquals(buildMap(2 to "B", 3 to "C"), map4)
    }

    @Test
    fun updateSameKey() {
        var map = buildMap(1 to "A")
        map = map.put(1, "B")
        assertEquals(buildMap(1 to "B"), map)
    }

    @Test
    fun updateSameKeyWithCollision() {
        var map = buildMap(CollidingKey(1, 1) to "A")
        map = map.put(CollidingKey(1, 1), "B")
        assertEquals(buildMap(CollidingKey(1, 1) to "B"), map)
    }

    @Test
    fun collisions() {
        val map = buildMap(CollidingKey(1, 1) to "A", CollidingKey(1, 2) to "B", CollidingKey(1, 3) to "C", CollidingKey(1, 4) to "D")
        assertEquals("A", map[CollidingKey(1, 1)])
        assertEquals("B", map[CollidingKey(1, 2)])
        assertEquals("C", map[CollidingKey(1, 3)])
        assertEquals("D", map[CollidingKey(1, 4)])
    }

    @Test
    fun collisionsUpdated() {
        var map = buildMap(CollidingKey(1, 1) to "A", CollidingKey(1, 2) to "B")
        map = map.put(CollidingKey(1, 1), "C")
        map = map.put(CollidingKey(1, 2), "D")
        assertEquals("C", map[CollidingKey(1, 1)])
        assertEquals("D", map[CollidingKey(1, 2)])
    }

    @Test
    fun collisionsUpdateWithSame() {
        var map = buildMap(CollidingKey(1, 1) to "A")
        map = map.put(CollidingKey(1, 1), "A")
        assertEquals("A", map[CollidingKey(1, 1)])
    }

    @Test
    fun collisionsRemoved() {
        var map = buildMap(CollidingKey(1, 1) to "A", CollidingKey(1, 2) to "B", CollidingKey(1, 3) to "C", CollidingKey(1, 4) to "D")
        assertEquals(4, map.size)

        map = map.remove(CollidingKey(1, 1)).remove(CollidingKey(1, 2)).remove(CollidingKey(1, 3)).remove(CollidingKey(1, 4))
        assertTrue(map.isEmpty())
    }

    @Test
    fun equal() {
        assertEquals(buildMap(1 to 2, 3 to 4), buildMap(1 to 2, 3 to 4))
    }

    @Test
    fun testHashCode() {
        assertEquals(buildMap(1 to 2, 3 to 4).hashCode(), buildMap(1 to 2, 3 to 4).hashCode())
    }

    @Test
    fun containsKey() {
        val map = buildMap(1 to 2, 3 to 4)
        assertTrue(map.containsKey(1))
        assertTrue(map.containsKey(3))
        assertFalse(map.containsKey(2))
        assertFalse(map.containsKey(4))
    }

    @Test
    fun emptyGet() {
        val map = buildMap<Int, Int>()
        assertNull(map[1])
    }

    @Test
    fun emptyRemove() {
        val map = buildMap<Int, Int>()
        assertTrue(map.remove(1).isEmpty())
    }

    @Test
    fun nullValue() {
        val map = buildMap<Int?, Int?>()
        assertNull(map.put(1, null)[1])
    }

    @Test
    fun containsKeyNullValue() {
        val map = buildMap<Int?, Int?>()
        assertTrue(map.put(1, null).containsKey(1))
    }

    @Test
    fun equalsNonMap() {
        assertFalse(buildMap(1 to 2).equals(""))
    }

    @Test
    fun equalsWithNullValues() {
        if (supportsNullValues) {
            assertEquals(buildMap(1 to null), buildMap(1 to null))
        }
    }

    @Test
    fun putGetRemoveRandom() {
        putGetRemoveRandom(10000)
    }

    fun putGetRemoveRandom(size: Int) {
        val numbers = randomNumbers(size)
        var map = buildMap<Int, Int>()
        for (i in numbers) {
            map = map.put(i, i)
        }
        // Do it again
        for (i in numbers) {
            map = map.put(i, i)
        }
        // Do it again with new value
        for (i in numbers) {
            map = map.put(i, i + 1)
        }
        assertEquals(numbers.size, map.size)

        for (i in numbers) {
            assertEquals(i + 1, map[i])
        }

        for (i in numbers) {
            map = map.remove(i)
        }
        assertTrue(map.isEmpty())

        // Do it again
        for (i in numbers) {
            map = map.remove(i)
        }
        assertTrue(map.isEmpty())
    }

    fun randomNumbers(size: Int): LinkedHashSet<Int> {
        val random = Random(12)
        val generated = LinkedHashSet<Int>()
        while (generated.size < size) {
            generated.add(random.nextInt())
        }
        return generated
    }
}
