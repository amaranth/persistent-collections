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
package com.probablycoding.persistent.fingertree

class FingerTree<T, M> private constructor(val measured: Measured<T, M>, private val front: Digit<T, M>,
                                           private val middle: FingerTree<Node<T, M>, M>?,
                                           private val back: Digit<T, M>) : Iterable<T> {
    val measure: M by lazy { measured.sum(measured.sum(front.measure, middle?.measure ?: measured.zero), back.measure) }

    fun append(element: T): FingerTree<T, M> {
        if (isEmpty()) {
            return FingerTree(measured, front.append(element), middle, back)
        } else if (back.hasRoom()) {
            return FingerTree(measured, front, middle, back.append(element))
        } else {
            val newMiddle = (middle ?: empty(measured.node())).append(back.head().toNode())
            return FingerTree(measured, front, newMiddle, Digit.of(measured, back.last(), element))
        }
    }

    fun clear(): FingerTree<T, M> {
        return empty(measured)
    }

    fun descendingIterator(): Iterator<T> {
        val frontSeq = front.descendingIterator().asSequence()
        val backSeq = back.descendingIterator().asSequence()

        if (middle == null) {
            return (backSeq + frontSeq).iterator()
        } else {
            val middleSeq = middle.descendingIterator().asSequence().map {
                it.descendingIterator().asSequence()
            }.flatten()

            return (backSeq + middleSeq + frontSeq).iterator()
        }
    }

    fun dropUntil(predicate: (M) -> Boolean) : FingerTree<T, M> {
        return split(predicate).second
    }

    fun first(): T {
        return front.first()
    }

    fun head(): FingerTree<T, M> {
        return createRight(front, middle, back.head())
    }

    fun isEmpty(): Boolean {
        return front.isEmpty() && middle == null
    }

    fun isNotEmpty(): Boolean {
        return !isEmpty()
    }

    override fun iterator(): Iterator<T> {
        val frontSeq = front.asSequence()
        val backSeq = back.asSequence()

        if (middle == null) {
            return (frontSeq + backSeq).iterator()
        } else {
            val middleSeq = middle.asSequence().map { it.asSequence() }.flatten()
            return (frontSeq + middleSeq + backSeq).iterator()
        }
    }

    fun last(): T {
        return if (back.isEmpty()) front.last() else back.last()
    }

    fun lookup(predicate: (M) -> Boolean): T? {
        val tree = dropUntil(predicate)
        if (tree.isEmpty()) {
            return null
        } else {
            return tree.first()
        }
    }

    fun merge(other: FingerTree<T, M>): FingerTree<T, M> {
        if (isEmpty()) {
            return other
        } else if (isSingle()) {
            return other.prepend(first())
        } else if (other.isEmpty()) {
            return this
        } else if (other.isSingle()) {
            return append(other.first())
        } else {
            val mergedMiddle = makeNodes(middle ?: empty(measured.node()), back, other.front)
            if (other.middle == null) {
                return FingerTree(measured, front, mergedMiddle, other.back)
            } else {
                return FingerTree(measured, front, mergedMiddle.merge(other.middle), other.back)
            }
        }
    }

    fun prepend(element: T): FingerTree<T, M> {
        if (isSingle()) {
            return FingerTree(measured, Digit.of(measured, element), middle, front)
        } else if (front.hasRoom()) {
            return FingerTree(measured, front.prepend(element), middle, back)
        } else {
            val newMiddle = (middle ?: empty(measured.node())).append(front.tail().toNode())
            return FingerTree(measured, Digit.of(measured, element, front.first()), newMiddle, back)
        }
    }

    fun reversed(): FingerTree<T, M> {
        val newFront = back.reversed()
        val newBack = front.reversed()

        if (middle == null) {
            return FingerTree(measured, newFront, middle, newBack)
        } else {
            val reversedMiddle = middle.reversed()
            val newMiddle = FingerTree(middle.measured, reversedMiddle.back, reversedMiddle.middle,
                                       reversedMiddle.front)
            return FingerTree(measured, newFront, newMiddle, newBack)
        }
    }

    fun split(predicate: (M) -> Boolean): Pair<FingerTree<T, M>, FingerTree<T, M>> {
        if (isNotEmpty() && predicate(measure)) {
            val split = splitTree(predicate)
            return Pair(split.first, split.third.prepend(split.second))
        } else {
            return Pair(this, empty(measured))
        }
    }

    fun splitTree(predicate: (M) -> Boolean, accumulator: M = measured.zero): Triple<FingerTree<T, M>, T, FingerTree<T, M>> {
        if (isSingle()) {
            return Triple(empty(measured), front.first(), empty(measured))
        } else {
            val frontAcc = measured.sum(accumulator, front.measure)
            if (predicate(frontAcc)) {
                val frontSplit = front.splitTree(predicate, accumulator)
                return Triple(frontSplit.first.toTree(), frontSplit.second, create(frontSplit.third, middle, back))
            } else {
                val middleAcc = measured.sum(frontAcc, middle?.measure ?: measured.zero)
                if (middle != null && predicate(middleAcc)) {
                    val middleSplit = middle.splitTree(predicate, frontAcc)
                    val middleNodeAcc = measured.sum(frontAcc, middleSplit.first.measure)
                    val middleNodeSplit = middleSplit.second.splitTree(predicate, middleNodeAcc)
                    return Triple(createRight(front, middleSplit.first, middleNodeSplit.first), middleNodeSplit.second,
                                  create(middleNodeSplit.third, middleSplit.third, back))
                } else {
                    val backSplit = back.splitTree(predicate, middleAcc)
                    return Triple(createRight(front, middle, backSplit.first), backSplit.second,
                                  backSplit.third.toTree())
                }
            }
        }
    }

    fun tail(): FingerTree<T, M> {
        return create(front.tail(), middle, back)
    }

    fun takeUntil(predicate: (M) -> Boolean) : FingerTree<T, M> {
        return split(predicate).first
    }

    private fun isSingle(): Boolean {
        return middle == null && back.isEmpty()
    }

    companion object {
        fun <T, M> empty(measured: Measured<T, M>): FingerTree<T, M> {
            return FingerTree(measured, Digit.of(measured), null, Digit.of(measured))
        }

        fun <T, M> fromSequence(measured: Measured<T, M>, sequence: Sequence<T>): FingerTree<T, M> {
            return sequence.fold(empty(measured)) { tree, element -> tree.append(element) }
        }

        private fun <T, M> create(front: Digit<T, M>, middle: FingerTree<Node<T, M>, M>?, back: Digit<T, M>): FingerTree<T, M> {
            if (front.isNotEmpty()) {
                return FingerTree(front.measured, front, middle, back)
            } else if (middle == null || middle.isEmpty()) {
                return fromSequence(front.measured, back.asSequence())
            } else {
                return FingerTree(front.measured, middle.first().toDigit(), middle.tail(), back)
            }
        }

        private fun <T, M> createRight(front: Digit<T, M>, middle: FingerTree<Node<T, M>, M>?, back: Digit<T, M>): FingerTree<T, M> {
            if (back.isNotEmpty()) {
                return FingerTree(front.measured, front, middle, back)
            } else if (middle == null || middle.isEmpty()) {
                return fromSequence(front.measured, front.asSequence())
            } else {
                return FingerTree(front.measured, front, middle.head(), middle.last().toDigit())
            }
        }

        private fun <T, M> makeNodes(middle: FingerTree<Node<T, M>, M>, front: Digit<T, M>, back: Digit<T, M>): FingerTree<Node<T, M>, M> {
            val elements = (front.asSequence() + back.descendingIterator().asSequence()).toList()
            return when (elements.size) {
                2 -> middle.append(Node.of(front.measured, elements[0], elements[1]))
                3 -> middle.append(Node.of(front.measured, elements[0], elements[1], elements[2]))
                4 -> middle.append(Node.of(front.measured, elements[0], elements[1])).append(Node.of(front.measured, elements[2], elements[3]))
                5 -> middle.append(Node.of(front.measured, elements[0], elements[1], elements[2])).append(Node.of(front.measured, elements[3], elements[4]))
                6 -> middle.append(Node.of(front.measured, elements[0], elements[1], elements[2])).append(Node.of(front.measured, elements[3], elements[4], elements[5]))
                7 -> middle.append(Node.of(front.measured, elements[0], elements[1], elements[2])).append(Node.of(front.measured, elements[3], elements[4])).append(Node.of(front.measured, elements[5], elements[6]))
                8 -> middle.append(Node.of(front.measured, elements[0], elements[1], elements[2])).append(Node.of(front.measured, elements[3], elements[4], elements[5])).append(Node.of(front.measured, elements[6], elements[7]))
                else -> throw IllegalStateException("Nodes must have 2 or 3 elements")
            }
        }
    }
}
