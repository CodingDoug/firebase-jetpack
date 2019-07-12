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

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.hyperaware.android.firebasejetpack.config.AppExecutors
import com.hyperaware.android.firebasejetpack.livedata.firestore.FirestoreDocumentLiveData
import com.hyperaware.android.firebasejetpack.livedata.firestore.FirestoreQueryLiveData
import com.hyperaware.android.firebasejetpack.model.StockPrice
import com.hyperaware.android.firebasejetpack.repo.*
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.util.concurrent.TimeUnit

class FirestoreStockRepository : BaseStockRepository(), KoinComponent {

    private val executors by inject<AppExecutors>()
    private val firestore by inject<FirebaseFirestore>()

    private val stocksLiveCollection = firestore.collection("stocks-live")

    private val stockPriceDeserializer = StockPriceDocumentSnapshotDeserializer()

    private val listeningExecutor = MoreExecutors.listeningDecorator(executors.networkExecutorService)

    override fun getStockPriceLiveData(ticker: String): LiveData<StockPriceOrException> {
        val stockDocRef = stocksLiveCollection.document(ticker)

        // This LiveData is going to emit DocumentSnapshot objects.  We need
        // to transform those into StockPrice objects for the consumer.
        val documentLiveData = FirestoreDocumentLiveData(stockDocRef)

        // When a transformation is fast and can be executed on the main
        // thread, we can use Transformations.map().
        return Transformations.map(documentLiveData, DeserializeDocumentSnapshotTransform(stockPriceDeserializer))

        // But if a transformation is slow/blocking and shouldn't be executed
        // on the main thread, we can use Transformations.switchMap() with a
        // function that transforms on another thread and returns a LiveData.
//        return Transformations.switchMap(documentLiveData, AsyncDeserializingDocumentSnapshotTransform(stockPriceDeserializer))
    }

    override fun getStockPriceHistoryLiveData(ticker: String): LiveData<StockPriceHistoryQueryResults> {
        val priceHistoryColl = stocksLiveCollection.document(ticker).collection("recent-history")
        val query = priceHistoryColl.orderBy("time")
        val queryLiveData = FirestoreQueryLiveData(query)
        return Transformations.map(queryLiveData, DeserializeDocumentSnapshotsTransform(stockPriceDeserializer))
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
            .setFetchExecutor(executors.networkExecutorService)
            .build()
    }

    override fun syncStockPrice(ticker: String, timeout: Long, unit: TimeUnit): ListenableFuture<StockRepository.SyncResult> {
        val stockDocRef = stocksLiveCollection.document(ticker)
        val callable = DocumentSyncCallable(stockDocRef, timeout, unit)
        return listeningExecutor.submit(callable)
    }

}
