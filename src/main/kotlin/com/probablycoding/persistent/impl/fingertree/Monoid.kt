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

interface Monoid<A> {
    val zero: A

    fun sum(a: A, b: A): A

    fun <B> compose(other: Monoid<B>): Monoid<Pair<A, B>> {
        return object : Monoid<Pair<A, B>> {
            override val zero = Pair(this@Monoid.zero, other.zero)

            override fun sum(a: Pair<A, B>, b: Pair<A, B>): Pair<A, B> {
                return Pair(this@Monoid.sum(a.first, b.first), other.sum(a.second, b.second))
            }
        }
    }
}
