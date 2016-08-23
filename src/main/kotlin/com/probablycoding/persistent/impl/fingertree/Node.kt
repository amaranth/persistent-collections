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
import com.probablycoding.persistent.impl.tail

class Node<T, M> private constructor(val measured: Measured<T, M>, private val elements: Array<T>) : Iterable<T> {
    val measure: M by lazy {
        elements.fold(measured.zero) { measure, element -> measured.sum(measure, measured.measure(element)) }
    }

    fun descendingIterator(): Iterator<T> {
        return elements.descendingIterator()
    }

    override fun iterator(): Iterator<T> {
        return elements.iterator()
    }

    fun splitTree(predicate: (M) -> Boolean, accumulator: M = measured.zero): Triple<Digit<T, M>, T, Digit<T, M>> {
        val firstAcc = measured.sum(accumulator, measured.measure(elements[0]))
        if (predicate(firstAcc)) {
            return Triple(Digit.of(measured), elements[0], Digit.of(measured, *elements.tail()))
        } else if (predicate(measured.sum(firstAcc, measured.measure(elements[1])))) {
            return Triple(Digit.of(measured, elements.first()), elements[1],
                          Digit.of(measured, *elements.copyOfRange(2, elements.size)))
        } else {
            return Triple(Digit.of(measured, *elements.head()), elements.last(), Digit.of(measured))
        }
    }

    fun toDigit(): Digit<T, M> {
        return Digit.of(measured, *elements)
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun <T, M> of(measured: Measured<T, M>, vararg elements: T): Node<T, M> {
            return Node(measured, elements as Array<T>)
        }
    }
}
