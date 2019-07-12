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
import com.hyperaware.android.firebasejetpack.model.StockPrice
import com.hyperaware.android.firebasejetpack.repo.QueryItem
import com.hyperaware.android.firebasejetpack.repo.QueryResultsOrException
import com.hyperaware.android.firebasejetpack.repo.StockRepository
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class StockPriceHistoryViewModel : ViewModel(), KoinComponent {

    private val stockRepo by inject<StockRepository>()

    private var stockHistoriesLiveData = HashMap<String, LiveData<StockPriceDisplayHistoryQueryResults>>()

    fun getStockPriceHistory(ticker: String): LiveData<StockPriceDisplayHistoryQueryResults> {
        var liveData = stockHistoriesLiveData[ticker]
        if (liveData == null) {
            // Convert StockPriceHistoryQueryResults to StockPriceDisplayHistoryQueryResults
            val historyLiveData = stockRepo.getStockPriceHistoryLiveData(ticker)
            liveData = Transformations.map(historyLiveData) { results ->
                val convertedResults = results.data?.map { StockPriceDisplayQueryItem(it) }
                val exception = results.exception
                StockPriceDisplayHistoryQueryResults(convertedResults, exception)
            }
        }
        return liveData!!
    }

}


typealias StockPriceDisplayHistoryQueryResults = QueryResultsOrException<StockPriceDisplay, Exception>

private data class StockPriceDisplayQueryItem(private val _item: QueryItem<StockPrice>) : QueryItem<StockPriceDisplay> {
    private val convertedPrice = _item.item.toStockPriceDisplay()

    override val item: StockPriceDisplay
        get() = convertedPrice
    override val id: String
        get() = _item.id
}
