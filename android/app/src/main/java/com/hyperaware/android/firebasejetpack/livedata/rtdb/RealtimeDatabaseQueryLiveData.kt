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

package com.hyperaware.android.firebasejetpack.livedata.rtdb

import com.google.firebase.database.*
import com.hyperaware.android.firebasejetpack.livedata.common.LingeringLiveData
import com.hyperaware.android.firebasejetpack.common.DataOrException

typealias DataSnapshotOrException = DataOrException<DataSnapshot?, DatabaseException?>

class RealtimeDatabaseQueryLiveData : LingeringLiveData<DataSnapshotOrException>, ValueEventListener {

    private val query: Query
    private val displayPath: String

    constructor(ref: DatabaseReference) {
        this.query = ref
        this.displayPath = refToPath(ref)
    }

    constructor(query: Query) {
        this.query = query
        this.displayPath = "query@${refToPath(query.ref)}"
    }

    private fun refToPath(ref: DatabaseReference): String {
        var r = ref
        val parts = mutableListOf<String>()
        while (r.key != null) {
            parts.add(r.key!!)
            r = r.parent!!
        }
        return parts.asReversed().joinToString("/")
    }

    override fun beginLingering() {
        query.addValueEventListener(this)
    }

    override fun endLingering() {
        query.removeEventListener(this)
    }

    override fun onDataChange(snap: DataSnapshot) {
        value = DataSnapshotOrException(snap, null)
    }

    override fun onCancelled(e: DatabaseError) {
        value = DataSnapshotOrException(null, e.toException())
    }

}
