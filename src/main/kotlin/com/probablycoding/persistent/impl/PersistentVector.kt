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

import java.util.RandomAccess

class PersistentVector<E> private constructor(size: Int, private val marker: Any?, private var root: Node<E>, private var tail: Array<E?>) : RandomAccess, AbstractList<E>() {
    override var size = size
        private set
    private val tailOffset: Int
        get() = if (size < WIDTH) 0 else lastIndex ushr BITS shl BITS

    override fun get(index: Int): E {
        rangeCheck(index)

        if (index < tailOffset) {
            return root[index]
        } else {
            return tail[index and MASK] as E
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun add(element: E): PersistentVector<E> {
        if (marker != null) {
            if (size - tailOffset < WIDTH) {
                tail[size and MASK] = element
            } else {
                root = root.addTail(marker, tail as Array<E>)
                tail = arrayOfNulls<Any?>(WIDTH) as Array<E?>
                tail[0] = element
            }

            size += 1
            return this
        } else {
            val newRoot: Node<E>
            val newTail: Array<E?>

            if (size - tailOffset < WIDTH) {
                newRoot = root
                newTail = tail + element
            } else {
                newRoot = root.addTail(marker, tail as Array<E>)
                newTail = arrayOf<Any?>(element) as Array<E?>
            }

            return PersistentVector(size + 1, marker, newRoot, newTail)
        }
    }

    override fun clear(): PersistentVector<E> {
        return empty()
    }

    @Suppress("UNCHECKED_CAST")
    override fun set(index: Int, element: E): PersistentVector<E> {
        rangeCheck(index)

        if (marker != null) {
            if (index < tailOffset) {
                root = root.set(marker, index, element)
            } else {
                tail[index and MASK] = element
            }

            return this
        } else {
            val newRoot: Node<E>
            val newTail: Array<E?>

            if (index < tailOffset) {
                newRoot = root.set(marker, index, element)
                newTail = tail
            } else {
                newRoot = root
                newTail = tail.copyOf()
                newTail[index and MASK] = element
            }

            return PersistentVector(size, marker, newRoot, newTail)
        }
    }

    private fun asTransient(): PersistentVector<E> {
        return PersistentVector(size, Any(), root, tail.copyOf(WIDTH))
    }

    private fun asPersistent(): PersistentVector<E> {
        return PersistentVector(size, null, root, tail.copyOf(size - tailOffset))
    }

    private sealed class Node<E>(protected var marker: Any?, protected val shift: Int) {
        abstract fun addTail(marker: Any?, tail: Array<E>): Node<E>
        abstract fun canAddTail(): Boolean
        abstract operator fun get(index: Int): E
        abstract fun set(marker: Any?, index: Int, element: E): Node<E>

        class Branch<E>(marker: Any?, shift: Int, private val elements: Array<Node<E>>) : Node<E>(marker, shift) {
            constructor(marker: Any?, child: Node<E>) : this(marker, child.shift + BITS, arrayOf(child))

            override fun addTail(marker: Any?, tail: Array<E>): Node<E> {
                if (elements.last().canAddTail()) {
                    val result = mutable(marker)
                    result.elements[result.elements.lastIndex] = result.elements.last().addTail(marker, tail)
                    return result
                } else if (elements.size < WIDTH) {
                    return Branch(marker, shift, elements + sinkNode(shift - BITS, Leaf(marker, tail)))
                } else {
                    return Branch(marker, this).addTail(marker, tail)
                }
            }

            override operator fun get(index: Int): E {
                return elements[index ushr shift and MASK][index]
            }

            override fun canAddTail(): Boolean {
                return elements.last().canAddTail() || elements.size < WIDTH
            }

            override fun set(marker: Any?, index: Int, element: E): Node<E> {
                val childIndex = index ushr shift and MASK
                val result = mutable(marker)
                result.elements[childIndex] = result.elements[childIndex].set(marker, index, element)
                return result
            }

            private fun mutable(marker: Any?): Branch<E> {
                if (marker != null && marker === this.marker) {
                    return this
                } else {
                    return Branch(marker, shift, elements.copyOf())
                }
            }

            private fun sinkNode(shift: Int, node: Node<E>): Node<E> {
                return (node.shift until shift step BITS).fold(node) { node, shift -> Branch(marker, node) }
            }
        }

        class Leaf<E>(marker: Any?, val elements: Array<E>) : Node<E>(marker, 0) {
            override fun addTail(marker: Any?, tail: Array<E>): Node<E> {
                if (elements.isEmpty()) {
                    return Leaf(marker, tail)
                } else {
                    return Branch(marker, this).addTail(marker, tail)
                }
            }

            override fun canAddTail(): Boolean {
                return elements.isEmpty()
            }

            override operator fun get(index: Int): E {
                return elements[index and MASK]
            }

            override fun set(marker: Any?, index: Int, element: E): Node<E> {
                val result = mutable(marker)
                result.elements[index and MASK] = element
                return result
            }

            private fun mutable(marker: Any?): Leaf<E> {
                if (marker != null && marker === this.marker) {
                    return this
                } else {
                    return Leaf(marker, elements.copyOf())
                }
            }
        }
    }

    companion object {
        private const val BITS = 5
        private const val WIDTH = 1 shl BITS
        private const val MASK = WIDTH - 1

        private val EMPTY_NODE = Node.Leaf(null, emptyArray<Any?>())
        private val EMPTY = PersistentVector(0, BITS, EMPTY_NODE, emptyArray<Any?>())

        @Suppress("UNCHECKED_CAST")
        fun <E> empty(): PersistentVector<E> {
            return EMPTY as PersistentVector<E>
        }

        fun <E> of(vararg elements: E): PersistentVector<E> {
            return fromSequence(elements.asSequence())
        }

        fun <E> fromSequence(sequence: Sequence<E>): PersistentVector<E> {
            return sequence.fold(empty<E>().asTransient()) { vector, element -> vector.add(element) }.asPersistent()
        }
    }
}
