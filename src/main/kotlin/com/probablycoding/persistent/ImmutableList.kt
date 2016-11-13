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

interface ImmutableList<E> : ImmutableCollection<E>, List<E> {
    override fun add(element: E): ImmutableList<E>
    override fun addAll(elements: Collection<E>): ImmutableList<E>
    override fun clear(): ImmutableList<E>
    override fun remove(element: E): ImmutableList<E>
    override fun removeAll(elements: Collection<E>): ImmutableList<E>
    override fun retainAll(elements: Collection<E>): ImmutableList<E>

    override fun subList(fromIndex: Int, toIndex: Int): ImmutableList<E>

    fun add(index: Int, element: E): ImmutableList<E>
    fun addAll(index: Int, elements: Collection<E>): ImmutableList<E>
    fun removeAt(index: Int): ImmutableList<E>
    fun reversed(): ImmutableList<E>
    operator fun set(index: Int, element: E): ImmutableList<E>
}
