/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hyperaware.android.firebasejetpack.repo

import com.hyperaware.android.firebasejetpack.common.DataOrException
import com.hyperaware.android.firebasejetpack.model.StockPrice

/**
 * An item of data type T that resulted from a query. It adds the notion of
 * a unique id to that item.
 */

interface QueryItem<T> {
    val item: T
    val id: String
}

typealias QueryItemOrException<T> = DataOrException<QueryItem<T>, Exception>


data class StockPriceQueryItem(private val _item: StockPrice, private val _id: String) : QueryItem<StockPrice> {
    override val item: StockPrice
        get() = _item
    override val id: String
        get() = _id
}

typealias StockPriceOrException = DataOrException<StockPrice, Exception>

/**
 * The results of a database query (a List of QueryItems), or an Exception.
 */

typealias QueryResultsOrException<T, E> = DataOrException<List<QueryItem<T>>, E>

typealias StockPriceHistoryQueryResults = QueryResultsOrException<StockPrice, Exception>
