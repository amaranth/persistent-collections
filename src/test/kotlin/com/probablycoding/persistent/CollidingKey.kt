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

class CollidingKey(val hashCode: Int, val key: Int) : Comparable<CollidingKey> {

    override fun hashCode() = hashCode

    override fun equals(other: Any?) = this === other || ((other is CollidingKey) && key == other.key)

    override fun compareTo(other: CollidingKey) = key.compareTo(other.key)

    override fun toString() = "CollidingKey(hashCode=$hashCode, key=$key)"
}
