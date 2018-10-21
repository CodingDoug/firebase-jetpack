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

class DocumentSynchronizer(private val documentRef: DocumentReference) {

    companion object {
        private const val TAG = "DocumentSynchronizer"
    }

    fun synchronize(callback: (syncResult: StockRepository.SyncResult) -> Unit) {
        lateinit var registration: ListenerRegistration
        val listener = EventListener<DocumentSnapshot> { snapshot, e ->
            var result: StockRepository.SyncResult? = null
            if (snapshot != null) {
                // When we have confirmation from the server that we have a
                // fully synchronized document, we're done.
                if (snapshot.exists() && !snapshot.metadata.isFromCache) {
                    Log.d(TAG, "Document ${documentRef.path} synchronized")
                    result = StockRepository.SyncResult.SUCCESS
                }
            }
            else if (e != null) {
                Log.e(TAG, "Reference ${documentRef.path} sync failed", e)
                result = StockRepository.SyncResult.FAILURE
            }

            if (result != null) {
                registration.remove()
                callback(result)
            }
        }

        registration = documentRef.addSnapshotListener(MetadataChanges.INCLUDE, listener)
    }

}
