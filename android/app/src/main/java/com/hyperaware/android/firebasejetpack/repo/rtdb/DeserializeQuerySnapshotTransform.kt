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

package com.hyperaware.android.firebasejetpack.repo.rtdb

import android.arch.core.util.Function
import com.hyperaware.android.firebasejetpack.livedata.rtdb.DataSnapshotOrException
import com.hyperaware.android.firebasejetpack.repo.Deserializer
import com.hyperaware.android.firebasejetpack.repo.QueryItem
import com.hyperaware.android.firebasejetpack.repo.QueryResultsOrException

internal class DeserializeQuerySnapshotTransform<T>(
    private val deserializer: DataSnapshotDeserializer<T>
) : Function<DataSnapshotOrException, QueryResultsOrException<T, Exception>> {

    override fun apply(input: DataSnapshotOrException): QueryResultsOrException<T, Exception> {
        val (snapshot, exception) = input
        return when {
            snapshot != null -> return try {
                val items = snapshot.children.map { child ->
                    val item = deserializer.deserialize(child)
                    object : QueryItem<T> {
                        override val item: T
                            get() = item
                        override val id: String
                            get() = child.key!!
                    }
                }
                QueryResultsOrException(items, null)
            }
            catch (e: Deserializer.DeserializerException) {
                QueryResultsOrException(null, e)
            }

            exception != null -> QueryResultsOrException(null, exception)

            else -> QueryResultsOrException(null, Exception(""))
        }
    }

}
