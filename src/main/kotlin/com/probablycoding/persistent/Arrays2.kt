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
@file:JvmName("Arrays2")

package com.probablycoding.persistent

import java.util.NoSuchElementException

fun <T> Array<T>.add(index: Int, element: T): Array<T> {
    return addAll(index, element)
}

@Suppress("UNCHECKED_CAST")
fun <T> Array<T>.addAll(index: Int, vararg elements: T): Array<T> {
    if (index < 0 || index > size) {
        throw IndexOutOfBoundsException("$index not in array of $size")
    }

    // Hack to get an array with the same element type
    val result = copyOf(0).copyOf(size + elements.size)
    System.arraycopy(this, 0, result, 0, index)
    System.arraycopy(elements, 0, result, index, elements.size)
    System.arraycopy(this, index, result, index + elements.size, size - index)

    return result as Array<T>
}

fun <T> Array<T>.descendingIterator(): Iterator<T> {
    return object : Iterator<T> {
        private var cursor = lastIndex

        override fun hasNext(): Boolean {
            return cursor >= 0
        }

        override fun next(): T {
            if (!hasNext()) throw NoSuchElementException()
            return get(cursor--)
        }
    }
}

fun <T> Array<T>.head(): Array<T> {
    return if (isEmpty()) this else copyOfRange(0, lastIndex)
}

operator fun <T> T.plus(array: Array<T>): Array<T> {
    return array.prepend(this)
}

@Suppress("UNCHECKED_CAST")
fun <T> Array<T>.prepend(element: T): Array<T> {
    // Hack to get an array with the same element type
    val result = copyOf(0).copyOf(size + 1)

    result[0] = element
    System.arraycopy(this, 0, result, 1, size)

    return result as Array<T>
}

@Suppress("UNCHECKED_CAST")
fun <T> Array<T>.removeAt(vararg indices: Int): Array<T> {
    // Hack to get an array with the same element type
    val result = copyOf(0).copyOf(size - indices.size)
    val sorted = indices.sortedArray()
    var start = 0
    var resultStart = 0

    for ((removeIndex, index) in sorted.withIndex()) {
        if (index < 0 || index > lastIndex) {
            throw IndexOutOfBoundsException("$index not in array of size $size")
        }

        System.arraycopy(this, start, result, resultStart, index - start)
        start = index + 1
        resultStart = index - removeIndex
    }

    System.arraycopy(this, start, result, resultStart, size - start)

    return result as Array<T>
}

fun <T> Array<T>.tail(): Array<T> {
    return if (isEmpty()) this else copyOfRange(1, size)
}
