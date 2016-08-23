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

interface ImmutableSortedSet<E> : ImmutableSet<E>, SortedSet<E> {
    override fun add(element: E): ImmutableSortedSet<E>
    override fun addAll(elements: Collection<E>): ImmutableSortedSet<E>
    override fun clear(): ImmutableSortedSet<E>
    override fun remove(element: E): ImmutableSortedSet<E>
    override fun removeAll(elements: Collection<E>): ImmutableSortedSet<E>
    override fun retainAll(elements: Collection<E>): ImmutableSortedSet<E>

    override fun headSet(toElement: E): ImmutableSortedSet<E>
    override fun subSet(fromElement: E, toElement: E): ImmutableSortedSet<E>
    override fun tailSet(fromElement: E): ImmutableSortedSet<E>
}
