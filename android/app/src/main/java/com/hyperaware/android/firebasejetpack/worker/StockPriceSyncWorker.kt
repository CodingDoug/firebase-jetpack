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

package com.hyperaware.android.firebasejetpack.worker

import android.util.Log
import androidx.work.Worker
import com.hyperaware.android.firebasejetpack.repo.StockRepository
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.util.concurrent.TimeUnit

/**
 * WorkManager Worker that synchronizes one stock price record, so it's
 * readily available offline.
 */

class StockPriceSyncWorker : Worker(), KoinComponent {

    companion object {
        private const val TAG = "StockSync"
    }

    private val repo by inject<StockRepository>()

    override fun doWork(): Result {
        val ticker = inputData.getString("ticker") ?: return Result.FAILURE
        Log.d(TAG, "Synchronizing $ticker")

        val sync = repo.syncStockPrice(ticker, 5, TimeUnit.SECONDS)
        return try {
            val result = sync.get()
            return when (result) {
                StockRepository.SyncResult.SUCCESS -> Result.SUCCESS
                StockRepository.SyncResult.UNKNOWN -> Result.SUCCESS
                StockRepository.SyncResult.FAILURE -> Result.FAILURE
                StockRepository.SyncResult.TIMEOUT -> Result.RETRY
                else -> Result.FAILURE
            }
        }
        catch (e: Exception) {
            Log.e(TAG, "Error synchronizing $ticker", e)
            Result.FAILURE
        }
    }

}
