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

package com.hyperaware.android.firebasejetpack.repo.firestore

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.hyperaware.android.firebasejetpack.common.DataOrException
import com.hyperaware.android.firebasejetpack.config.AppExecutors
import com.hyperaware.android.firebasejetpack.livedata.firestore.FirestoreDocumentLiveData
import com.hyperaware.android.firebasejetpack.livedata.firestore.FirestoreQueryLiveData
import com.hyperaware.android.firebasejetpack.model.StockPrice
import com.hyperaware.android.firebasejetpack.repo.*
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class FirestoreStockRepository : BaseStockRepository(), KoinComponent {

    private val executors by inject<AppExecutors>()
    private val firestore by inject<FirebaseFirestore>()

    private val stocksLiveCollection = firestore.collection("stocks-live")

    private val stockPriceDeserializer = StockPriceDocumentSnapshotDeserializer()

    override fun getStockPriceLiveData(ticker: String): LiveData<StockPriceOrException> {
        val stockDocRef = stocksLiveCollection.document(ticker)
        val documentLiveData = FirestoreDocumentLiveData(stockDocRef)
        val stockPriceLiveData = MediatorLiveData<DataOrException<StockPrice, Exception>>()
        val snapshotObserver = DeserializingDocumentSnapshotObserver(stockPriceDeserializer, stockPriceLiveData)
        stockPriceLiveData.addSource(documentLiveData, snapshotObserver)
        return stockPriceLiveData
    }

    override fun getStockPriceHistoryLiveData(ticker: String): LiveData<StockPriceHistoryQueryResults> {
        val priceHistoryColl = stocksLiveCollection.document(ticker).collection("recent-history")
        val query = priceHistoryColl.orderBy("time")
        val queryLiveData = FirestoreQueryLiveData(query)
        val priceHistoryLiveData = MediatorLiveData<StockPriceHistoryQueryResults>()
        val queryObserver = StockPriceDeserializingObserver(priceHistoryLiveData, stockPriceDeserializer)
        priceHistoryLiveData.addSource(queryLiveData, queryObserver)
        return priceHistoryLiveData
    }

    override fun getStockPricePagedListLiveData(pageSize: Int): LiveData<PagedList<QueryItemOrException<StockPrice>>> {
        val query = stocksLiveCollection.orderBy(FieldPath.documentId())
        val dataSourceFactory = FirestoreQueryDataSource.Factory(query, Source.DEFAULT)
        val deserializedDataSourceFactory = dataSourceFactory.map { snapshot ->
            try {
                val item = StockPriceQueryItem(stockPriceDeserializer.deserialize(snapshot), snapshot.id)
                QueryItemOrException(item, null)
            }
            catch (e: Exception) {
                QueryItemOrException<StockPrice>(null, e)
            }
        }

        return LivePagedListBuilder(deserializedDataSourceFactory, pageSize)
            .setFetchExecutor(executors.cpuExecutorService)
            .build()
    }

    override fun syncStockPrice(ticker: String, timeout: Long, unit: TimeUnit): Future<StockRepository.SyncResult> {
        val stockDocRef = stocksLiveCollection.document(ticker)
        val callable = DocumentSyncCallable(stockDocRef, timeout, unit)
        return executors.networkExecutorService.submit(callable)
    }

}
