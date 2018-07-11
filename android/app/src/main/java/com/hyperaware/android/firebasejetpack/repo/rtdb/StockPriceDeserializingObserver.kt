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

package com.hyperaware.android.firebasejetpack.repo.rtdb

import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.Observer
import com.hyperaware.android.firebasejetpack.livedata.rtdb.DataSnapshotOrException
import com.hyperaware.android.firebasejetpack.model.StockPrice
import com.hyperaware.android.firebasejetpack.repo.QueryResultsOrException
import com.hyperaware.android.firebasejetpack.repo.StockPriceQueryItem

// TODO Kick off to executor via subclass
// generify better
internal class StockPriceDeserializingObserver(
    private val liveData: MediatorLiveData<QueryResultsOrException<StockPrice, Exception>>,
    private val deserializer: StockLiveDataSnapshotDeserializer
) : Observer<DataSnapshotOrException> {

    override fun onChanged(results: DataSnapshotOrException?) {
        if (results != null) {
            val snapshot = results.data
            val exception = results.exception
            if (snapshot != null) {
                val items = snapshot.children.map { child ->
                    val stock = deserializer.deserialize(child)
                    StockPriceQueryItem(stock, child.key!!)
                }
                liveData.postValue(QueryResultsOrException(items, null))
            }
            else if (exception != null) {
                liveData.postValue(QueryResultsOrException(null, exception))
            }
        }
    }

}
