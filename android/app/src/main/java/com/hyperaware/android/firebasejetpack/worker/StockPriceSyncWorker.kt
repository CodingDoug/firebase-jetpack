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

@file:Suppress("UnstableApiUsage")

package com.hyperaware.android.firebasejetpack.worker

import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.common.base.Function
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import com.hyperaware.android.firebasejetpack.config.AppExecutors
import com.hyperaware.android.firebasejetpack.repo.StockRepository
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.util.concurrent.TimeUnit

class StockPriceSyncWorker(context: Context, params: WorkerParameters)
    : ListenableWorker(context, params), KoinComponent {

    companion object {
        private const val TAG = "StockSync"

        // Take a sync result and convert it to an appropriate WorkManager result
        private val convertResult = Function<StockRepository.SyncResult, Result> { syncResult ->
            when (syncResult) {
                StockRepository.SyncResult.SUCCESS -> Result.success()
                StockRepository.SyncResult.UNKNOWN -> Result.success()
                StockRepository.SyncResult.FAILURE -> Result.failure()
                StockRepository.SyncResult.TIMEOUT -> Result.retry()
                else -> Result.failure()
            }
        }
    }

    private val repo by inject<StockRepository>()
    private val executors by inject<AppExecutors>()

    override fun startWork(): ListenableFuture<Result> {
        val ticker = inputData.getString("ticker")
        if (ticker == null) {
            Log.e(TAG, "No ticker given to synchronize")
            val future = SettableFuture.create<Result>()
            future.set(Result.failure())
            return future
        }

        Log.d(TAG, "Synchronizing $ticker")
        val syncFuture = repo.syncStockPrice(ticker, 5, TimeUnit.SECONDS)
        return Futures.transform(syncFuture, convertResult, executors.cpuExecutorService)
    }

}
