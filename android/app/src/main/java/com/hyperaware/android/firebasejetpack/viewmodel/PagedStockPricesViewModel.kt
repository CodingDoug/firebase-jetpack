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

package com.hyperaware.android.firebasejetpack.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.arch.paging.PagedList
import android.support.annotation.MainThread
import com.hyperaware.android.firebasejetpack.model.StockPrice
import com.hyperaware.android.firebasejetpack.repo.QueryItemOrException
import com.hyperaware.android.firebasejetpack.repo.StockRepository
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class PagedStockPricesViewModel : ViewModel(), KoinComponent {

    private val stockRepo by inject<StockRepository>()

    private var stockPricesLiveData: LiveData<PagedList<QueryItemOrException<StockPrice>>>? = null

    @MainThread
    fun getAllStockPricesPagedListLiveData(): LiveData<PagedList<QueryItemOrException<StockPrice>>> {
        var liveData = stockPricesLiveData
        if (liveData == null) {
            // 5 is a ridiculously low page size in practice
            liveData = stockRepo.getStockPricePagedListLiveData(5)
            stockPricesLiveData = liveData
        }
        return liveData
    }

}
