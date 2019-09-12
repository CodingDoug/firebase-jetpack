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

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.annotation.MainThread
import com.hyperaware.android.firebasejetpack.common.DataOrException
import com.hyperaware.android.firebasejetpack.repo.StockRepository
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

typealias StockPriceDisplayOrException = DataOrException<StockPriceDisplay, Exception>

class StockPriceViewModel : ViewModel(), KoinComponent {

    private val stockRepo by inject<StockRepository>()

    private var stockPricesLiveData = HashMap<String, LiveData<StockPriceDisplayOrException>>()

    @MainThread
    fun getStockLiveData(ticker: String): LiveData<StockPriceDisplayOrException> {
        var liveData = stockPricesLiveData[ticker]
        if (liveData == null) {
            val ld = stockRepo.getStockPriceLiveData(ticker)
            liveData = Transformations.map(ld) {
                StockPriceDisplayOrException(it.data?.toStockPriceDisplay(), it.exception)
            }
            stockPricesLiveData[ticker] = liveData
        }
        return liveData
    }

    val allTickers = stockRepo.allTickers

}
