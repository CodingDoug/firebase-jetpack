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

package com.hyperaware.android.firebasejetpack.fcm

import android.util.Log
import androidx.work.*
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hyperaware.android.firebasejetpack.worker.StockPriceSyncWorker

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MessagingService"
    }

    override fun onNewToken(token: String?) {
        Log.d(TAG, "FCM token: $token")
        // TODO do this proper with auth and stuff
        FirebaseMessaging.getInstance().subscribeToTopic("HSTK")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "From: ${message.from}, To: ${message.to}")
        Log.d(TAG, "Data: ${message.data}")

        val ticker = message.data["ticker"]
        if (ticker == null || ticker.isEmpty()) {
            Log.w(TAG, "No ticker found in message")
            return
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val data: Data = mapOf("ticker" to ticker).toWorkData()
        val workRequest = OneTimeWorkRequestBuilder<StockPriceSyncWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        WorkManager.getInstance()
            .beginUniqueWork("sync_$ticker", ExistingWorkPolicy.REPLACE, workRequest)
            .enqueue()
    }

}
