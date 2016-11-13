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

import com.probablycoding.persistent.ImmutableList
import com.probablycoding.persistent.emptyImmutableList
import com.probablycoding.persistent.toImmutableList
import java.util.NoSuchElementException

abstract class AbstractList<E> : AbstractCollection<E>(), ImmutableList<E> {
    override operator fun contains(element: E): Boolean {
        return indexOf(element) >= 0
    }

    override fun indexOf(element: E): Int {
        return asSequence().indexOf(element)
    }

    override operator fun iterator(): Iterator<E> {
        return listIterator(0)
    }

    override fun lastIndexOf(element: E): Int {
        return (lastIndex downTo 0).firstOrNull { get(it) == element } ?: -1
    }

    override fun listIterator(): ListIterator<E> {
        return listIterator(0)
    }

    override fun listIterator(index: Int): ListIterator<E> {
        rangeCheckInclusive(index)

        return object : ListIterator<E> {
            private val size = this@AbstractList.size
            private var cursor = index

            override fun hasNext(): Boolean {
                return cursor < size
            }

            override fun hasPrevious(): Boolean {
                return cursor > 0
            }

            override fun next(): E {
                if (!hasNext()) throw NoSuchElementException()
                return get(cursor++)
            }

            override fun nextIndex(): Int {
                return cursor
            }

            override fun previous(): E {
                if (!hasPrevious()) throw NoSuchElementException()
                return get(--cursor)
            }

            override fun previousIndex(): Int {
                return cursor - 1
            }
        }
    }

    override fun subList(fromIndex: Int, toIndex: Int): ImmutableList<E> {
        rangeCheckSubList(fromIndex, toIndex)

        if (fromIndex == 0 && toIndex == size) {
            return this
        } else {
            return ImmutableSubList(this, fromIndex, toIndex)
        }
    }

    override fun addAll(elements: Collection<E>): ImmutableList<E> {
        return (asSequence() + elements.asSequence()).toImmutableList()
    }

    override fun remove(element: E): ImmutableList<E> {
        val index = indexOf(element)
        if (index < 0) {
            return this
        } else {
            return removeAt(index)
        }
    }

    override fun removeAll(elements: Collection<E>): ImmutableList<E> {
        return (asSequence() - elements.asSequence()).toImmutableList()
    }

    override fun retainAll(elements: Collection<E>): ImmutableList<E> {
        return asSequence().filterNot { it in elements }.toImmutableList()
    }

    override fun add(index: Int, element: E): ImmutableList<E> {
        return addAll(index, listOf(element))
    }

    override fun addAll(index: Int, elements: Collection<E>): ImmutableList<E> {
        rangeCheckInclusive(index)

        val before = asSequence().take(index)
        val after = asSequence().drop(index)
        return (before + elements.asSequence() + after).toImmutableList()
    }

    override fun removeAt(index: Int): ImmutableList<E> {
        rangeCheck(index)

        return (asSequence().take(index) + asSequence().drop(index + 1)).toImmutableList()
    }

    override fun reversed(): ImmutableList<E> {
        return ImmutableReversedList(this)
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is List<*>) return false
        if (size != other.size) return false

        return asSequence().zip(other.asSequence()).all { it.first == it.second }
    }

    override fun hashCode(): Int {
        return fold(1) { hash, element -> 31 * hash + (element?.hashCode() ?: 0) }
    }

    protected fun rangeCheck(index: Int) {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException("Index: $index, Size: $size")
        }
    }

    protected fun rangeCheckInclusive(index: Int) {
        if (index < 0 || index > size) {
            throw IndexOutOfBoundsException("Index: $index, Size: $size")
        }
    }

    protected fun rangeCheckSubList(fromIndex: Int, toIndex: Int) {
        if (fromIndex < 0) {
            throw IndexOutOfBoundsException("fromIndex = $fromIndex")
        }

        if (toIndex > size) {
            throw IndexOutOfBoundsException("toIndex = $toIndex")
        }

        require(fromIndex <= toIndex) { "fromIndex($fromIndex) > toIndex($toIndex)" }
    }

    private class ImmutableReversedList<E>(private val parent: ImmutableList<E>) : AbstractList<E>() {
        override val size = parent.size

        override fun get(index: Int): E {
            rangeCheck(index)

            return parent[size - index]
        }

        override fun add(element: E): ImmutableList<E> {
            // We could just prepend to the parent but that makes this O(n) so instead do
            // the proper reverse so this is amortized O(1) as expected
            return asSequence().toImmutableList().add(element)
        }

        override fun clear(): ImmutableList<E> {
            return emptyImmutableList()
        }

        override fun set(index: Int, element: E): ImmutableList<E> {
            rangeCheck(index)

            return ImmutableReversedList(parent.set(size - index, element))
        }

        override fun reversed(): ImmutableList<E> {
            return parent
        }
    }

    private class ImmutableSubList<E>(private val parent: ImmutableList<E>, private val fromIndex: Int,
                                      private val toIndex: Int) : AbstractList<E>() {
        override val size = toIndex - fromIndex

        override fun get(index: Int): E {
            rangeCheck(index)

            return parent[index + fromIndex]
        }

        override fun add(element: E): ImmutableList<E> {
            return ImmutableSubList(parent.add(toIndex, element), fromIndex, toIndex + 1)
        }

        override fun clear(): ImmutableList<E> {
            return emptyImmutableList()
        }

        override fun set(index: Int, element: E): ImmutableList<E> {
            rangeCheck(index)

            return ImmutableSubList(parent.set(index + fromIndex, element), fromIndex, toIndex)
        }
    }
}
