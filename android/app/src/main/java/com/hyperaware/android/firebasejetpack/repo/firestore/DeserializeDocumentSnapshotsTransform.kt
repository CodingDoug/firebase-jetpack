/*
 * Copyright 2019 Google LLC
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

import androidx.arch.core.util.Function
import com.hyperaware.android.firebasejetpack.livedata.firestore.DocumentSnapshotsOrException
import com.hyperaware.android.firebasejetpack.repo.Deserializer
import com.hyperaware.android.firebasejetpack.repo.QueryItem
import com.hyperaware.android.firebasejetpack.repo.QueryResultsOrException

internal class DeserializeDocumentSnapshotsTransform<T>(
    private val deserializer: DocumentSnapshotDeserializer<T>
) : Function<DocumentSnapshotsOrException, QueryResultsOrException<T, Exception>> {

    override fun apply(input: DocumentSnapshotsOrException): QueryResultsOrException<T, Exception> {
        val (snapshots, exception) = input
        return when {
            snapshots != null -> return try {
                val items = snapshots.map { snapshot ->
                    val data = deserializer.deserialize(snapshot)
                    object : QueryItem<T> {
                        override val item: T
                            get() = data
                        override val id: String
                            get() = snapshot.id
                    }
                }
                QueryResultsOrException(items, null)
            }
            catch (e: Deserializer.DeserializerException) {
                QueryResultsOrException(null, e)
            }

            exception != null -> QueryResultsOrException(null, exception)

            else -> QueryResultsOrException(null, Exception("Both data and exception were null"))
        }
    }

}
