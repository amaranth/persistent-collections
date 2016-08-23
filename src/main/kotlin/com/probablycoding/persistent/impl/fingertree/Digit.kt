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
package com.probablycoding.persistent.impl.fingertree

import com.probablycoding.persistent.impl.descendingIterator
import com.probablycoding.persistent.impl.head
import com.probablycoding.persistent.impl.plus
import com.probablycoding.persistent.impl.tail

class Digit<T, M> private constructor(val measured: Measured<T, M>, private val elements: Array<T>) : Iterable<T> {
    val measure: M by lazy {
        elements.fold(measured.zero) { measure, element -> measured.sum(measure, measured.measure(element)) }
    }

    fun append(element: T): Digit<T, M> {
        return Digit(measured, elements + element)
    }

    fun descendingIterator(): Iterator<T> {
        return elements.descendingIterator()
    }

    fun first(): T {
        return elements.first()
    }

    fun hasRoom(): Boolean {
        return elements.size < 4
    }

    fun head(): Digit<T, M> {
        return Digit(measured, elements.head())
    }

    fun isEmpty(): Boolean {
        return elements.isEmpty()
    }

    fun isNotEmpty(): Boolean {
        return !isEmpty()
    }

    override fun iterator(): Iterator<T> {
        return elements.iterator()
    }

    fun last(): T {
        return elements.last()
    }

    fun prepend(element: T): Digit<T, M> {
        return Digit(measured, element + elements)
    }

    fun reversed(): Digit<T, M> {
        return Digit(measured, elements.reversedArray())
    }

    fun splitTree(predicate: (M) -> Boolean, accumulator: M = measured.zero): Triple<Digit<T, M>, T, Digit<T, M>> {
        check(isNotEmpty()) { "splitDigit() called on empty digit" }

        if (elements.size == 1) {
            return Triple(Digit.of(measured), first(), Digit.of(measured))
        } else {
            var elementAcc = accumulator
            for (index in 0 until elements.size) {
                elementAcc = measured.sum(elementAcc, measured.measure(elements[index]))
                if (predicate(elementAcc)) {
                    return Triple(Digit.of(measured, *elements.copyOfRange(0, index)), elements[index],
                                  Digit.of(measured, *elements.copyOfRange(index + 1, elements.size)))
                }
            }

            throw IllegalStateException("Predicate failed to match on digit it was already measured against")
        }
    }

    fun tail(): Digit<T, M> {
        return Digit(measured, elements.tail())
    }

    fun toNode(): Node<T, M> {
        require(elements.size == 2 || elements.size == 3)
        return Node.of(measured, *elements)
    }

    fun toTree(): FingerTree<T, M> {
        return FingerTree.fromSequence(measured, asSequence())
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun <T, M> of(measured: Measured<T, M>, vararg elements: T): Digit<T, M> {
            return Digit(measured, elements as Array<T>)
        }
    }
}
