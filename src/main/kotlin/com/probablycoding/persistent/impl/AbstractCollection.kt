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

import com.probablycoding.persistent.ImmutableCollection
import com.probablycoding.persistent.toImmutableList

abstract class AbstractCollection<E> : ImmutableCollection<E> {
    override fun containsAll(elements: Collection<E>): Boolean {
        return elements.all { contains(it) }
    }

    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun addAll(elements: Collection<E>): ImmutableCollection<E> {
        return (asSequence() + elements.asSequence()).toImmutableList()
    }

    override fun remove(element: E): ImmutableCollection<E> {
        return asSequence().filterNot { it == element }.toImmutableList()
    }

    override fun removeAll(elements: Collection<E>): ImmutableCollection<E> {
        return (asSequence() - elements.asSequence()).toImmutableList()
    }

    override fun retainAll(elements: Collection<E>): ImmutableCollection<E> {
        return asSequence().filterNot { it in elements }.toImmutableList()
    }

    override fun toString(): String {
        return joinToString(", ", "[", "]")
    }
}
