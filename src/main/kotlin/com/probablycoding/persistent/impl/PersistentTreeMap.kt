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
package com.probablycoding.persistent.impl

import java.util.Comparator

class PersistentTreeMap<K, V> private constructor(override val comparator: java.util.Comparator<in K>, override val size: Int,
                                                  private val root: com.probablycoding.persistent.impl.PersistentTreeMap.Node<K, V>) : com.probablycoding.persistent.impl.AbstractSortedMap<K, V>() {
    override fun containsKey(key: K): Boolean {
        return find(root, key) != null
    }

    override operator fun get(key: K): V? {
        return find(root, key)?.value
    }

    override fun clear(): com.probablycoding.persistent.impl.PersistentTreeMap<K, V> {
        return com.probablycoding.persistent.impl.PersistentTreeMap.Companion.empty(comparator)
    }

    override fun put(key: K, value: V): com.probablycoding.persistent.impl.PersistentTreeMap<K, V> {
        val (added, newRoot) = put(root, key, value)
        if (added) {
            return com.probablycoding.persistent.impl.PersistentTreeMap(comparator, size + 1, newRoot)
        } else {
            return com.probablycoding.persistent.impl.PersistentTreeMap(comparator, size, newRoot)
        }
    }

    override fun remove(key: K): com.probablycoding.persistent.impl.PersistentTreeMap<K, V> {
        val (removed, newRoot) = remove(root, key)
        if (removed) {
            return com.probablycoding.persistent.impl.PersistentTreeMap(comparator, size - 1, newRoot)
        } else {
            return com.probablycoding.persistent.impl.PersistentTreeMap(comparator, size, newRoot)
        }
    }

    override fun subMap(fromKey: K, toKey: K): com.probablycoding.persistent.impl.PersistentTreeMap<K, V> {
        val compareFromTo = comparator.compare(fromKey, toKey)
        require(compareFromTo <= 0) { "fromKey > toKey" }

        val first = firstEntry()
        val last = lastEntry()
        val compareFromFirst = comparator.compare(fromKey, first.key)
        val compareFromLast = comparator.compare(fromKey, last.key)
        val compareToFirst = comparator.compare(toKey, first.key)
        val compareToLast = comparator.compare(toKey, last.key)

        if (compareFromTo == 0 || compareFromLast > 0 || compareToFirst < 0) {
            return com.probablycoding.persistent.impl.PersistentTreeMap.Companion.empty(comparator)
        } else if (compareFromFirst <= 0 && compareToLast > 0) {
            return this
        } else if (compareFromLast == 0) {
            return com.probablycoding.persistent.impl.PersistentTreeMap.Companion.of(comparator, last)
        } else {
            var result = com.probablycoding.persistent.impl.PersistentTreeMap.Companion.empty<K, V>(comparator)

            for ((key, value) in this) {
                if (comparator.compare(toKey, key) <= 0) {
                    break
                } else if (comparator.compare(fromKey, key) <= 0) {
                    result = result.put(key, value)
                }
            }

            return result
        }
    }

    override fun tailMap(fromKey: K): com.probablycoding.persistent.impl.PersistentTreeMap<K, V> {
        val first = firstEntry()
        val last = lastEntry()
        val compareFromFirst = comparator.compare(fromKey, first.key)
        val compareFromLast = comparator.compare(fromKey, last.key)

        if (compareFromLast > 0) {
            return com.probablycoding.persistent.impl.PersistentTreeMap.Companion.empty(comparator)
        } else if (compareFromFirst <= 0) {
            return this
        } else if (compareFromLast == 0) {
            return com.probablycoding.persistent.impl.PersistentTreeMap.Companion.of(comparator, last)
        } else {
            var result = com.probablycoding.persistent.impl.PersistentTreeMap.Companion.empty<K, V>(comparator)

            for ((key, value) in this) {
                if (comparator.compare(fromKey, key) <= 0) {
                    result = result.put(key, value)
                }
            }

            return result
        }
    }

    override fun comparator(): java.util.Comparator<in K>? {
        if (comparator === com.probablycoding.persistent.impl.PersistentTreeMap.Companion.COMPARABLE_COMPARATOR) {
            return null
        } else {
            return comparator
        }
    }

    override fun firstEntry(): Map.Entry<K, V> {
        var node = root
        if (node !== com.probablycoding.persistent.impl.PersistentTreeMap.Companion.EMPTY_NODE) {
            while (node.left !== com.probablycoding.persistent.impl.PersistentTreeMap.Companion.EMPTY_NODE) {
                node = node.left
            }
        } else {
            throw java.util.NoSuchElementException()
        }

        return com.probablycoding.persistent.impl.Entry(node.key, node.value)
    }

    override fun lastEntry(): Map.Entry<K, V> {
        var node = root
        if (node !== com.probablycoding.persistent.impl.PersistentTreeMap.Companion.EMPTY_NODE) {
            while (node.right !== com.probablycoding.persistent.impl.PersistentTreeMap.Companion.EMPTY_NODE) {
                node = node.right
            }
        } else {
            throw java.util.NoSuchElementException()
        }

        return com.probablycoding.persistent.impl.Entry(node.key, node.value)
    }

    override fun iterator(): Iterator<Map.Entry<K, V>> {
        return object : Iterator<Map.Entry<K, V>> {
            private val stack = java.util.Stack<Node<K, V>>()

            init {
                push(root)
            }

            private fun push(root: com.probablycoding.persistent.impl.PersistentTreeMap.Node<K, V>) {
                var node = root
                while (node !== com.probablycoding.persistent.impl.PersistentTreeMap.Companion.EMPTY_NODE) {
                    stack.push(node)
                    node = node.left
                }
            }

            override fun hasNext(): Boolean {
                return stack.isNotEmpty()
            }

            override fun next(): Map.Entry<K, V> {
                val node = stack.pop()
                push(node.right)
                return com.probablycoding.persistent.impl.Entry(node.key, node.value)
            }
        }
    }

    private fun find(node: com.probablycoding.persistent.impl.PersistentTreeMap.Node<K, V>, key: K): com.probablycoding.persistent.impl.PersistentTreeMap.Node<K, V>? {
        if (node === com.probablycoding.persistent.impl.PersistentTreeMap.Companion.EMPTY_NODE) {
            return null
        } else {
            val compare = comparator.compare(key, node.key)

            if (compare < 0) {
                return find(node.left, key)
            } else if (compare > 0) {
                return find(node.right, key)
            } else {
                return node
            }
        }
    }

    private fun put(node: com.probablycoding.persistent.impl.PersistentTreeMap.Node<K, V>, key: K, value: V): Pair<Boolean, com.probablycoding.persistent.impl.PersistentTreeMap.Node<K, V>> {
        if (node === com.probablycoding.persistent.impl.PersistentTreeMap.Companion.EMPTY_NODE) {
            return true to com.probablycoding.persistent.impl.PersistentTreeMap.Node(1, emptyNode(), emptyNode(), key, value)
        } else {
            val compare = comparator.compare(key, node.key)
            val added: Boolean
            val newNode: com.probablycoding.persistent.impl.PersistentTreeMap.Node<K, V>

            if (compare < 0) {
                val result = put(node.left, key, value)
                added = result.first
                newNode = node.copy(left = result.second)
            } else if (compare > 0) {
                val result = put(node.right, key, value)
                added = result.first
                newNode = node.copy(right = result.second)
            } else {
                added = false
                newNode = node.copy(value = value)
            }

            if (added) {
                return true to split(skew(newNode))
            } else {
                return false to newNode
            }
        }
    }

    private fun remove(node: com.probablycoding.persistent.impl.PersistentTreeMap.Node<K, V>, key: K): Pair<Boolean, com.probablycoding.persistent.impl.PersistentTreeMap.Node<K, V>> {
        var removed = false
        var newNode = node

        if (node !== com.probablycoding.persistent.impl.PersistentTreeMap.Companion.EMPTY_NODE) {
            val compare = comparator.compare(node.key, key)
            if (compare == 0 && node.level == 1) {
                removed = true
                newNode = node.right
            } else {
                if (compare > 0) {
                    val result = remove(node.left, key)
                    removed = result.first
                    newNode = node.copy(left = result.second)
                } else if (compare < 0) {
                    val result = remove(node.right, key)
                    removed = result.first
                    newNode = node.copy(right = result.second)
                } else {
                    removed = true

                    if (node.left !== com.probablycoding.persistent.impl.PersistentTreeMap.Companion.EMPTY_NODE && node.right !== com.probablycoding.persistent.impl.PersistentTreeMap.Companion.EMPTY_NODE) {
                        var heir = node.left
                        while (heir.right !== com.probablycoding.persistent.impl.PersistentTreeMap.Companion.EMPTY_NODE) {
                            heir = heir.right
                        }

                        val result = remove(node.left, heir.key)
                        newNode = node.copy(left = result.second, key = heir.key, value = heir.value)
                    } else if (node.left !== com.probablycoding.persistent.impl.PersistentTreeMap.Companion.EMPTY_NODE) {
                        newNode = node.left
                    } else {
                        newNode = node.right
                    }
                }
            }
        }

        if (newNode.left.level < newNode.level - 1 || newNode.right.level < newNode.level - 1) {
            if (newNode.right.level > newNode.level - 1) {
                val right = newNode.right.copy(level = newNode.level - 1)
                newNode = newNode.copy(level = newNode.level - 1, right = right)
            }

            newNode = skew(newNode)
            newNode = newNode.copy(right = skew(newNode.right))
            if (newNode.right !== com.probablycoding.persistent.impl.PersistentTreeMap.Companion.EMPTY_NODE) {
                newNode = newNode.copy(right = newNode.right.copy(right = skew(newNode.right.right)))
            }
            newNode = split(newNode)
            newNode = newNode.copy(right = split(newNode.right))
        }

        return removed to newNode
    }

    private fun skew(node: com.probablycoding.persistent.impl.PersistentTreeMap.Node<K, V>): com.probablycoding.persistent.impl.PersistentTreeMap.Node<K, V> {
        if (node.level != 0 && node.left.level == node.level) {
            val right = node.copy(left = node.left.right, right = node.right)
            return com.probablycoding.persistent.impl.PersistentTreeMap.Node(node.level, node.left.left, right, node.left.key, node.left.value)
        } else {
            return node
        }
    }

    private fun split(node: com.probablycoding.persistent.impl.PersistentTreeMap.Node<K, V>): com.probablycoding.persistent.impl.PersistentTreeMap.Node<K, V> {
        if (node.level != 0 && node.right.right.level == node.level) {
            val left = node.copy(right = node.right.left)
            return com.probablycoding.persistent.impl.PersistentTreeMap.Node(node.right.level + 1, left, node.right.right, node.right.key, node.right.value)
        } else {
            return node
        }
    }

    private class Node<K, V>(val level: Int, left: com.probablycoding.persistent.impl.PersistentTreeMap.Node<K, V>?, right: com.probablycoding.persistent.impl.PersistentTreeMap.Node<K, V>?, var key: K, var value: V) {
        val left: com.probablycoding.persistent.impl.PersistentTreeMap.Node<K, V> = left ?: this
        val right: com.probablycoding.persistent.impl.PersistentTreeMap.Node<K, V> = right ?: this

        init {
            require((left == null && right == null) || level > 0)
        }

        fun copy(level: Int = this.level, left: com.probablycoding.persistent.impl.PersistentTreeMap.Node<K, V> = this.left, right: com.probablycoding.persistent.impl.PersistentTreeMap.Node<K, V> = this.right, key: K = this.key, value: V = this.value): com.probablycoding.persistent.impl.PersistentTreeMap.Node<K, V> {
            return com.probablycoding.persistent.impl.PersistentTreeMap.Node(level, left, right, key, value)
        }
    }

    companion object {
        private val COMPARABLE_COMPARATOR = java.util.Comparator<Any?> { first, second ->
            @Suppress("UNCHECKED_CAST")
            (kotlin.comparisons.compareValues(first as Comparable<Any>?, second as Comparable<Any>?))
        }
        private val EMPTY_NODE = Node<Any?, Any?>(0, null, null, null, null)
        private val EMPTY = PersistentTreeMap(COMPARABLE_COMPARATOR, 0, EMPTY_NODE)

        private fun <K, V> emptyNode(): Node<K, V> {
            @Suppress("UNCHECKED_CAST")
            return EMPTY_NODE as Node<K, V>
        }

        fun <K : Comparable<K>, V> empty(): PersistentTreeMap<K, V> {
            @Suppress("UNCHECKED_CAST")
            return EMPTY as PersistentTreeMap<K, V>
        }

        fun <K, V> empty(comparator: Comparator<in K>): PersistentTreeMap<K, V> {
            return PersistentTreeMap(comparator, 0, emptyNode())
        }

        fun <T, K : Comparable<K>, V> fromSequence(sequence: Sequence<T>, keySelector: (T) -> K,
                                                   valueTransform: (T) -> V): PersistentTreeMap<K, V> {
            return sequence.fold(empty()) { map, element -> map.put(keySelector(element), valueTransform(element)) }
        }

        fun <T, K, V> fromSequence(comparator: Comparator<in K>, sequence: Sequence<T>, keySelector: (T) -> K,
                                   valueTransform: (T) -> V): PersistentTreeMap<K, V> {
            return sequence.fold(empty(comparator)) { map, element ->
                map.put(keySelector(element), valueTransform(element))
            }
        }

        fun <T, K : Comparable<K>, V> fromSequence(sequence: Sequence<T>,
                                                   transform: (T) -> Pair<K, V>): PersistentTreeMap<K, V> {
            return sequence.fold(empty()) { map, element ->
                val pair = transform(element)
                map.put(pair.first, pair.second)
            }
        }

        fun <T, K, V> fromSequence(comparator: Comparator<in K>, sequence: Sequence<T>,
                                   transform: (T) -> Pair<K, V>): PersistentTreeMap<K, V> {
            return sequence.fold(empty(comparator)) { map, element ->
                val pair = transform(element)
                map.put(pair.first, pair.second)
            }
        }

        fun <K : Comparable<K>, V> of(vararg entries: Map.Entry<K, V>): PersistentTreeMap<K, V> {
            return entries.fold(empty()) { map, entry -> map.put(entry.key, entry.value) }
        }

        fun <K, V> of(comparator: Comparator<in K>, vararg entries: Map.Entry<K, V>): PersistentTreeMap<K, V> {
            return entries.fold(empty(comparator)) { map, entry -> map.put(entry.key, entry.value) }
        }

        fun <K : Comparable<K>, V> of(vararg entries: Pair<K, V>): PersistentTreeMap<K, V> {
            return entries.fold(empty()) { map, entry -> map.put(entry.first, entry.second) }
        }

        fun <K, V> of(comparator: Comparator<in K>, vararg entries: Pair<K, V>): PersistentTreeMap<K, V> {
            return entries.fold(empty(comparator)) { map, entry -> map.put(entry.first, entry.second) }
        }
    }
}
