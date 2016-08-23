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
@file:JvmName("ImmutableCollections")

package com.probablycoding.persistent

import com.probablycoding.persistent.impl.PersistentHashMap
import com.probablycoding.persistent.impl.PersistentVector

fun <T> emptyImmutableList(): ImmutableList<T> {
    return PersistentVector.empty()
}

fun <T> immutableListOf(vararg elements: T): ImmutableList<T> {
    if (elements.isEmpty()) {
        return emptyImmutableList()
    } else {
        return PersistentVector.of(*elements)
    }
}

fun <K, V> emptyImmutableMap(): ImmutableMap<K, V> {
    return PersistentHashMap.empty()
}

fun <K, V> immutableMapOf(vararg pairs: Pair<K, V>): ImmutableMap<K, V> {
    if (pairs.isEmpty()) {
        return emptyImmutableMap()
    } else {
        return PersistentHashMap.of(*pairs)
    }
}
