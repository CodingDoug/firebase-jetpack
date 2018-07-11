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

package com.hyperaware.android.firebasejetpack.livedata.firestore

import android.arch.lifecycle.LiveData
import android.os.Handler
import android.util.Log
import com.google.firebase.firestore.*
import com.hyperaware.android.firebasejetpack.config.AppExecutors
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

typealias DocumentSnapshotsOrException = Pair<List<DocumentSnapshot>?, FirebaseFirestoreException?>

class FirestoreQueryLiveData(private val query: Query)
    : LiveData<DocumentSnapshotsOrException>(), EventListener<QuerySnapshot>, KoinComponent {

    companion object {
        private const val TAG = "FirestoreQueryLiveData"
        // Listener removal is scheduled after a 2s delay of inactivity
        private const val STOP_LISTENING_DELAY = 2000L
    }

    private val executors by inject<AppExecutors>()

    private val handler = Handler()

    private var listenerRegistration: ListenerRegistration? = null
    private var listenerRemovePending = false
    private val removeListenerRunnable = RemoveListenerRunnable()

    override fun onActive() {
        if (listenerRemovePending) {
            handler.removeCallbacks(removeListenerRunnable)
        }
        else {
            listenerRegistration = query.addSnapshotListener(
                executors.cpuExecutorService,
                MetadataChanges.INCLUDE,
                this
            )
        }
        listenerRemovePending = false
    }

    override fun onInactive() {
        handler.postDelayed(removeListenerRunnable, STOP_LISTENING_DELAY)
        listenerRemovePending = true
    }

    private inner class RemoveListenerRunnable : Runnable {
        override fun run() {
            if (listenerRemovePending) {
                Log.d(TAG, "Stop listing to query")
                listenerRegistration?.remove()
                listenerRegistration = null
                listenerRemovePending = false
            }
        }
    }

    override fun onEvent(snapshot: QuerySnapshot?, e: FirebaseFirestoreException?) {
        val documents = snapshot?.documents
        postValue(DocumentSnapshotsOrException(documents, e))
    }

}
