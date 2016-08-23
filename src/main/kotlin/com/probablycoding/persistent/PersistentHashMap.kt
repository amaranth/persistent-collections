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

import java.util.NoSuchElementException

class PersistentHashMap<K, V> private constructor(size: Int, private val marker: Any?,
                                                  private var root: Node<K, V>) : AbstractMap<K, V>() {
    override var size = size
        private set

    override fun containsKey(key: K): Boolean {
        val entry = root.get(0, key?.hashCode() ?: 0, key)
        return entry != null
    }

    override operator fun get(key: K): V? {
        val entry = root.get(0, key?.hashCode() ?: 0, key)
        return entry?.value
    }

    override fun clear(): PersistentHashMap<K, V> {
        return empty()
    }

    override fun put(key: K, value: V): PersistentHashMap<K, V> {
        val (changed, newRoot) = root.put(marker, 0, key?.hashCode() ?: 0, key, value)
        if (newRoot === root) {
            return this
        } else {
            return PersistentHashMap(size + changed, marker, newRoot!!)
        }
    }

    override fun putAll(from: Map<out K, V>): PersistentHashMap<K, V> {
        return from.entries.fold(asTransient()) { map, entry -> map.put(entry.key, entry.value) }.asPersistent()
    }

    override fun remove(key: K): PersistentHashMap<K, V> {
        val (changed, newRoot) = root.remove(marker, 0, key?.hashCode() ?: 0, key)
        if (newRoot === root) {
            return this
        } else {
            return PersistentHashMap(size + changed, marker, newRoot ?: emptyNode())
        }
    }

    override fun iterator(): Iterator<Map.Entry<K, V>> {
        return root.iterator()
    }

    internal fun asPersistent(): PersistentHashMap<K, V> {
        return PersistentHashMap(size, null, root)
    }

    internal fun asTransient(): PersistentHashMap<K, V> {
        return PersistentHashMap(size, Any(), root)
    }

    private sealed class Node<K, V>(val marker: Any?): Iterable<Map.Entry<K, V>> {
        data class Result<K, V>(val changed: Int, val node: Node<K, V>?)

        abstract fun get(shift: Int, hash: Int, key: K): Map.Entry<K, V>?
        abstract fun put(marker: Any?, shift: Int, hash: Int, key: K, value: V): Result<K, V>
        abstract fun remove(marker: Any?, shift: Int, hash: Int, key: K): Result<K, V>

        class Collision<K, V>(marker: Any?, val keys: Array<K>, val values: Array<V>) : Node<K, V>(marker) {
            override fun get(shift: Int, hash: Int, key: K): Map.Entry<K, V>? {
                val index = keys.indexOf(key)
                if (index < 0) {
                    return null
                } else {
                    return Entry(keys[index], values[index])
                }
            }

            override fun put(marker: Any?, shift: Int, hash: Int, key: K, value: V): Result<K, V> {
                val index = keys.indexOf(key)
                if (index < 0) {
                    return Result(1, Collision(marker, keys + key, values + value))
                } else {
                    val result = mutable(marker)
                    result.values[index] = value
                    return Result(0, result)
                }
            }

            override fun remove(marker: Any?, shift: Int, hash: Int, key: K): Result<K, V> {
                val index = keys.indexOf(key)
                if (index < 0) {
                    return Result(0, this)
                } else if (keys.size == 1) {
                    return Result(-1, null)
                } else {
                    return Result(-1, Collision(marker, keys.removeAt(index), values.removeAt(index)))
                }
            }

            private fun mutable(marker: Any?): Collision<K, V> {
                if (marker != null && marker === this.marker) {
                    return this
                } else {
                    return Collision(marker, keys.copyOf(), values.copyOf())
                }
            }

            override fun iterator(): Iterator<Map.Entry<K, V>> {
                return object : Iterator<Map.Entry<K, V>> {
                    private val keys = this@Collision.keys
                    private val values = this@Collision.values
                    private var cursor = 0

                    override fun hasNext(): Boolean {
                        return cursor < keys.size
                    }

                    override fun next(): Map.Entry<K, V> {
                        if (!hasNext()) throw NoSuchElementException()
                        return Entry(keys[cursor], values[cursor++])
                    }
                }
            }
        }

        class Bitmap<K, V>(marker: Any?, var childMap: Int, var leafMap: Int, var children: Array<Node<K, V>>,
                           var leaves: Array<Any?>) : Node<K, V>(marker) {
            @Suppress("UNCHECKED_CAST")
            override fun get(shift: Int, hash: Int, key: K): Map.Entry<K, V>? {
                val bit = computeBit(hash, shift)
                if (contains(childMap, bit)) {
                    val index = index(childMap, bit)
                    return children[index].get(shift + BITS, hash, key)
                }

                val index = index(leafMap, bit) * 2
                if (contains(leafMap, bit) && leaves[index] == key) {
                    return Entry(leaves[index] as K, leaves[index + 1] as V)
                }

                return null
            }

            @Suppress("UNCHECKED_CAST")
            override fun put(marker: Any?, shift: Int, hash: Int, key: K, value: V): Result<K, V> {
                val bit = computeBit(hash, shift)
                if (contains(childMap, bit)) {
                    val index = index(childMap, bit)

                    val newChildren = mutable(marker, children)
                    val (change, node) = newChildren[index].put(marker, shift + BITS, hash, key, value)
                    newChildren[index] = node!!
                    return Result(change, Bitmap(marker, childMap, leafMap, newChildren, leaves))
                }

                val index = index(leafMap, bit) * 2
                if (contains(leafMap, bit)) {
                    if (leaves[index] == key) {
                        val newLeaves = mutable(marker, leaves)
                        newLeaves[index + 1] = value
                        return Result(0, Bitmap(marker, childMap, leafMap, children, newLeaves))
                    }

                    // Move leaf to child
                    val childIndex = index(childMap, bit)
                    val child = makeNode(marker, shift + BITS, hash, key, value, leaves[index]?.hashCode() ?: 0,
                                         leaves[index] as K, leaves[index + 1] as V)
                    val newChildren = children.add(childIndex, child)
                    val newLeaves = leaves.removeAt(index, index + 1)
                    return Result(1, Bitmap(marker, set(childMap, bit), clear(leafMap, bit), newChildren, newLeaves))
                }

                val newLeaves = leaves.addAll(index, key, value)
                return Result(1, Bitmap(marker, childMap, set(leafMap, bit), children, newLeaves))
            }

            override fun remove(marker: Any?, shift: Int, hash: Int, key: K): Result<K, V> {
                val bit = computeBit(hash, shift)
                if (contains(childMap, bit)) {
                    val index = index(childMap, bit)
                    val (changed, child) = children[index].remove(marker, shift + BITS, hash, key)

                    if (child == null && children.size == 1 && leaves.isEmpty()) {
                            return Result(-1, null)
                    } else if (child == null) {
                        return Result(-1, Bitmap(marker, clear(childMap, bit), leafMap, children.removeAt(index),
                                                 leaves))
                    } else {
                        val newChildren = mutable(marker, children)
                        newChildren[index] = child
                        return Result(changed, Bitmap(marker, childMap, leafMap, newChildren, leaves))
                    }
                }

                val index = index(leafMap, bit) * 2
                if (contains(leafMap, bit) && leaves[index] == key) {
                    if (children.isEmpty() && leaves.size == 2) {
                        return Result(-1, null)
                    } else {
                        val newLeaves = leaves.removeAt(index, index + 1)
                        return Result(-1, Bitmap(marker, childMap, clear(leafMap, bit), children, newLeaves))
                    }
                }

                return Result(0, this)
            }

            override fun iterator(): Iterator<Map.Entry<K, V>> {
                return object : Iterator<Map.Entry<K, V>> {
                    private val children = this@Bitmap.children
                    private val leaves = this@Bitmap.leaves
                    private var childIterator: Iterator<Map.Entry<K, V>>? = null
                    private var childCursor = 0
                    private var leafCursor = 0

                    override fun hasNext(): Boolean {
                        return leafCursor < leaves.size ||(childIterator != null && childIterator!!.hasNext()) ||
                               childCursor < children.size
                    }

                    @Suppress("UNCHECKED_CAST")
                    override fun next(): Map.Entry<K, V> {
                        if (!hasNext()) throw NoSuchElementException()

                        if (leafCursor < leaves.size) {
                            return Entry(leaves[leafCursor++] as K, leaves[leafCursor++] as V)
                        } else if (childIterator != null && childIterator!!.hasNext()) {
                            return childIterator!!.next()
                        } else {
                            while (childCursor < children.size) {
                                childIterator = children[childCursor++].iterator()
                                if (childIterator!!.hasNext()) {
                                    return childIterator!!.next()
                                }
                            }
                        }

                        throw IllegalStateException("Got to end of Bitmap.Iterator.next()")
                    }
                }
            }

            private fun <T> mutable(marker: Any?, array: Array<T>): Array<T> {
                if (this.marker != null && this.marker === marker) {
                    return array
                }
                return array.copyOf()
            }

            companion object {
                private fun mask(hash: Int, shift: Int) = hash ushr shift and MASK
                private fun computeBit(hash: Int, shift: Int) = 1 shl mask(hash, shift)
                private fun contains(map: Int, bit: Int) = map and bit != 0
                private fun set(map: Int, bit: Int) = map or bit
                private fun clear(map: Int, bit: Int) = map xor bit
                private fun index(map: Int, bit: Int) = Integer.bitCount(map and (bit - 1))

                @Suppress("UNCHECKED_CAST")
                private fun <K, V> makeNode(marker: Any?, shift: Int, hash0: Int, key0: K, value0: V,
                                            hash1: Int, key1: K, value1: V): Node<K, V> {
                    if (hash0 == hash1) {
                        return Collision(marker, arrayOf<Any?>(key0, key1) as Array<K>,
                                         arrayOf<Any?>(value0, value1) as Array<V>)
                    } else {
                        return Bitmap<K, V>(marker, 0, 0, emptyArray(), emptyArray())
                            .put(marker, shift, hash0, key0, value0).node!!
                            .put(marker, shift, hash1, key1, value1).node!!
                    }
                }
            }
        }
    }

    companion object {
        private const val BITS = 5
        private const val WIDTH = 1 shl BITS
        private const val MASK = WIDTH - 1

        private val EMPTY_NODE = Node.Bitmap<Any?, Any?>(null, 0, 0, emptyArray(), emptyArray())
        private val EMPTY = PersistentHashMap(0, null, EMPTY_NODE)

        @Suppress("CAST_NEVER_SUCCEEDS")
        private fun <K, V> emptyNode(): Node<K, V> {
            return EMPTY_NODE as Node<K, V>
        }

        @Suppress("UNCHECKED_CAST")
        fun <K, V> empty(): PersistentHashMap<K, V> {
            return EMPTY as PersistentHashMap<K, V>
        }

        fun <T, K, V> fromSequence(sequence: Sequence<T>, keySelector: (T) -> K,
                                   valueTransform: (T) -> V): PersistentHashMap<K, V> {
            return sequence.fold(empty<K, V>().asTransient()) { map, element ->
                map.put(keySelector(element), valueTransform(element))
            }.asPersistent()
        }

        fun <T, K, V> fromSequence(sequence: Sequence<T>, transform: (T) -> Pair<K, V>): PersistentHashMap<K, V> {
            return sequence.fold(empty<K, V>().asTransient()) { map, element ->
                val pair = transform(element)
                map.put(pair.first, pair.second)
            }.asPersistent()
        }

        fun <K, V> of(vararg entries: Map.Entry<K, V>): PersistentHashMap<K, V> {
            return entries.fold(empty<K, V>().asTransient()) { map, entry ->
                map.put(entry.key, entry.value)
            }.asPersistent()
        }

        fun <K, V> of(vararg entries: Pair<K, V>): PersistentHashMap<K, V> {
            return entries.fold(empty<K, V>().asTransient()) { map, entry ->
                map.put(entry.first, entry.second)
            }.asPersistent()
        }
    }
}
