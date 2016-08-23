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

abstract class AbstractSet<E> : AbstractCollection<E>(), ImmutableSet<E> {
    override fun addAll(elements: Collection<E>): ImmutableSet<E> {
        return (asSequence() + elements.asSequence()).toImmutableSet()
    }

    override fun remove(element: E): ImmutableSet<E> {
        return asSequence().filterNot { it == element }.toImmutableSet()
    }

    override fun removeAll(elements: Collection<E>): ImmutableSet<E> {
        return (asSequence() - elements.asSequence()).toImmutableSet()
    }

    override fun retainAll(elements: Collection<E>): ImmutableSet<E> {
        return asSequence().filterNot { it in elements }.toImmutableSet()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Set<*>) return false

        if (size != other.size) return false

        try {
            return containsAll(other)
        } catch (e: ClassCastException) {
            return false
        } catch (e: NullPointerException) {
            return false
        }
    }

    override fun hashCode(): Int {
        return fold(0) { hash, element -> hash + (element?.hashCode() ?: 0) }
    }
}
