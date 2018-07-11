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

import com.google.firebase.firestore.*
import com.hyperaware.android.firebasejetpack.config.AppExecutors
import com.hyperaware.android.firebasejetpack.livedata.common.LingeringLiveData
import com.hyperaware.android.firebasejetpack.common.DataOrException
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

typealias DocumentSnapshotOrException = DataOrException<DocumentSnapshot?, FirebaseFirestoreException?>

class FirestoreDocumentLiveData(private val ref: DocumentReference)
    : LingeringLiveData<DocumentSnapshotOrException>(), EventListener<DocumentSnapshot>, KoinComponent {

    private val executors by inject<AppExecutors>()

    private var listenerRegistration: ListenerRegistration? = null

    override fun beginLingering() {
        listenerRegistration = ref.addSnapshotListener(executors.cpuExecutorService, this)
    }

    override fun endLingering() {
        listenerRegistration?.remove()
    }

    override fun onEvent(snapshot: DocumentSnapshot?, e: FirebaseFirestoreException?) {
        if (snapshot != null) {
            postValue(DocumentSnapshotOrException(snapshot, null))
        }
        else if (e != null) {
            postValue(DocumentSnapshotOrException(null, e))
        }
    }

}
