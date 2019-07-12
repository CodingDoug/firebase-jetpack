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

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.google.common.util.concurrent.ListenableFuture
import com.hyperaware.android.firebasejetpack.model.StockPrice
import java.util.*
import java.util.concurrent.TimeUnit

interface StockRepository {

    val allTickers: SortedSet<String>

    /**
     * Gets a LiveData object from this repo that reflects the current value of
     * a single Stock, given by its ticker.
     */
    fun getStockPriceLiveData(ticker: String): LiveData<StockPriceOrException>

    fun getStockPriceHistoryLiveData(ticker: String): LiveData<StockPriceHistoryQueryResults>

    fun getStockPricePagedListLiveData(pageSize: Int): LiveData<PagedList<QueryItemOrException<StockPrice>>>

    /**
     * Synchronizes one stock record so it's available to this repo while offline
     */
    fun syncStockPrice(ticker: String, timeout: Long, unit: TimeUnit): ListenableFuture<SyncResult>


    enum class SyncResult {
        SUCCESS, UNKNOWN, FAILURE, TIMEOUT
    }

}
