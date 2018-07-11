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

import android.util.Log
import com.google.firebase.firestore.*
import com.hyperaware.android.firebasejetpack.repo.StockRepository
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal class DocumentSyncCallable(
    private val documentRef: DocumentReference,
    private val timeout: Long,
    private val unit: TimeUnit
) : Callable<StockRepository.SyncResult>, EventListener<DocumentSnapshot> {

    companion object {
        private const val TAG = "DocumentSyncCallable"
    }

    private val latch = CountDownLatch(1)

    private var snapshot: DocumentSnapshot? = null
    private var exception: Exception? = null

    override fun call(): StockRepository.SyncResult {
        // Adding MetadataChanges.INCLUDE here ensures that the listener
        // gets a callback when the server confirms the document we hold
        // is the latest (isFromCache == false)
        val registration = documentRef.addSnapshotListener(MetadataChanges.INCLUDE, this)
        try {
            if (latch.await(timeout, unit)) {
                if (snapshot != null) {
                    return StockRepository.SyncResult.SUCCESS
                }
            }
            else {
                return StockRepository.SyncResult.TIMEOUT
            }

            return StockRepository.SyncResult.FAILURE
        }
        finally {
            registration.remove()
        }
    }

    override fun onEvent(snapshot: DocumentSnapshot?, e: FirebaseFirestoreException?) {
        if (snapshot != null) {
            // When we have confirmation from the server that we have a
            // fully synchronized document, we're done.
            if (snapshot.exists() && !snapshot.metadata.isFromCache) {
                Log.d(TAG, "Document ${documentRef.path} synchronized")
                this.snapshot = snapshot
                latch.countDown()
            }
        }
        else if (e != null) {
            Log.e(TAG, "Reference ${documentRef.path} sync failed", e)
            exception = e
            latch.countDown()
        }
    }

}
