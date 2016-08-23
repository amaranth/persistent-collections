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

interface Measured<E, M> {
    val monoid: Monoid<M>
    val zero: M
        get() = monoid.zero

    fun measure(element: E): M

    fun sum(a: M, b: M): M {
        return monoid.sum(a, b)
    }

    fun node(): Measured<Node<E, M>, M> {
        return object : Measured<Node<E, M>, M> {
            override val monoid = this@Measured.monoid

            override fun measure(element: Node<E, M>): M {
                return element.measure
            }
        }
    }
}
