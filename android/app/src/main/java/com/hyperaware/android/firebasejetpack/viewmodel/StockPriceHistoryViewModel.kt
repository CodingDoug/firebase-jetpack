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
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.hyperaware.android.firebasejetpack.model.StockPrice
import com.hyperaware.android.firebasejetpack.repo.QueryItem
import com.hyperaware.android.firebasejetpack.repo.QueryResultsOrException
import com.hyperaware.android.firebasejetpack.repo.StockRepository
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class StockPriceHistoryViewModel : ViewModel(), KoinComponent {

    private val stockRepo by inject<StockRepository>()

    fun getStockPriceHistory(ticker: String): LiveData<StockPriceDisplayHistoryQueryResults> {
        // Convert StockPriceHistoryQueryResults to StockPriceDisplayHistoryQueryResults
        val liveDataIn = stockRepo.getStockPriceHistoryLiveData(ticker)
        return Transformations.map(liveDataIn) { results ->
            val convertedResults = results.data?.map { StockPriceDisplayQueryItem(it) }
            val exception = results.exception
            StockPriceDisplayHistoryQueryResults(convertedResults, exception)
        }
    }

}


typealias StockPriceDisplayHistoryQueryResults = QueryResultsOrException<StockPriceDisplay, Exception>

private data class StockPriceDisplayQueryItem(private val item: QueryItem<StockPrice>) : QueryItem<StockPriceDisplay> {
    private val convertedPrice = item.getItem().toStockPriceDisplay()

    override fun getItem(): StockPriceDisplay {
        return convertedPrice
    }

    override fun getId(): String {
        return item.getId()
    }
}
