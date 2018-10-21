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

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.hyperaware.android.firebasejetpack.repo.StockRepository

internal class DatabaseReferenceSynchronizer(private val ref: DatabaseReference) {

    companion object {
        private const val TAG = "DbReferenceSynchronizer"
    }

    fun synchronize(callback: (syncResult: StockRepository.SyncResult) -> Unit) {
        var numSnapshotsReceived = 0
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Cache state is uncertain, but if we receive more than one
                    // snapshot, we can be relatively certain that the stock is
                    // fresh from the server.
                    numSnapshotsReceived++
                    Log.d(TAG, "Snapshot received")
                    if (numSnapshotsReceived > 1) {
                        finish(StockRepository.SyncResult.SUCCESS)
                    }
                }
                else {
                    finish(StockRepository.SyncResult.FAILURE)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                val exception = error.toException()
                Log.e(TAG, "Reference ${ref.key} sync failed", exception)
                finish(StockRepository.SyncResult.FAILURE)
            }

            fun finish(syncResult: StockRepository.SyncResult) {
                callback(syncResult)
                ref.removeEventListener(this)
            }
        }

        ref.addValueEventListener(listener)
    }

}
