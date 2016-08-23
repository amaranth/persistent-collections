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

import com.probablycoding.persistent.ImmutableSortedSet
import java.util.Comparator

abstract class AbstractSortedSet<E> : AbstractSet<E>(), ImmutableSortedSet<E> {
    abstract val comparator: Comparator<in E>

    override fun addAll(elements: Collection<E>): ImmutableSortedSet<E> {
        return elements.fold(this as ImmutableSortedSet<E>) { set, element -> set.add(element) }
    }

    override fun remove(element: E): ImmutableSortedSet<E> {
        var result = clear()
        for (item in this) {
            if (item != element) {
                result = result.add(item)
            }
        }

        return result
    }

    override fun removeAll(elements: Collection<E>): ImmutableSortedSet<E> {
        val other = elements.toHashSet()
        var result = clear()

        for (element in this) {
            if (element !in other) {
                result = result.add(element)
            }
        }

        return result
    }

    override fun retainAll(elements: Collection<E>): ImmutableSortedSet<E> {
        val other = elements.toHashSet()
        var result = clear()

        for (element in this) {
            if (element in other) {
                result = result.add(element)
            }
        }

        return result
    }

    override fun headSet(toElement: E): ImmutableSortedSet<E> {
        val first = first()
        if (comparator.compare(first, toElement) >= 0) {
            return clear()
        } else {
            return subSet(first, toElement)
        }
    }
}
